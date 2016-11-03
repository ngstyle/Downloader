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
        mDataChanger = DataChanger.getInstance(context);
        context.startService(new Intent(context,DownloadService.class));
    }

    private DataChanger mDataChanger;
//    private static final int MIN_TIME_INTERVAL = 200;
    private long mLastOperateTime = 0;

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
        doOperation(entry, Constants.KEY_DOWNLOAD_ACTION_ADD);
    }

    public void pause(DownloadEntry entry) {
        doOperation(entry,Constants.KEY_DOWNLOAD_ACTION_PAUSE);
    }

    public void resume(DownloadEntry entry) {
        doOperation(entry,Constants.KEY_DOWNLOAD_ACTION_RESUME);
    }

    public void cancel(DownloadEntry entry) {
        doOperation(entry,Constants.KEY_DOWNLOAD_ACTION_CANCEL);
    }

    public void pauseAll() {
        doOperation(null,Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL);
    }

    public void recoverAll() {
        doOperation(null,Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL);
    }

    private boolean checkIfExecutable() {
        long tempTime = System.currentTimeMillis();
        boolean result = tempTime - mLastOperateTime < DownloadConfig.getConfig().getMinOperateInterval();
        mLastOperateTime = tempTime;
        return result;
    }

    public void addObserver(DataWatcher watcher) {
        mDataChanger.addObserver(watcher);
    }

    public void removeObserver(DataWatcher watcher) {
        mDataChanger.deleteObserver(watcher);
    }

    private void doOperation(DownloadEntry entry, String action) {
        if (checkIfExecutable()) {
            return;
        }
        Intent intent = new Intent(mContext,DownloadService.class);
        if (entry != null) {
            intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY,entry);
        }
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION,action);
        mContext.startService(intent);
    }

    public DownloadEntry queryDownloadEntry(String id) {
        return mDataChanger.queryDownloadEntryByID(id);
    }

}
