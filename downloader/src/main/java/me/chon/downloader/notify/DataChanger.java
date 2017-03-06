package me.chon.downloader.notify;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;

import me.chon.downloader.DownloadEntry;
import me.chon.downloader.db.DBController;

/**
 * Created by chon on 2016/10/21.
 * What? How? Why?
 * 被观察者（数据改变通知观察者）
 */

public class DataChanger extends Observable {
    private Context mContext;
    // all operated record,may eat up a lot of memory
    private LinkedHashMap<String,DownloadEntry> mOperatedEntries;

    private DataChanger(Context context) {
        this.mContext = context;
        mOperatedEntries = new LinkedHashMap<>();
    }

    private static DataChanger instance;
    public static DataChanger getInstance(Context context) {
        if (instance == null) {
            synchronized (DataChanger.class) {
                if (instance == null) {
                    instance = new DataChanger(context);
                }
            }
        }
        return instance;
    }

    public void postStatus(DownloadEntry entry) {
        // save to the memory
        mOperatedEntries.put(entry.id,entry);

        // save to db
        DBController.getInstance(mContext).newOrUpdate(entry);

        setChanged();
        notifyObservers(entry);
    }

    public ArrayList<DownloadEntry> queryAllRecoverableEntries() {
        ArrayList<DownloadEntry> mRecoverableEntries = new ArrayList<>();

        for (Map.Entry<String,DownloadEntry> entry: mOperatedEntries.entrySet()) {
            if (entry.getValue().status == DownloadEntry.DownloadStatus.paused) {
                mRecoverableEntries.add(entry.getValue());
            }
        }

        return  mRecoverableEntries;
    }

    public DownloadEntry queryDownloadEntryByID(String id) {
        return mOperatedEntries.get(id);
    }

    public void addToOperateEntryMap(String id,DownloadEntry entry) {
        mOperatedEntries.put(id,entry);
    }

    public boolean containsDownloadEntry(String id) {
        return mOperatedEntries.containsKey(id);
    }

    public void deleteDownloadEntry(String id){
        mOperatedEntries.remove(id);
        DBController.getInstance(mContext).deleteById(id);
    }
}
