package org.kgmeng.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.internal.widget.ThemeUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import org.kgmeng.dmlib.DownloadManager;
import org.kgmeng.dmlib.IDownloadStateListener;
import org.kgmeng.dmlib.Type;
import org.kgmeng.dmlib.config.Constants;
import org.kgmeng.dmlib.model.AppInfo;
import org.kgmeng.dmlib.status.DownloadStatus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class MainActivity extends Activity {

    List<AppInfo> appInfoList;
    ListView lvMain;
    LvAdapter lvAdapter;
    DownloadManager downloadManager;
    ExecutorService executor;
    Dialog dialog;
    boolean isDetached = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executor = Executors.newSingleThreadExecutor();
        lvMain = (ListView) findViewById(R.id.lv_main);
        dialog = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT)
                .setMessage("waiting...")
                .show();
        FutureTask<Boolean> futureTask = new GetSizeFutureTask(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    makeData();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        executor.execute(futureTask);
        Log.i("tag", "make data finish");

        try {
            downloadManager = DownloadManager.INSTANCE.init(MainActivity.this);
            downloadManager.registerStateListener(iDownloadStateListener);
            downloadManager.onStart();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isDetached = false;
    }

    /**
     * 下载状态回调
     */
    IDownloadStateListener iDownloadStateListener = new IDownloadStateListener() {
        @Override
        public void onPrepare(final Object entity, final long size) {
            Log.i("tag", "onPrepare " + entity.toString() + ",size=" + size);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int progress = (int)(((double)size / (double)((AppInfo)entity).APPSIZE)  * 100);
                    ((AppInfo)entity).cur_size = progress;
                    updateView(entity, DownloadStatus.WAIT, progress);
                }
            });
        }

        @Override
        public void onProcess(final Object entity, final long size) {
//            Log.i("tag", "onProcess " + entity.toString() + ",size=" + size);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int progress = (int)(((double)size / (double)((AppInfo)entity).APPSIZE)  * 100);
                    ((AppInfo)entity).cur_size = progress;
                    updateView(entity, DownloadStatus.DLING, progress);
                }
            });
        }

        @Override
        public void onFinish(final Object entity, final String savePath) {
            Log.i("tag", "onFinish " + entity.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView(entity, DownloadStatus.DONE, savePath);
                }
            });
        }

        @Override
        public void onFailed(final Object entity, final String msg) {
            Log.i("tag", "onFailed " + entity.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView(entity, DownloadStatus.ERROR, msg);
                }
            });
        }

        @Override
        public void onPause(final Object entity, final long size) {
            Log.i("tag", "onProcess " + entity.toString() + ",size=" + size);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int progress = (int)(((double)size / (double)((AppInfo)entity).APPSIZE)  * 100);
                    ((AppInfo)entity).cur_size = progress;
                    updateView(entity, DownloadStatus.PAUSE, progress);
                }
            });
        }

        @Override
        public void onCancel(final Object entity) {
            Log.i("tag", "onCancel " + entity.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateView(entity, DownloadStatus.NONE);
                }
            });
        }
    };

    /**
     * 更新item
     * @param entity
     * @param status
     * @param objects
     */
    protected void updateView(Object entity, DownloadStatus status, Object... objects) {
        ((AppInfo)entity).curStatus = status.getValue();
        int firstPos = lvMain.getFirstVisiblePosition();
        int itemIdx = appInfoList.indexOf(entity);
        if (itemIdx - firstPos >= 0) {
            View view = lvMain.getChildAt(itemIdx - firstPos);
            LvAdapter.ViewHolder vh = (LvAdapter.ViewHolder) view.getTag();
            vh.btnDl.showDownloadStatus(status.getValue(), objects);
        }
    }

    /**
     * 随便找了几个APP，但不知道大小，联网获取一下大小
     */
    protected void makeData() throws IOException {
        appInfoList = new ArrayList<AppInfo>();
        AppInfo info = new AppInfo();
        info.APPNAME = "app1";
        info.APKURL = "30690.apk";
        info.BAONAME = "org.kemeng.test1";
        info.curStatus = DownloadStatus.NONE.getValue();
        info.downloadType = Type.MULTI;
        appInfoList.add(info);

        info = new AppInfo();
        info.APPNAME = "app2";
        info.APKURL = "23209.apk";
        info.BAONAME = "org.kemeng.test2";
        info.curStatus = DownloadStatus.NONE.getValue();
        info.downloadType = Type.MULTI;
        appInfoList.add(info);

        info = new AppInfo();
        info.APPNAME = "app3";
        info.APKURL = "55057.apk";
        info.BAONAME = "org.kemeng.test3";
        info.curStatus = DownloadStatus.NONE.getValue();
        info.downloadType = Type.MULTI;
        appInfoList.add(info);

        info = new AppInfo();
        info.APPNAME = "app4";
        info.APKURL = "42661.apk";
        info.BAONAME = "org.kemeng.test4";
        info.curStatus = DownloadStatus.NONE.getValue();
        info.downloadType = Type.MULTI;
        appInfoList.add(info);

        info = new AppInfo();
        info.APPNAME = "app5";
        info.APKURL = "110746.apk";
        info.BAONAME = "org.kemeng.test5";
        info.curStatus = DownloadStatus.NONE.getValue();
        info.downloadType = Type.MULTI;
        appInfoList.add(info);

        info = new AppInfo();
        info.APPNAME = "app6";
        info.APKURL = "56889.apk";
        info.BAONAME = "org.kemeng.test6";
        info.curStatus = DownloadStatus.NONE.getValue();
        info.downloadType = Type.MULTI;
        appInfoList.add(info);

        for (AppInfo appInfo : appInfoList) {
            URL url = new URL(Constants.URL_PREFIX + appInfo.APKURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            int rspCode = conn.getResponseCode();
            if (rspCode == 200) {
                appInfo.APPSIZE = conn.getContentLength();
            } else {
                appInfo.APPSIZE = 0;
            }
            conn.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDetached = true;
        executor.shutdownNow();
        if (downloadManager != null) {
            downloadManager.unRegisterStateListener(iDownloadStateListener);
            downloadManager.onDestroy();
        }
    }

    class GetSizeFutureTask extends FutureTask<Boolean> {

        public GetSizeFutureTask(Callable<Boolean> callable) {
            super(callable);
        }

        @Override
        protected void done() {
            super.done();
            try {
                lvAdapter = new LvAdapter(MainActivity.this, appInfoList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dialog != null && dialog.isShowing() && !isDetached) {
                            dialog.dismiss();
                        }
                        lvMain.setAdapter(lvAdapter);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            return super.cancel(mayInterruptIfRunning);
        }
    }
}
