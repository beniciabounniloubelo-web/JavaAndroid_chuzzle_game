package com.example.projetsae;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        CheckBox checkboxHard = findViewById(R.id.checkbox_hard);
        Button btnSauvegarder = findViewById(R.id.btn_sauvegarder);

        // Charger la valeur actuelle du mode hard
        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);
        checkboxHard.setChecked(prefs.getBoolean("modeHard", false));

        // Sauvegarder et retourner au menu
        btnSauvegarder.setOnClickListener(v -> {
            prefs.edit()
                    .putBoolean("modeHard", checkboxHard.isChecked())
                    .apply();
            finish();
        });
    }

    // retour arrière → menu
    @Override
    public void onBackPressed() {
        finish();
    }
}