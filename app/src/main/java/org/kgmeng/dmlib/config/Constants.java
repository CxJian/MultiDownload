package org.kgmeng.dmlib.config;

import android.os.Environment;

/**
 * Constants
 *
 * @author JF Zhang
 * @date 2015/9/7
 */
public class Constants {

    public static final String SDCARD_BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    /**
     * MMAssistant project's root folder path
     */
    public static final String APP_BASE_PATH = SDCARD_BASE_PATH + "/kgmeng";
    /**
     * APK folder path
     */
    public static final String APK_PATH = APP_BASE_PATH + "/apk";

    /**
     * download URL prefix
     */
    public static final String URL_PREFIX = "http://app.2345.cn/appsoft/";

}
