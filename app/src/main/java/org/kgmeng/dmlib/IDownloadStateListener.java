package org.kgmeng.dmlib;

/**
 * DownloadStateListener
 *
 * @author JF.Chang
 * @date 2015/8/31
 */
public interface IDownloadStateListener {

    void onPrepare(Object entity, long size);

    void onProcess(Object entity, long size);

    void onFinish(Object entity, String savePath);

    void onFailed(Object entity, String msg);

    void onPause(Object entity, long size);

    void onCancel(Object entity);
}
