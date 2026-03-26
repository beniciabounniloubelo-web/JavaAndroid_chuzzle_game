package com.example.projetsae;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        Button normale = findViewById(R.id.normale);

        normale.setOnClickListener(v -> {

            // Lancer le jeu
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });

        Button destine = findViewById(R.id.destine);
        destine.setOnClickListener(v -> {


            Intent intent = new Intent(this, ListeActivity.class);
            startActivity(intent);
        });

    }



}
