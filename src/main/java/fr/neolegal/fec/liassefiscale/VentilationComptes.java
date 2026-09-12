package fr.neolegal.fec.liassefiscale;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import fr.neolegal.fec.Fec;
import fr.neolegal.fec.LEC;

/**
 * Contrôle de la ventilation des comptes d'un fichier des écritures comptables
 * dans les repères de la liasse fiscale.
 * <p>
 * Les formules de calcul désignent les comptes par leur numéro, parfois jusqu'à
 * cinq chiffres. Un plan comptable qui s'écarte de cette nomenclature — un
 * fournisseur en 4082 là où la formule attend 4081, un stock en 302 là où elle
 * attend 31 — verrait son solde disparaître de la liasse sans que rien ne le
 * signale. Ce contrôle recense ces comptes et le montant en cause.
 */
public class VentilationComptes {

    /**
     * Classes de comptes reprises par la liasse : le bilan pour les classes 1 à 5,
     * le compte de résultat pour les classes 6 et 7.
     */
    private static final String CLASSES_REPRISES = "1234567";

    private final Map<String, Double> comptesNonAffectes;
    private final double montantNonAffecte;
    private final double montantTotal;

    private VentilationComptes(Map<String, Double> comptesNonAffectes, double montantNonAffecte,
            double montantTotal) {
        this.comptesNonAffectes = Collections.unmodifiableMap(comptesNonAffectes);
        this.montantNonAffecte = montantNonAffecte;
        this.montantTotal = montantTotal;
    }

    /**
     * Recense les comptes de bilan dont le solde n'est repris par aucun repère de
     * la liasse.
     */
    public static VentilationComptes analyser(Fec fec, LiasseFiscale liasse) {
        Set<AgregationComptes> agregations = agregationsCouvertes(liasse);

        Map<String, Double> soldes = new TreeMap<>();
        for (LEC ligne : fec.getLignes()) {
            String compte = ligne.getCompteNum();
            if (compte == null || compte.isEmpty() || CLASSES_REPRISES.indexOf(compte.charAt(0)) < 0) {
                continue;
            }
            soldes.merge(compte, ligne.getCreditOuZero() - ligne.getDebitOuZero(), Double::sum);
        }

        Map<String, Double> nonAffectes = new LinkedHashMap<>();
        double montantNonAffecte = 0;
        double montantTotal = 0;
        for (Map.Entry<String, Double> solde : soldes.entrySet()) {
            montantTotal += Math.abs(solde.getValue());
            if (Math.abs(solde.getValue()) < 0.005) {
                continue;
            }
            if (agregations.stream().noneMatch(agregation -> reprend(agregation, solde.getKey(), solde.getValue()))) {
                nonAffectes.put(solde.getKey(), solde.getValue());
                montantNonAffecte += Math.abs(solde.getValue());
            }
        }

        return new VentilationComptes(nonAffectes, montantNonAffecte, montantTotal);
    }

    /** Agrégations de comptes référencées par les formules de calcul de la liasse. */
    static Set<AgregationComptes> agregationsCouvertes(LiasseFiscale liasse) {
        Set<AgregationComptes> agregations = new LinkedHashSet<>();
        for (Formulaire formulaire : liasse.getFormulaires()) {
            for (Repere repere : formulaire.getAllReperes()) {
                for (AgregationComptes agregation : RepereHelper.resolveComptes(liasse, repere)) {
                    if (!agregation.getPrefixNumeroCompte().isEmpty()) {
                        agregations.add(agregation);
                    }
                }
            }
        }
        return agregations;
    }

    /**
     * Une agrégation ne reprend le solde d'un compte que si elle en accepte le
     * sens : un solde créditeur laissé à une agrégation débitrice n'apparaît nulle
     * part dans la liasse.
     */
    static boolean reprend(AgregationComptes agregation, String compte, double solde) {
        if (!agregation.appliesTo(compte)) {
            return false;
        }
        switch (agregation.getAgregateur()) {
            case CREDIT:
                return solde > 0;
            case DEBIT:
                return solde < 0;
            default:
                return true;
        }
    }

    /** Comptes dont le solde n'alimente aucun repère de la liasse, et leur solde. */
    public Map<String, Double> getComptesNonAffectes() {
        return comptesNonAffectes;
    }

    /** Somme des soldes, en valeur absolue, des comptes non affectés. */
    public double getMontantNonAffecte() {
        return montantNonAffecte;
    }

    /** Part des soldes qui n'alimente aucun repère, entre 0 et 1. */
    public double getPartNonAffectee() {
        return montantTotal == 0 ? 0 : montantNonAffecte / montantTotal;
    }

    @Override
    public String toString() {
        return String.format("%d comptes non affectés, %.2f € (%.1f %% des soldes)",
                comptesNonAffectes.size(), montantNonAffecte, getPartNonAffectee() * 100);
    }
}
