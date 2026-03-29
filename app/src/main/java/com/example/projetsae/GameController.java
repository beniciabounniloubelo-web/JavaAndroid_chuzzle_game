package com.example.projetsae;

import android.content.Context;
import android.content.SharedPreferences;

public class GameController {

    private GameModel model;
    private int le_score = 0;
    private int nbCoups = 0;
    private long la_graine;
    private boolean modelDestine;

    public GameController(GameModel model, long graine, boolean destine) {
        this.model = model;
        this.la_graine = graine;
        this.modelDestine = destine;
    }

    public int getScore() { return le_score; }
    public int getNbCoups() { return nbCoups; }
    public long getGraine() { return la_graine; }
    public boolean isModelDestine() { return modelDestine; }
    public GameModel getModel() { return model; }

    public void setScore(int score) { le_score = score; }
    public void setNbCoups(int nbCoups) { this.nbCoups = nbCoups; }

    public void incrementerCoups() { nbCoups++; }

    /**
     * Tente un déplacement horizontal ou vertical.
     * Vérifie le verrou, applique le déplacement, valide si un alignement est créé.
     * Restaure l'état si le coup est invalide.
     * @return true si le coup a été accepté (alignement créé), false sinon
     */
    public boolean tenterDeplacement(boolean horizontal, int ligneOuColonne, int deplacement) {
        if (deplacement == 0) return false;

        // Vérifier le verrou selon la direction
        if (horizontal) {
            for (int j = 0; j < 6; j++) {
                if (model.getVerrou(ligneOuColonne, j)) return false;
            }
        } else {
            for (int i = 0; i < 6; i++) {
                if (model.getVerrou(i, ligneOuColonne)) return false;
            }
        }

        // Sauvegarder l'état avant le mouvement
        int[][] sauvegarde            = model.sauvegarderGrille();
        int[][] sauvegardeCouleur     = new int[6][6];
        int[][] sauvegardeResistance  = new int[6][6];
        boolean[][] sauvegardeVerrous = new boolean[6][6];
        for (int o = 0; o < 6; o++) {
            sauvegardeCouleur[o]    = model.getCouleurCacheeLigne(o);
            sauvegardeResistance[o] = model.getResistanceLigne(o);
            sauvegardeVerrous[o]    = model.getVerrouLigne(o);
        }

        // Appliquer le déplacement
        if (horizontal) {
            int d = deplacement;
            while (d > 0) { model.decalerLigneDroite(ligneOuColonne);  d--; }
            while (d < 0) { model.decalerLigneGauche(ligneOuColonne);  d++; }
        } else {
            int d = deplacement;
            while (d > 0) { model.decalerColonneBas(ligneOuColonne);  d--; }
            while (d < 0) { model.decalerColonneHaut(ligneOuColonne); d++; }
        }

        // Valider : le mouvement crée-t-il un alignement ?
        if (!model.aUnAlignement(model.getGrille(), model.getCouleurCachee())) {
            // Invalide : restaurer
            model.restaurerGrille(sauvegarde);
            model.restaurerCouleurCachee(sauvegardeCouleur);
            model.restaurerResistance(sauvegardeResistance);
            model.restaurerVerrous(sauvegardeVerrous);
            return false;
        }

        // Coup valide
        nbCoups++;
        return true;
    }

    public void rejouer() {
        le_score = 0;
        nbCoups = 0;
        if (modelDestine) {
            model = new GameModel(la_graine, model.isModeHard());
        } else {
            model = new GameModel(model.isModeHard());
            la_graine = model.getGraine();
        }
    }

