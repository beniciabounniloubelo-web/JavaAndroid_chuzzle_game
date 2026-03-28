package com.example.projetsae;

import android.util.Log;

import java.util.Random;

public class GameModel {

    private int[][] grille = new int[6][6];
    private long seed; //la graine est creee
    private Random rand; //creation du random avec cette graine
    //pour les cases speciales !!
    private int[][] couleurCachee = new int[6][6]; // couleur sous Mystere/Persistante
    private int[][] resistance = new int[6][6];     // 3 pour Persistante, 0 sinon
    private boolean modeHard;
    private boolean[][] verrou = new boolean[6][6];

    public GameModel() {
        this.modeHard = false;
        seed = Math.abs(new Random().nextLong()); // on génère la graine de la partie + peut que etre positif
        rand = new Random(seed);        // on crée le Random avec cette graine
        genererGrilleInitiale();
    }

    public int[] getCouleurCacheeLigne(int i) { return couleurCachee[i].clone(); }
    public int[] getResistanceLigne(int i) { return resistance[i].clone(); }

    public int[][] getCouleurCachee() { return couleurCachee; }
    public boolean isModeHard() { return modeHard; }
    public void restaurerCouleurCachee(int[][] sauvegarde) {
        for (int i = 0; i < 6; i++) couleurCachee[i] = sauvegarde[i].clone();
    }
    public void restaurerResistance(int[][] sauvegarde) {
        for (int i = 0; i < 6; i++) resistance[i] = sauvegarde[i].clone();
    }
    public boolean getVerrou(int i, int j) { return verrou[i][j]; }
    public void setVerrou(int i, int j, boolean val) { verrou[i][j] = val; }
    public boolean[] getVerrouLigne(int i) { return verrou[i].clone(); }
    public void restaurerVerrous(boolean[][] sauvegarde) {
        for (int i = 0; i < 6; i++) verrou[i] = sauvegarde[i].clone();
    }

    // constructeurs avec modeHard
    public GameModel(boolean modeHard) {
        this.modeHard = modeHard;
        seed = Math.abs(new Random().nextLong());
        rand = new Random(seed);
        genererGrilleInitiale();
    }

    public GameModel(long graineFournie, boolean modeHard) {
        this.modeHard = modeHard;
        seed = graineFournie;
        rand = new Random(seed);
        genererGrilleInitiale();
    }

    public int[][] sauvegarderGrille() {
        int[][] copie = new int[6][6];
        for (int i = 0; i < 6; i++)
            copie[i] = grille[i].clone();
        return copie;
    }

    public void restaurerGrille(int[][] sauvegarde) {
        for (int i = 0; i < 6; i++)
            grille[i] = sauvegarde[i].clone();
    }

    public long getGraine(){return seed;}

    //genere aussi cases speciales
    public int prochaineCouleur(int i, int j, int nbCoups) { //c'est la mm graine qui sera reutilisee
            double r = rand.nextDouble();
            // verrou : rare au début, augmente très lentement
            double probVerrou = 0.001 * nbCoups;
            if (r < probVerrou) {
                verrou[i][j] = true;
                return rand.nextInt(7); // case normale mais verrouillée
            }
        if (modeHard) {
            r = rand.nextDouble();
            if (r < 0.05) { //5% de chances d'apparaitre sinon trop frequent
                couleurCachee[i][j] = rand.nextInt(7);
                return -1; // case mystère
            }
            if (r < 0.1) { //5% de chances d'apparaitre sinon trop frequent
                couleurCachee[i][j] = rand.nextInt(7);
                resistance[i][j] = 3;
                return -2; // case persistante
            }
        }
        return rand.nextInt(7); //90% de chances pour le reste
    }

    public int getCase(int i, int j) {
        return grille[i][j];
    }


    public void setCouleurCachee(int i, int j, int couleur) {
        couleurCachee[i][j] = couleur;
        // NOTE : on ne remet plus resistance à 3 ici pour ne pas écraser la résistance actuelle
    }

    public int getCouleurCachee(int i, int j) { return couleurCachee[i][j]; }
    public int getResistance(int i, int j) { return resistance[i][j]; }
    public void setResistance(int i, int j, int val) {
        resistance[i][j] = val;
    }

