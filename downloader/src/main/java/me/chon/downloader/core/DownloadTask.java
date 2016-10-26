package me.chon.downloader.core;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.SparseIntArray;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import me.chon.downloader.DownloadEntry;
import me.chon.downloader.util.Constants;
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

    public DownloadTask(DownloadEntry entry, Handler mHandler, ExecutorService mExecutors) {
        this.mEntry = entry;
        this.mHandler = mHandler;
        this.mExecutors = mExecutors;
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
        if (mEntry.isSupportRange) {
            startMultiDownload();
        } else {
            startSingleDownload();
        }
    }

    private void notifyUpdate(int what) {
        Message message = mHandler.obtainMessage();
        message.obj = mEntry;
        message.what = what;
        mHandler.sendMessage(message);
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
        mEntry.status = DownloadEntry.DownloadStatus.downloading;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOADING);

        mDownloadThreads = new DownloadThread[1];
        mDownloadThreads[0] = new DownloadThread(mEntry.url,0,0,0,this);
        mExecutors.execute(mDownloadThreads[0]);
    }

    private void startMultiDownload() {
        mEntry.status = DownloadEntry.DownloadStatus.downloading;
        notifyUpdate(DownloadService.NOTIFY_DOWNLOADING);

        int block = mEntry.totalLength / Constants.MAX_DOWNLOAD_THREADS;
        int startPos;
        int endPos;
        if (mEntry.ranges == null) {
            mEntry.ranges = new HashMap<>();
            for (int i = 0; i < Constants.MAX_DOWNLOAD_THREADS; i++) {
                mEntry.ranges.put(i,0);
            }
        }

        mDownloadThreads = new DownloadThread[Constants.MAX_DOWNLOAD_THREADS];
        for (int i = 0; i < Constants.MAX_DOWNLOAD_THREADS; i++) {
            startPos = i * block + mEntry.ranges.get(i);
            if (i == Constants.MAX_DOWNLOAD_THREADS - 1) {
                endPos = mEntry.totalLength;
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos < endPos) {
                mDownloadThreads[i] = new DownloadThread(mEntry.url,i,startPos,endPos,this);
                mExecutors.execute(mDownloadThreads[i]);
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
        if (mEntry.currentLength == mEntry.totalLength){
            mEntry.percent = 100;
            mEntry.status = DownloadEntry.DownloadStatus.completed;
            notifyUpdate(DownloadService.NOTIFY_COMPLETED);
        } else {
            int percent = (int) (mEntry.currentLength * 100l / mEntry.totalLength);
            if (percent > mEntry.percent) {
                mEntry.percent = percent;
                notifyUpdate(DownloadService.NOTIFY_UPDATING);
            }
        }

    }

    @Override
    public synchronized void onDownloadCompleted(int index) {
        Trace.e("Thread " + index + " completed: " + mEntry.ranges.get(index));
    }

    @Override
    public synchronized void onDownloadPaused(int index) {
        for (DownloadThread mDownloadThread : mDownloadThreads) {
            if (mDownloadThread != null && !mDownloadThread.isPaused()) {
                return;
            }
        }

        mEntry.status = DownloadEntry.DownloadStatus.paused;
        notifyUpdate(DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }

    @Override
    public synchronized void onDownloadCancelled(int index) {
        for (DownloadThread mDownloadThread : mDownloadThreads) {
            if (mDownloadThread != null && !mDownloadThread.isCancelled()) {
                return;
            }
        }

        // reset mEntry,delete local file
        mEntry.reset();
        String path = Environment.getExternalStorageDirectory() + File.separator +
                "downloader" + File.separator + mEntry.url.substring(mEntry.url.lastIndexOf("/"));
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }

        mEntry.status = DownloadEntry.DownloadStatus.cancelled;
        notifyUpdate(DownloadService.NOTIFY_PAUSED_OR_CANCELLED);
    }

    @Override
    public synchronized void onDownloadError(int index,String message) {
        // sth wrong may happened this thread
        boolean isAllThreadError = true;

        for (int i = 0; i < mDownloadThreads.length; i++) {
            if (mDownloadThreads[i] != null & !mDownloadThreads[i].isError()) {
                isAllThreadError = false;
                Trace.e("cancel download thread["+i+"] manually cause net error:" + message);
                mDownloadThreads[i].setErrorManually();
            }
        }

        if (isAllThreadError) {
            mEntry.status = DownloadEntry.DownloadStatus.error;
            notifyUpdate(DownloadService.NOTIFY_ERROR);
            Trace.e(message);
        }
    }

//    TODO 1.check if support range, get content-length
//    TODO 2.if not, single thread to download. can't be paused|resumed
//    TODO 3.if support, multiple threads to download
//    TODO 3.1 compute the block size per thread
//    TODO 3.2 execute sub-threads
//    TODO 3.3 combine the progress and notify
}
