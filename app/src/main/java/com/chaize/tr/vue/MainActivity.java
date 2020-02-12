package com.chaize.tr.vue;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.chaize.tr.R;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private ImageButton btnShop;
    private ImageButton btnMaj;
    private ImageButton btnSetup;
    private ImageButton btnCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        btnShop = findViewById(R.id.btnMagasin);
        btnShop.setOnClickListener(this);
        btnMaj = findViewById(R.id.btnMaj);
        btnMaj.setOnClickListener(this);
        btnSetup = findViewById(R.id.btnSetup);
        btnSetup.setOnClickListener(this);
        btnCourses = findViewById(R.id.btnCourses);
        btnCourses.setOnClickListener(this);
    }

    public void onClick(View v){
        Intent intent;
        switch (v.getId()) {
            case R.id.btnMagasin:
                intent = new Intent(MainActivity.this, ShopSelectionActivity.class);
                startActivity(intent);
                break;
            case R.id.btnCourses:
                intent = new Intent(MainActivity.this, PanierActivity.class);
                startActivity(intent);
                break;
            case R.id.btnMaj:
                intent = new Intent(MainActivity.this, MajActivity.class);
                startActivity(intent);
                break;
            case R.id.btnSetup:
                intent = new Intent(MainActivity.this, SetupActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
