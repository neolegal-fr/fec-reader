package fr.neolegal.fec.liassefiscale;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import fr.neolegal.fec.Fec;
import fr.neolegal.fec.FecHelper;
import net.objecthunter.exp4j.VariableProvider;

public class FecVariableProvider implements VariableProvider {

    final Fec fec;
    final RegimeImposition regime;
    Map<String, Double> cache = new HashMap<>();

    public FecVariableProvider(Fec fec, RegimeImposition regime) {
        this.fec = fec;
        this.regime = regime;
    }

    @Override
    public Double get(String variable) {
        if (cache.containsKey(variable)) {
            return cache.get(variable);
        }

        Double montant = null;
        Optional<AgregationComptes> compteMatch = RepereHelper.parseNumeroCompte(variable);        
        Optional<Repere> repereMatch = RepereHelper.parseRepereCellule(regime, variable);
        if (compteMatch.isPresent()) {
            montant = FecHelper.computeAgregationComptes(fec.getLignes(), compteMatch.get());
        } else if (repereMatch.isPresent()) {
            montant = RepereHelper.computeMontantRepereCellule(repereMatch.get(), fec, this).orElse(0.0);
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
        return cache.containsKey(name) || RepereHelper.isRepereCellule(regime, name) || RepereHelper.isNumeroCompte(name);
    }

}
