package me.chon.downloader.core;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import me.chon.downloader.DownloadConfig;
import me.chon.downloader.DownloadEntry;
import me.chon.downloader.util.Trace;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class DownloadTask implements ConnectThread.ConnectListener, DownloadThread.DownloadListener {
    private final DownloadEntry mEntry;
    private final Handler mHandler;
    private ExecutorService mExecutors;

    private volatile boolean isPaused;
    private volatile boolean isCancelled;

    private ConnectThread mConnectThread;
    private DownloadThread[] mDownloadThreads;

    private DownloadEntry.DownloadStatus[] mDownloadStatus;

    private long mLastTimestamp;
    private File destFile;

    public DownloadTask(DownloadEntry entry, Handler mHandler, ExecutorService mExecutors) {
        this.mEntry = entry;
        this.mHandler = mHandler;
        this.mExecutors = mExecutors;
        this.destFile = DownloadConfig.getConfig().getDownloadFile(entry.url);
    }

    public void start() {
        if (mEntry.totalLength > 0) {
            Trace.e("no need to check if support range and totalLength");
            startDownload();
        } else {
            // check if support range and get content-length
            mEntry.status = DownloadEntry.DownloadStatus.connecting;
            notifyUpdate(DownloadService.NOTIFY_CONNECTING);
            mConnectThread = new ConnectThread(mEntry.url, this);
            mExecutors.execute(mConnectThread);
        }
    }

    private void startDownload() {
        mEntry.status = DownloadEntry.DownloadStatus.downloading;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOADING);

        if (mEntry.isSupportRange) {
            startMultiDownload();
        } else {
            startSingleDownload();
        }
    }

    private void notifyUpdate(int what) {
        Trace.e("notify: " + mEntry.status + " -- " + what + ": " + mEntry.currentLength);
        Message message = mHandler.obtainMessage();
        message.obj = mEntry;
        message.what = what;
        mHandler.sendMessage(message);

        SystemClock.sleep(10);
    }

    public void pause() {
        isPaused = true;
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }

        if (mDownloadThreads != null) {
            for (DownloadThread mDownloadThread : mDownloadThreads) {
                if (mDownloadThread != null && mDownloadThread.isRunning()) {
                    if (mEntry.isSupportRange) {
                        mDownloadThread.pause();
                    } else {
                        mDownloadThread.cancel();
                    }
                }
            }
        }
    }

    public void cancel() {
        isCancelled = true;
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }

        if (mDownloadThreads != null) {
            for (DownloadThread mDownloadThread : mDownloadThreads) {
                if (mDownloadThread != null && mDownloadThread.isRunning()) {
                    mDownloadThread.cancel();
                }
            }
        }
    }

    private void startSingleDownload() {
        mDownloadThreads = new DownloadThread[1];
        mDownloadThreads[0] = new DownloadThread(mEntry.url, destFile, 0,0,0,this);
        mExecutors.execute(mDownloadThreads[0]);

        mDownloadStatus = new DownloadEntry.DownloadStatus[1];
        mDownloadStatus[0] = DownloadEntry.DownloadStatus.downloading;
    }

    private void startMultiDownload() {
        int block = mEntry.totalLength / DownloadConfig.getConfig().getMaxDownloadThreads();
        int startPos;
        int endPos;
        if (mEntry.ranges == null) {
            mEntry.ranges = new HashMap<>();
            for (int i = 0; i < DownloadConfig.getConfig().getMaxDownloadThreads(); i++) {
                mEntry.ranges.put(i,0);
            }
        }

        mDownloadThreads = new DownloadThread[DownloadConfig.getConfig().getMaxDownloadThreads()];
        mDownloadStatus = new DownloadEntry.DownloadStatus[DownloadConfig.getConfig().getMaxDownloadThreads()];
        for (int i = 0; i < DownloadConfig.getConfig().getMaxDownloadThreads(); i++) {
            startPos = i * block + mEntry.ranges.get(i);
            if (i == DownloadConfig.getConfig().getMaxDownloadThreads() - 1) {
                endPos = mEntry.totalLength;
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(mEntry.url,destFile,i,startPos,endPos,this);
                mExecutors.execute(mDownloadThreads[i]);
                mDownloadStatus[i] = DownloadEntry.DownloadStatus.downloading;
            } else {
                mDownloadStatus[i] = DownloadEntry.DownloadStatus.completed;
            }
        }
    }

    @Override
    public void onConnected(boolean isSupportRange, int totalLength) {
        mEntry.isSupportRange = isSupportRange;
        mEntry.totalLength = totalLength;

        startDownload();
    }

    @Override
    public void onConnectError(String message) {
        if (isPaused || isCancelled) {
            mEntry.status = isPaused ? DownloadEntry.DownloadStatus.paused : DownloadEntry.DownloadStatus.cancelled;
            notifyUpdate(DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
        } else {
            mEntry.status = DownloadEntry.DownloadStatus.error;
            notifyUpdate(DownloadService.NOTIFY_ERROR);
            Trace.e("connect error: " + message);
        }

    }

    @Override
    public synchronized void onProgressChanged(int index, int progress) {
        if (mEntry.isSupportRange) {
            int range = mEntry.ranges.get(index) + progress;
            mEntry.ranges.put(index, range);
        }

        mEntry.currentLength += progress;

        long timeStamp = System.currentTimeMillis();
        if (timeStamp - mLastTimestamp > 1000) {
            mLastTimestamp = timeStamp;
            if (mEntry.totalLength > 0) {
                double percent =  mEntry.currentLength * 10000l / mEntry.totalLength / 100.0;
                mEntry.percent = percent;
            }
            // else content-length unknown,have no percent info
            notifyUpdate(DownloadService.NOTIFY_UPDATING);
        }
    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        if (mEntry.isSupportRange){
            Trace.e("Thread " + index + " completed: " + mEntry.ranges.get(index));
        }

        mDownloadStatus[index] = DownloadEntry.DownloadStatus.completed;
        for (DownloadEntry.DownloadStatus mDownloadStatu : mDownloadStatus) {
            if (mDownloadStatu != DownloadEntry.DownloadStatus.completed) {
                return;
            }
        }

        if (mEntry.totalLength > 0 && mEntry.currentLength != mEntry.totalLength){
            // unknown error
            mEntry.status = DownloadEntry.DownloadStatus.error;
            mEntry.reset();
            notifyUpdate(DownloadService.NOTIFY_ERROR);
        }else {
            mEntry.status = DownloadEntry.DownloadStatus.completed;
            mEntry.percent = 100;
            notifyUpdate(DownloadService.NOTIFY_COMPLETED);
        }

    }

    @Override
    public synchronized void onDownloadPaused(int index) {
        mDownloadStatus[index] = DownloadEntry.DownloadStatus.paused;
        for (DownloadEntry.DownloadStatus mDownloadStatu : mDownloadStatus) {
            if (mDownloadStatu != DownloadEntry.DownloadStatus.completed && mDownloadStatu != DownloadEntry.DownloadStatus.paused) {
                return;
            }
        }

        mEntry.status = DownloadEntry.DownloadStatus.paused;
        notifyUpdate(DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }

    @Override
    public synchronized void onDownloadCancelled(int index) {
        mDownloadStatus[index] = DownloadEntry.DownloadStatus.cancelled;
        for (DownloadEntry.DownloadStatus mDownloadStatu : mDownloadStatus) {
            if (mDownloadStatu != DownloadEntry.DownloadStatus.completed && mDownloadStatu != DownloadEntry.DownloadStatus.cancelled) {
                return;
            }
        }

        // reset mEntry,delete local file
        mEntry.reset();
        mEntry.status = DownloadEntry.DownloadStatus.cancelled;
        notifyUpdate(DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }

    @Override
    public synchronized void onDownloadError(int index,String message) {
        // sth wrong may happened this thread

        mDownloadStatus[index] = DownloadEntry.DownloadStatus.error;
        for (int i = 0; i < mDownloadStatus.length; i++) {
            if (mDownloadStatus[i] != DownloadEntry.DownloadStatus.completed && mDownloadStatus[i] != DownloadEntry.DownloadStatus.error) {
                mDownloadThreads[i].setErrorManually();
                return;
            }
        }

        mEntry.status = DownloadEntry.DownloadStatus.error;
        notifyUpdate(DownloadService.NOTIFY_ERROR);
        Trace.e(message);

    }

//    TODO 1.check if support range, get content-length
//    TODO 2.if not, single thread to download. can't be paused|resumed
//    TODO 3.if support, multiple threads to download
//    TODO 3.1 compute the block size per thread
//    TODO 3.2 execute sub-threads
//    TODO 3.3 combine the progress and notify
}
