package fr.neolegal.fec.liassefiscale.pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;

/** Une page d'un document PDF, vue comme un ensemble de mots positionnés. */
@Getter
public class PagePdf {

    /** Hauteur relative de la zone considérée comme l'en-tête de la page */
    private static final float RATIO_ENTETE = 0.12f;

    private final int numero;
    private final float largeur;
    private final float hauteur;
    private final List<MotPdf> mots;
    private final List<LignePdf> lignes;
    /**
     * Regroupement plus large des lignes : dans de nombreuses éditions, les
     * montants sont imprimés quelques points au-dessous du libellé et du code du
     * repère de leur ligne. Les rangées réunissent les lignes ainsi décalées.
     */
    private final List<LignePdf> rangees;
    private final String texte;
    private final String texteNormalise;
    private final String entete;
    private final List<ColonneValeurs> colonnes;
    /** true si le texte de la page provient d'une reconnaissance optique */
    private final boolean issueOcr;

    public PagePdf(int numero, float largeur, float hauteur, List<MotPdf> mots, boolean issueOcr) {
        this.numero = numero;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.issueOcr = issueOcr;
        List<MotPdf> tries = new ArrayList<>(mots);
        tries.sort(Comparator.comparing(MotPdf::getBas).thenComparing(MotPdf::getGauche));
        this.mots = Collections.unmodifiableList(tries);
        this.lignes = construireLignes(this.mots);
        this.rangees = construireRangees(this.lignes);
        // Le texte est restitué rangée par rangée : un libellé et la valeur qui le
        // suit ne sont pas toujours imprimés à la même hauteur
        this.texte = rangees.stream().map(LignePdf::getTexte).collect(Collectors.joining("\n"));
        this.texteNormalise = MotPdf.normalise(this.texte);
        this.entete = rangees.stream().filter(rangee -> rangee.getBas() <= hauteur * RATIO_ENTETE)
                .map(LignePdf::getTexte).collect(Collectors.joining("\n"));
        this.colonnes = ColonneValeurs.detecter(this.mots, largeur);
    }

    public boolean estVide() {
        return mots.isEmpty();
    }

    private static List<LignePdf> construireLignes(List<MotPdf> mots) {
        List<LignePdf> lignes = new ArrayList<>();
        List<MotPdf> courante = new ArrayList<>();
        for (MotPdf mot : mots) {
            if (!courante.isEmpty() && !memeLigne(courante, mot)) {
                lignes.add(nouvelleLigne(courante));
                courante = new ArrayList<>();
            }
            courante.add(mot);
        }
        if (!courante.isEmpty()) {
            lignes.add(nouvelleLigne(courante));
        }
        return Collections.unmodifiableList(lignes);
    }

    /**
     * Regroupe en rangées les lignes appartenant à une même ligne de tableau.
     * <p>
     * De nombreuses éditions impriment le libellé, le code du repère et le montant
     * d'une même cellule à des hauteurs légèrement différentes : ils sont alors
     * restitués sur plusieurs lignes de texte. Les lignes dépourvues de libellé
     * sont rattachées à la ligne de libellé la plus proche.
     */
    private static List<LignePdf> construireRangees(List<LignePdf> lignes) {
        float seuil = seuilFusion(lignes);
        List<List<MotPdf>> rangees = new ArrayList<>();
        Map<Integer, Integer> rangeeParLigne = new LinkedHashMap<>();

        for (int i = 0; i < lignes.size(); i++) {
            if (porteUnLibelle(lignes.get(i))) {
                rangeeParLigne.put(i, rangees.size());
                rangees.add(new ArrayList<>(lignes.get(i).getMots()));
            }
        }

        for (int i = 0; i < lignes.size(); i++) {
            if (rangeeParLigne.containsKey(i)) {
                continue;
            }
            Integer rattachement = ligneLaPlusProche(lignes, i, seuil, rangeeParLigne.keySet());
            if (rattachement == null) {
                rangeeParLigne.put(i, rangees.size());
                rangees.add(new ArrayList<>(lignes.get(i).getMots()));
            } else {
                rangees.get(rangeeParLigne.get(rattachement)).addAll(lignes.get(i).getMots());
            }
        }

        List<LignePdf> resultat = new ArrayList<>(rangees.stream().map(PagePdf::nouvelleLigne).toList());
        resultat.sort(Comparator.comparing(LignePdf::getMilieuY));
        return Collections.unmodifiableList(resultat);
    }

