package com.chaize.tr.vue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.chaize.tr.R;
import com.chaize.tr.controleur.Controle;
import com.chaize.tr.modele.Shop;

import java.util.ArrayList;

public class ShopSelectionActivity extends AppCompatActivity {

    private ArrayList<ItemShop> arrayItems;
    private GridListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        init();
    }


    private void init(){
        updateListeMagasins();

        findViewById(R.id.btnChercheCodePostal).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                updateListeMagasins();
            }
        });

        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                onBackPressed();
            }
        });

        findViewById(R.id.btnSelect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (adapter.getSelectedItem()!=null) {
                    Controle.getInstance(ShopSelectionActivity.this).setMagasin(adapter.getSelectedItem().getShop());
                    // Renvoie un code retour au MainActivity
                    Intent intent=new Intent();
                    setResult(Controle.SEL_MAGASIN_OK,intent);

                    onBackPressed();
                }
                else {
                    Toast.makeText(ShopSelectionActivity.this,"Sélectionnez un magasin",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateListeMagasins() {
        int cp;
        ArrayList<Shop> arrayShops;
        EditText txtCodePostal = findViewById(R.id.codePostal);
        ListView listShop = findViewById(R.id.listViewShop);
        ItemShop item;

        int seqMagasinCourant = Controle.getInstance(ShopSelectionActivity.this).getMagasin().getSequence();
        int itemSelected=-1;

        if (txtCodePostal.getText().toString().equals(""))
            cp=0;
        else
            cp = Integer.parseInt(txtCodePostal.getText().toString());
        arrayShops = Controle.getInstance(ShopSelectionActivity.this).getListeMagasins(cp);
        arrayItems=new ArrayList<>();
        for (int i = 0; i < arrayShops.size(); i++) {
            Shop s = arrayShops.get(i);
            item = new ItemShop(s);
            if (s.getSequence() == seqMagasinCourant)
                itemSelected = i;
            arrayItems.add(item);
        }

// array adapter to hold the list items
        adapter = new GridListAdapter(
                ShopSelectionActivity.this, arrayItems);

        listShop.setAdapter(adapter);

        if (itemSelected>=0)
            adapter.itemCheckChanged(itemSelected);
        // Notifying adapter for data changed
        adapter.notifyDataSetChanged();

    }

}
