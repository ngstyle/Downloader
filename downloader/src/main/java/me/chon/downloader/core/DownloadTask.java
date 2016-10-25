package me.chon.downloader.core;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.util.concurrent.ExecutorService;

import me.chon.downloader.DownloadEntry;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class DownloadTask implements ConnectThread.ConnectListener {
    private final DownloadEntry mEntry;
    private final Handler mHandler;
    private ExecutorService mExecutors;

    private ConnectThread mConnectThread;

    public DownloadTask(DownloadEntry entry, Handler mHandler, ExecutorService mExecutors) {
        this.mEntry = entry;
        this.mHandler = mHandler;
        this.mExecutors = mExecutors;
    }

    public void start() {
        mEntry.status = DownloadEntry.DownloadStatus.connecting;
        notifyUpdate(DownloadService.NOTIFY_CONNECTING);
        mConnectThread = new ConnectThread(mEntry.url,this);
        mExecutors.execute(mConnectThread);

//        mEntry.status = DownloadEntry.DownloadStatus.downloading;
//        notifyUpdate();
//
//        mEntry.totalLength = 1024 * 100;
//
//        while(mEntry.currentLength < mEntry.totalLength) {
//            if (isCancelled || isPaused) {
//                // TODO if cancelled,del file,if paused,record process
//                mEntry.status = isCancelled ? DownloadEntry.DownloadStatus.cancelled : DownloadEntry.DownloadStatus.paused;
//                notifyUpdate();
//                return;
//            }
//
//            mEntry.currentLength += 1024;
////            DataChanger.getInstance().postStatus(mEntry);
//            notifyUpdate();
//
//            SystemClock.sleep(200);
//        }
//
//        mEntry.status = DownloadEntry.DownloadStatus.completed;
//        notifyUpdate();
    }

    private void notifyUpdate(int what) {
        Message message = mHandler.obtainMessage();
        message.obj = mEntry;
        message.what = what;
        mHandler.sendMessage(message);
    }

    public void pause() {
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }
    }


    public void cancel() {
        if (mConnectThread != null && mConnectThread.isRunning()) {
            mConnectThread.cancel();
        }
    }

    @Override
    public void onConnected(boolean isSupportRange, int totalLength) {
        mEntry.isSupportRange = isSupportRange;
        mEntry.totalLength = totalLength;

        if (isSupportRange) {
            startMultiDownload();
        } else {
            startSingleDownload();
        }
    }

    private void startSingleDownload() {

    }

    private void startMultiDownload() {

    }

    @Override
    public void onConnectError(String message) {
        mEntry.status = DownloadEntry.DownloadStatus.error;
        notifyUpdate(DownloadService.NOTIFY_ERROR);
    }

//    TODO 1.check if support range, get content-length
//    TODO 2.if not, single thread to download. can't be paused|resumed
//    TODO 3.if support, multiple threads to download
//    TODO 3.1 compute the block size per thread
//    TODO 3.2 execute sub-threads
//    TODO 3.3 combine the progress and notify
}
