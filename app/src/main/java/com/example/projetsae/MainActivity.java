package com.example.projetsae;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;


import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private GameModel model;
    private TableLayout table;

    private int[] images = {
            R.drawable.c1,
            R.drawable.c2,
            R.drawable.c3,
            R.drawable.c4,
            R.drawable.c5,
            R.drawable.c6,
            R.drawable.c7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = new GameModel();
        table = findViewById(R.id.table);
        Log.d("DEBUG", "table = " + table);

        afficherGrille();
        table.requestLayout();
        table.invalidate();
    }

    private void afficherGrille() {
        table.removeAllViews();

        for (int i = 0; i < 6; i++) {

            TableRow row = new TableRow(this);

            for (int j = 0; j < 6; j++) {

                ImageView img = new ImageView(this);
                img.setImageResource(images[model.getCase(i, j)]);
                img.setLayoutParams(new TableRow.LayoutParams(150, 150));

                row.addView(img);
            }

            table.addView(row);
        }
    }

}