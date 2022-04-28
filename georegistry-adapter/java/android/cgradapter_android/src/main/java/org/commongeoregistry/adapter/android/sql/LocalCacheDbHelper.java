package org.commongeoregistry.adapter.android.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.commongeoregistry.adapter.android.sql.LocalCacheContract.GeoObjectEntry;
import org.commongeoregistry.adapter.android.sql.LocalCacheContract.TreeNodeEntry;
import org.commongeoregistry.adapter.android.sql.LocalCacheContract.ActionEntry;

public class LocalCacheDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "LocalCache.db";

    private static final String SQL_CREATE_OBJECT_ENTRY =
            "CREATE TABLE " + GeoObjectEntry.TABLE_NAME + " ( " +
                    GeoObjectEntry._ID + " INTEGER PRIMARY KEY, " +
                    GeoObjectEntry.COLUMN_NAME_UID + " TEXT NOT NULL UNIQUE, " +
                    GeoObjectEntry.COLUMN_NAME_OBJECT + " TEXT )";

    private static final String SQL_CREATE_NODE_ENTRY =
            "CREATE TABLE " + TreeNodeEntry.TABLE_NAME + " ( " +
                    TreeNodeEntry._ID + " INTEGER PRIMARY KEY," +
                    TreeNodeEntry.COLUMN_NAME_PARENT + " TEXT NOT NULL, " +
                    TreeNodeEntry.COLUMN_NAME_CHILD + " TEXT NOT NULL, " +
                    TreeNodeEntry.COLUMN_NAME_HIERARCHY + " TEXT NOT NULL )";

    private static final String SQL_CREATE_ACTION_ENTRY =
            "CREATE TABLE " + ActionEntry.TABLE_NAME + " ( " +
                    ActionEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ActionEntry.COLUMN_NAME_JSON + " TEXT NOT NULL, " +
                    ActionEntry.COLUMN_NAME_CREATE_ACTION_DATE + " INTEGER " + " )"; // https://stackoverflow.com/questions/7363112/best-way-to-work-with-dates-in-android-sqlite
    
    private static final String SQL_REGISTRY_ID =
        "CREATE TABLE " + LocalCacheContract.RegistryIdEntry.TABLE_NAME + " ( " +
                LocalCacheContract.RegistryIdEntry.COLUMN_NAME_REGISTRY_ID + " TEXT NOT NULL, " +
                LocalCacheContract.RegistryIdEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY )";

    private static final String SQL_CREATE_PUSH_HISTORY =
            "CREATE TABLE " + LocalCacheContract.ActionPushHistoryEntry.TABLE_NAME + " ( " +
                    LocalCacheContract.ActionPushHistoryEntry.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LocalCacheContract.ActionPushHistoryEntry.COLUMN_NAME_LAST_PUSH_DATE + " INTEGER )";

    private static final String SQL_DELETE_OBJECT_ENTRY =
            "DROP TABLE IF EXISTS " + GeoObjectEntry.TABLE_NAME;

    private static final String SQL_DELETE_NODE_ENTRY =
            "DROP TABLE IF EXISTS " + TreeNodeEntry.TABLE_NAME;

    private static final String SQL_DELETE_ACTION_ENTRY =
            "DROP TABLE IF EXISTS " + ActionEntry.TABLE_NAME;

    private static final String SQL_DELETE_PUSH_HISTORY_ENTRY =
            "DROP TABLE IF EXISTS " + LocalCacheContract.ActionPushHistoryEntry.TABLE_NAME;

    private static final String SQL_DELETE_REGISTRY_IDS =
            "DROP TABLE IF EXISTS " + LocalCacheContract.RegistryIdEntry.TABLE_NAME;


    public LocalCacheDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_OBJECT_ENTRY);
        db.execSQL(SQL_CREATE_NODE_ENTRY);
        db.execSQL(SQL_CREATE_ACTION_ENTRY);
        db.execSQL(SQL_CREATE_PUSH_HISTORY);
        db.execSQL(SQL_REGISTRY_ID);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        recreate(db);
    }

    public void recreate(SQLiteDatabase db) {
        db.execSQL(SQL_DELETE_NODE_ENTRY);
        db.execSQL(SQL_DELETE_OBJECT_ENTRY);
        db.execSQL(SQL_DELETE_ACTION_ENTRY);
        db.execSQL(SQL_DELETE_PUSH_HISTORY_ENTRY);
        db.execSQL(SQL_DELETE_REGISTRY_IDS);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}