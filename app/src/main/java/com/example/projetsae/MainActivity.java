package com.example.projetsae;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.RelativeLayout;



import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private int rows = 6;
    private int cols = 6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RelativeLayout jeu = findViewById(R.id.jeu);


        jeu.post(() -> {
            int layoutWidth = jeu.getWidth();
            int layoutHeight = jeu.getHeight();

            int cellSize = Math.min(layoutWidth / cols, layoutHeight / rows);

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    View cell = new View(this);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                            cellSize,
                            cellSize
                    );

                    params.leftMargin = j * cellSize;
                    params.topMargin = i * cellSize;

                    cell.setLayoutParams(params);
                    cell.setBackgroundColor(Color.BLUE);


                    jeu.addView(cell);
                }
            }
        });
    }

}