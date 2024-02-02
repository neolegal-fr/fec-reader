package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class RepereTest {

    @Test
    void loadDefinitionsReperes() {
        /**
         * La méthode loadDefinitionsReperes est invoquée statiquement pour initialiser
         * les règles de calcul
         * On doit s'assurer que les règles ont été correctement chargées
         */
        assertTrue(Repere.DEFINITIONS.size() > 0);
    }

    @Test
    void checkValiditeRegles() {
        /**
         * On vérifie que les règles de calcul définies dans le fichier de configuration
         * sont valides
         */
        List<Repere> reperes = Repere.DEFINITIONS.values().stream().flatMap(r -> r.values().stream())
                .collect(Collectors.toList());
        Collections.sort(reperes);
        for (Repere repere : reperes) {
            List<AgregationComptes> comptes = RepereHelper.resolveComptes(repere);
            Set<AgregationComptes> doublons = comptes.stream()
                    .filter(agg1 -> comptes.stream()
                            .filter(agg2 -> agg1.isSupersetOf(agg2))
                            .count() > 1)
                    .collect(Collectors.toSet());
            if (!doublons.isEmpty()) {
                fail("La règle de calcul pour le repère " + repere.getSymbole()
                        + " est invalide car elle référence plus d'une fois " + doublons.stream()
                                .map(AgregationComptes::toString).collect(Collectors.joining(", ")));
            }
        }
    }
}
