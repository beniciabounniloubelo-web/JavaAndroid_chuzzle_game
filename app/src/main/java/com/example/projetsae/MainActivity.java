package com.example.projetsae;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;


import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private GameModel model;
    private TableLayout table;

    private float debutX, debutY;
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
                final int ligne = i;
                final int colonne = j;

                img.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        switch (event.getAction()) {

                            case MotionEvent.ACTION_DOWN:
                                debutX = event.getX();
                                debutY = event.getY();
                                return true;

                            case MotionEvent.ACTION_UP:
                                float dx = event.getX() - debutX;
                                float dy = event.getY() - debutY;

                                if (Math.abs(dx) > Math.abs(dy)) {
                                    model.decalerLigneDroite(ligne);
                                }
                                else if (Math.abs(dy) > Math.abs(dx)){
                                    model.decalerColonneBas(colonne);
                                }

                                v.performClick();
                                afficherGrille();
                                return true;
                        }
                        return false;
                    }
                });
                row.addView(img);
            }

            table.addView(row);
        }
    }

}