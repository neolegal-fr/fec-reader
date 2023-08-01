package fr.neolegal.fec.liassefiscale;

import java.util.HashMap;
import java.util.Map;
import net.objecthunter.exp4j.VariableProvider;

public class Variables implements VariableProvider {

    Map<String, Double> customVariables = new HashMap<>();

    @Override
    public Double get(String variable) {
        return customVariables.get(variable);    
    }

    @Override
    public void set(String variable, Double value) {
        customVariables.put(variable, value);    
    }

    @Override
    public boolean contains(String name) {
        return LigneRepereHelper.isLigneRepere(name) || LigneRepereHelper.isNumeroCompte(name)
                || customVariables.containsKey(name);

    }

}
