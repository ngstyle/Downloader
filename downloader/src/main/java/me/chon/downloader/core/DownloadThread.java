package me.chon.downloader.core;

import android.os.Environment;

import java.io.File;
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
    private final String path;
    private final DownloadListener listener;
    private final int index;
    private boolean isPaused;

    private DownloadEntry.DownloadStatus mStatus;

    public DownloadThread(String url,int index, int startPos, int endPos,DownloadListener listener) {
        this.url = url;
        this.index = index;
        this.startPos = startPos;
        this.endPos = endPos;
        this.path = Environment.getExternalStorageDirectory() + File.separator +
                "downloader" + File.separator + url.substring(url.lastIndexOf("/"));
        this.listener = listener;
    }

    @Override
    public void run() {
        mStatus = DownloadEntry.DownloadStatus.downloading;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            connection.setConnectTimeout(Constants.CONNECT_TIME);
            connection.setReadTimeout(Constants.READ_TIME);
            int responseCode = connection.getResponseCode();
            File file = new File(path);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            RandomAccessFile raf;
            InputStream is;
            if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                raf = new RandomAccessFile(file, "rw");
                raf.seek(startPos);
                is = connection.getInputStream();
                byte[] buffer = new byte[2048];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (isPaused) {
                        break;
                    }
                    raf.write(buffer, 0, len);
                    listener.onProgressChanged(index,len);
                }
                raf.close();
                is.close();
            }

            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                listener.onDownloadPaused(index);
            } else {
                mStatus = DownloadEntry.DownloadStatus.completed;
                listener.onDownloadCompleted(index);
            }

        } catch (IOException e) {
            if (isPaused) {
                mStatus = DownloadEntry.DownloadStatus.paused;
                listener.onDownloadPaused(index);
            } else {
                mStatus = DownloadEntry.DownloadStatus.error;
                listener.onDownloadError(e.getMessage());
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

    interface DownloadListener{
        void onProgressChanged(int index, int progress);

        void onDownloadCompleted(int index);

        void onDownloadError(String message);

        void onDownloadPaused(int index);
    }
}

