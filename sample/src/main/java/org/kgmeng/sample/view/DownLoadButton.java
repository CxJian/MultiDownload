package org.kgmeng.sample.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import org.kgmeng.dmlib.status.DownloadStatus;
import org.kgmeng.sample.R;


/**
 * DownLoadButton
 *
 * @author JF.Chang
 * @date 2015/8/31
 */
public class DownLoadButton extends FrameLayout
{
    
    private ProgressBar progressBar;
    
    private Button button;
    
    public DownLoadButton(Context context) {
        super(context);
        initView(context);
    }
    
    public DownLoadButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    
    public DownLoadButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }
    
    // 初始化控件
    private void initView(Context context) {
        progressBar = new ProgressBar(getContext(),null,android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(false);
        progressBar.setMinimumHeight(getHeight());

//        progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_selector));
//        progressBar.setBackgroundColor(Color.BLUE);
        progressBar.setMax(100);
        progressBar.setProgress(0);

        button = new Button(context);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setBackground(null);

        addView(button);
        addView(progressBar);

//        View v = LayoutInflater.from(context).inflate(R.layout.download_button, null);
//        button = (Button)v.findViewById(R.id.btn_download);
//        progressBar = (ProgressBar)v.findViewById(R.id.btn_download_progressBar);
        button.setText("下载");
        progressBar.setVisibility(View.GONE);
    }

    /**
     * 显示下载状态
     * @param status
     */
    public void showDownloadStatus (int status, Object... objects) {
        if (status == DownloadStatus.DLING.getValue() /*|| appInfo.curStatus == DownloadStatus.WAIT.getValue()*/)
        {
            int progress = (int) objects[0];
            if (progress >= 99 ){
                button.setText("暂停  (" + 99 + "%)");
            }else {
                button.setText("暂停  (" + progress + "%)");
            }
            progressBar.setProgress(progress);
            button.setTextColor(Color.parseColor("#8ed5ff"));
        }
        else if(status == DownloadStatus.WAIT.getValue()){
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            button.setText("等待");
        }
        else if (status == DownloadStatus.DONE.getValue()) {
            progressBar.setVisibility(View.GONE);
            button.setText("完成");
            button.setEnabled(false);
        }
        else if (status == DownloadStatus.PAUSE.getValue()) {
            int progress = (int) objects[0];
            button.setText("继续  (" + progress + "%)");
            progressBar.setProgress(progress);
            button.setTextColor(Color.parseColor("#8ed5ff"));
        }
        else if (status == DownloadStatus.ERROR.getValue()) {
            progressBar.setProgress(0);
            progressBar.setVisibility(View.GONE);
            button.setText("失败");
        }
        else if (status == DownloadStatus.NONE.getValue()) {
            progressBar.setProgress(0);
            button.setText("下载");
            button.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }
    
    public void setOnClickListener(OnClickListener listener) {
        button.setOnClickListener(listener);
    }
    
}
