package me.chon.downloader;

import android.util.SparseIntArray;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.prefs.Preferences;

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
    @DatabaseField
    public float percent;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    public HashMap<Integer,Integer> ranges;

    public DownloadEntry(String url) {
        this.url = url;
        this.id = url;
        this.name = url.substring(url.lastIndexOf("/") + 1);
    }

    public DownloadEntry() {

    }

    public void reset() {
        percent = 0;
        currentLength = 0;
        ranges = null;
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
        return "{" +
                "name='" + name + '\'' +
                ", status=" + status +
                ", " + currentLength + "/" +totalLength +
                ", percent = " + percent + "%" +
                '}';
    }
}
