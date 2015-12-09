package org.kgmeng.dmlib;

/**
 * BaseTask
 * 策略基类,提供操作
 * @author JF.Chang
 * @date 2015/8/27
 */
public interface IDownloadBaseOption {

    void onPrepareOption();

    void onStartOption();

    void onPauseOption();

    void onStopOption();

    void onCancelOption();

}
