package com.chaize.tr.vue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.chaize.tr.R;
import com.chaize.tr.controleur.Controle;
import com.chaize.tr.modele.Produit;
import com.chaize.tr.outils.ConnectionStateMonitor;
import com.chaize.tr.outils.DbContract;
import com.chaize.tr.outils.DbHelper;
import com.chaize.tr.outils.MySingleton;

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

    ListeProduitsRecyclerAdapter adapter;
    ArrayList<Produit> arrayList = new ArrayList<>();

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_produits);
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new ListeProduitsRecyclerAdapter(arrayList);
        recyclerView.setAdapter(adapter);
        DbHelper.initSynchronization(this);
        readFromLocalStorage();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                readFromLocalStorage();
            }
        };
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                Toast.makeText(ListeProduitsActivity.this, "swiped", Toast.LENGTH_SHORT).show();
                //Remove swiped item from list and notify the RecyclerView
                int pos = viewHolder.getAdapterPosition();
                if (swipeDir == ItemTouchHelper.RIGHT) {
                    Produit itemSelected = arrayList.get(pos);
                    updateProduitFromListe(itemSelected);
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    private void updateProduitFromListe(Produit itemSelected) {
        Intent intent;
        intent = new Intent(ListeProduitsActivity.this, MajActivity.class);
        intent.putExtra(Controle.EXTRA_PRODUIT, itemSelected);
        startActivityForResult(intent, Controle.MAJ_PRODUIT_FROM_LISTE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case Controle.MAJ_PRODUIT_FROM_LISTE:
                // On relit tous les produits
                readFromLocalStorage();

        }
    }

    public void submitProduit(View view) {
        Produit produit = new Produit(Long.toString((new Date()).getTime()),
                100,
                "description article",
                new Random().nextInt(3),
                new Float(new Random().nextInt(10000) / 100),
                DbContract.SYNC_STATUS_FAILED);

        saveToAppServer(produit);
    }

    private void readFromLocalStorage() {

        arrayList.clear();
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.readTousProduitsFromLocalDatabase(database);

        while (cursor.moveToNext()) {
            String code = cursor.getString(cursor.getColumnIndex(DbContract.PRD_CODE));
            int magasin = cursor.getInt(cursor.getColumnIndex(DbContract.PRD_MAGASIN));
            String desc = cursor.getString(cursor.getColumnIndex(DbContract.PRD_DESC));
            int flgTR = cursor.getInt(cursor.getColumnIndex(DbContract.PRD_FLGTR));
            float prix = cursor.getFloat(cursor.getColumnIndex(DbContract.PRD_PRIX));
            int sync_status = cursor.getInt(cursor.getColumnIndex(DbContract.PRD_SYNC));
            arrayList.add(new Produit(code, magasin, desc, flgTR, prix, sync_status));
        }

        adapter.notifyDataSetChanged();

        cursor.close();
        dbHelper.close();
    }

    private void saveToAppServer(Produit produit) {

        if (ConnectionStateMonitor.checkNetworkConnection(ListeProduitsActivity.this)) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, DbContract.SERVER_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String strResponse = jsonObject.getString("response");
                                if (strResponse.equals("OK")) {
                                    saveToLocalStorage(produit, DbContract.SYNC_STATUS_OK);
                                } else {
                                    saveToLocalStorage(produit, DbContract.SYNC_STATUS_FAILED);
                                    Controle.getInstance(ListeProduitsActivity.this).addLog(Controle.typeLog.ERROR, "ListeProduitsActivity.saveToAppServer erreur serveur: " + strResponse);
                                    Toast.makeText(ListeProduitsActivity.this, "Erreur serveur: " + strResponse, Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                Controle.getInstance(ListeProduitsActivity.this).addLog(Controle.typeLog.ERROR, "ListeProduitsActivity.saveToAppServer.onResponse " + e.getLocalizedMessage());
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    saveToLocalStorage(produit, DbContract.SYNC_STATUS_FAILED);

                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();

                    try {
                        params.put("code", produit.getCode());
                        params.put("magasin", String.valueOf(produit.getMagasin()));
                        params.put("desc", produit.getDescription());
                        params.put("flgTR", String.valueOf(produit.getFlgTR()));
                        params.put("prix", String.valueOf(produit.getPrix()));
                    } catch (Exception e) {
                        Controle.addLog(Controle.typeLog.ERROR, "ListeProduitsActivity.saveToAppServer.getParams " + e.getLocalizedMessage());
                    }
                    return params;
                }
            };
            MySingleton.getInstance(ListeProduitsActivity.this).addToRequestQueue(stringRequest);

        } else {
            saveToLocalStorage(produit, DbContract.SYNC_STATUS_FAILED);

        }
    }

    public void saveToLocalStorage(Produit produit, int sync) {
        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.saveProduitToLocalDatabase(produit, sync, database);
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
