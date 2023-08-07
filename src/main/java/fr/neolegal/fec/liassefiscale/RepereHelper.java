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

    
    public final static String REPERE_REGEX = "(?i)([A-Z][A-Z])";
    public final static String COMPTE_REGEX = "(?i)(((SLD)|(DIF)|(CRD)|(DEB))_[0-9]+)";
    public final static String REPERE_PREFIX = "REP_";

    private RepereHelper() {
    }

    static boolean isNumeroCompte(String candidate) {
        return parseNumeroCompte(candidate).isPresent();
    }

    static boolean isLigneRepere(String candidate) {
        return parseLigneRepere(candidate).isPresent();
    }

    static Optional<Repere> parseLigneRepere(String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return Optional.empty();
        }

        candidate = candidate.trim();
        if (candidate.length() == 2 && candidate.matches(REPERE_REGEX)) {
            String symbole = candidate.toUpperCase();
            Repere repere = Repere.DEFINITIONS.get(symbole);
            return Optional.ofNullable(repere);
        }

        if (StringUtils.startsWithIgnoreCase(candidate, REPERE_PREFIX)) {
            String symbole = StringUtils.substring(candidate, REPERE_PREFIX.length()).toUpperCase();
            Repere repere = Repere.DEFINITIONS.get(symbole);
            return Optional.ofNullable(repere);
        }


        return Optional.empty();
    }

    static Optional<AgregationComptes> parseNumeroCompte(String candidate) {
        if (StringUtils.isBlank(candidate)) {
            return Optional.empty();
        }

        candidate = candidate.trim();
        if (StringUtils.isNumeric(candidate)) {
            int numCompte = Integer.parseInt(candidate);
                return Optional.of(new AgregationComptes(candidate, AgregateurCompte.SOLDE));
        }

        for (AgregateurCompte agregateur : AgregateurCompte.values()) {
            if (StringUtils.startsWithIgnoreCase(candidate, agregateur.getPrefixe())) {
                return Optional.of(new AgregationComptes(
                        StringUtils.substring(candidate, agregateur.getPrefixe().length()), agregateur));
            }
        }

        return Optional.empty();
    }

    public static List<AgregationComptes> resolveComptes(String repereLigneOrNumeroCompte) {
        return resolveComptes(repereLigneOrNumeroCompte, 10);
    }

    private static List<AgregationComptes> resolveComptes(String repereLigneOrNumeroCompte, int maxDepth) {
        Optional<AgregationComptes> match = parseNumeroCompte(repereLigneOrNumeroCompte);
        if (match.isPresent()) {
            return List.of(match.get());
        }

        Repere repere = Repere.DEFINITIONS.get(repereLigneOrNumeroCompte);
        if (Objects.isNull(repere)) {
            return List.of();
        }

        return resolveComptes(repere, maxDepth);
    }

    public static List<AgregationComptes> resolveComptes(Repere repere) {
        return resolveComptes(repere, 10);
    }

    private static List<AgregationComptes> resolveComptes(Repere repere, int maxDepth) {
        if (Objects.isNull(repere)) {
            return List.of();
        }

        if (maxDepth < 0) {
            throw new RuntimeException("Boucle infine détectée lors de la résolution des numéros de comptes");
        }

        List<AgregationComptes> comptes = new LinkedList<>();
        Matcher matcher = Pattern.compile(COMPTE_REGEX).matcher(repere.getExpression());
        while (matcher.find()) {
            comptes.addAll(resolveComptes(matcher.group(), maxDepth - 1));
        }

        matcher = Pattern.compile(REPERE_REGEX).matcher(repere.getExpression());
        while (matcher.find()) {
            comptes.addAll(resolveComptes(matcher.group(), maxDepth - 1));
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
