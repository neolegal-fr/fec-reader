package fr.neolegal.fec.liassefiscale;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import fr.neolegal.fec.Fec;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

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

    static Optional<Terme> parseNumeroCompte(String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return Optional.empty();
        }

        candidate = candidate.trim();
        if (StringUtils.isNumeric(candidate)) {
            int numCompte = Integer.parseInt(candidate);
            if (numCompte >= MIN_NUM_COMPTE && numCompte <= MAX_NUM_COMPTE) {
                return Optional.of(new Terme(candidate, true));
            }
        }

        if (StringUtils.startsWith(candidate, Repere.PREFIXE_SOLDE_COMPTE)) {
            return Optional.of(new Terme(StringUtils.substring(candidate, Repere.PREFIXE_SOLDE_COMPTE.length()), true));
        }

        if (StringUtils.startsWith(candidate, Repere.PREFIXE_DIFF_COMPTE)) {
            return Optional.of(new Terme(StringUtils.substring(candidate, Repere.PREFIXE_DIFF_COMPTE.length()), false));
        }

        return Optional.empty();
    }

    public static List<Terme> resolveTermes(String repereLigneOrNumeroCompte) {
        return resolveTermes(repereLigneOrNumeroCompte, 10);
    }

    private static List<Terme> resolveTermes(String repereLigneOrNumeroCompte, int maxDepth) {
        Optional<Terme> match = parseNumeroCompte(repereLigneOrNumeroCompte);
        if (match.isPresent()) {
            return List.of(match.get());
        }

        Repere repere = Repere.DEFINITIONS.get(repereLigneOrNumeroCompte);
        if (Objects.isNull(repere)) {
            return List.of();
        }

        return resolveTermes(repere, maxDepth);
    }

    public static List<Terme> resolveTermes(Repere repere) {
        return resolveTermes(repere, 10);
    }

    private static List<Terme> resolveTermes(Repere repere, int maxDepth) {
        if (Objects.isNull(repere)) {
            return List.of();
        }

        if (maxDepth < 0) {
            throw new RuntimeException("Boucle infine détectée lors de la résolution des numéros de comptes");
        }

        List<Terme> comptes = new LinkedList<>();
        Matcher matcher = Pattern.compile(COMPTE_REGEX).matcher(repere.getExpression());
        while (matcher.find()) {
            comptes.addAll(resolveTermes(matcher.group(), maxDepth - 1));
        }

        matcher = Pattern.compile(REPERE_REGEX).matcher(repere.getExpression());
        while (matcher.find()) {
            comptes.addAll(resolveTermes(matcher.group(), maxDepth - 1));
        }

        return comptes;
    }

    public static double computeMontantLigneRepere(String repere, Fec fec) {
        Repere ligneRepere = Repere.get(repere);
        return computeMontantLigneRepere(ligneRepere, fec);
    }

    public static double computeMontantLigneRepere(Repere repere, Fec fec) {
        FecVariableProvider variables = new FecVariableProvider(fec);

        if (StringUtils.isBlank(repere.getExpression())) {
            return 0.0;
        }
        
        Expression expression = new ExpressionBuilder(repere.getExpression()).variables(variables).build();
        
        return Math.round(expression.evaluate());
    }
}
