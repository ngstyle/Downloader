package me.chon.downloader;

import java.util.Observable;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 * 被观察者（数据改变通知观察者）
 */

public class DataChanger extends Observable {
    private DataChanger() {}
    private static DataChanger instance;
    public static DataChanger getInstance() {
        if (instance == null) {
            synchronized (DataChanger.class) {
                if (instance == null) {
                    instance = new DataChanger();
                }
            }
        }
        return instance;
    }

    public void postStatus(DownloadEntry entry) {
        setChanged();
        notifyObservers(entry);
    }
}
