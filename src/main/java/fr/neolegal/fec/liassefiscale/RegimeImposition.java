package fr.neolegal.fec.liassefiscale;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum RegimeImposition {
    REEL_NORMAL, REEL_SIMPLIFIE, REEL_SIMPLIFIE_AGRICOLE;

    public Set<NatureFormulaire> formulaires() {
        return Stream.of(NatureFormulaire.values())
                .filter(f -> f.getRegimeImposition() == this)
                .collect(Collectors.toSet());
    }
}
