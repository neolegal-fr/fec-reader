package fr.neolegal.fec.liassefiscale;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import fr.neolegal.fec.Fec;
import fr.neolegal.fec.FecHelper;
import net.objecthunter.exp4j.VariableProvider;

public class FecVariableProvider implements VariableProvider {

    final Fec fec;
    final LiasseFiscale liasse;
    Map<String, Double> cache = new HashMap<>();

    public FecVariableProvider(Fec fec, LiasseFiscale liasse) {
        this.fec = fec;
        this.liasse = liasse;
    }

    @Override
    public Double get(String variable) {
        if (cache.containsKey(variable)) {
            return cache.get(variable);
        }

        Double montant = null;
        Optional<AgregationComptes> compteMatch = RepereHelper.parseNumeroCompte(variable);        
        Optional<Repere> repereMatch = RepereHelper.parseRepereCellule(liasse, variable);
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
        return cache.containsKey(name) || RepereHelper.isRepereCellule(liasse, name) || RepereHelper.isNumeroCompte(name);
    }

}
