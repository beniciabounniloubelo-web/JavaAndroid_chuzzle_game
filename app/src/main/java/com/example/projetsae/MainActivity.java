package com.example.projetsae;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.CheckBox;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private GameModel model;
    private TableLayout table;

    private boolean enDrag = false;
    private int CELL_SIZE;
    private float debutX, debutY;
    private boolean modeHorizontal;

    private boolean modelDestine = false;
    private final int SEUIL = 20;
    private int ligneSelectionnee;
    private int colonneSelectionnee;

    private int le_score = 0;

    private long la_graine;
    private TextView score;
    private boolean directionFixee = false;
    private int nbCoups = 0;
    private TextView coups;

    private int[] images = {
            R.drawable.c1,
            R.drawable.c2,
            R.drawable.c3,
            R.drawable.c4,
            R.drawable.c5,
            R.drawable.c6,
            R.drawable.c7,
            R.drawable.cpersistante,
            R.drawable.cmystere
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        table = findViewById(R.id.table);
        score = findViewById(R.id.score);
        coups = findViewById(R.id.coups);

        if (savedInstanceState != null) {
            // --- Restaurer la partie ---
            le_score = savedInstanceState.getInt("score");
            nbCoups = savedInstanceState.getInt("nbCoups");
            la_graine = savedInstanceState.getLong("graine");
            boolean modeHard = savedInstanceState.getBoolean("modeHard");

            // Créer le modèle avec la graine existante
            model = new GameModel(la_graine, modeHard);

            // Restaurer la grille
            int[][] grille = new int[6][6];
            int[][] couleurCachee = new int[6][6];
            int[][] resistance = new int[6][6];

            for (int i = 0; i < 6; i++) {
                grille[i] = savedInstanceState.getIntArray("grille_" + i);
                couleurCachee[i] = savedInstanceState.getIntArray("couleurCachee_" + i);
                resistance[i] = savedInstanceState.getIntArray("resistance_" + i);
            }

            model.restaurerGrille(grille);
            model.restaurerCouleurCachee(couleurCachee);
            model.restaurerResistance(resistance);

            score.setText("Score : " + le_score);
            coups.setText("nombre de coups : " + nbCoups);
            afficherGrille();

        } else {
            // --- Nouvelle partie normale ---
            String seedExtra = getIntent().getStringExtra("seed");
            if (seedExtra != null) {
                modelDestine = true;
                la_graine = Long.parseLong(seedExtra);
            } else {
                modelDestine = false;
            }

            afficherDialogLancement();
        }
    }

    private void afficherDialogLancement() {
        CheckBox checkHard = new CheckBox(this);
        checkHard.setText("Mode Hard");
        checkHard.setPadding(40, 20, 40, 20);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Nouvelle partie")
                .setView(checkHard)
                .setCancelable(false)
                .setPositiveButton("Jouer", (dialog, which) -> {
                    boolean modeHard = checkHard.isChecked();
                    if (modelDestine) {
                        model = new GameModel(la_graine, modeHard);
                    } else {
                        model = new GameModel(modeHard);
                        la_graine = model.getGraine();
                    }
                    afficherGrille();
                    table.post(() -> {
                        TableRow row = (TableRow) table.getChildAt(0);
                        View v = row.getChildAt(0);
                        CELL_SIZE = v.getWidth();
                    });
                })
                .show();
    }

    private void afficherGrille() {
        table.removeAllViews();

        for (int i = 0; i < 6; i++) {

            TableRow row = new TableRow(this);

            for (int j = 0; j < 6; j++) {

                ImageView img = new ImageView(this);
                int val = model.getCase(i, j);
                if (val == -1) img.setForeground(ContextCompat.getDrawable(this, R.drawable.cmystere)); //pour les case mysteres
                else if (val == -2) {
                    img.setImageResource(images[model.getCouleurCachee(i, j)]); // couleur en fond
                    img.setForeground(ContextCompat.getDrawable(this, R.drawable.cpersistante)); //pour es cases persistantes
                }
                else if (val >= 0 && val <= 6) {
                    img.setImageResource(images[val]);
                    img.setForeground(null); // important : effacer le foreground des cases normales
                }
                else {
                    // valeur inattendue, on met une case vide pour éviter le crash
                    img.setImageResource(0);
                    img.setForeground(null);
                }
                //img.setForeground(ContextCompat.getDrawable(this, R.drawable.cverrou)); //pour mettre les verrous
                img.setLayoutParams(new TableRow.LayoutParams(100, 100));
                final int ligne = i;
                final int colonne = j;

                img.setOnTouchListener((v, event) -> {


                        switch (event.getAction()) {

                            case MotionEvent.ACTION_DOWN:

                                debutX = event.getRawX();
                                debutY = event.getRawY();

                                ligneSelectionnee = ligne;
                                colonneSelectionnee = colonne;

                                enDrag = true;
                                return true;

                            case MotionEvent.ACTION_MOVE:

                                if (!enDrag) return false;

                                float diffX = event.getRawX() - debutX;
                                float diffY = event.getRawY() - debutY;

                                if (!directionFixee) {
                                    if (Math.abs(diffX) > SEUIL || Math.abs(diffY) > SEUIL) {

                                        if (Math.abs(diffX) > Math.abs(diffY)) {
                                            modeHorizontal = true;
                                        } else {
                                            modeHorizontal = false;
                                        }

                                        directionFixee = true;
                                    }
                                }
                                

                                return true;

                            case MotionEvent.ACTION_UP:

                                if (!enDrag) return false;

                                float finalX = event.getRawX() - debutX;
                                float finalY = event.getRawY() - debutY;

                                boolean mouvementFait = false;

                                //histoire de se rappeler de la grille avant un mouv
                                int[][] sauvegarde = model.sauvegarderGrille();
                                int[][] sauvegardeCouleur = new int[6][6];
                                int[][] sauvegardeResistance = new int[6][6];
                                for (int o = 0; o < 6; o++) {
                                    sauvegardeCouleur[o] = model.getCouleurCacheeLigne(o);
                                    sauvegardeResistance[o] = model.getResistanceLigne(o);
                                }

                                if (modeHorizontal) {
                                    int deplacement = Math.round(finalX / CELL_SIZE);
                                    while (deplacement > 0) {
                                        model.decalerLigneDroite(ligneSelectionnee);
                                        deplacement--;
                                        mouvementFait = true;
                                    }
                                    while (deplacement < 0) {
                                        model.decalerLigneGauche(ligneSelectionnee);
                                        deplacement++;
                                        mouvementFait = true;
                                    }
                                } else {
                                    int deplacement = Math.round(finalY / CELL_SIZE);
                                    while (deplacement > 0) {
                                        model.decalerColonneBas(colonneSelectionnee);
                                        deplacement--;
                                        mouvementFait = true;
                                    }
                                    while (deplacement < 0) {
                                        model.decalerColonneHaut(colonneSelectionnee);
                                        deplacement++;
                                        mouvementFait = true;
                                    }
                                }

                                for (int ii = 0; ii < 6; ii++)
                                    for (int jj = 0; jj < 6; jj++)
                                        if (model.getCase(ii,jj) == -2)
                                            Log.d("PERSIST", "persistante en ["+ii+"]["+jj+"] resist="+model.getResistance(ii,jj));

                                // vérifier si le déplacement crée un alignement
                                if (model.aUnAlignement(model.getGrille(), model.getCouleurCachee())) {
                                    mouvementFait = true;
                                } else {
                                    // restaurer tout
                                    model.restaurerGrille(sauvegarde);
                                    model.restaurerCouleurCachee(sauvegardeCouleur);
                                    model.restaurerResistance(sauvegardeResistance);
                                }

                                directionFixee = false;
                                modeHorizontal = false;
                                enDrag = false;
                                afficherGrille();

                                
                                directionFixee = false;
                                modeHorizontal = false;

                                enDrag = false;
                                afficherGrille();

                                if(mouvementFait) {
                                    nbCoups ++;
                                    coups.setText("nombre de coups : " + nbCoups);
                                }

                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    boolean continuer = true;
                                    while (continuer) {
                                        int avant = le_score;
                                        verifierAlignements();
                                        continuer = (le_score > avant); // encore des combos ?
                                    }
                                    afficherGrille();

                                    // Fin de partie
                                    if (model.aucunCoupPossible()) { //verification si partie finie
                                        SharedPreferences prefs = getSharedPreferences("saves", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        int nb = prefs.getInt("nbParties", 0);
                                        editor.putLong("seed_" + nb, la_graine);
                                        editor.putInt("nbParties", nb + 1);
                                        editor.apply();
                                        new androidx.appcompat.app.AlertDialog.Builder(this)
                                                .setTitle("Partie terminée !")
                                                .setMessage("Aucun coup possible.\nScore final : " + le_score+"\nGraine de la partie : "+ la_graine)
                                                .setPositiveButton("Rejouer", (dialog, which) -> { //soit on rejoue et tt est remis a 0
                                                    if (modelDestine) {
                                                        boolean modeHard = model.isModeHard();
                                                        model = new GameModel(la_graine, modeHard);
                                                        // même graine
                                                    } else {
                                                        model = new GameModel();          // nouvelle graine aléatoire
                                                        la_graine = model.getGraine();
                                                    }
                                                    le_score = 0;
                                                    nbCoups = 0;
                                                    score.setText("Score : 0");
                                                    coups.setText("nombre de coups : 0");
                                                    afficherGrille();
                                                })
                                                .setNegativeButton("Menu", (dialog, which) -> finish()) //soit direction menu
                                                .setCancelable(false) //pas possible de fermer le dialog donc grille visible mais inutilisable
                                                .show(); //dialog rendu visible
                                    }
                                }, 2000);



                                return true;
                        }


                        return false;

                });
                row.addView(img);
            }

            table.addView(row);
        }
    }

    private int couleurEffective(int i, int j) { //histoire de renvoyer la vraie couleur de la case
        int val = model.getCase(i, j);
        if (val == -1 || val == -2) return model.getCouleurCachee(i, j);
        return val;
    }

    private int verifierAlignements() {
        int scoreGagne = 0;
        boolean aEuSuppresion; //pour aider a la reaction en cascade
        int combo = 0;

        do {
            boolean[][] aSupprimer = new boolean[6][6];
            boolean[][] aResister = new boolean[6][6];
            aEuSuppresion = false;

            // 1) détecter alignements horizontaux
            for (int i = 0; i < 6; i++) {
                int count = 1;
                for (int j = 1; j < 6; j++) {
                    if (couleurEffective(i, j) == couleurEffective(i, j - 1)) {
                        count++;
                    } else {
                        if (count >= 3) {
                            for (int k = 0; k < count; k++) {
                                int li = i, lj = j - 1 - k;
                            if (model.getCase(li,lj) == -2 && model.getResistance(li,lj) > 0) { //si resistance, la diminue de 1
                                aResister[li][lj] = true;
                            } else
                                if (model.getCase(li, lj) == -1) {
                                    model.setCase(li, lj, model.getCouleurCachee(li, lj)); // si mystere, revele la couleur
                                } else {
                                    aSupprimer[li][lj] = true; // sinon suppression normale
                                }
                            }
                            int gain = ajouterScore(count);

                            double bonus = 1 + 0.5 * combo; // combo 0 = x1, combo 1 = x1.5, etc.
                            gain = (int) (gain * bonus);

                            le_score += gain;
                            score.setText("Score : " + le_score);
                            aEuSuppresion = true;
                        }
                        count = 1;
                    }
                }
                if (count >= 3) {
                    for (int k = 0; k < count; k++) {
                        int li = i, lj = 6 - 1 - k;
                    if (model.getCase(li,lj) == -2 && model.getResistance(li,lj) > 0) {
                        aResister[li][lj] = true;
                    } else
                        if (model.getCase(li, lj) == -1) {
                            model.setCase(li, lj, model.getCouleurCachee(li, lj)); // révèle la couleur cachée
                        } else {
                            aSupprimer[li][lj] = true; // suppression normale
                        }
                    }
                    int gain = ajouterScore(count);

                    double bonus = 1 + 0.5 * combo; // combo 0 = x1, combo 1 = x1.5, etc.
                    gain = (int) (gain * bonus);

                    le_score += gain;
                    score.setText("Score : " + le_score);
                    aEuSuppresion = true;
                }
            }

            // 2) détecter alignements verticaux
            for (int j = 0; j < 6; j++) {
                int count = 1;
                for (int i = 1; i < 6; i++) {
                    if (couleurEffective(i, j) == couleurEffective(i - 1, j)) {
                        count++;
                    } else {
                        if (count >= 3) {
                            for (int k = 0; k < count; k++) {
                                int li = i - 1 - k, lj = j;
                            if (model.getCase(li,lj) == -2 && model.getResistance(li,lj) > 0) {
                                aResister[li][lj] = true;
                            } else
                                if (model.getCase(li, lj) == -1) {
                                    model.setCase(li, lj, model.getCouleurCachee(li, lj)); // révèle la couleur cachée
                                } else {
                                    aSupprimer[li][lj] = true; // suppression normale
                                }
                            }

                            int gain = ajouterScore(count);

                            double bonus = 1 + 0.5 * combo; // combo 0 = x1, combo 1 = x1.5, etc.
                            gain = (int) (gain * bonus);

                            le_score += gain;
                            score.setText("Score : " + le_score);
                            aEuSuppresion = true;
                        }
                        count = 1;
                    }
                }
                if (count >= 3) {
                    for (int k = 0; k < count; k++) {
                        int li = 6 - 1 - k, lj = j;
                    if (model.getCase(li,lj) == -2 && model.getResistance(li,lj) > 0) {
                        aResister[li][lj] = true;
                    } else
                        if (model.getCase(li, lj) == -1) {
                            model.setCase(li, lj, model.getCouleurCachee(li, lj)); // révèle la couleur cachée
                        } else {
                            aSupprimer[li][lj] = true; // suppression normale
                        }
                    }
                    int gain = ajouterScore(count);

                    double bonus = 1 + 0.5 * combo; // combo 0 = x1, combo 1 = x1.5, etc.
                    gain = (int) (gain * bonus);

                    le_score += gain;
                    score.setText("Score : " + le_score);
                    aEuSuppresion = true;
                }
            }

            // 3a) appliquer le s resistances une seule fois
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    if (aResister[i][j]) {
                        Log.d("RESIST", "case ["+i+"]["+j+"] résistance AVANT="+model.getResistance(i,j));
                        model.setResistance(i, j, model.getResistance(i, j) - 1);
                        Log.d("RESIST", "case ["+i+"]["+j+"] résistance APRES="+model.getResistance(i,j));
                        if (model.getResistance(i, j) == 0) {
                            Log.d("RESIST0", "case ["+i+"]["+j+"] résistance=0, sera supprimée="+aResister[i][j]);
                            aSupprimer[i][j] = true;
                        }
                        aEuSuppresion = true;
                    }
                }
            }
