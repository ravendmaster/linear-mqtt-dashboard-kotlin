package com.ravendmaster.linearmqttdashboard.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TOPICS)
        db.execSQL("CREATE INDEX topics_index on topics (topic);")

        db.execSQL(SQL_CREATE_HISTORY)
        db.execSQL("CREATE INDEX history_index on history (detail_level, timestamp, topic_id);")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //db.execSQL(SQL_DELETE_TOPICS);
        //db.execSQL(SQL_DELETE_HISTORY);
        //onCreate(db);
        //db.execSQL("DELETE FROM HISTORY WHERE value>1000");
        //db.execSQL("CREATE INDEX topics_index on topics (topic);");
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        private val INTEGER_TYPE = " INTEGER"
        private val REAL_TYPE = " REAL"
        private val NUMERIC_TYPE = " NUMERIC"
        private val BLOB_TYPE = " BLOB"
        private val TEXT_TYPE = " TEXT"
        private val COMMA_SEP = ","
        private val SQL_CREATE_TOPICS = "CREATE TABLE " + HistoryContract.TopicEntry.TABLE_NAME + " (" +
                HistoryContract.TopicEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                HistoryContract.TopicEntry.COLUMN_NAME_TOPIC + TEXT_TYPE +
                " )"
        private val SQL_DELETE_TOPICS = "DROP TABLE IF EXISTS " + HistoryContract.TopicEntry.TABLE_NAME

        private val SQL_CREATE_HISTORY = "CREATE TABLE " + HistoryContract.HistoryEntry.TABLE_NAME + " (" +
                HistoryContract.HistoryEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
                HistoryContract.HistoryEntry.COLUMN_NAME_DETAIL_LEVEL + INTEGER_TYPE + COMMA_SEP +
                HistoryContract.HistoryEntry.COLUMN_NAME_TIMESTAMP + NUMERIC_TYPE + COMMA_SEP +
                HistoryContract.HistoryEntry.COLUMN_NAME_TOPIC_ID + INTEGER_TYPE + COMMA_SEP +
                HistoryContract.HistoryEntry.COLUMN_NAME_VALUE + NUMERIC_TYPE +
                " )"
        private val SQL_DELETE_HISTORY = "DROP TABLE IF EXISTS " + HistoryContract.HistoryEntry.TABLE_NAME


        // If you change the database schema, you must increment the database version.
        val DATABASE_VERSION = 1
        val DATABASE_NAME = "linear.db"
    }
}