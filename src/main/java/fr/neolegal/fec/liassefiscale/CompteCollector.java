package fr.neolegal.fec.liassefiscale;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import net.objecthunter.exp4j.VariableProvider;

/** Permet de collecter les numéros de comptes référencés par une expression */
@Data
public class CompteCollector implements VariableProvider {

    List<AgregationComptes> comptes = new LinkedList<>();

    public CompteCollector() {
    }

    @Override
    public Double get(String variable) {
        RepereHelper.parseNumeroCompte(variable).ifPresent(compte -> comptes.add(compte));
        RepereHelper.parseLigneRepere(variable).ifPresent(repere -> comptes.addAll(RepereHelper.resolveComptes(repere)));
        return 0.0;    
    }

    @Override
    public void set(String variable, Double value) {
    }

    @Override
    public boolean contains(String name) {
        return RepereHelper.isLigneRepere(name) || RepereHelper.isNumeroCompte(name);
    }

}
