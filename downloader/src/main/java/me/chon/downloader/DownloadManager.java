package me.chon.downloader;

import android.content.Context;
import android.content.Intent;

import me.chon.downloader.core.DownloadService;
import me.chon.downloader.notify.DataChanger;
import me.chon.downloader.notify.DataWatcher;
import me.chon.downloader.util.Constants;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class DownloadManager {
    private DownloadManager(Context context){
        this.mContext = context;
    }
    private static DownloadManager instance;

    public static DownloadManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(context);
                }
            }
        }
        return instance;
    }
    private Context mContext;

    public void add(DownloadEntry entry) {
//        Intent intent = new Intent(mContext,DownloadService.class);
//        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
//        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,Constants.KEY_DOWNLOAD_ACTION_ADD);
//        mContext.startService(intent);
        mContext.startService(generateIntent(entry, Constants.KEY_DOWNLOAD_ACTION_ADD));
    }

    public void pause(DownloadEntry entry) {
        mContext.startService(generateIntent(entry,Constants.KEY_DOWNLOAD_ACTION_PAUSE));
    }

    public void resume(DownloadEntry entry) {
        mContext.startService(generateIntent(entry,Constants.KEY_DOWNLOAD_ACTION_RESUME));
    }

    public void cancel(DownloadEntry entry) {
        mContext.startService(generateIntent(entry,Constants.KEY_DOWNLOAD_ACTION_CANCEL));
    }

    public void addObserver(DataWatcher watcher) {
        DataChanger.getInstance().addObserver(watcher);
    }

    public void removeObserver(DataWatcher watcher) {
        DataChanger.getInstance().deleteObserver(watcher);
    }

    private Intent generateIntent(DownloadEntry entry,String action) {
        Intent intent = new Intent(mContext,DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,action);
        return intent;
    }
}
