package me.chon.downloader.notify;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

import me.chon.downloader.DownloadEntry;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 * 被观察者（数据改变通知观察者）
 */

public class DataChanger extends Observable {

    private LinkedHashMap<String,DownloadEntry> mOperationEntries;

    private DataChanger() {
        mOperationEntries = new LinkedHashMap<>();
    }

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
        mOperationEntries.put(entry.id,entry);

        setChanged();
        notifyObservers(entry);
    }

    public ArrayList<DownloadEntry> queryAllRecoverableEntries() {
        ArrayList<DownloadEntry> mRecoverableEntries = new ArrayList<>();

        for (Map.Entry<String,DownloadEntry> entry:mOperationEntries.entrySet()) {
            if (entry.getValue().status == DownloadEntry.DownloadStatus.paused) {
                mRecoverableEntries.add(entry.getValue());
            }
        }

        return  mRecoverableEntries;
    }
}
