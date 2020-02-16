package com.chaize.tr.outils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.chaize.tr.controleur.Controle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    private String creation_base[];
    private String init_base[];


    public MySQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        int n=0;
        creation_base = new String[6];
        creation_base[n++] = "CREATE TABLE IF NOT EXISTS `TR_magasins` (\n" +
                            "  `sequence` INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                            "  `nom` varchar(50) NOT NULL,\n" +
                            "  `code_postal` INTEGER  NOT NULL,\n" +
                            "  `ville` varchar(50)  NOT NULL) ;";
        creation_base[n++] = "CREATE INDEX IF NOT EXISTS mag0 on TR_magasins (`code_postal`,`nom`);";
        creation_base[n++] = "CREATE TABLE IF NOT EXISTS `TR_produits` (\n" +
                            "  `code` varchar(13) PRIMARY KEY NOT NULL,\n" +
                            "  `magasin` int(11) NOT NULL,\n" +
                            "  `description` varchar(50) NOT NULL,\n" +
                            "  `flgTR` int(11) NOT NULL,\n" +
                            "  `credattim` timestamp NOT NULL default CURRENT_TIMESTAMP, \n"+
                            "  `syncstatus` int(11) NOT NULL,\n" +
                            "    FOREIGN KEY (magasin)\n" +
                            "    REFERENCES TR_magasins (magasin) )";
        creation_base[n++] = "CREATE UNIQUE INDEX IF NOT EXISTS prd0 on TR_produits (`code`,'magasin');";
        creation_base[n++] = "CREATE TABLE IF NOT EXISTS `TR_parametres` (\n" +
                            "  `param` varchar(50) PRIMARY KEY NOT NULL,\n" +
                            "  `valeur` varchar(50) NOT NULL )";
        creation_base[n++] = "CREATE UNIQUE INDEX IF NOT EXISTS par0 on TR_parametres (`param`);";
        n=0;
        init_base = new String[2];
        init_base[n++] = "INSERT INTO `TR_magasins` (`sequence`, `nom`, `code_postal`, `ville`) VALUES\n" +
                "(1, 'Hyper U', '74150', 'Rumilly'),"+
                "(2, 'E. Leclerc', '74650', 'Chavanod'),"+
                "(3, 'Carrefour', '42300', 'Mably')";
        init_base[n++] = "INSERT INTO `TR_produits` (`code`, `magasin`, `description`, `flgTR`, `credattim`) VALUES\n" +
                            "('0', 1, 'test inconnu', 0, '2019-08-06 19:01:49'),\n" +
                            "('1', 1, 'test 1 tr', 1, '2019-08-06 19:02:33'),\n" +
                            "('2', 1, 'test 2 no tr', 2, '2019-08-06 19:02:33');";

    }

    /**
     * Si changement de BDD
     * @param sqLiteDatabase
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //sqLiteDatabase.enableWriteAheadLogging();
        this.setWriteAheadLoggingEnabled(true);
        if (!tableExiste("TR_magasins"))
            reset();
    }

    public void dropTable(String name){
        try {
            String req = "DROP TABLE IF EXISTS " + name;
            this.getWritableDatabase().execSQL(req);
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.toString());
        }
    }

    /**
     * Initialise la base : creation des tables magasins et produits
     * @param sqLiteDatabase
     */
    public void execSQL(SQLiteDatabase sqLiteDatabase, String req[]) {
        int i=0;
        try {
            while (i<req.length) {
                sqLiteDatabase.execSQL(req[i]);
                i++;
            }
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.toString());
        }
    }

    public void reset(){
        try {
            dropTable("TR_magasins");
            dropTable("TR_produits");
            dropTable("TR_parametres");
            execSQL(this.getWritableDatabase(), creation_base);
            execSQL(this.getWritableDatabase(), init_base);
        } catch (Exception e){
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.toString());
        }
    }

    public JSONObject listeTables(){
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            SQLiteDatabase bdd = this.getReadableDatabase();
            String req = "SELECT \n" +
                    "    name\n" +
                    "FROM \n" +
                    "    sqlite_master \n" +
                    "WHERE \n" +
                    "    type ='table'"+
                    " and name like \"TR_%\"";
            Cursor curseur = bdd.rawQuery(req, null);
            curseur.moveToFirst();
            if (!curseur.isBeforeFirst()) {
                do {

                    String req2 = "SELECT count(*) FROM "+curseur.getString(0);
                    Cursor curseur2 = bdd.rawQuery(req2, null);
                    curseur2.moveToFirst();

                    array.put(new JSONObject().put("nom",curseur.getString(0))
                            .put("nbenreg",curseur2.getInt(0)));

                } while (curseur.moveToNext());
            }
            json.put("tables",array);
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.toString());
        }
        return json;
    }

    public boolean tableExiste(String nomTable){
        boolean exist = false;
        try {
            SQLiteDatabase bdd = this.getReadableDatabase();
            String req = "SELECT \n" +
                    "    name\n" +
                    "FROM \n" +
                    "    sqlite_master \n" +
                    "WHERE \n" +
                    "    type ='table' AND name=\""+nomTable+"\"";
            Cursor curseur = bdd.rawQuery(req, null);
            curseur.moveToFirst();
            if (!curseur.isBeforeFirst()) {
                exist = true;
            }
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.toString());
        }
        return exist;
    }

    /**
     * Si changement de version
     * @param sqLiteDatabase
     * @param i ancienne version
     * @param i1 nouvelle version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
