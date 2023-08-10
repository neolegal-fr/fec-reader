package fr.neolegal.fec.liassefiscale;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

import fr.neolegal.fec.Fec;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.VariableProvider;

public class RepereHelper {

    public final static String REPERE_REGEX = "(?i)([A-Z][A-Z])";
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

    public static List<AgregationComptes> resolveComptes(String symboleRepere) {
        Repere repere = Repere.DEFINITIONS.get(symboleRepere);
        if (Objects.isNull(repere)) {
            return List.of();
        }
        return resolveComptes(repere);   
    }

    public static List<AgregationComptes> resolveComptes(Repere repere) {
        CompteCollector comptes = new CompteCollector();
        computeMontantLigneRepere(repere, Fec.builder().build(), comptes);
        return comptes.getComptes();
    }

    public static Optional<Double> computeMontantLigneRepere(String repere, Fec fec) {
        Repere ligneRepere = Repere.get(repere);
        return computeMontantLigneRepere(ligneRepere, fec);
    }


    public static Optional<Double> computeMontantLigneRepere(Repere repere, Fec fec) {
        FecVariableProvider variables = new FecVariableProvider(fec);
        return computeMontantLigneRepere(repere, fec, variables);
    }

    public static Optional<Double> computeMontantLigneRepere(Repere repere, Fec fec, VariableProvider variables) {        
        if (Objects.isNull(repere) || StringUtils.isBlank(repere.getExpression())) {
            return Optional.empty();
        }

        Expression expression = new ExpressionBuilder(repere.getExpression()).variables(variables).build();

        return Optional.of((double)Math.round(expression.evaluate()));
    }
}
