package me.chon.downloader.core;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.util.Random;

import me.chon.downloader.DownloadEntry;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class DownloadTask implements Runnable{
    private final DownloadEntry mEntry;
    private final Handler mHandler;
    private volatile boolean isPaused;
    private volatile boolean isCancelled;

    public DownloadTask(DownloadEntry entry, Handler mHandler) {
        this.mEntry = entry;
        this.mHandler = mHandler;
    }

    public void start() {
        mEntry.status = DownloadEntry.DownloadStatus.downloading;
        notifyUpdate();

        mEntry.totalLength = 1024 * 100;

        while(mEntry.currentLength < mEntry.totalLength) {
            if (isCancelled || isPaused) {
                // TODO if cancelled,del file,if paused,record process
                mEntry.status = isCancelled ? DownloadEntry.DownloadStatus.cancelled : DownloadEntry.DownloadStatus.paused;
                notifyUpdate();
                return;
            }

            mEntry.currentLength += 1024;
//            DataChanger.getInstance().postStatus(mEntry);
            notifyUpdate();

            SystemClock.sleep(200);
        }
        
        mEntry.status = DownloadEntry.DownloadStatus.completed;
        notifyUpdate();
    }

    private void notifyUpdate() {
        Message message = mHandler.obtainMessage();
        message.obj = mEntry;
        mHandler.sendMessage(message);
    }

    public void pause() {
        isPaused = true;
    }


    public void cancel() {
        isCancelled = true;
    }

    @Override
    public void run() {
        start();
    }

    // TODO check if support range,
}
