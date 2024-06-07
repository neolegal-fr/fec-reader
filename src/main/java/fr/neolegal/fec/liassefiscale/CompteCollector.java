package fr.neolegal.fec.liassefiscale;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;
import net.objecthunter.exp4j.VariableProvider;

/** Permet de collecter les numéros de comptes référencés par une expression */
@Data
public class CompteCollector implements VariableProvider {

    LiasseFiscale liasse;
    List<AgregationComptes> comptes = new LinkedList<>();

    public CompteCollector(LiasseFiscale liasse) {
        this.liasse = liasse;
    }

    @Override
    public Double get(String variable) {
        RepereHelper.parseNumeroCompte(variable).ifPresent(compte -> comptes.add(compte));
        liasse.getRepere(variable).ifPresent(repere -> comptes.addAll(RepereHelper.resolveComptes(liasse, repere)));
        return 0.0;    
    }

    @Override
    public void set(String variable, Double value) {
    }

    @Override
    public boolean contains(String name) {
        return RepereHelper.isRepereCellule(liasse, name) || RepereHelper.isNumeroCompte(name);
    }

}
