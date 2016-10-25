package me.chon.downloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import me.chon.downloader.DownloadEntry;

/**
 * Created by chon on 2016/10/25.
 * What? How? Why?
 */
public class OrmDBHelper extends OrmLiteSqliteOpenHelper {

    public static final String DB_NAME = "downloader";
    public static final int DB_VERSION = 1;

    public OrmDBHelper(Context context, String databaseName, CursorFactory factory, int databaseVersion) {
        super(context, DB_NAME, factory, DB_VERSION);
    }

    public OrmDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, DownloadEntry.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int arg2, int arg3) {
    }

}
