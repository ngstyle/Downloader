package me.chon.downloader.core;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import me.chon.downloader.db.DBController;
import me.chon.downloader.util.Constants;
import me.chon.downloader.notify.DataChanger;
import me.chon.downloader.DownloadEntry;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class DownloadService extends Service {
    private HashMap<String, DownloadTask> mDownloadingTasks = new HashMap<>();
    private LinkedBlockingQueue<DownloadEntry> mWaitingQueue = new LinkedBlockingQueue<>();
    private ExecutorService mExecutors;
    private DataChanger mDataChanger;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadEntry entry = (DownloadEntry) msg.obj;
            switch (entry.status) {
                case completed:
                    // completed task needs to be removed.
                    mDownloadingTasks.remove(entry);
                case paused:
                case cancelled:
                    checkNext();
                    break;
            }

            mDataChanger.postStatus(entry);
        }
    };

    private void checkNext() {
        DownloadEntry entry = mWaitingQueue.poll();
        if (entry != null) {
            startDownload(entry);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutors = Executors.newCachedThreadPool();
        mDataChanger = DataChanger.getInstance(getApplicationContext());

        // The first time Service start,recover data from db.
        DBController dbController = DBController.getInstance(getApplicationContext());
        ArrayList<DownloadEntry> entries = dbController.queryAll();
        if (entries != null) {
            for (DownloadEntry entry : entries) {
                if (entry.status == DownloadEntry.DownloadStatus.downloading || entry.status == DownloadEntry.DownloadStatus.waiting) {
                    entry.status = DownloadEntry.DownloadStatus.paused;
                    // TODO add a config if recover download needed.
                    addDownload(entry);
                }
                mDataChanger.addToOperateEntryMap(entry.id,entry);
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DownloadEntry entry = (DownloadEntry) intent.getSerializableExtra(Constants.KEY_DOWNLOAD_ENTRY);
        String action = intent.getStringExtra(Constants.KEY_DOWNLOAD_ACTION);

        if (!TextUtils.isEmpty(action)) {
            doAction(action, entry);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(String action, DownloadEntry entry) {
        switch (action) {
            case Constants.KEY_DOWNLOAD_ACTION_ADD:
                addDownload(entry);
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
            case Constants.KEY_DOWNLOAD_ACTION_PAUSE_ALL:
                pauseAllDownload();
                break;
            case Constants.KEY_DOWNLOAD_ACTION_RECOVER_ALL:
                recoverAllDownload();
                break;
        }
    }

    private void addDownload(DownloadEntry entry) {
        if (mDownloadingTasks.size() >= Constants.MAX_DOWNLOAD_TASKS) {
            mWaitingQueue.offer(entry);
            entry.status = DownloadEntry.DownloadStatus.waiting;
            mDataChanger.postStatus(entry);
        } else {
            startDownload(entry);
        }
    }

    private void startDownload(DownloadEntry entry) {
        DownloadTask task = new DownloadTask(entry, mHandler);
        mDownloadingTasks.put(entry.id, task);
        mExecutors.execute(task);
    }

    private void pauseDownload(DownloadEntry entry) {
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if (task != null) {
            task.pause();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.paused;
            mDataChanger.postStatus(entry);
        }
    }

    private void resumeDownload(DownloadEntry entry) {
        addDownload(entry);
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask task = mDownloadingTasks.remove(entry.id);
        if (task != null) {
            task.cancel();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.cancelled;
            mDataChanger.postStatus(entry);
        }
    }

    private void pauseAllDownload() {
        for(Map.Entry<String,DownloadTask> entry:mDownloadingTasks.entrySet()) {
            entry.getValue().pause();
        }
        mDownloadingTasks.clear();

        while(mWaitingQueue.iterator().hasNext()) {
            DownloadEntry entry = mWaitingQueue.poll();
            entry.status = DownloadEntry.DownloadStatus.paused;
            mDataChanger.postStatus(entry);
        }
    }

    private void recoverAllDownload() {
        ArrayList<DownloadEntry> mRecoverableEntries = mDataChanger.queryAllRecoverableEntries();

        for (DownloadEntry entry : mRecoverableEntries) {
            resumeDownload(entry);
        }
    }


}
