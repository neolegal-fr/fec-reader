package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RepereTest {
    
    @Test
    void loadDefinitionsReperes() {
        /** La méthode loadDefinitionsReperes est invoquée statiquement pour initialiser les règles de calcul
         * On doit s'assurer que les règles ont été correctement chargées         */
        assertTrue(Repere.DEFINITIONS.size() > 0);
    }
}
