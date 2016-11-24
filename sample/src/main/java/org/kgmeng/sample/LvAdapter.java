package org.kgmeng.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.kgmeng.dmlib.DownloadManager;
import org.kgmeng.dmlib.model.AppInfo;
import org.kgmeng.dmlib.status.DownloadStatus;
import org.kgmeng.sample.view.DownLoadButton;

import java.io.IOException;
import java.util.List;

/**
 * LvAdapter
 *
 * @author JF Zhang
 * @date 2015/12/7
 */
public class LvAdapter extends BaseAdapter{

    List<AppInfo> appInfos;
    Context context;
    DownloadManager downloadManager;

    public LvAdapter(Context context, List<AppInfo> apps) throws IOException {
        this.context = context;
        this.appInfos = apps;
        downloadManager = DownloadManager.INSTANCE;
    }

    @Override
    public int getCount() {
        return appInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return appInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh = null;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            convertView = LayoutInflater.from(context).inflate(R.layout.lv_item, null);
            convertView = inflater.inflate(R.layout.lv_item, null);
            vh = new ViewHolder();
            vh.tvName = (TextView) convertView.findViewById(R.id.tv_appname);
            vh.btnDl = (DownLoadButton) convertView.findViewById(R.id.btn_download);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        final AppInfo appInfo = appInfos.get(position);
        vh.tvName.setText(appInfo.APPNAME);

        /** download */
        int progress = (int)(((double)appInfo.cur_size / (double)Integer.valueOf(appInfo.APPSIZE)) * 100);

        vh.btnDl.showDownloadStatus(appInfo.curStatus, progress);
        vh.btnDl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (appInfo.curStatus == DownloadStatus.NONE.getValue()) {
                    downloadManager.onOffer(appInfo);
                } else if (appInfo.curStatus == DownloadStatus.DLING.getValue()) {
                    downloadManager.onPause(appInfo);
                } else if (appInfo.curStatus == DownloadStatus.PAUSE.getValue()) {
                    downloadManager.onContinue(appInfo);
                } else if (appInfo.curStatus == DownloadStatus.ERROR.getValue()) {
                    downloadManager.onContinue(appInfo);
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
        DownLoadButton btnDl;
    }
}
