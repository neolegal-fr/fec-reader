package fr.neolegal.fec.liassefiscale;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import fr.neolegal.fec.Fec;
import fr.neolegal.fec.FecHelper;

public class RepereHelper {

    public final static Character MIN_REPERE = 'A';
    public final static Character MAX_REPERE = 'O';
    public final static int MIN_NUM_COMPTE = 1;
    public final static int MAX_NUM_COMPTE = 7999;
    public final static String REPERE_REGEX = "(?i)([" + MIN_REPERE + "-" + MAX_REPERE + "][A-Z])";
    public final static String COMPTE_REGEX = "(?i)([SD]_[0-9]+)";

    private RepereHelper() {
    }

    static boolean isNumeroCompte(String candidate) {
        return parseNumeroCompte(candidate).isPresent();
    }

    static boolean isLigneRepere(String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return false;
        }
        candidate = candidate.trim();
        return candidate.length() == 2
                && candidate.matches(REPERE_REGEX);
    }

    static Optional<InfoCompte> parseNumeroCompte(String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return Optional.empty();
        }

        candidate = candidate.trim();
        if (StringUtils.isNumeric(candidate)) {
            int numCompte = Integer.parseInt(candidate);
            if (numCompte >= MIN_NUM_COMPTE && numCompte <= MAX_NUM_COMPTE) {
                return Optional.of(new InfoCompte(candidate, true));
            }
        }

        if (StringUtils.startsWith(candidate, Repere.PREFIXE_SOLDE_COMPTE)) {
            return Optional.of(new InfoCompte(StringUtils.substring(candidate, Repere.PREFIXE_SOLDE_COMPTE.length()), true));
        }

        if (StringUtils.startsWith(candidate, Repere.PREFIXE_DIFF_COMPTE)) {
            return Optional.of(new InfoCompte(StringUtils.substring(candidate, Repere.PREFIXE_DIFF_COMPTE.length()), false));
        }

        return Optional.empty();
    }

    public static List<InfoCompte> resolveComptes(String repereLigneOrNumeroCompte) {
        return resolveComptes(repereLigneOrNumeroCompte, 10);
    }

    private static List<InfoCompte> resolveComptes(String repereLigneOrNumeroCompte, int maxDepth) {
        Optional<InfoCompte> match = parseNumeroCompte(repereLigneOrNumeroCompte);
        if (match.isPresent()) {
            return List.of(match.get());
        }

        Repere repere = Repere.DEFINITIONS.get(repereLigneOrNumeroCompte);
        if (Objects.isNull(repere)) {
            return List.of();
        }

        return resolveComptes(repere, maxDepth);        
    }

    public static List<InfoCompte> resolveComptes(Repere repere) {
        return resolveComptes(repere, 10);
    }

    private static List<InfoCompte> resolveComptes(Repere repere, int maxDepth) {
        if (Objects.isNull(repere)) {
            return List.of();
        }

        if (maxDepth < 0) {
            throw new RuntimeException("Boucle infine détectée lors de la résolution des numéros de comptes");
        }
        
        List<InfoCompte> comptes = new LinkedList<>();
        Matcher matcher = Pattern.compile(COMPTE_REGEX).matcher(repere.getExpression());
        while (matcher.find()) {
            comptes.addAll(resolveComptes(matcher.group(), maxDepth-1));
        }

        matcher = Pattern.compile(REPERE_REGEX).matcher(repere.getExpression());
        while (matcher.find()) {
            comptes.addAll(resolveComptes(matcher.group(), maxDepth-1));
        }

        
        return comptes;
    }

    public static double computeMontantLigneRepere(String repere, Fec fec) {
        Repere ligneRepere = Repere.get(repere);
        return computeMontantLigneRepere(ligneRepere, fec);
    }

    public static double computeMontantLigneRepere(Repere repere, Fec fec) {
        List<InfoCompte> comptes = resolveComptes(repere);
        return Math.round(FecHelper.computeInfoComptesByNumero(fec.getLignes(),comptes));
    }
}
