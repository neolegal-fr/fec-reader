package fr.neolegal.fec;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import fr.neolegal.fec.liassefiscale.AgregationComptes;

/**
 * Soldes des comptes d'un fichier des écritures comptables.
 * <p>
 * Les soldes sont calculés une fois pour toutes à la lecture du fichier, puis
 * agrégés par préfixe de numéro de compte : le calcul d'une liasse fiscale
 * évalue plusieurs centaines de formules, qui parcourraient sinon chacune
 * l'intégralité des écritures.
 * <p>
 * Deux jeux de soldes sont tenus : l'un sur l'ensemble des écritures, l'autre
 * en excluant la reprise des soldes de l'exercice antérieur, nécessaire au
 * calcul des variations de l'exercice.
 */
public class SoldesComptes {

    /**
     * Codes et libellés des journaux de reprise des soldes : "AN", "ANO",
     * "A-NOUVEAUX", "A Nouveaux Détaillés", "Report à nouveau"...
     */
    private static final Pattern JOURNAL_A_NOUVEAUX = Pattern
            .compile("^A[\\s\\-_]?N(OU?V(EAUX?)?)?$|NOUVEAU|REPORT[\\s\\-_]?A[\\s\\-_]?NOUVEAU|OUVERTURE",
                    Pattern.CASE_INSENSITIVE);

    /** Solde (crédit - débit) de chaque compte, toutes écritures confondues */
    private final Map<String, Double> soldes;

    /** Solde de chaque compte, hors reprise des soldes de l'exercice antérieur */
    private final Map<String, Double> soldesHorsRepriseSoldes;

    /** Codes des journaux portant la reprise des soldes */
    private final Set<String> journauxRepriseSoldes;

    SoldesComptes(List<LEC> lignes) {
        this.journauxRepriseSoldes = resoudreJournauxRepriseSoldes(lignes);
        Map<String, Double> tous = new LinkedHashMap<>();
        Map<String, Double> hors = new LinkedHashMap<>();
        for (LEC ligne : lignes) {
            String compte = ligne.getCompteNum();
            if (StringUtils.isBlank(compte)) {
                continue;
            }
            double solde = ligne.getCreditOuZero() - ligne.getDebitOuZero();
            tous.merge(compte, solde, Double::sum);
            if (!journauxRepriseSoldes.contains(ligne.getJournalCode())) {
                hors.merge(compte, solde, Double::sum);
            }
        }
        this.soldes = Collections.unmodifiableMap(tous);
        this.soldesHorsRepriseSoldes = Collections.unmodifiableMap(hors);
    }

    /**
     * Identifie les journaux de reprise des soldes de l'exercice antérieur.
     * <p>
     * Ces écritures sont conventionnellement isolées dans un journal dédié, dont le
     * code ou le libellé désigne les "à-nouveaux" ; il ne se trouve pas
     * nécessairement en tête du fichier. Un journal n'est retenu que s'il ne
     * mouvemente que des comptes de bilan, ce qui écarte les homonymies.
     * <p>
     * À défaut, le livre des procédures fiscales prévoit que « les premiers numéros
     * d'écritures comptables du fichier correspondent aux écritures de reprise des
     * soldes de l'exercice antérieur » : le journal de la première ligne est alors
     * retenu, aux mêmes conditions.
     */
    static Set<String> resoudreJournauxRepriseSoldes(List<LEC> lignes) {
        Set<String> journaux = new LinkedHashSet<>();
        for (LEC ligne : lignes) {
            if (JOURNAL_A_NOUVEAUX.matcher(StringUtils.defaultString(ligne.getJournalCode()).trim()).find()
                    || JOURNAL_A_NOUVEAUX.matcher(StringUtils.defaultString(ligne.getJournalLib()).trim()).find()) {
                journaux.add(ligne.getJournalCode());
            }
        }

        journaux.removeIf(journal -> !neContientQueDesComptesDeBilan(lignes, journal));

        if (journaux.isEmpty() && !lignes.isEmpty()) {
            String premier = lignes.get(0).getJournalCode();
            if (neContientQueDesComptesDeBilan(lignes, premier)) {
                journaux.add(premier);
            }
        }

        return journaux;
    }

    /**
     * Une reprise des soldes ne mouvemente que des comptes de bilan (classes 1 à
     * 5) : un journal qui contient des charges ou des produits n'en est pas une.
     */
    private static boolean neContientQueDesComptesDeBilan(List<LEC> lignes, String journal) {
        boolean auMoinsUneLigne = false;
        for (LEC ligne : lignes) {
            if (!StringUtils.equals(journal, ligne.getJournalCode()) || StringUtils.isBlank(ligne.getCompteNum())) {
                continue;
            }
            auMoinsUneLigne = true;
            char classe = ligne.getCompteNum().charAt(0);
            if (classe < '1' || classe > '5') {
                return false;
            }
        }
        return auMoinsUneLigne;
    }

    public Set<String> getJournauxRepriseSoldes() {
        return journauxRepriseSoldes;
    }

    /** Solde d'un compte, ou 0 s'il n'est pas mouvementé. */
    public double getSolde(String compte) {
        return soldes.getOrDefault(compte, 0.0);
    }

    /** Agrège les soldes des comptes désignés par le préfixe de l'agrégation. */
    public double agreger(AgregationComptes agregation) {
        Map<String, Double> reference = agregation.getAgregateur().isRepriseSoldeIncluded() ? soldes
                : soldesHorsRepriseSoldes;

        double total = 0;
        for (Map.Entry<String, Double> compte : reference.entrySet()) {
            if (!agregation.appliesTo(compte.getKey())) {
                continue;
            }
            switch (agregation.getAgregateur()) {
                case CREDIT:
                    // Seuls les comptes dont le solde est créditeur
                    total += Math.max(0, compte.getValue());
                    break;
                case DEBIT:
                    // Seuls les comptes dont le solde est débiteur, exprimé positivement
                    total += Math.max(0, -compte.getValue());
                    break;
                default:
                    total += compte.getValue();
                    break;
            }
        }
        return total;
    }
}
