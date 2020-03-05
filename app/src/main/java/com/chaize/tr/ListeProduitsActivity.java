package com.chaize.tr;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chaize.tr.controleur.Controle;
import com.chaize.tr.modele.Produit;
import com.chaize.tr.outils.ConnectionStateMonitor;
import com.chaize.tr.outils.DbContract;
import com.chaize.tr.outils.DbHelper;
import com.chaize.tr.outils.MySingleton;
import com.chaize.tr.outils.NetworkMonitor;
import com.chaize.tr.vue.RecyclerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ListeProduitsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    RecyclerAdapter adapter;
    ArrayList<Produit> arrayList = new ArrayList<>();

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_produits);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new RecyclerAdapter(arrayList);
        recyclerView.setAdapter(adapter);
        readFromLocalStorage();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                readFromLocalStorage();
            }
        };
        ConnectionStateMonitor connectionStateMonitor = new ConnectionStateMonitor(this);
        connectionStateMonitor.observe(this, new Observer<Boolean>(){
            public void onChanged(Boolean aBoolean) {
                if (aBoolean){
                    // network available
                    Context context = ListeProduitsActivity.this;
                    if (ConnectionStateMonitor.checkNetworkConnection(context)) {
                        DbHelper dbHelper = new DbHelper(context);
                        SQLiteDatabase database = dbHelper.getWritableDatabase();

                        Cursor cursor = dbHelper.readFromLocalDatabase(database);

                        while (cursor.moveToNext()) {
                            int sync_status = cursor.getInt(cursor.getColumnIndex((DbContract.COL_SYNC)));
                            if (sync_status==DbContract.SYNC_STATUS_FAILED) {
                                final String code = cursor.getString(cursor.getColumnIndex(DbContract.COL_CODE));
                                final Integer magasin = cursor.getInt(cursor.getColumnIndex(DbContract.COL_MAGASIN));
                                final String desc = cursor.getString(cursor.getColumnIndex(DbContract.COL_DESC));
                                final Integer flgTR = cursor.getInt(cursor.getColumnIndex(DbContract.COL_FLGTR));
                                StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContract.SERVER_URL,
                                        new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
                                                try {
                                                    JSONObject jsonObject = new JSONObject(response);
                                                    String strResponse = jsonObject.getString("response");
                                                    if (strResponse.equals("OK")) {
                                                        dbHelper.updateLocalDatabase(code,magasin,desc,flgTR,DbContract.SYNC_STATUS_OK, database);
                                                        context.sendBroadcast(new Intent(DbContract.UI_UPDATE_BROADCAST));

                                                    }
                                                } catch (JSONException e) {
                                                    Controle.getInstance(context).addLog(Controle.typeLog.ERROR, "NetworkMonitor.onResponse "+e.getLocalizedMessage());
                                                }


                                            }
                                        }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                    }
                                })
                                {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        Map<String,String> params = new HashMap<>();
                                        params.put("code", code);
                                        params.put("magasin", String.valueOf(magasin));
                                        params.put("desc", desc);
                                        params.put("flgTR", String.valueOf(flgTR));
                                        return params;
                                    }
                                };

                                MySingleton.getInstance(context).addToRequestQueue(stringRequest);
                            }
                        }
                        //cursor.close();
                        //dbHelper.close();
                    }
                }else{
                    // network lost
                }
            }
        });
    }

    public void submitProduit(View view) {
        // récupérer ici les infos saisies. Par exemple:
        // String code = XXX.getText().toString();
        // puis sauvegarder en passant les info à la méthode saveToLocalStorage
        try {
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("code", Long.toString((new Date()).getTime()));
            jsonParam.put("magasin", 100);
            jsonParam.put("desc", "description article");
            jsonParam.put("flgTR", new Random().nextInt(3));

            saveToAppServer(jsonParam);
        } catch (JSONException e) {
            Controle.getInstance(this).addLog(Controle.typeLog.ERROR, "submitProduit "+e.getLocalizedMessage());
        }
    }

    private void readFromLocalStorage() {

        arrayList.clear();
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.readFromLocalDatabase(database);

        while (cursor.moveToNext()) {
            String code = cursor.getString(cursor.getColumnIndex(DbContract.COL_CODE));
            int magasin = cursor.getInt(cursor.getColumnIndex(DbContract.COL_MAGASIN));
            String desc = cursor.getString(cursor.getColumnIndex(DbContract.COL_DESC));
            int flgTR = cursor.getInt(cursor.getColumnIndex(DbContract.COL_FLGTR));
            int sync_status = cursor.getInt(cursor.getColumnIndex(DbContract.COL_SYNC));
            arrayList.add(new Produit(code,magasin,desc,flgTR,sync_status));
        }

        adapter.notifyDataSetChanged();

        cursor.close();
        dbHelper.close();
    }

    private void saveToAppServer(JSONObject jsonParam) {

        if(ConnectionStateMonitor.checkNetworkConnection(ListeProduitsActivity.this)) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContract.SERVER_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String strResponse = jsonObject.getString("response");
                                if (strResponse.equals("OK")) {
                                    saveToLocalStorage(jsonParam, DbContract.SYNC_STATUS_OK);
                                } else {
                                    saveToLocalStorage(jsonParam, DbContract.SYNC_STATUS_FAILED);

                                }

                            } catch (JSONException e) {
                                Controle.getInstance(ListeProduitsActivity.this).addLog(Controle.typeLog.ERROR, "ListeProduitsActivity.saveToAppServer.onResponse "+e.getLocalizedMessage());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    saveToLocalStorage(jsonParam, DbContract.SYNC_STATUS_FAILED);

                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params = new HashMap<>();

                    try {
                        params.put("code", jsonParam.getString("code"));
                        params.put("magasin", String.valueOf(jsonParam.getInt("magasin")));
                        params.put("desc", jsonParam.getString("desc"));
                        params.put("flgTR", String.valueOf(jsonParam.getInt("flgTR")));
                    } catch (JSONException e) {
                        Controle.getInstance(ListeProduitsActivity.this).addLog(Controle.typeLog.ERROR, "ListeProduitsActivity.saveToAppServer.getParams "+e.getLocalizedMessage());
                    }
                    return params;
                }
            };
            MySingleton.getInstance(ListeProduitsActivity.this).addToRequestQueue(stringRequest);

        } else {
            saveToLocalStorage(jsonParam, DbContract.SYNC_STATUS_FAILED);

        }
    }

    public void saveToLocalStorage(JSONObject jsonParam, int sync) {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.saveToLocalDatabase(jsonParam, sync, database);
        readFromLocalStorage();
        dbHelper.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter(DbContract.UI_UPDATE_BROADCAST));
      }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }
}
