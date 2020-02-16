package com.chaize.tr.outils;

public class DbContract {

    public static final int SYNC_STATUS_OK = 0;
    public static final int SYNC_STATUS_FAILED = 1;

    public static final String DATABASE_NAME = "bddTR.sqlite";
    public static final String TABLE_NAME = "TR_produits";
    public static final String COL_CODE = "code";
    public static final String COL_MAGASIN = "magasin";
    public static final String COL_DESC = "description";
    public static final String COL_FLGTR = "flgTR";
    public static final String COL_CREDAT = "credattim";
    public static final String COL_SYNC = "syncstatus";

}
