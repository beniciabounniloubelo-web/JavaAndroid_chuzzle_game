package com.example.projetsae;

import java.util.Random;

public class GameModel {

    private int[][] grille        = new int[6][6];
    private int[][] couleurCachee = new int[6][6];
    private int[][] resistance    = new int[6][6];
    private boolean[][] verrou    = new boolean[6][6];
    private long seed;
    private Random rand;
    private boolean modeHard;

    public GameModel() {
        this.modeHard = false;
        seed = Math.abs(new Random().nextLong());
        rand = new Random(seed);
        genererGrilleInitiale();
    }

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

    // --- Accesseurs ---
    public long getGraine()                        { return seed; }
    public boolean isModeHard()                    { return modeHard; }
    public int getCase(int i, int j)               { return grille[i][j]; }
    public void setCase(int i, int j, int val)     { grille[i][j] = val; }
    public int[][] getGrille()                     { return grille; }
    public int getCouleurCachee(int i, int j)      { return couleurCachee[i][j]; }
    public void setCouleurCachee(int i, int j, int c) { couleurCachee[i][j] = c; }
    public int[][] getCouleurCachee()              { return couleurCachee; }
    public int[] getCouleurCacheeLigne(int i)      { return couleurCachee[i].clone(); }
    public int getResistance(int i, int j)         { return resistance[i][j]; }
    public void setResistance(int i, int j, int v) { resistance[i][j] = v; }
    public int[] getResistanceLigne(int i)         { return resistance[i].clone(); }
    public boolean getVerrou(int i, int j)         { return verrou[i][j]; }
    public void setVerrou(int i, int j, boolean v) { verrou[i][j] = v; }
    public boolean[] getVerrouLigne(int i)         { return verrou[i].clone(); }

    // --- Sauvegarde / restauration ---
    public int[][] sauvegarderGrille() {
        int[][] copie = new int[6][6];
        for (int i = 0; i < 6; i++) copie[i] = grille[i].clone();
        return copie;
    }
    public void restaurerGrille(int[][] s)        { for (int i = 0; i < 6; i++) grille[i]        = s[i].clone(); }
    public void restaurerCouleurCachee(int[][] s) { for (int i = 0; i < 6; i++) couleurCachee[i]  = s[i].clone(); }
    public void restaurerResistance(int[][] s)    { for (int i = 0; i < 6; i++) resistance[i]     = s[i].clone(); }
    public void restaurerVerrous(boolean[][] s)   { for (int i = 0; i < 6; i++) verrou[i]         = s[i].clone(); }

