package com.chaize.tr.outils;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chaize.tr.controleur.Controle;
import com.chaize.tr.modele.Produit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static String creation_base[];
    private static String init_base[];

    public DbHelper(@Nullable Context context) {
        super(context, DbContract.DATABASE_NAME, null, DATABASE_VERSION);
        int n = 0;
        creation_base = new String[8];

        creation_base[n++] = "CREATE TABLE IF NOT EXISTS " + DbContract.MAG_TABLE_NAME + " (\n" +
                DbContract.MAG_SEQ + " INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                DbContract.MAG_NOM + " varchar(50) NOT NULL,\n" +
                DbContract.MAG_CP + " INTEGER  NOT NULL,\n" +
                DbContract.MAG_VILLE + " varchar(50)  NOT NULL) ;";
        creation_base[n++] = "CREATE INDEX IF NOT EXISTS mag0 on " + DbContract.MAG_TABLE_NAME + " (" + DbContract.MAG_CP + "," + DbContract.MAG_NOM + ");";

        creation_base[n++] = "CREATE TABLE IF NOT EXISTS " + DbContract.PRD_TABLE_NAME + " (\n" +
                DbContract.PRD_CODE + " varchar(13) PRIMARY KEY NOT NULL,\n" +
                DbContract.PRD_MAGASIN + " int(11) NOT NULL,\n" +
                DbContract.PRD_DESC + " varchar(50) NOT NULL,\n" +
                DbContract.PRD_FLGTR + " int(11) NOT NULL,\n" +
                DbContract.PRD_PRIX + " decimal(5,2) NOT NULL,\n" +
                DbContract.PRD_CREDAT + " timestamp NOT NULL default CURRENT_TIMESTAMP, \n" +
                DbContract.PRD_SYNC + " int(1) NOT NULL,\n" +
                "    FOREIGN KEY (" + DbContract.PRD_MAGASIN + ")\n" +
                "    REFERENCES " + DbContract.MAG_TABLE_NAME + " (" + DbContract.PRD_MAGASIN + ") )";
        creation_base[n++] = "CREATE UNIQUE INDEX IF NOT EXISTS prd0 on " + DbContract.PRD_TABLE_NAME + " (" + DbContract.PRD_CODE + "," + DbContract.PRD_MAGASIN + ");";

        creation_base[n++] = "CREATE TABLE IF NOT EXISTS " + DbContract.PAR_TABLE_NAME + " (\n" +
                DbContract.PAR_PARAM + " varchar(50) PRIMARY KEY NOT NULL,\n" +
                DbContract.PAR_VALEUR + " varchar(50) NOT NULL )";
        creation_base[n++] = "CREATE UNIQUE INDEX IF NOT EXISTS par0 on " + DbContract.PAR_TABLE_NAME + " (" + DbContract.PAR_PARAM + ");";

        creation_base[n++] = "CREATE TABLE IF NOT EXISTS `TR_panier` (\n" +
                DbContract.PAN_CODE + " varchar(13) PRIMARY KEY NOT NULL,\n" +
                DbContract.PAN_QUANTITE + " int(3) NOT NULL,\n" +
                DbContract.PAN_PRIX + " decimal(5,2) NOT NULL,\n"+
                DbContract.PAN_POSITION + " int(3) NOT NULL)";
        creation_base[n++] = "CREATE UNIQUE INDEX IF NOT EXISTS pan0 on " + DbContract.PAN_TABLE_NAME + " (" + DbContract.PAN_CODE + ");";

        n = 0;
        init_base = new String[2];
        init_base[n++] = "INSERT INTO " + DbContract.MAG_TABLE_NAME + " (" + DbContract.MAG_SEQ + ", " + DbContract.MAG_NOM + ", " + DbContract.MAG_CP + ", " + DbContract.MAG_VILLE + ") VALUES\n" +
                "(1, 'Hyper U', '74150', 'Rumilly')," +
                "(2, 'E. Leclerc', '74650', 'Chavanod')," +
                "(3, 'Carrefour', '42300', 'Mably')";
        init_base[n++] = "INSERT INTO " + DbContract.PRD_TABLE_NAME + " (" + DbContract.PRD_CODE + ", " + DbContract.PRD_MAGASIN + ", " + DbContract.PRD_DESC + ", " + DbContract.PRD_FLGTR + ", " + DbContract.PRD_PRIX + ", " + DbContract.PRD_CREDAT + ", " + DbContract.PRD_SYNC + ") VALUES\n" +
                "('0', 1, 'test inconnu', 0, 0.12, '2019-08-06 19:01:49', 0),\n" +
                "('1', 1, 'test 1 tr', 1, 1.23, '2019-08-06 19:02:33', 0),\n" +
                "('2', 1, 'test 2 no tr', 2, 2.34, '2019-08-06 19:02:33', 0);";
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)  // Build.VERSION_CODES.JELLY_BEAN = 16
    @Override
    public void onCreate(SQLiteDatabase db) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //sqLiteDatabase.enableWriteAheadLogging();
            this.setWriteAheadLoggingEnabled(true);
        }
        execSQLs(db, creation_base);
        execSQLs(db, init_base);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for (int i = 0; i < DbContract.TABLE_NAMES.length; i++)
            dropTable(db, DbContract.TABLE_NAMES[i]);

        onCreate(db);
    }

    // Suppression d'une table dans la base de donnees
    public void dropTable(SQLiteDatabase db, String name) {
        try {
            String req = "DROP TABLE IF EXISTS " + name;
            db.execSQL(req);
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "DbHeleper.dropTable " + name + e.toString());
        }
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < DbContract.TABLE_NAMES.length; i++)
            dropTable(db, DbContract.TABLE_NAMES[i]);

        onCreate(db);
    }

    /**
     * Execute un ensemble de requêtes en écriture
     *
     * @param db  : (SQLiteDatabase) database ouverte en écriture
     * @param req : (String []) liste de requêtes SQL à lancer
     */
    public void execSQLs(SQLiteDatabase db, String req[]) {
        int i = 0;
        try {
            while (i < req.length) {
                db.execSQL(req[i]);
                i++;
            }
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "DbHelper.execSQLs i=" + i + " " + e.toString());
        }
    }

    public static JSONArray cursor2Json(Cursor cursor) {

        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        rowObject.put(cursor.getColumnName(i),
                                cursor.getString(i));
                    } catch (Exception e) {
                        Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.toString());
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }

        cursor.close();
        return resultSet;

    }

    public String status() {
        String res = "Statut base locale:\n";
        for (int i = 0; i < DbContract.TABLE_NAMES.length; i++)
            if (!tableExiste(DbContract.TABLE_NAMES[i]))
                res += "La table " + DbContract.TABLE_NAMES[i] + " n'existe pas\n";
        res += "Liste des tables:\n";
        try {
            res += listeTables().toString(3);
        } catch (JSONException e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.toString());
        }
        return res;
    }

    /**
     * Renvoie la liste des tables TR_*
     *
     * @return JSONObject {tables:[{nom:"TR_xxx1", nbenreg:n1},{nom:"TR_xxx2", nbenreg:n2},{nom:"TR_xxx3", nbenreg:n3},...]}
     */
    public JSONObject listeTables() {
        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();
        try {
            SQLiteDatabase bdd = this.getReadableDatabase();
            String req = "SELECT name FROM sqlite_master" +
                    " WHERE type ='table' AND name like \"TR_%\"";
            Cursor curseur = bdd.rawQuery(req, null);
            curseur.moveToFirst();
            if (!curseur.isBeforeFirst()) {
                do {

                    String req2 = "SELECT count(*) FROM " + curseur.getString(0);
                    Cursor curseur2 = bdd.rawQuery(req2, null);
                    curseur2.moveToFirst();

                    array.put(new JSONObject().put("nom", curseur.getString(0))
                            .put("nbenreg", curseur2.getInt(0)));

                } while (curseur.moveToNext());
            }
            json.put("tables", array);
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.toString());
        }
        return json;
    }

    /**
     * Indique si une table existe dans la base
     *
     * @param nomTable : nom de la table à trouver
     * @return : true si trouvée, false sinon
     */
    public boolean tableExiste(String nomTable) {
        boolean exist = false;
        try {
            SQLiteDatabase bdd = this.getReadableDatabase();
            String req = "SELECT name FROM sqlite_master" +
                    " WHERE type ='table' AND name=\"" + nomTable + "\"";
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

    public void saveProduitToLocalDatabase(Produit produit, int sync_status, SQLiteDatabase database) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.PRD_CODE, produit.getCode());
        contentValues.put(DbContract.PRD_MAGASIN, produit.getMagasin());
        contentValues.put(DbContract.PRD_DESC, produit.getDescription());
        contentValues.put(DbContract.PRD_FLGTR, produit.getFlgTR());
        contentValues.put(DbContract.PRD_PRIX, produit.getPrix());
        contentValues.put(DbContract.PRD_SYNC, sync_status);

        database.replace(DbContract.PRD_TABLE_NAME, null, contentValues);
    }

    public Cursor readTousProduitsFromLocalDatabase(SQLiteDatabase database) {
        try {
            String[] projection = {DbContract.PRD_CODE, DbContract.PRD_MAGASIN, DbContract.PRD_DESC,
                    DbContract.PRD_FLGTR, DbContract.PRD_PRIX, DbContract.PRD_CREDAT, DbContract.PRD_SYNC};

            return (database.query(DbContract.PRD_TABLE_NAME, projection, null, null, null, null, null));
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.getLocalizedMessage());
            return null;
        }
    }

    public Produit readProduitFromLocalDatabase(String code, int magasin, SQLiteDatabase database) {
        Produit produit = null;
        try {
            String[] projection = {DbContract.PRD_CODE, DbContract.PRD_MAGASIN, DbContract.PRD_DESC,
                    DbContract.PRD_FLGTR, DbContract.PRD_PRIX, DbContract.PRD_CREDAT, DbContract.PRD_SYNC};
            String selection = DbContract.PRD_CODE + "='" + code + "' and " + DbContract.PRD_MAGASIN + "=" + magasin;

            JSONArray result = cursor2Json(database.query(DbContract.PRD_TABLE_NAME, projection, selection, null, null, null, null));
            if (result.length() > 0) {
                JSONObject row = result.getJSONObject(0);
                produit = new Produit(row);
            } else {
                produit = new Produit(code, magasin);
            }
            return produit;

        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, e.getLocalizedMessage());
            return null;
        }
    }

    public void updateProduitIntoLocalDatabase(String code, int magasin, String desc, int flgTR, float prix, int sync_status, SQLiteDatabase database) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbContract.PRD_SYNC, sync_status);
        contentValues.put(DbContract.PRD_DESC, desc);
        contentValues.put(DbContract.PRD_FLGTR, flgTR);
        contentValues.put(DbContract.PRD_PRIX, prix);
        String selection = DbContract.PRD_CODE + " = ? and " + DbContract.PRD_MAGASIN + "= ?";
        String[] selection_args = {code, Integer.toString(magasin)};
        database.update(DbContract.PRD_TABLE_NAME, contentValues, selection, selection_args);
    }

    public void resetPanier(SQLiteDatabase database) {
        try {
            database.delete(DbContract.PAN_TABLE_NAME, null, null);
        } catch (Exception e) {
            Controle.addLog(Controle.typeLog.ERROR, e.toString());
        }
    }

    public void removeFromPanier(String code, SQLiteDatabase database) {
        try {
            database.delete(DbContract.PAN_TABLE_NAME, DbContract.PAN_CODE + "=?", new String[]{code});
        } catch (Exception e) {
            Controle.addLog(Controle.typeLog.ERROR, e.toString());
        }
    }

    public void saveProduitToPanier(JSONObject jsonObject, SQLiteDatabase database) {

        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DbContract.PAN_CODE, jsonObject.getString("code"));
            contentValues.put(DbContract.PAN_QUANTITE, jsonObject.getInt("quantite"));
            contentValues.put(DbContract.PAN_PRIX, jsonObject.getDouble("prix"));
            contentValues.put(DbContract.PAN_POSITION, jsonObject.getDouble("position"));

            database.replace(DbContract.PAN_TABLE_NAME, null, contentValues);
        } catch (JSONException e) {
            Controle.addLog(Controle.typeLog.ERROR, "DbHelper.saveProduitToPanier " + e.getLocalizedMessage());
        }
    }

    public Cursor readPanierFromLocalDatabase(SQLiteDatabase database) {
        try {
            String[] projection = {DbContract.PAN_CODE, DbContract.PAN_QUANTITE, DbContract.PAN_PRIX, DbContract.PAN_POSITION};

            return (database.query(DbContract.PAN_TABLE_NAME, projection, null, null, null, null, DbContract.PAN_POSITION));
        } catch (Exception e) {
            Controle.addLog(Controle.typeLog.ERROR, "DbHelper.readPanierFromLocalDatabase " + e.getLocalizedMessage());
            return null;
        }
    }

    public static void synchronize(Context context, int force){
        DbHelper dbHelper = new DbHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        Cursor cursor = dbHelper.readTousProduitsFromLocalDatabase(database);

        while (cursor.moveToNext()) {
            int sync_status = cursor.getInt(cursor.getColumnIndex((DbContract.PRD_SYNC)));
            if (sync_status == DbContract.SYNC_STATUS_FAILED || force==1) {
                final String code = cursor.getString(cursor.getColumnIndex(DbContract.PRD_CODE));
                final Integer magasin = cursor.getInt(cursor.getColumnIndex(DbContract.PRD_MAGASIN));
                final String desc = cursor.getString(cursor.getColumnIndex(DbContract.PRD_DESC));
                final Integer flgTR = cursor.getInt(cursor.getColumnIndex(DbContract.PRD_FLGTR));
                final Float prix = cursor.getFloat(cursor.getColumnIndex(DbContract.PRD_PRIX));
                StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContract.SERVER_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String strResponse = jsonObject.getString("response");
                                    if (strResponse.equals("OK")) {
                                        dbHelper.updateProduitIntoLocalDatabase(code, magasin, desc, flgTR, prix, DbContract.SYNC_STATUS_OK, database);
                                        context.sendBroadcast(new Intent(DbContract.UI_UPDATE_BROADCAST));
                                    } else {
                                        Toast.makeText(context,"La mise à jour sur le serveur a échoué "+code+" "+desc,Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Controle.getInstance(context).addLog(Controle.typeLog.ERROR, "ListeProduitsActivity.onResponse " + e.getLocalizedMessage() + " : server response= " + response);
                                }


                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error while updating server: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("code", code);
                        params.put("magasin", String.valueOf(magasin));
                        params.put("description", desc);
                        params.put("flgTR", String.valueOf(flgTR));
                        params.put("prix", String.valueOf(prix));
                        return params;
                    }
                };

                MySingleton.getInstance(context).addToRequestQueue(stringRequest);
            }
        }
        cursor.close();
        //dbHelper.close();

    }

    public static void initSynchronization(AppCompatActivity activity){
        ConnectionStateMonitor connectionStateMonitor = new ConnectionStateMonitor(activity);
        connectionStateMonitor.observe(activity, new Observer<Boolean>() {
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    // network available
                    //Context context = MainActivity.this;
                    if (ConnectionStateMonitor.checkNetworkConnection(activity)) {
                        DbHelper.synchronize(activity, 0);
                    }
                } else {
                    // network lost
                }
            }
        });
    }


}
