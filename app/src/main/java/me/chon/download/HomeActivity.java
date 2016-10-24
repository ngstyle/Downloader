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
            if (mEntry.status == DownloadEntry.DownloadStatus.cancelled) {
                mEntry = null;
            }
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
            mEntry = new DownloadEntry("http://api.stay4it.com/uploads/test.jpg");
        }

        if (id == R.id.download) {

            if (mEntry.status == DownloadEntry.DownloadStatus.idle) {
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