    // --- Génération ---
    public void genererGrilleInitiale() {
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                boolean recommence = true;
                while (recommence) {
                    int couleur = rand.nextInt(7);
                    boolean trop = false;
                    if (j >= 2 && grille[i][j-1] == couleur && grille[i][j-2] == couleur) trop = true;
                    if (i >= 2 && grille[i-1][j] == couleur && grille[i-2][j] == couleur) trop = true;
                    if (!trop) { grille[i][j] = couleur; recommence = false; }
                }
            }
        }
    }

    public int prochaineCouleur(int i, int j, int nbCoups) {
        double r = rand.nextDouble();
        double probVerrou = 0.01 * nbCoups;
        if (r < probVerrou) {
            verrou[i][j] = true;
            return rand.nextInt(7);
        }
        if (modeHard) {
            r = rand.nextDouble();
            if (r < 0.05) {
                couleurCachee[i][j] = rand.nextInt(7);
                return -1;
            }
            if (r < 0.10) {
                couleurCachee[i][j] = rand.nextInt(7);
                resistance[i][j] = 3;
                return -2;
            }
        }
        return rand.nextInt(7);
    }

    // --- Déplacements ---
    public void decalerLigneDroite(int ligne) {
        int temp = grille[ligne][5]; int tC = couleurCachee[ligne][5];
        int tR = resistance[ligne][5]; boolean tV = verrou[ligne][5];
        for (int j = 5; j > 0; j--) {
            grille[ligne][j]        = grille[ligne][j-1];
            couleurCachee[ligne][j] = couleurCachee[ligne][j-1];
            resistance[ligne][j]    = resistance[ligne][j-1];
            verrou[ligne][j]        = verrou[ligne][j-1];
        }
        grille[ligne][0] = temp; couleurCachee[ligne][0] = tC;
        resistance[ligne][0] = tR; verrou[ligne][0] = tV;
    }

    public void decalerLigneGauche(int ligne) {
        int temp = grille[ligne][0]; int tC = couleurCachee[ligne][0];
        int tR = resistance[ligne][0]; boolean tV = verrou[ligne][0];
        for (int j = 0; j < 5; j++) {
            grille[ligne][j]        = grille[ligne][j+1];
            couleurCachee[ligne][j] = couleurCachee[ligne][j+1];
            resistance[ligne][j]    = resistance[ligne][j+1];
            verrou[ligne][j]        = verrou[ligne][j+1];
        }
        grille[ligne][5] = temp; couleurCachee[ligne][5] = tC;
        resistance[ligne][5] = tR; verrou[ligne][5] = tV;
    }

    public void decalerColonneBas(int colonne) {
        int temp = grille[5][colonne]; int tC = couleurCachee[5][colonne];
        int tR = resistance[5][colonne]; boolean tV = verrou[5][colonne];
        for (int i = 5; i > 0; i--) {
            grille[i][colonne]        = grille[i-1][colonne];
            couleurCachee[i][colonne] = couleurCachee[i-1][colonne];
            resistance[i][colonne]    = resistance[i-1][colonne];
            verrou[i][colonne]        = verrou[i-1][colonne];
        }
        grille[0][colonne] = temp; couleurCachee[0][colonne] = tC;
        resistance[0][colonne] = tR; verrou[0][colonne] = tV;
    }

    public void decalerColonneHaut(int colonne) {
        int temp = grille[0][colonne]; int tC = couleurCachee[0][colonne];
        int tR = resistance[0][colonne]; boolean tV = verrou[0][colonne];
        for (int i = 0; i < 5; i++) {
            grille[i][colonne]        = grille[i+1][colonne];
            couleurCachee[i][colonne] = couleurCachee[i+1][colonne];
            resistance[i][colonne]    = resistance[i+1][colonne];
            verrou[i][colonne]        = verrou[i+1][colonne];
        }
        grille[5][colonne] = temp; couleurCachee[5][colonne] = tC;
        resistance[5][colonne] = tR; verrou[5][colonne] = tV;
    }

    // --- Détection ---
    public boolean aUnAlignement(int[][] g, int[][] cc) {
        for (int i = 0; i < 6; i++) {
            int count = 1;
            for (int j = 1; j < 6; j++) {
                int c1 = g[i][j]   < 0 ? cc[i][j]   : g[i][j];
                int c2 = g[i][j-1] < 0 ? cc[i][j-1] : g[i][j-1];
                if (c1 == c2) { if (++count >= 3) return true; } else count = 1;
            }
        }
        for (int j = 0; j < 6; j++) {
            int count = 1;
            for (int i = 1; i < 6; i++) {
                int c1 = g[i][j]   < 0 ? cc[i][j]   : g[i][j];
                int c2 = g[i-1][j] < 0 ? cc[i-1][j] : g[i-1][j];
                if (c1 == c2) { if (++count >= 3) return true; } else count = 1;
            }
        }
        return false;
    }

    private boolean aUnAlignementSimple(int[][] g) {
        for (int i = 0; i < 6; i++) {
            int count = 1;
            for (int j = 1; j < 6; j++) {
                if (g[i][j] == g[i][j-1]) { if (++count >= 3) return true; } else count = 1;
            }
        }
        for (int j = 0; j < 6; j++) {
            int count = 1;
            for (int i = 1; i < 6; i++) {
                if (g[i][j] == g[i-1][j]) { if (++count >= 3) return true; } else count = 1;
            }
        }
        return false;
    }

    private int[][] copierGrilleEffective() {
        int[][] copie = new int[6][6];
        for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) {
                int val = grille[i][j];
                copie[i][j] = (val < 0) ? couleurCachee[i][j] : val;
            }
        return copie;
    }

    public boolean aucunCoupPossible() {
        int[][] grilleResolue = copierGrilleEffective();

        // Tester les lignes
        for (int i = 0; i < 6; i++) {
            boolean ligneLocked = false;
            for (int j = 0; j < 6; j++) { if (verrou[i][j]) { ligneLocked = true; break; } }

            if (ligneLocked) {
                // La ligne est verrouillée : on ne peut pas la glisser,
                // mais une autre ligne/colonne pourrait créer un alignement
                // qui inclut une case verrouillée de cette ligne → géré par les autres tests
                continue;
            }

            for (int shift = 1; shift < 6; shift++) {
                int[][] copie = new int[6][6];
                for (int r = 0; r < 6; r++) copie[r] = grilleResolue[r].clone();
                for (int s = 0; s < shift; s++) {
                    int temp = copie[i][5];
                    for (int j = 5; j > 0; j--) copie[i][j] = copie[i][j-1];
                    copie[i][0] = temp;
                }
                if (aUnAlignementSimple(copie)) return false;
            }
        }

        // Tester les colonnes
        for (int j = 0; j < 6; j++) {
            boolean colLocked = false;
            for (int i = 0; i < 6; i++) { if (verrou[i][j]) { colLocked = true; break; } }

            if (colLocked) continue;

            for (int shift = 1; shift < 6; shift++) {
                int[][] copie = new int[6][6];
                for (int r = 0; r < 6; r++) copie[r] = grilleResolue[r].clone();
                for (int s = 0; s < shift; s++) {
                    int temp = copie[5][j];
                    for (int i = 5; i > 0; i--) copie[i][j] = copie[i-1][j];
                    copie[0][j] = temp;
                }
                if (aUnAlignementSimple(copie)) return false;
            }
        }

        return true;
    }
}