package com.chaize.tr.modele;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.chaize.tr.controleur.Controle;
import com.chaize.tr.outils.DbContract;
import com.chaize.tr.outils.DbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AccesLocal {

    private String nomBase = "bddTR.sqlite";
    private Integer versionBase = 1;
    private DbHelper accesBDD;
    private SQLiteDatabase bdd;

    public AccesLocal(Context context) {
        accesBDD = new DbHelper(context);
    }

    public void envoi(String operation, JSONArray lesDonneesJSON) {
        switch (operation) {
            case "lecture": lectureProduit(lesDonneesJSON);
                break;
        }
    }

    private void lectureProduit(JSONArray jsonParam) {

        Produit produit=null;

        try {
            bdd = accesBDD.getReadableDatabase();
            String req = "Select * from TR_produits";
            req += " Where code='"+jsonParam.getJSONObject(0).getString("code")+"'";
            req += " and magasin="+jsonParam.getJSONObject(0).getString("magasin")+"";

            Cursor curseur = bdd.rawQuery(req, null);
            JSONArray result = DbHelper.cursor2Json(curseur);

            if (result.length()>0) {
                JSONObject row = result.getJSONObject(0);
                produit = new Produit(row);
            } else {
                produit = new Produit(jsonParam.getJSONObject(0).getString("code"),
                        jsonParam.getJSONObject(0).getInt("magasin"),
                        "inconnu", 0, 0.0f, 0);
            }

        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "lectureProduit : "+e.toString());
        }
        Controle.getInstance(null).setProduit(produit);

    }



    /**
     * Ajout d'un produit dans la BDD
     * @param produit
     */
    public void enregProduit(Produit produit) {

        try {
            bdd = accesBDD.getReadableDatabase();
            String req = "select count(*) from TR_produits where code='"+produit.getCode()+"' and magasin="+produit.getMagasin();
            Cursor curseur = bdd.rawQuery(req, null);
            curseur.moveToFirst();

            bdd = accesBDD.getWritableDatabase();
            if (curseur.getInt(0)==0) {
                req = "insert into TR_produits (code, description, flgTR, magasin)"
                        + " values ('" + produit.getCode() + "','" + produit.getDescription() + "'," + produit.getFlgTR() + "," + produit.getMagasin() + ")";
            } else {
                req = "UPDATE TR_produits "
                   + " SET description='" + produit.getDescription() + "', flgTR=" + produit.getFlgTR()
                   + " WHERE code='"+produit.getCode()+"' and magasin="+produit.getMagasin();
            }
            bdd.execSQL(req);
        } catch (SQLException e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "ajoutProduit : "+e.toString());
        }
    }

    public ArrayList getListeMagasins(int cPostal) {
        ArrayList<Shop> arrayListMagasins = new ArrayList<>();
        try {
            bdd = accesBDD.getReadableDatabase();
            String req = "Select sequence, nom, code_postal, ville from TR_magasins";
            if (cPostal==0)
                req+=" LIMIT 10";
            else if (cPostal<100) {
                req += " Where code_postal>=" + cPostal * 1000;
                req += "   and code_postal<" + (cPostal + 1) * 1000;
            }
            else {
                req+=" Where code_postal="+cPostal;
            }
            Cursor curseur = bdd.rawQuery(req, null);
            JSONArray result = DbHelper.cursor2Json(curseur);
            for(int i=0;i<result.length();i++) {
                arrayListMagasins.add(new Shop(result.getJSONObject(i)));
            }

        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "getListeMagasins : "+e.toString());
        }
        return arrayListMagasins;
    }

    public void enregParam(String param, String valeur) {
        String req;
        try {
            bdd = accesBDD.getWritableDatabase();
            req = "REPLACE INTO TR_parametres\n" +
                    "  (param, valeur)\n" +
                    "VALUES\n" +
                    "  (? , ?) ";
            SQLiteStatement stmt = bdd.compileStatement(req);
            stmt.bindString(1,param);
            stmt.bindString(2,valeur);
            stmt.execute();
        } catch (SQLException e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "enregParam : "+e.toString());
        }
    }

    public String getParam(String param) {
        String valeur="";
        try {
            bdd = accesBDD.getReadableDatabase();
            String table = "TR_parametres";
            String[] columnsToReturn = { "valeur" };
            String selection = "param =?";
            String[] selectionArgs = { param }; // matched to "?" in selection
            Cursor curseur = bdd.query(table, columnsToReturn, selection, selectionArgs, null, null, null);

            JSONArray result = DbHelper.cursor2Json(curseur);

            if (result.length()>0) {
                JSONObject row = result.getJSONObject(0);
                valeur = row.getString("valeur");
            }
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "AccesLocal.getParam : "+e.toString());
        }
        return valeur;
    }

    public Shop getMagasin(int seqMagasin) {
        Shop magasin=null;
        try {
            bdd = accesBDD.getReadableDatabase();
            String table = "TR_magasins";
            String[] columnsToReturn = { "sequence", "nom", "code_postal", "ville" };
            String selection = "sequence = ?";
            String[] selectionArgs = { String.valueOf(seqMagasin) }; // matched to "?" in selection
            Cursor curseur = bdd.query(table, columnsToReturn, selection, selectionArgs, null, null, null);

            JSONArray result = DbHelper.cursor2Json(curseur);

            if (result.length()>0) {
                magasin = new Shop(result.getJSONObject(0));
            }
        } catch (Exception e) {
            Controle.getInstance(null).addLog(Controle.typeLog.ERROR, "AccesLocal.getMagasin : "+e.toString());
        }
        return magasin;
    }
}
