package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
        for (Repere repere : Repere.DEFINITIONS.values()) {
            List<InfoCompte> comptes = RepereHelper.resolveComptes(repere);
            Set<InfoCompte> doublons = comptes.stream().filter(c1 -> comptes.stream().filter(c2 -> c2.equals(c1)).count() > 1)
                    .collect(Collectors.toSet());
            if (!doublons.isEmpty()) {
                fail("Les règles de calcul du repère " + repere.getRepere()
                        + " ne sont pas valides à cause de sdoublons suivantes: " + doublons.stream()
                                .map(InfoCompte::getPrefixeNumero).collect(Collectors.joining(", ")));
            }
        }
    }

}