    /** Une ligne porte un libellé si elle contient un mot qui n'est ni un montant ni un code. */
    private static boolean porteUnLibelle(LignePdf ligne) {
        return ligne.getMots().stream()
                .anyMatch(mot -> !mot.estMontant() && mot.getTexte().trim().length() >= 4);
    }

    /** Index de la ligne de libellé la plus proche, dans la limite de la distance. */
    private static Integer ligneLaPlusProche(List<LignePdf> lignes, int index, float distanceMaximale,
            Set<Integer> candidates) {
        Integer plusProche = null;
        float distance = distanceMaximale;
        for (int i = index - 1; i <= index + 1; i += 2) {
            if (!candidates.contains(i)) {
                continue;
            }
            float ecart = Math.abs(lignes.get(i).getMilieuY() - lignes.get(index).getMilieuY());
            if (ecart < distance) {
                distance = ecart;
                plusProche = i;
            }
        }
        return plusProche;
    }

    /**
     * Détermine l'écart vertical en deçà duquel deux lignes appartiennent à la même
     * rangée. Les écarts entre lignes consécutives se répartissent en deux
     * populations : les décalages à l'intérieur d'une rangée et l'interligne du
     * tableau. Le seuil qui les sépare le mieux est obtenu par la méthode d'Otsu.
     */
    static float seuilFusion(List<LignePdf> lignes) {
        List<Float> ecarts = new ArrayList<>();
        for (int i = 1; i < lignes.size(); i++) {
            float ecart = lignes.get(i).getMilieuY() - lignes.get(i - 1).getMilieuY();
            if (ecart > 0.2f) {
                ecarts.add(ecart);
            }
        }
        if (ecarts.size() < 6) {
            return 0;
        }
        List<Float> tries = new ArrayList<>(ecarts);
        Collections.sort(tries);
        float parDefaut = 0.45f * tries.get(tries.size() / 2);
        Collections.sort(ecarts);

        double total = ecarts.stream().mapToDouble(Float::doubleValue).sum();
        double meilleureVariance = 0;
        int coupure = -1;
        double sommeBasse = 0;
        for (int i = 0; i < ecarts.size() - 1; i++) {
            sommeBasse += ecarts.get(i);
            int nombreBas = i + 1;
            int nombreHaut = ecarts.size() - nombreBas;
            double moyenneBasse = sommeBasse / nombreBas;
            double moyenneHaute = (total - sommeBasse) / nombreHaut;
            double variance = (double) nombreBas * nombreHaut * Math.pow(moyenneBasse - moyenneHaute, 2);
            if (variance > meilleureVariance) {
                meilleureVariance = variance;
                coupure = i;
            }
        }

        // Les deux populations doivent être nettement séparées : sans cela, toutes les
        // lignes sont des rangées distinctes
        if (coupure < 0 || ecarts.get(coupure) > 0.6f * ecarts.get(coupure + 1)) {
            return parDefaut;
        }
        // Le seuil reste en deçà de la moitié de l'interligne du tableau, pour ne
        // jamais réunir deux rangées voisines
        float interligne = ecarts.get((coupure + 1 + ecarts.size()) / 2);
        return Math.min((ecarts.get(coupure) + ecarts.get(coupure + 1)) / 2f, 0.45f * interligne);
    }

    private static LignePdf nouvelleLigne(List<MotPdf> mots) {
        List<MotPdf> tries = new ArrayList<>(mots);
        tries.sort(Comparator.comparing(MotPdf::getGauche));
        return new LignePdf(tries);
    }

    private static boolean memeLigne(List<MotPdf> ligne, MotPdf mot) {
        MotPdf dernier = ligne.get(ligne.size() - 1);
        float recouvrement = Math.min(dernier.getBas(), mot.getBas()) - Math.max(dernier.getHaut(), mot.getHaut());
        float hauteurMin = Math.max(0.1f, Math.min(dernier.getHauteur(), mot.getHauteur()));
        return recouvrement / hauteurMin > 0.5f;
    }

    /** Colonne de valeurs à laquelle appartient le mot, s'il en existe une. */
    public int indexColonne(MotPdf mot) {
        for (int i = 0; i < colonnes.size(); i++) {
            if (colonnes.get(i).contient(mot)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        return String.format("page %d (%d mots, %d lignes)", numero, mots.size(), lignes.size());
    }
}
