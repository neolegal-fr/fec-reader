package fr.neolegal.fec.liassefiscale.pdf;

/** Mesure de similarité entre deux libellés. */
public class Similarite {

    private Similarite() {
    }

    /**
     * Similarité entre deux textes, entre 0 (aucun rapport) et 1 (identiques),
     * calculée sur les textes normalisés (sans accent, sans espace, en
     * majuscules).
     */
    public static double calculer(String gauche, String droite) {
        String a = MotPdf.normalise(gauche);
        String b = MotPdf.normalise(droite);
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        if (a.equals(b)) {
            return 1;
        }
        int distance = levenshtein(a, b);
        return 1.0 - (double) distance / Math.max(a.length(), b.length());
    }

    /**
     * Similarité tolérante aux libellés tronqués : le texte le plus court est
     * recherché comme préfixe du plus long.
     */
    public static double calculerAvecTroncature(String gauche, String droite) {
        String a = MotPdf.normalise(gauche);
        String b = MotPdf.normalise(droite);
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        String court = a.length() <= b.length() ? a : b;
        String longue = a.length() <= b.length() ? b : a;
        // Les libellés des formulaires sont fréquemment tronqués à l'impression
        double directe = calculer(a, b);
        if (court.length() < longue.length() && longue.startsWith(court)) {
            return Math.max(directe, 0.90 + 0.10 * court.length() / longue.length());
        }
        return directe;
    }

    static int levenshtein(String a, String b) {
        int[] precedent = new int[b.length() + 1];
        int[] courant = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            precedent[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            courant[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cout = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                courant[j] = Math.min(Math.min(courant[j - 1] + 1, precedent[j] + 1), precedent[j - 1] + cout);
            }
            int[] echange = precedent;
            precedent = courant;
            courant = echange;
        }
        return precedent[b.length()];
    }
}
