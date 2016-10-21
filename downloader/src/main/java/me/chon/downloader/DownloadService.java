package me.chon.downloader;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class DownloadService extends Service {
    private HashMap<String,DownloadTask> mDownloadingTasks = new HashMap<>();
    private ExecutorService mExecutors;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutors = Executors.newCachedThreadPool();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
        String action = intent.getStringExtra(Constants.KEY_DOWNLOAD_ACTION);

        doAction(action,entry);

        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(String action, DownloadEntry entry) {
        switch (action) {
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                startDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE:
                pauseDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RESUME:
                resumeDownload(entry);
                break;
            case Constants.KEY_DOWNLOAD_ACTION_CANCEL:
                cancelDownload(entry);
                break;
        }
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if (task != null) {
            task.cancel();
        }
    }

    private void resumeDownload(DownloadEntry entry) {
        startDownload(entry);
    }

    private void pauseDownload(DownloadEntry entry) {
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if (task != null) {
            task.pause();
        }
    }

    private void startDownload(DownloadEntry entry) {
        DownloadTask task = new DownloadTask(entry);
        mDownloadingTasks.put(entry.id,task);
        mExecutors.execute(task);
    }
}
