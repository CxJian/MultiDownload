package org.kgmeng.dmlib;

import org.kgmeng.dmlib.model.AppInfo;

/**
 * Created by jf.zhang on 16/11/24.
 */

public enum DownloadController {

    INSTANCE;



    public void onFinishTask(Object entity) {
        DownloadManager.INSTANCE.onFinished((AppInfo)entity);

    }

    public void onFailedTask(Object entity) {
        DownloadManager.INSTANCE.onFailed((AppInfo)entity);
    }

}