/*
            // 3b) supprimer les persistantes dont la résistance est déjà 0
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    if (model.getCase(i, j) == -2 && model.getResistance(i, j) == 0) {
                        aSupprimer[i][j] = true;
                        aEuSuppresion = true;
                    }
                }
            }
*/

            // 4) faire tomber les cases et remplir les vides
            if (aEuSuppresion) {
                for (int j = 0; j < 6; j++) {
                    int vide = 0;
                    for (int i = 5; i >= 0; i--) {
                        if (aSupprimer[i][j]) {
                            vide++;
                        } else if (vide > 0) {
                            int valCase = model.getCase(i, j);
                            int resist = model.getResistance(i, j);

                            // si c'est une persistante épuisée, on la supprime plutôt que de la déplacer
                            if (valCase == -2 && resist == 0) {
                                vide++; // on l'ajoute aux vides
                            } else {
                                model.setCase(i + vide, j, valCase);
                                model.setCouleurCachee(i + vide, j, model.getCouleurCachee(i, j));
                                model.setResistance(i + vide, j, resist);
                                model.setCase(i, j, 0);
                                model.setCouleurCachee(i, j, 0);
                                model.setResistance(i, j, 0);
                            }
                        }
                    }
                    // nouvelles cases en haut
                    for (int i = 0; i < vide; i++) {
                        int nouvelleCouleur = model.prochaineCouleur(i, j);
                        model.setCase(i, j, nouvelleCouleur);
                        if (nouvelleCouleur >= 0) {
                            // case normale : on remet à zéro
                            model.setCouleurCachee(i, j, 0);
                            model.setResistance(i, j, 0);
                        }
                        // si -1 ou -2 : prochaineCouleur a déjà rempli couleurCachee et resistance, on ne touche pas
                    }
                }
                combo++;
            }
        } while (aEuSuppresion);
        // ajouterScore(scoreGagne);
        return scoreGagne;
    }





    private int ajouterScore(int nb) {
        if (nb == 3) return 8;
        if (nb == 4) return 16;
        if (nb == 5) return 32;
        return 64;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Grille principale
        int[][] grille = model.getGrille();
        for (int i = 0; i < 6; i++) outState.putIntArray("grille_" + i, grille[i]);

        // Couleur cachée et résistances
        for (int i = 0; i < 6; i++) {
            outState.putIntArray("couleurCachee_" + i, model.getCouleurCacheeLigne(i));
            outState.putIntArray("resistance_" + i, model.getResistanceLigne(i));
        }

        // Score et coups
        outState.putInt("score", le_score);
        outState.putInt("nbCoups", nbCoups);

        // Graine et mode hard
        outState.putLong("graine", la_graine);
        outState.putBoolean("modeHard", model.isModeHard());
    }
}