    // Génération de la grille
    public void genererGrilleInitiale() {

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {

                boolean recommence = true;

                while (recommence) {

                    int couleur = rand.nextInt(7);
                        /*
                        int countLigne = 0;
                        int countColonne = 0;

                        // Ligne
                        for (int k = 0; k < j; k++) {
                            if (grille[i][k] == couleur) {
                                countLigne++;
                            }
                        }

                        // Colonne
                        for (int k = 0; k < i; k++) {
                            if (grille[k][j] == couleur) {
                                countColonne++;
                            }
                        }

                        if (countLigne < 2 && countColonne < 2) {
                            grille[i][j] = couleur;
                            recommence = false;
                        }*/
                    boolean trop = false;
                    if (j >= 2 && grille[i][j-1] == couleur && grille[i][j-2] == couleur) trop = true;
                    if (i >= 2 && grille[i-1][j] == couleur && grille[i-2][j] == couleur) trop = true;
                    if (!trop) {
                        grille[i][j] = couleur;
                        recommence = false;
                    }
                }
            }
        }
    }

    // Décalage circulaire ligne droite
    public void decalerLigneDroite(int ligne) {
        int temp = grille[ligne][5];
        int tempCouleur = couleurCachee[ligne][5];
        int tempResistance = resistance[ligne][5];
        boolean tempVerrou = verrou[ligne][5];

        for (int j = 5; j > 0; j--) {
            grille[ligne][j] = grille[ligne][j - 1];
            couleurCachee[ligne][j] = couleurCachee[ligne][j - 1];
            resistance[ligne][j] = resistance[ligne][j - 1];
            verrou[ligne][j] = verrou[ligne][j - 1];
        }

        grille[ligne][0] = temp;
        couleurCachee[ligne][0] = tempCouleur;
        resistance[ligne][0] = tempResistance;
        verrou[ligne][0] = tempVerrou;
    }

    public void decalerLigneGauche(int ligne) {
        int temp = grille[ligne][0];
        int tempCouleur = couleurCachee[ligne][0];
        int tempResistance = resistance[ligne][0];
        boolean tempVerrou = verrou[ligne][0];

        for (int j = 0; j < 5; j++){
            grille[ligne][j] = grille[ligne][j + 1];
            couleurCachee[ligne][j] = couleurCachee[ligne][j + 1];
            resistance[ligne][j] = resistance[ligne][j + 1];
            verrou[ligne][j] = verrou[ligne][j + 1];
        }
        grille[ligne][5] = temp;
        couleurCachee[ligne][5] = tempCouleur;
        resistance[ligne][5] = tempResistance;
        verrou[ligne][5] = tempVerrou;
    }

    // Décalage circulaire colonne bas
    public void decalerColonneBas(int colonne) {
        int temp = grille[5][colonne];
        int tempCouleur = couleurCachee[5][colonne];
        int tempResistance = resistance[5][colonne];
        boolean tempVerrou = verrou[5][colonne];

        for (int i = 5; i > 0; i--) {
            grille[i][colonne] = grille[i - 1][colonne];
            couleurCachee[i][colonne] = couleurCachee[i - 1][colonne];
            resistance[i][colonne] = resistance[i - 1][colonne];
            verrou[i][colonne] = verrou[i - 1][colonne];
        }

        grille[0][colonne] = temp;
        couleurCachee[0][colonne] = tempCouleur;
        resistance[0][colonne] = tempResistance;
        verrou[0][colonne] = tempVerrou;
    }

    public void decalerColonneHaut(int colonne) {
        int temp = grille[0][colonne];
        int tempCouleur = couleurCachee[0][colonne];
        int tempResistance = resistance[0][colonne];
        boolean tempVerrou = verrou[0][colonne];

        for (int i = 0; i < 5; i++) {
            grille[i][colonne] = grille[i + 1][colonne];
            couleurCachee[i][colonne] = couleurCachee[i + 1][colonne];
            resistance[i][colonne] = resistance[i + 1][colonne];
            verrou[i][colonne] = verrou[i + 1][colonne];
        }
        grille[5][colonne] = temp;
        couleurCachee[5][colonne] = tempCouleur;
        resistance[5][colonne] = tempResistance;
        verrou[5][colonne] = tempVerrou;
    }


    public void setCase(int i, int j, int val) {
        grille[i][j] = val;
    }

    /**
     * Retourne true si aucun glissement ne peut creer un alignement de 3+ elements
     */
    //copie temporaire pour manipuler la grille et ainsi verifier si il reste encore des coups possibles
    private int[][] copierGrille() {
        int[][] copie = new int[6][6];
        for (int i = 0; i < 6; i++)
            copie[i] = grille[i].clone();
        return copie;
    }


    public int[][] getGrille() { return grille; }

    //pareil que verifierAlignement mais sans modifier la grille
    /*public boolean aUnAlignement(int[][] g) {
        // Horizontal
        for (int i = 0; i < 6; i++) {
            int count = 1;
            for (int j = 1; j < 6; j++) {
                if (g[i][j] == g[i][j - 1]) {
                    count++;
                    if (count >= 3) return true;
                } else {
                    count = 1;
                }
            }
        }
        // Vertical
        for (int j = 0; j < 6; j++) {
            int count = 1;
            for (int i = 1; i < 6; i++) {
                if (g[i][j] == g[i - 1][j]) {
                    count++;
                    if (count >= 3) return true;
                } else {
                    count = 1;
                }
            }
        }
        return false;
    }*/

    public boolean aUnAlignement(int[][] g, int[][] couleurCachee) {
        // Horizontal
        for (int i = 0; i < 6; i++) {
            int count = 1;
            for (int j = 1; j < 6; j++) {
                int c1 = g[i][j] < 0 ? couleurCachee[i][j] : g[i][j];
                int c2 = g[i][j-1] < 0 ? couleurCachee[i][j-1] : g[i][j-1];
                if (c1 == c2) {
                    count++;
                    if (count >= 3) return true;
                } else {
                    count = 1;
                }
            }
        }
        // Vertical
        for (int j = 0; j < 6; j++) {
            int count = 1;
            for (int i = 1; i < 6; i++) {
                int c1 = g[i][j] < 0 ? couleurCachee[i][j] : g[i][j];
                int c2 = g[i-1][j] < 0 ? couleurCachee[i-1][j] : g[i-1][j];
                if (c1 == c2) {
                    count++;
                    if (count >= 3) return true;
                } else {
                    count = 1;
                }
            }
        }
        return false;
    }

    public boolean aucunCoupPossible() {

        // Tester tous les decalages de ligne possible
        for (int i = 0; i < 6; i++) {
            // ignorer les lignes verrouillées
            boolean ligneLocked = false;
            for (int j = 0; j < 6; j++) if (verrou[i][j]) { ligneLocked = true; break; }
            if (ligneLocked) continue;

            for (int shift = 1; shift < 6; shift++) {
                int[][] copie = copierGrille(); //on manipule une copie de la grille
                // Simuler un décalage droite de 'shift' cases
                for (int s = 0; s < shift; s++) {
                    int temp = copie[i][5];
                    for (int j = 5; j > 0; j--) copie[i][j] = copie[i][j - 1];
                    copie[i][0] = temp;
                }
                if (aUnAlignement(copie, couleurCachee)) return false;
            }
        }

        // Tester tous les décalages de colonne possible
        for (int j = 0; j < 6; j++) {
            // ignorer les colonnes verrouillées
            boolean colLocked = false;
            for (int i = 0; i < 6; i++) if (verrou[i][j]) { colLocked = true; break; }
            if (colLocked) continue;

            for (int shift = 1; shift < 6; shift++) {
                int[][] copie = copierGrille();
                // Simuler un décalage bas de 'shift' cases
                for (int s = 0; s < shift; s++) {
                    int temp = copie[5][j];
                    for (int i = 5; i > 0; i--) copie[i][j] = copie[i - 1][j];
                    copie[0][j] = temp;
                }
                if (aUnAlignement(copie, couleurCachee)) return false;
            }
        }

        return true; // aucun coup trouvé → partie terminée
    }

}