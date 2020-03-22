package com.chaize.tr.outils;

public class DbContract {

    public static final int SYNC_STATUS_OK = 0;
    public static final int SYNC_STATUS_FAILED = 1;

    public static final int TR_INCONNU = 0;
    public static final int TR_ACCEPTE = 1;
    public static final int TR_REFUSE  = 2;

    public static final String DATABASE_NAME = "bddTR.sqlite";
    public static final String SERVER_URL =  "http://cyril.chaize.free.fr/baseTR/syncinfo.php";
    public static final String UI_UPDATE_BROADCAST = "com.chaize.tr.uiupdatebroadcast";

    public static final String TABLE_NAMES[] = {"TR_produits", "TR_magasins", "TR_parametres", "TR_panier"};

    // Tables des produits
    public static final String PRD_TABLE_NAME = TABLE_NAMES[0];
    public static final String PRD_CODE = "code";
    public static final String PRD_MAGASIN = "magasin";
    public static final String PRD_DESC = "description";
    public static final String PRD_FLGTR = "flgTR";
    public static final String PRD_PRIX = "prix";
    public static final String PRD_CREDAT = "credattim";
    public static final String PRD_SYNC = "syncstatus";

    // table des magasins
    public static final String MAG_TABLE_NAME = TABLE_NAMES[1];
    public static final String MAG_SEQ = "sequence";
    public static final String MAG_NOM = "nom";
    public static final String MAG_CP = "code_postal";
    public static final String MAG_VILLE = "ville";

    // table des paramètres
    public static final String PAR_TABLE_NAME = TABLE_NAMES[2];
    public static final String PAR_PARAM = "param";
    public static final String PAR_VALEUR = "valeur";

    //table du panier
    public static final String PAN_TABLE_NAME = TABLE_NAMES[3];
    public static final String PAN_CODE = PRD_CODE;
    public static final String PAN_QUANTITE = "quantite";
    public static final String PAN_PRIX = "prix";
}
