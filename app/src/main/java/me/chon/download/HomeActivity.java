package me.chon.download;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import me.chon.downloader.notify.DataWatcher;
import me.chon.downloader.DownloadEntry;
import me.chon.downloader.DownloadManager;
import me.chon.downloader.util.Trace;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private DownloadEntry mEntry;
    private DataWatcher watcher = new DataWatcher() {
        @Override
        public void notifyUpdate(DownloadEntry entry) {
            mEntry = entry;
//            if (mEntry.status == DownloadEntry.DownloadStatus.cancelled) {
//                mEntry = null;
//            }
            Trace.e(entry.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViewById(R.id.download).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (mEntry == null) {
//            mEntry = new DownloadEntry("http://gdown.baidu.com/data/wisegame/0d89baa0cf1f8baa/baidushoujizhushou_16792112.apk");
//            mEntry = new DownloadEntry("http://shouji.360tpcdn.com/150707/2ef5e16e0b8b3135aa714ad9b56b9a3d/com.happyelements.AndroidAnimal_25.apk");
            mEntry = new DownloadEntry("http://shouji.360tpcdn.com/150723/de6fd89a346e304f66535b6d97907563/com.sina.weibo_2057.apk");
//            mEntry = new DownloadEntry("http://api.stay4it.com/uploads/test.jpg");
        }

        if (id == R.id.download) {

            if (mEntry.status == DownloadEntry.DownloadStatus.idle || mEntry.status == DownloadEntry.DownloadStatus.cancelled) {
                DownloadManager.getInstance(this).add(mEntry);
            } else if (mEntry.status == DownloadEntry.DownloadStatus.downloading) {
                DownloadManager.getInstance(this).pause(mEntry);
            } else if (mEntry.status == DownloadEntry.DownloadStatus.paused) {
                DownloadManager.getInstance(this).resume(mEntry);
            }

        } else if (id == R.id.cancel) {
            DownloadManager.getInstance(this).cancel(mEntry);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DownloadManager.getInstance(this).addObserver(watcher);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadManager.getInstance(this).removeObserver(watcher);
    }
}
