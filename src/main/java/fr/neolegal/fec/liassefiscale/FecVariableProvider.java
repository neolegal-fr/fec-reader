package fr.neolegal.fec.liassefiscale;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import fr.neolegal.fec.Fec;
import fr.neolegal.fec.FecHelper;
import net.objecthunter.exp4j.VariableProvider;

public class FecVariableProvider implements VariableProvider {

    final Fec fec;
    Map<String, Double> cache = new HashMap<>();

    public FecVariableProvider(Fec fec) {
        this.fec = fec;
    }

    @Override
    public Double get(String variable) {
        if (cache.containsKey(variable)) {
            return cache.get(variable);
        }

        Double montant = null;
        Optional<Terme> compteMatch = RepereHelper.parseNumeroCompte(variable);        
        Repere repere = Repere.get(variable);
        if (compteMatch.isPresent()) {
            montant = FecHelper.computeTermes(fec.getLignes(), List.of(compteMatch.get()));
        } else if (Objects.nonNull(repere)) {
            montant = RepereHelper.computeMontantLigneRepere(repere, fec);
        }
        cache.put(variable, montant);

        return montant;    
    }

    @Override
    public void set(String variable, Double value) {
        cache.put(variable, value);    
    }

    @Override
    public boolean contains(String name) {
        return cache.containsKey(name) || RepereHelper.isLigneRepere(name) || RepereHelper.isNumeroCompte(name);
    }

}
