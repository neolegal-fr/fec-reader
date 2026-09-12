package fr.neolegal.fec.liassefiscale.pdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;

/**
 * Colonne de montants d'un formulaire.
 * <p>
 * Les colonnes sont reconstituées en regroupant les montants dont les
 * rectangles se chevauchent horizontalement : contrairement à une détection
 * fondée sur l'alignement à droite, ce regroupement fonctionne quel que soit
 * l'alignement retenu par l'éditeur du document, et sans dépendre du
 * quadrillage du tableau.
 */
@Getter
public class ColonneValeurs {

    /** Nombre minimal de montants alignés pour constituer une colonne */
    private static final int MINIMUM_VALEURS = 3;

    /** Part de sa largeur qu'un montant doit partager avec la colonne */
    private static final float RECOUVREMENT_MINIMUM = 0.4f;

    /** Écart en deçà duquel deux montants sont considérés comme se touchant */
    private static final float JOINTURE = 1.5f;

    /** Tolérance sur la largeur d'une colonne, rapportée à son plus large montant */
    private static final float MARGE_LARGEUR = 1.5f;

    private final float gauche;
    private final float droite;
    private final int nombreValeurs;

    ColonneValeurs(float gauche, float droite, int nombreValeurs) {
        this.gauche = gauche;
        this.droite = droite;
        this.nombreValeurs = nombreValeurs;
    }

    public float getLargeur() {
        return droite - gauche;
    }

    public boolean contient(MotPdf mot) {
        return recouvrement(mot) >= RECOUVREMENT_MINIMUM * Math.max(1f, mot.getLargeur());
    }

    private float recouvrement(MotPdf mot) {
        return Math.min(droite, mot.getDroite()) - Math.max(gauche, mot.getGauche());
    }

    /**
     * Détecte les colonnes de montants d'une page : ce sont les composantes
     * connexes de la projection horizontale des montants. Les colonnes d'un
     * tableau étant nécessairement disjointes, deux montants qui se chevauchent
     * appartiennent à la même colonne, qu'ils soient alignés à gauche, à droite ou
     * centrés.
     */
    public static List<ColonneValeurs> detecter(Collection<MotPdf> mots, float largeurPage) {
        List<MotPdf> montants = mots.stream().filter(MotPdf::estMontant)
                .filter(mot -> mot.getLargeur() <= largeurPage * 0.25f)
                .sorted(Comparator.comparing(MotPdf::getGauche)).toList();

        List<ColonneValeurs> colonnes = new ArrayList<>();
        List<MotPdf> composante = new ArrayList<>();
        float droite = 0;
        for (MotPdf montant : montants) {
            if (!composante.isEmpty() && montant.getGauche() <= droite + JOINTURE) {
                droite = Math.max(droite, montant.getDroite());
            } else {
                ajouter(colonnes, composante, largeurPage);
                composante = new ArrayList<>();
                droite = montant.getDroite();
            }
            composante.add(montant);
        }
        ajouter(colonnes, composante, largeurPage);

        colonnes.sort(Comparator.comparing(ColonneValeurs::getGauche));
        return colonnes;
    }

    private static void ajouter(List<ColonneValeurs> colonnes, List<MotPdf> montants, float largeurPage) {
        if (montants.isEmpty()) {
            return;
        }
        float gauche = montants.get(0).getGauche();
        float droite = (float) montants.stream().mapToDouble(MotPdf::getDroite).max().orElse(gauche);

        // Une colonne n'est pas plus large que le plus large des montants qu'elle
        // contient : au-delà, un nombre isolé dans la zone des libellés relie deux
        // colonnes, que l'on sépare à leur plus grand espace intérieur
        float largeurMaximale = (float) montants.stream().mapToDouble(MotPdf::getLargeur).max().orElse(0);
        if (droite - gauche > MARGE_LARGEUR * largeurMaximale + JOINTURE) {
            int coupure = plusGrandEspace(montants);
            if (coupure > 0) {
                ajouter(colonnes, montants.subList(0, coupure), largeurPage);
                ajouter(colonnes, montants.subList(coupure, montants.size()), largeurPage);
                return;
            }
        }

        // Une colonne est constituée de valeurs réparties sur plusieurs lignes : une
        // suite de nombres sur une même ligne est un en-tête, pas une colonne
        Set<Float> rangees = new HashSet<>();
        montants.forEach(montant -> rangees.add(montant.getMilieuY()));
        if (rangees.size() >= MINIMUM_VALEURS) {
            colonnes.add(new ColonneValeurs(gauche, droite, rangees.size()));
        }
    }

    /** Index du montant précédé du plus grand espace, 0 s'il n'y en a pas. */
    private static int plusGrandEspace(List<MotPdf> montants) {
        int coupure = 0;
        float espace = 0;
        float droite = montants.get(0).getDroite();
        for (int i = 1; i < montants.size(); i++) {
            float ecart = montants.get(i).getGauche() - droite;
            if (ecart > espace) {
                espace = ecart;
                coupure = i;
            }
            droite = Math.max(droite, montants.get(i).getDroite());
        }
        return coupure;
    }

    @Override
    public String toString() {
        return String.format("colonne[%.1f-%.1f]x%d", gauche, droite, nombreValeurs);
    }
}
