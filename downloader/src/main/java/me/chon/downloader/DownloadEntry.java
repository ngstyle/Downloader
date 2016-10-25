package me.chon.downloader;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 */
@DatabaseTable(tableName = "downloadentry")
public class DownloadEntry implements Serializable {
    @DatabaseField(id = true)
    public String id;
    @DatabaseField
    public String name;
    @DatabaseField
    public String url;
    @DatabaseField
    public int currentLength;
    @DatabaseField
    public int totalLength;
    @DatabaseField
    public DownloadStatus status = DownloadStatus.idle;
    @DatabaseField
    public boolean isSupportRange;

    public DownloadEntry(String url) {
        this.url = url;
        this.id = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }

    public DownloadEntry() {

    }

    public enum DownloadStatus{
        idle, connecting,waiting, downloading, paused, resumed, cancelled, completed, error
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
