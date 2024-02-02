package fr.neolegal.fec.liassefiscale;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import net.objecthunter.exp4j.VariableProvider;

/** Permet de collecter les numéros de comptes référencés par une expression */
@Data
public class CompteCollector implements VariableProvider {

    RegimeImposition regime;
    List<AgregationComptes> comptes = new LinkedList<>();

    public CompteCollector(RegimeImposition regime) {
        this.regime = regime;
    }

    @Override
    public Double get(String variable) {
        RepereHelper.parseNumeroCompte(variable).ifPresent(compte -> comptes.add(compte));
        RepereHelper.parseRepereCellule(regime, variable).ifPresent(repere -> comptes.addAll(RepereHelper.resolveComptes(repere)));
        return 0.0;    
    }

    @Override
    public void set(String variable, Double value) {
    }

    @Override
    public boolean contains(String name) {
        return RepereHelper.isRepereCellule(regime, name) || RepereHelper.isNumeroCompte(name);
    }

}
