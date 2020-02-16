package com.chaize.tr;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.chaize.tr.modele.Produit;
import com.chaize.tr.outils.DbContract;
import com.chaize.tr.outils.DbHelper;
import com.chaize.tr.vue.RecyclerAdapter;

import java.util.ArrayList;

public class ListeProduitsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    RecyclerAdapter adapter;
    ArrayList<Produit> arrayList = new ArrayList<>();

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
    }

    public void submitProduit(View view) {
        // récupérer ici les infos saisies. Par exemple:
        // String code = XXX.getText().toString();
        // puis sauvegarder en passant les info à la méthode saveToLocalStorage
        saveToLocalStorage();
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

    private void saveToLocalStorage() {

        DbHelper dbHelper = new DbHelper(this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        if(checkNetworkConnection()) {

        } else {
            dbHelper.saveToLocalDatabase(DbContract.SYNC_STATUS_FAILED, database);

        }
        readFromLocalStorage();
        dbHelper.close();
    }


    @TargetApi(23)
    public  boolean checkNetworkConnection() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) this.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >=23) {

                final Network network = connectivityManager.getActiveNetwork();
                final NetworkCapabilities capabilities = connectivityManager
                        .getNetworkCapabilities(network);

                return capabilities != null
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {

                final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                return networkInfo != null
                        && networkInfo.isConnected();

                }
        } catch (Exception e) {
            return false;
        }
    }
}
