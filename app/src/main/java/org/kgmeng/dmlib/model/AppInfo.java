package org.kgmeng.dmlib.model;

import org.kgmeng.dmlib.Type;

import java.io.Serializable;


/**
 * AppInfo
 *
 * @author JF Zhang
 * @date 2015/8/21
 */
public class AppInfo implements Serializable {
    public static final long serialVersionUID = 1L;

    public String APPNAME;//名称
    public String BAONAME;//应用包名
    public String CAVERSION;//versionName
    public String VERSIONCODE;//versionCode
    public String APKURL;//下载地址
    public String CALOGO;//logo地址
    public int APPSIZE;
    public String HASHVALUE;
    public int curStatus;
    public int cur_size;
    public Type downloadType;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppInfo appInfo = (AppInfo) o;

        if (APKURL != null ? !APKURL.equals(appInfo.APKURL) : appInfo.APKURL != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "APPNAME='" + APPNAME + '\'' +
                ", BAONAME='" + BAONAME + '\'' +
                ", CAVERSION='" + CAVERSION + '\'' +
                ", VERSIONCODE='" + VERSIONCODE + '\'' +
                ", APKURL='" + APKURL + '\'' +
                ", CALOGO='" + CALOGO + '\'' +
                ", APPSIZE=" + APPSIZE +
                ", HASHVALUE='" + HASHVALUE + '\'' +
                ", curStatus=" + curStatus +
                ", cur_size=" + cur_size +
                ", downloadType=" + downloadType +
                '}';
    }
}
