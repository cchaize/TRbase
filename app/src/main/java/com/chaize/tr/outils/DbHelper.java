package com.chaize.tr.outils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.chaize.tr.controleur.Controle;

import java.util.Date;
import java.util.Random;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+DbContract.TABLE_NAME+" (\n" +
            DbContract.COL_CODE+" varchar(13) PRIMARY KEY NOT NULL,\n" +
            DbContract.COL_MAGASIN+" int(11) NOT NULL,\n" +
            DbContract.COL_DESC+" varchar(50) NOT NULL,\n" +
            DbContract.COL_FLGTR+" int(11) NOT NULL,\n" +
            DbContract.COL_CREDAT+" timestamp NOT NULL default CURRENT_TIMESTAMP, \n"+
            DbContract.COL_SYNC+" int(1) NOT NULL,\n" +
            "    FOREIGN KEY ("+DbContract.COL_MAGASIN+")\n" +
            "    REFERENCES TR_magasins ("+DbContract.COL_MAGASIN+") )";
    private static final String DROP_TABLE = "drop table if exists "+DbContract.TABLE_NAME;

    public DbHelper(@Nullable Context context) {
        super(context, DbContract.DATABASE_NAME, null, DATABASE_VERSION);
    }

        @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(DROP_TABLE);
        onCreate(db);
    }

    public void saveToLocalDatabase(int sync_status, SQLiteDatabase database){

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.COL_CODE,Long.toString((new Date()).getTime()));
        contentValues.put(DbContract.COL_MAGASIN,100);
        contentValues.put(DbContract.COL_DESC,100);
        contentValues.put(DbContract.COL_FLGTR,new Random().nextInt(3));
        contentValues.put(DbContract.COL_SYNC,sync_status);

        database.insert(DbContract.TABLE_NAME, null, contentValues);
    }

    public Cursor readFromLocalDatabase(SQLiteDatabase database) {
        try {
            String[] projection = {DbContract.COL_CODE, DbContract.COL_MAGASIN, DbContract.COL_DESC,
                    DbContract.COL_FLGTR, DbContract.COL_CREDAT, DbContract.COL_SYNC};

            return (database.query(DbContract.TABLE_NAME, projection, null, null, null, null, null));
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.getLocalizedMessage());
            return null;
        }
    }

    public void updateLocalDatabase(String code, int magasin, String desc, int flgTR, int sync_status, SQLiteDatabase database){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.COL_SYNC, sync_status);
        String selection = DbContract.COL_CODE+" LIKE ? and "+DbContract.COL_MAGASIN+"= ?";
        String[] selection_args = {code, Integer.toString(magasin)};
        database.update(DbContract.TABLE_NAME, contentValues, selection, selection_args);
    }
}
