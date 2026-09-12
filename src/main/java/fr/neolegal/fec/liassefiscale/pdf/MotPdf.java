package fr.neolegal.fec.liassefiscale.pdf;

import java.util.Optional;
import java.util.regex.Pattern;

import fr.neolegal.fec.liassefiscale.StrUtils;
import lombok.Getter;

/**
 * Un mot extrait d'une page PDF, avec sa position et sa taille.
 * <p>
 * Les coordonnées sont exprimées dans le repère "utilisateur" de la page, après
 * correction de la rotation : l'origine est en haut à gauche, x croît vers la
 * droite et y vers le bas.
 */
@Getter
public class MotPdf {

    /** Caractères assimilés à des espaces dans les montants (espace fine, insécable, etc.) */
    private static final Pattern ESPACES = Pattern.compile("[\\s\\u00a0\\u202f\\u2007\\u2009']+");

    /** Tout ce qui n'est ni lettre ni chiffre est ignoré lors des comparaisons de libellés */
    private static final Pattern SEPARATEURS = Pattern.compile("[^\\p{Alnum}]+");

    /** Un montant ne contient que des chiffres et d'éventuels séparateurs */
    private static final Pattern MONTANT = Pattern.compile("\\d{1,3}(?:[.,]\\d+)*|\\d+(?:[.,]\\d+)?");

    /**
     * Groupes de chiffres séparés par des espaces : le premier compte au plus trois
     * chiffres, les suivants exactement trois, le dernier pouvant porter les
     * décimales. Une suite irrégulière ("39 41560 745120 252") ne provient pas
     * d'une cellule unique mais d'une colonne entière mal découpée.
     */
    private static final Pattern GROUPES = Pattern.compile(
            "\\d{1,3}(?:[\\s\\u00a0\\u202f]\\d{3})*(?:[.,]\\d+)?");

    /** Au-delà de mille milliards, il ne s'agit plus d'un montant de liasse fiscale */
    private static final double MONTANT_MAXIMUM = 1e12;

    private final String texte;
    private final float gauche;
    private final float droite;
    private final float haut;
    private final float bas;
    private final float largeurEspace;
    private final int page;

    /** Texte normalisé : sans accent, sans espace, en majuscules */
    private final String texteNormalise;

    private final Double montant;

    public MotPdf(String texte, float gauche, float droite, float haut, float bas, float largeurEspace, int page) {
        this.texte = texte;
        this.gauche = gauche;
        this.droite = droite;
        this.haut = haut;
        this.bas = bas;
        this.largeurEspace = largeurEspace;
        this.page = page;
        this.texteNormalise = normalise(texte);
        this.montant = parseMontant(texte).orElse(null);
    }

    public float getLargeur() {
        return droite - gauche;
    }

    public float getHauteur() {
        return bas - haut;
    }

    /** Ordonnée du milieu du mot, utilisée pour regrouper les mots en lignes */
    public float getMilieuY() {
        return (haut + bas) / 2f;
    }

    public boolean estMontant() {
        return montant != null;
    }

    public static String normalise(String texte) {
        if (texte == null) {
            return "";
        }
        return SEPARATEURS.matcher(StrUtils.stripAccents(texte)).replaceAll("").toUpperCase();
    }

    /**
     * Interprète le texte d'une cellule comme un montant.
     * <p>
     * Gère les séparateurs de milliers (espace, espace insécable, point), le
     * séparateur décimal français (virgule) ou anglo-saxon (point), et les
     * notations de montants négatifs : signe moins en tête ou en fin, ou montant
     * entre parenthèses.
     *
     * @return le montant, ou {@link Optional#empty()} si le texte n'est pas un
     *         montant
     */
    public static Optional<Double> parseMontant(String texte) {
        if (texte == null) {
            return Optional.empty();
        }
        // Une parenthèse séparée des chiffres par une espace ferme le plus souvent une
        // incise du libellé ("(déduire ... 3 971 804 )") : elle ne marque pas un
        // montant négatif, contrairement à la parenthèse accolée au nombre
        String texteUtile = texte.replaceAll("(?<=\\d)[\\s\\u00a0\\u202f]+\\)\\s*$", "")
                .replaceAll("^\\s*\\([\\s\\u00a0\\u202f]+(?=\\d)", "");
        String valeur = ESPACES.matcher(texteUtile).replaceAll("").replace("€", "").replace("€", "");
        if (valeur.isEmpty()) {
            return Optional.empty();
        }

        String chiffres = texteUtile.trim().replaceAll("^[-–−(]\\s*", "").replaceAll("[)\\-]\\s*$", "").trim();
        if (chiffres.matches(".*[\\s\\u00a0\\u202f].*") && !GROUPES.matcher(chiffres).matches()) {
            return Optional.empty();
        }

        boolean negatif = false;
        if (valeur.startsWith("(") && valeur.endsWith(")")) {
            negatif = true;
            valeur = valeur.substring(1, valeur.length() - 1);
        } else if (valeur.startsWith("(") || valeur.endsWith(")")) {
            // Parenthèse ouvrante ou fermante isolée : la cellule voisine porte l'autre
            negatif = true;
            valeur = valeur.replace("(", "").replace(")", "");
        }
        if (valeur.startsWith("-") || valeur.startsWith("–") || valeur.startsWith("−")) {
            negatif = !negatif;
            valeur = valeur.substring(1);
        } else if (valeur.endsWith("-")) {
            negatif = !negatif;
            valeur = valeur.substring(0, valeur.length() - 1);
        }

        if (valeur.isEmpty() || !MONTANT.matcher(valeur).matches()) {
            return Optional.empty();
        }

        // Recherche du séparateur décimal : le dernier séparateur suivi d'au plus deux
        // chiffres, à condition de ne pas être un séparateur de milliers
        int decimale = -1;
        int dernierSeparateur = Math.max(valeur.lastIndexOf(','), valeur.lastIndexOf('.'));
        if (dernierSeparateur >= 0) {
            int chiffresApres = valeur.length() - dernierSeparateur - 1;
            boolean separateurMilliers = chiffresApres == 3
                    && valeur.charAt(dernierSeparateur) == premierSeparateur(valeur);
            if (!separateurMilliers) {
                decimale = dernierSeparateur;
            }
        }

        String partieEntiere = decimale < 0 ? valeur : valeur.substring(0, decimale);
        String partieDecimale = decimale < 0 ? "" : valeur.substring(decimale + 1);
        partieEntiere = partieEntiere.replace(",", "").replace(".", "");
        if (partieEntiere.isEmpty() && partieDecimale.isEmpty()) {
            return Optional.empty();
        }
        if (partieDecimale.contains(",") || partieDecimale.contains(".")) {
            return Optional.empty();
        }

        try {
            double resultat = Double.parseDouble((partieEntiere.isEmpty() ? "0" : partieEntiere)
                    + (partieDecimale.isEmpty() ? "" : "." + partieDecimale));
            if (resultat > MONTANT_MAXIMUM) {
                return Optional.empty();
            }
            return Optional.of(negatif ? -resultat : resultat);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static char premierSeparateur(String valeur) {
        for (int i = 0; i < valeur.length(); i++) {
            char c = valeur.charAt(i);
            if (c == ',' || c == '.') {
                return c;
            }
        }
        return ' ';
    }

    @Override
    public String toString() {
        return String.format("%s[%.1f-%.1f;%.1f]", texte, gauche, droite, haut);
    }
}
