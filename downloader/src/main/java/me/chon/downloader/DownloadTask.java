package me.chon.downloader;

import android.os.SystemClock;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class DownloadTask implements Runnable{
    private final DownloadEntry entry;
    private boolean isPaused;
    private boolean isCancelled;

    public DownloadTask(DownloadEntry entry) {
        this.entry = entry;
    }

    public void start() {
        entry.status = DownloadEntry.DownloadStatus.downloading;
        DataChanger.getInstance().postStatus(entry);
        entry.totalLength = 1024 * 10;

        while(entry.currentLength < entry.totalLength) {
            SystemClock.sleep(200);

            if (isCancelled || isPaused) {
                // TODO if cancelled,del file,if paused,record process
                entry.status = isCancelled ? DownloadEntry.DownloadStatus.cancelled : DownloadEntry.DownloadStatus.paused;
                DataChanger.getInstance().postStatus(entry);
                return;
            }

            entry.currentLength += 1024;
            DataChanger.getInstance().postStatus(entry);
        }

        entry.status = DownloadEntry.DownloadStatus.completed;
        DataChanger.getInstance().postStatus(entry);
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