    public void sauvegarderPartie(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("saves", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int nb = prefs.getInt("nbParties", 0);
        editor.putLong("seed_" + nb, la_graine);
        editor.putInt("nbParties", nb + 1);
        editor.apply();
    }

    public int verifierAlignements() {
        boolean aEuSuppresion;
        int combo = 0;

        do {
            boolean[][] aSupprimer = new boolean[6][6];
            boolean[][] aResister  = new boolean[6][6];
            aEuSuppresion = false;

            // 1) Alignements horizontaux
            for (int i = 0; i < 6; i++) {
                int count = 1;
                for (int j = 1; j < 6; j++) {
                    if (couleurEffective(i, j) == couleurEffective(i, j - 1)) {
                        count++;
                    } else {
                        if (count >= 3) {
                            traiterAlignement(aSupprimer, aResister, i, j - 1, count, true);
                            le_score += (int) (ajouterScore(count) * (1 + 0.5 * combo));
                            aEuSuppresion = true;
                        }
                        count = 1;
                    }
                }
                if (count >= 3) {
                    traiterAlignement(aSupprimer, aResister, i, 5, count, true);
                    le_score += (int) (ajouterScore(count) * (1 + 0.5 * combo));
                    aEuSuppresion = true;
                }
            }

            // 2) Alignements verticaux
            for (int j = 0; j < 6; j++) {
                int count = 1;
                for (int i = 1; i < 6; i++) {
                    if (couleurEffective(i, j) == couleurEffective(i - 1, j)) {
                        count++;
                    } else {
                        if (count >= 3) {
                            traiterAlignement(aSupprimer, aResister, i - 1, j, count, false);
                            le_score += (int) (ajouterScore(count) * (1 + 0.5 * combo));
                            aEuSuppresion = true;
                        }
                        count = 1;
                    }
                }
                if (count >= 3) {
                    traiterAlignement(aSupprimer, aResister, 5, j, count, false);
                    le_score += (int) (ajouterScore(count) * (1 + 0.5 * combo));
                    aEuSuppresion = true;
                }
            }

            // 3) Appliquer les résistances
            for (int i = 0; i < 6; i++) {
                for (int j = 0; j < 6; j++) {
                    if (aResister[i][j]) {
                        model.setResistance(i, j, model.getResistance(i, j) - 1);
                        if (model.getResistance(i, j) == 0) {
                            aSupprimer[i][j] = true;
                            model.setVerrou(i, j, false);
                        }
                        aEuSuppresion = true;
                    }
                }
            }

            // 4) Faire tomber les cases et remplir les vides
            if (aEuSuppresion) {
                for (int j = 0; j < 6; j++) {
                    int vide = 0;
                    for (int i = 5; i >= 0; i--) {
                        if (aSupprimer[i][j]) {
                            vide++;
                        } else if (vide > 0) {
                            int valCase = model.getCase(i, j);
                            int resist  = model.getResistance(i, j);
                            if (valCase == -2 && resist == 0) {
                                vide++;
                            } else {
                                model.setCase(i + vide, j, valCase);
                                model.setCouleurCachee(i + vide, j, model.getCouleurCachee(i, j));
                                model.setResistance(i + vide, j, resist);
                                model.setVerrou(i + vide, j, model.getVerrou(i, j));
                                model.setCase(i, j, 0);
                                model.setCouleurCachee(i, j, 0);
                                model.setResistance(i, j, 0);
                                model.setVerrou(i, j, false);
                            }
                        }
                    }
                    for (int i = 0; i < vide; i++) {
                        int nouvelleCouleur = model.prochaineCouleur(i, j, nbCoups);
                        model.setCase(i, j, nouvelleCouleur);
                        if (nouvelleCouleur >= 0) {
                            model.setCouleurCachee(i, j, 0);
                            model.setResistance(i, j, 0);
                        }
                    }
                }
                combo++;
            }
        } while (aEuSuppresion);

        return le_score;
    }

    private void traiterAlignement(boolean[][] aSupprimer, boolean[][] aResister,
                                   int ligneOuCol, int fin, int count, boolean horizontal) {
        for (int k = 0; k < count; k++) {
            int li = horizontal ? ligneOuCol     : ligneOuCol - k;
            int lj = horizontal ? fin - k        : fin;
            if (model.getCase(li, lj) == -2 && model.getResistance(li, lj) > 0) {
                aResister[li][lj] = true;
            } else if (model.getCase(li, lj) == -1) {
                model.setCase(li, lj, model.getCouleurCachee(li, lj));
            } else {
                aSupprimer[li][lj] = true;
                model.setVerrou(li, lj, false);
            }
        }
    }

    public int couleurEffective(int i, int j) {
        int val = model.getCase(i, j);
        if (val == -1 || val == -2) return model.getCouleurCachee(i, j);
        return val;
    }

    private int ajouterScore(int nb) {
        if (nb == 3) return 8;
        if (nb == 4) return 16;
        if (nb == 5) return 32;
        return 64;
    }
}