package me.chon.downloader.notify;

import java.util.Observable;
import java.util.Observer;

import me.chon.downloader.DownloadEntry;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 * 观察者
 */

public abstract class DataWatcher implements Observer {
    @Override
    public void update(Observable observable, Object o) {
        if (o instanceof DownloadEntry) {
            notifyUpdate((DownloadEntry)o);
        }
    }

    public abstract void notifyUpdate(DownloadEntry entry);
}