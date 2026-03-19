package com.example.projetsae;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class ListeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choix_destine);
        ListView liste = findViewById(R.id.liste);

        SharedPreferences prefs = getSharedPreferences("saves", MODE_PRIVATE);

        int nb = prefs.getInt("nbParties", 0);

        String[] data = new String[nb];

        for (int i = 0; i < nb; i++) {
            data[i] = "Partie " + (i + 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                data
        );

        liste.setAdapter(adapter);

        liste.setOnItemClickListener((parent, view, position, id) -> {

            long seed = prefs.getLong("seed_" + position, 0);

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("seed", String.valueOf(seed));

            startActivity(intent);
        });
    }
}
