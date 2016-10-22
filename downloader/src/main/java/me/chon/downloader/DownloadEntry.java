package me.chon.downloader;

import java.io.Serializable;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */

public class DownloadEntry implements Serializable {
    public String id;
    public String name;
    public String url;
    public DownloadStatus status = DownloadStatus.idle;

    public int currentLength;
    public int totalLength;

    public DownloadEntry(String url) {
        this.url = url;
        this.id = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }

    public enum DownloadStatus{
        idle, waiting, downloading, paused, resumed, cancelled, completed
    }


    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "DownloadEntry{" +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", status=" + status + '\'' +
                ", " + currentLength + "/" +totalLength +
                '}';
    }
}
