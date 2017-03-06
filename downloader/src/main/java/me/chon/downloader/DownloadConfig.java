package me.chon.downloader;

import android.os.Environment;


import java.io.File;

import me.chon.downloader.util.FileUtilities;

/**
 * Created by Stay on 18/8/15.
 * Powered by www.stay4it.com
 */
public class DownloadConfig {
    private static DownloadConfig mConfig;

    private int max_download_tasks = 3;
    private int max_download_threads = 3;
    private File downloadDir;
    private int min_operate_interval = 200;
    private boolean recoverDownloadWhenStart = true;
    // FIXME: no implement
    private int max_retry_count = 3;

    private DownloadConfig() {
        downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static DownloadConfig getConfig() {
        if (mConfig == null) {
            mConfig = new DownloadConfig();
        }
        return mConfig;
    }

    public int getMaxDownloadTasks() {
        return max_download_tasks;
    }

    public void setMaxDownloadTasks(int max_download_tasks) {
        this.max_download_tasks = max_download_tasks;
    }

    public int getMaxDownloadThreads() {
        return max_download_threads;
    }

    public void setMaxDownloadThreads(int max_download_threads) {
        this.max_download_threads = max_download_threads;
    }

    public File getDownloadDir() {
        return downloadDir;
    }

    public void setDownloadDir(File downloadDir) {
        this.downloadDir = downloadDir;
    }

    public int getMinOperateInterval() {
        return min_operate_interval;
    }

    public void setMinOperateInterval(int min_operate_interval) {
        this.min_operate_interval = min_operate_interval;
    }

    public boolean isRecoverDownloadWhenStart() {
        return recoverDownloadWhenStart;
    }

    public void setRecoverDownloadWhenStart(boolean recoverDownloadWhenStart) {
        this.recoverDownloadWhenStart = recoverDownloadWhenStart;
    }

    public int getMaxRetryCount() {
        return max_retry_count;
    }

    public void setMaxRetryCount(int max_retry_count) {
        this.max_retry_count = max_retry_count;
    }

    public File getDownloadFile(String url) {
        return new File(downloadDir, FileUtilities.getMd5FileName(url));
    }
}
