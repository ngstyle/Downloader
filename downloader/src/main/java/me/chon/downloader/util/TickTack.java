package me.chon.downloader.util;

/**
 * Created by Stay on 19/8/15.
 * Powered by www.stay4it.com
 */
public class TickTack {
    private static TickTack mInstance;
    private long mLastStamp;
    private TickTack(){

    }

    public synchronized static TickTack getInstance(){
        if (mInstance == null){
            mInstance = new TickTack();
        }
        return mInstance;
    }

    public synchronized boolean needToNotify(){
        long stamp = System.currentTimeMillis();
        if (stamp - mLastStamp > 1000){
            mLastStamp = stamp;
            return true;
        }
        return false;
    }
}
