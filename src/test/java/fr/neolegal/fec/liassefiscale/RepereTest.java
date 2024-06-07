package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class RepereTest {

    @Test
    void checkValiditeRegles() {
        /**
         * On vérifie que les règles de calcul définies dans le fichier de configuration
         * sont valides
         */
        for (RegimeImposition regime : RegimeImposition.values()) {
            LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(regime);
            for (Formulaire formulaire : liasse.getFormulaires()) {
                for (Repere repere : formulaire.getAllReperes()) {
                    List<AgregationComptes> comptes = RepereHelper.resolveComptes(liasse, repere);
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
    }
}
