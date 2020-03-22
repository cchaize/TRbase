package com.chaize.tr.vue;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaize.tr.R;
import com.chaize.tr.controleur.Controle;
import com.chaize.tr.modele.Produit;
import com.chaize.tr.outils.DbContract;
import com.chaize.tr.outils.DbHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PanierActivity extends AppCompatActivity {

    float prix;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    PanierRecyclerAdapter adapter;
    ArrayList<ItemPanier> arrayList = new ArrayList<>();

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
            String code = arrayList.get(viewHolder.getAdapterPosition()).getProduit().getCode();
            DbHelper dbHelper = new DbHelper(PanierActivity.this);
            dbHelper.removeFromPanier(code, dbHelper.getWritableDatabase());
            dbHelper.close();
            arrayList.remove(viewHolder.getAdapterPosition());
            // this line animates what happens after delete
            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            Snackbar.make(recyclerView, "delete successful", Snackbar.LENGTH_SHORT).show();
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panier);
        recyclerView = (RecyclerView) findViewById(R.id.panierRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new PanierRecyclerAdapter(arrayList);
        recyclerView.setAdapter(adapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        readPanier();
    }

    public void addNoScan(View view) {
        demanderPrix();
    }

    public void scanProduit(View view) {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanningResult != null) {
            String codeEAN = scanningResult.getContents();
            EditText txtEan = findViewById(R.id.txtEAN);
            txtEan.setText(codeEAN);
            demanderPrix();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void demanderPrix() {
        final boolean[] result = new boolean[1];
        AlertDialog.Builder builder = new AlertDialog.Builder(PanierActivity.this);
        // Get the layout inflater
        LayoutInflater inflater = PanierActivity.this.getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dlg_prix_layout, null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogLayout);
        EditText txtEan = findViewById(R.id.txtEAN);
        EditText et = dialogLayout.findViewById(R.id.txtDemandePrix);
        // Add action buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                try {
                    prix = Float.parseFloat(et.getText().toString());
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("code", txtEan.getText());
                    jsonParam.put("quantite", "1");
                    jsonParam.put("prix", prix);

                    saveToPanier(jsonParam);
                } catch (JSONException e) {
                    Controle.addLog(Controle.typeLog.ERROR, "PanierActivity.OnActivityResult " + e.getLocalizedMessage());
                }
            }
        })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                    }
                });

        builder.create().show();
    }


    public void resetPanier(View view) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(PanierActivity.this);
        builder.setMessage("Supprimer tous les éléments du panier ?")
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        DbHelper dbHelper = new DbHelper(PanierActivity.this);
                        dbHelper.resetPanier(dbHelper.getWritableDatabase());
                        readPanier();
                        dbHelper.close();
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // ne rien faire
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    public void saveToPanier(JSONObject jsonParam) {
        DbHelper dbHelper = new DbHelper(PanierActivity.this);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        dbHelper.saveProduitToPanier(jsonParam, database);
        readPanier();
        dbHelper.close();
    }

    private void readPanier() {
        arrayList.clear();

        DbHelper dbHelper = new DbHelper(PanierActivity.this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = dbHelper.readPanierFromLocalDatabase(database);
        float totalTR = 0;

        try {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String code = cursor.getString(cursor.getColumnIndex(DbContract.PAN_CODE));
                    float quantite = cursor.getFloat(cursor.getColumnIndex(DbContract.PAN_QUANTITE));
                    float prix = cursor.getFloat(cursor.getColumnIndex(DbContract.PAN_PRIX));
                    Produit produit = dbHelper.readProduitFromLocalDatabase(code, Controle.getInstance(PanierActivity.this).getMagasin().getSequence(), database);
                    produit.setPrix(prix);
                    arrayList.add(new ItemPanier(produit, quantite, prix));
                    if (produit.getFlgTR()==1)
                        totalTR += prix * quantite;
                }
                cursor.close();
            }
        } catch (Exception e) {
            Controle.addLog(Controle.typeLog.ERROR, "PanierActivity.readPanier " + e.getLocalizedMessage());
        }

        TextView tv = findViewById(R.id.txtTotalTR);
        tv.setText(Float.toString(totalTR));
        adapter.notifyDataSetChanged();

        dbHelper.close();

    }

}
