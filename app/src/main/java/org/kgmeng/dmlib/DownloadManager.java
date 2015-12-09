package org.kgmeng.dmlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.kgmeng.dmlib.config.Constants;
import org.kgmeng.dmlib.impl.BaseTask;
import org.kgmeng.dmlib.model.AppInfo;
import org.kgmeng.dmlib.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * DownloadManager
 *
 * @author JF.Chang
 * @date 2015/8/28
 */
@SuppressLint("NewApi")
public class DownloadManager {
    private static final String TAG = DownloadManager.class.getSimpleName();

    //队列最大容量
    public final static int MAX_DOWNLOAD_SIZE = 99;
    //可执行下载的任务数
    public final static int MAX_DOWNLING_PROCESS_SIZE = 2;

    private Context mContext;
    // 等待下载队列
    private BlockingQueue<BaseTask> mTaskQueue;
    //正在执行的任务列表
    private List<BaseTask> mDownloadingTasks;
    //暂停执行的任务列表
    private List<BaseTask> mPausingTasks;
    //错误的任务列表
    private List<BaseTask> mErrorTasks;
    /**
     * 队列互斥变量
     */
//    private Object mutex = new Object();

    private Boolean isInterrupt = false;

    private boolean isAllPause = false; //全部暂停

    private PollThread pollThread;

    private volatile int mDownloadCount;

    private static DownloadManager downloadManager;

//    private Type downloadType;//default cdn

    private List<WeakReference<IDownloadStateListener>> downloadStateListeners;

    /**
     * 注册状态监听
     */
    public void registerStateListener(IDownloadStateListener downloadStateListener) {
        downloadStateListeners.add(new WeakReference<IDownloadStateListener>(downloadStateListener));
    }

    /**
     * 取消注册状态监听
     */
    public void unRegisterStateListener(IDownloadStateListener downloadStateListener) {
        Iterator<WeakReference<IDownloadStateListener>> iterator = downloadStateListeners.iterator();
        while (iterator.hasNext()) {
            WeakReference<IDownloadStateListener> listenerWeakReference = iterator.next();
            IDownloadStateListener listener = listenerWeakReference.get();
            if (listener != null && listener == downloadStateListener) {
                iterator.remove();
            }
        }
    }

    public static DownloadManager getInstance(Context context) throws IOException {
        synchronized (DownloadManager.class) {
            if (downloadManager == null) {
                downloadManager = new DownloadManager(context);
            }
            return downloadManager;
        }
    }

    private DownloadManager(Context context) throws IOException {
        mContext = context;
        downloadStateListeners = new LinkedList<WeakReference<IDownloadStateListener>>();
        mTaskQueue = new LinkedBlockingQueue<BaseTask>(MAX_DOWNLOAD_SIZE);
        mDownloadingTasks = Collections.synchronizedList(new ArrayList<BaseTask>());
        mPausingTasks = Collections.synchronizedList(new ArrayList<BaseTask>());
        mErrorTasks = Collections.synchronizedList(new ArrayList<BaseTask>());
        pollThread = new PollThread();
    }

