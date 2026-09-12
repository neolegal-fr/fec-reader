package fr.neolegal.fec.liassefiscale.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimilariteTest {

    @Test
    void calculer_identique() {
        assertEquals(1.0, Similarite.calculer("Bilan - actif", "BILAN ACTIF"), 0.001);
    }

    @Test
    void calculer_different() {
        assertTrue(Similarite.calculer("Bilan - actif", "Compte de résultat") < 0.4);
    }

    @Test
    void calculerAvecTroncature_libelleTronque() {
        // Les libellés des formulaires sont fréquemment tronqués à l'impression
        assertTrue(Similarite.calculerAvecTroncature("Concessions, brevets et droits similair",
                "Concessions, brevets et droits similaires") > 0.9);
        assertTrue(Similarite.calculerAvecTroncature("Capital social ou individuel (1)* (Dont versé : 71 693 860)",
                "Capital social ou individuel") > 0.85);
    }
}
