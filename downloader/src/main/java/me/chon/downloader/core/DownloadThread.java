package me.chon.downloader.core;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import me.chon.downloader.DownloadEntry;
import me.chon.downloader.util.Constants;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class DownloadThread implements Runnable {
    private final String url;
    private final int startPos;
    private final int endPos;
    private final File destFile;
    private final DownloadListener listener;
    private final int index;
    private volatile boolean isPaused;
    private volatile boolean isCancelled;
    private volatile boolean isError;

    private boolean isSingleDownload;

    private DownloadEntry.DownloadStatus mStatus;

    public DownloadThread(String url, File destFile, int index, int startPos, int endPos, DownloadListener listener) {
        this.url = url;
        this.index = index;
        this.startPos = startPos;
        this.destFile = destFile;
        this.endPos = endPos;
        this.listener = listener;
        isSingleDownload = startPos == endPos;
    }

    @Override
    public void run() {
        mStatus = DownloadEntry.DownloadStatus.downloading;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            if (!isSingleDownload) {
                connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            }
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();

            RandomAccessFile raf;
            FileOutputStream fos;
            InputStream is;
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                raf = new RandomAccessFile(destFile, "rw");
                raf.seek(startPos);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCancelled || isError) {
                        break;
                    }
                    raf.write(buffer, 0, len);
                    listener.onProgressChanged(index,len);
                }
                raf.close();
                is.close();
            } else if (responseCode == HttpURLConnection.HTTP_OK) {
                fos = new FileOutputStream(destFile);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused || isCancelled || isError) {
                        break;
                    }
                    fos.write(buffer, 0, len);
                    listener.onProgressChanged(index,len);
                }
                fos.close();
                is.close();
            } else {
                // other response code
                mStatus = DownloadEntry.DownloadStatus.error;
                listener.onDownloadError(index,"server error:" + responseCode);
                return;
            }

            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                listener.onDownloadPaused(index);
            } else if (isCancelled) {
                mStatus = DownloadEntry.DownloadStatus.cancelled;
                listener.onDownloadCancelled(index);
            } else if (isError) {
                mStatus = DownloadEntry.DownloadStatus.error;
                listener.onDownloadError(index,"set error manually");
            } else {
                mStatus = DownloadEntry.DownloadStatus.completed;
                listener.onDownloadCompleted(index);
            }

        } catch (IOException e) {
            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                listener.onDownloadPaused(index);
            } else if (isCancelled) {
                mStatus = DownloadEntry.DownloadStatus.cancelled;
                listener.onDownloadCancelled(index);
            } else {
                mStatus = DownloadEntry.DownloadStatus.error;
                String message = e.getMessage();
                if (TextUtils.isEmpty(message)) {
                    message = e.toString();
                }
                listener.onDownloadError(index,message);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public void pause() {
        isPaused = true;
        Thread.currentThread().interrupt();
    }

    public boolean isPaused() {
        return mStatus == DownloadEntry.DownloadStatus.paused || mStatus == DownloadEntry.DownloadStatus.completed;
    }

    public boolean isRunning() {
        return mStatus == DownloadEntry.DownloadStatus.downloading;
    }

    public void cancel() {
        isCancelled = true;
        Thread.currentThread().interrupt();
    }

    public boolean isCancelled() {
        return mStatus == DownloadEntry.DownloadStatus.cancelled || mStatus == DownloadEntry.DownloadStatus.completed;
    }

    public boolean isError() {
        return mStatus == DownloadEntry.DownloadStatus.error;
    }

    public void setErrorManually() {
        isError = true;
        Thread.currentThread().interrupt();
    }

    public boolean isCompleted() {
        return mStatus == DownloadEntry.DownloadStatus.completed;
    }

    interface DownloadListener{
        void onProgressChanged(int index, int progress);

        void onDownloadCompleted(int index);

        void onDownloadError(int index,String message);

        void onDownloadPaused(int index);

        void onDownloadCancelled(int index);
    }
}