    private Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x01:
                    BaseTask task = (BaseTask) msg.obj;
                    if (task != null)
                        task.onStartOption();
                    break;
            }
        }
    };

    //取任务线程
    private class PollThread extends Thread {

        @Override
        public void run() {
            super.run();
            try {
                takeTask();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                Log.e(TAG, "Poll DownloadThread Exception:" + e1);
            }
        }

        private void takeTask() throws InterruptedException {
            while (isInterrupt) {
                BaseTask task = null;
                synchronized (mTaskQueue) {
                    if (mTaskQueue.isEmpty() || mDownloadCount >= MAX_DOWNLING_PROCESS_SIZE) {
                        Log.d("WEN", "mTaskQueue.wait() mDownloadingTasks.size()=" + mDownloadingTasks.size()
                                + ",mTaskQueue.size()=" + mTaskQueue.size());
                        mTaskQueue.wait();
                    }
                    if(mTaskQueue.size()>0 && mDownloadingTasks.size() < MAX_DOWNLING_PROCESS_SIZE){
                        task = mTaskQueue.take();
                        Log.d("WEN", "mTaskQueue.take()");
                    }

                }
                if (task != null) {
                    if(mDownloadingTasks.size()< MAX_DOWNLING_PROCESS_SIZE){
                        mDownloadingTasks.add(task);
                        Log.d("WEN", "mDownloadingTasks.add(task);");
                        mDownloadCount = mDownloadingTasks.size();
                        Message msg = uiHandler.obtainMessage();
                        msg.what = 0x01;
                        msg.obj = task;
                        msg.sendToTarget();
                    }
                }
            }
        }
    }

    /**
     * 开启
     */
    public void onStart() {
        Log.i(TAG, "DownloadManage start");
        isInterrupt = true;
        if (!pollThread.isAlive()) {
            pollThread.start();
        }
    }

    /**
     * 销毁
     */
    public void onDestroy() {
        isInterrupt = false;
        mTaskQueue.clear();
        mDownloadingTasks.clear();
        mErrorTasks.clear();
        mPausingTasks.clear();
        downloadManager = null;
    }

    public boolean isRunning() {
        return !isInterrupt;
    }

    //计算所有的任务数
    public int getTotalTaskCount() {
        Log.i(TAG, "mErrorTasks.size():" + mErrorTasks.size());
        return mTaskQueue.size() + mDownloadingTasks.size() + mPausingTasks.size() + mErrorTasks.size();
    }

    /**
     * 是否有任务
     * @param entity
     * @return
     */
    public boolean isExists(Object entity) {
        BaseTask task = buildNewTask((AppInfo) entity, ((AppInfo)entity).downloadType);
        //task is null not allow put into queue
        if (task == null)
            return true;
        return isExists(task);
    }

    /**
     * 是否有该任务
     *
     * @return
     */
    private boolean isExists(BaseTask task) {
        return mTaskQueue.contains(task) ||
                mDownloadingTasks.contains(task) ||
                mPausingTasks.contains(task) ||
                mErrorTasks.contains(task);
    }

    /**
     * 构造一个新的下载任务
     *
     * @param entity
     * @return
     */
    public BaseTask buildNewTask(AppInfo entity, Type type) {
        BaseTask strate = null;
        try {
            String fileName = entity.BAONAME + ".apk";
            switch (type) {
                case MULTI://cdn
                    URL url4Multi = new URL(Constants.URL_PREFIX + entity.APKURL);
                    strate = new MultiTask(fileName, url4Multi, entity, downloadStateListeners);//test
                    break;
                default://https
                    URL url4Single = new URL(Constants.URL_PREFIX + entity.APKURL);
                    strate = new SingleTask(fileName, url4Single, entity, downloadStateListeners);
                    break;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return strate;
    }

    /**
     * 添加任务 url
     */
    public void onOffer(AppInfo entity) {
        if (!FileUtils.isSDMounted()) {
            new Handler(mContext.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "Not Found SD Card", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        if (getTotalTaskCount() >= MAX_DOWNLOAD_SIZE) {
            new Handler(mContext.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext, "任务太多啦，等我先下载完一部分哈", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }
        BaseTask task = buildNewTask(entity, entity.downloadType);
        if (!isExists(task)) {
            onOffer(task);
        }
    }

    /**
     * 添加任务 task
     *
     * @param task
     */
    private boolean onOffer(BaseTask task) {
        boolean ret = false;
        if (isAllPause) {
            ret = mPausingTasks.add(task);
        } else {
            synchronized (mTaskQueue) {
                ret = mTaskQueue.offer(task);
                mTaskQueue.notifyAll();
                task.onPrepareOption();
                Log.d("WEN", "等待队列中增加1个任务，并通知消费者");
            }
        }
        return ret;
    }

    /**
     * 停止单个任务
     */
    public void onPause(AppInfo entity) {
        BaseTask task = buildNewTask(entity, entity.downloadType);
        onPause(task);
    }

    private void onPause(BaseTask task) {
        if (mDownloadingTasks.contains(task)) {
            int idx = mDownloadingTasks.indexOf(task);
            BaseTask dlTask = mDownloadingTasks.get(idx);
            mDownloadingTasks.remove(idx);
            dlTask.onPauseOption();

        } else if (mTaskQueue.contains(task)) {
            mTaskQueue.remove(task);
        }
        if (!mPausingTasks.contains(task))
            mPausingTasks.add(task);

        //有暂停操作，说明 下载队列的名额空了1个出来，赶紧通知消费者从 等待队列取任务
        if(mTaskQueue.size()>0){
            synchronized (mTaskQueue){
                mTaskQueue.notifyAll();
            }
        }
    }

    /**
     * 删除单个任务
     */
    public void onCancel(AppInfo entity) {
        BaseTask task = buildNewTask(entity, entity.downloadType);
        if (mTaskQueue.contains(task)) {
            mTaskQueue.remove(task);
        } else if (mDownloadingTasks.contains(task)) {
//            mDownloadingTasks.remove(task);
            int idx = mDownloadingTasks.indexOf(task);
            BaseTask dlTask = mDownloadingTasks.get(idx);
            dlTask.onCancelOption();
            mDownloadingTasks.remove(idx);
        } else if (mPausingTasks.contains(task)) {
            mPausingTasks.remove(task);
        } else if (mErrorTasks.contains(task)) {
            mErrorTasks.remove(task);
        } else {
            Log.w(TAG, "can not find task :" + entity.APKURL);
        }
    }

    /**
     * 暂停状态恢复下载任务;把任务放回调度队列
     */
    public void onContinue(AppInfo entity) {
        BaseTask task = buildNewTask(entity, entity.downloadType);
        if (mPausingTasks.contains(task)) {
            if (onOffer(task)) {
                mPausingTasks.remove(task);
            }
        } else if (mErrorTasks.contains(task)) {
            if (onOffer(task)) {
                mErrorTasks.remove(task);
            }
        }
    }

    public synchronized void onFailed(AppInfo entity,Type  type){
        BaseTask task = buildNewTask(entity,type);
        onFailed(task,type);
    }

    public  void onFailed(BaseTask task,Type  type){
        if(mDownloadingTasks.contains(task)){
            int idx = mDownloadingTasks.indexOf(task);
            BaseTask dltask = mDownloadingTasks.get(idx);
            dltask.onStopOption();
            mDownloadingTasks.remove(idx);
//            Log.d("WEN", "DownloadManager  onFailed函数中  "+"下载失败，从下载队列中移除"+" "+type);
        }else if (mTaskQueue.contains(task)) {
            mTaskQueue.remove(task);
        }
        //通知 pollThread取任务
        if(mTaskQueue.size()>0){
            synchronized (mTaskQueue){
                mTaskQueue.notifyAll();
            }
        }
    }

    public void onFinished(AppInfo entity) {
        //调用buildNewTask会产生临时文件,
        // 所以此处调用了,就应该把生成的临时文件删除
        BaseTask task = buildNewTask(entity, entity.downloadType);
        onFinished(task);
        //delete temp files
        switch (entity.downloadType) {
            case MULTI:
                for (int i = 1; i <= MultiTask.MAX_THREAD_SIZE; i++) {
                    String fileName = task.buildFileName(true, i);
                    File file = new File(fileName);
                    if (file.exists()) file.delete();
                }
                break;
            default:
                String fileName = task.buildFileName(false, 1);
                File file = new File(fileName);
                if (file.exists()) file.delete();
                break;
        }
    }

    public void onFinished(BaseTask task){
        if(mDownloadingTasks.contains(task)){
            int idx = mDownloadingTasks.indexOf(task);
            BaseTask dltask = mDownloadingTasks.get(idx);
            dltask.onStopOption();
            mDownloadingTasks.remove(idx);
            Log.d("WEN", "下载完成，从下载队列中移除-" + task.getEntity().toString());

            //通知pollThread取任务
            if(mTaskQueue.size()>0){
                synchronized (mTaskQueue){
                    mTaskQueue.notifyAll();
                }
            }
        }else if(mTaskQueue.contains(task)){
            mTaskQueue.remove(task);
        }
    }
}
