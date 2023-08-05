package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import fr.neolegal.fec.Fec;
import fr.neolegal.fec.FecHelper;

public class LiasseFiscaleHelperTest {

    static Fec fec = FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt"));

    @Test
    void buildLiasseFiscale() throws FileNotFoundException, IOException {
        LiasseFiscale actual = LiasseFiscaleHelper.buildLiasseFiscale(fec);
        assertEquals(actual.getFormulaires().size(), Formulaire.values().length);

        Map<String, Double> expectedValues = new HashMap<>();
        // Bilan actif
        expectedValues.put("AA", 0.0);
        expectedValues.put("AB", 0.0);
        expectedValues.put("CX", 0.0);
        

        // Bilan passif
        expectedValues.put("DA", 356000.0);
        expectedValues.put("DB", 0.0);
        expectedValues.put("DC", 0.0);        
        expectedValues.put("DD", 35600.0);
        expectedValues.put("DE", 0.0);
        expectedValues.put("DF", 0.0);
        expectedValues.put("DG", 0.0);
        expectedValues.put("DH", 121396.0);
        expectedValues.put("DL", 639231.0);
        expectedValues.put("DM", 0.0);
        expectedValues.put("DN", 0.0);
        expectedValues.put("DO", 0.0);
        expectedValues.put("DR", 0.0);
        expectedValues.put("DU", 147174.0);
        expectedValues.put("DV",  41056.0);
        expectedValues.put("DW", 0.0);
        expectedValues.put("DX", 154891.0); // devrait être 156766
        expectedValues.put("DY", 16159.0);  // devrait être 32361
        expectedValues.put("DZ", 0.0);
        expectedValues.put("EA", 0.0);
        expectedValues.put("EB", 0.0);
        expectedValues.put("EC", 359280.0); // Devrait être 377357
        expectedValues.put("ED", 0.0);
        expectedValues.put("EE", 998511.0); // erreur : devrait être 1016587

        // Compte de résultats en liste
        expectedValues.put("FL", 1212844.0);
        expectedValues.put("FR", 1225777.0);
        expectedValues.put("FS", 410953.0);
        expectedValues.put("GF", 1107619.0);
        expectedValues.put("FY", 249858.0);
        expectedValues.put("FZ", 83308.0);
        expectedValues.put("GG", 118158.0);
        expectedValues.put("GV", -3044.0);
        expectedValues.put("GW", 115114.0);

        // Compte de résultats
        expectedValues.put("HA", 857.0);
        expectedValues.put("HB", 10417.0);
        expectedValues.put("HC", 0.0);
        expectedValues.put("HD", 11274.0);
        expectedValues.put("HE", 35.0);
        expectedValues.put("HF", 0.0);
        expectedValues.put("HG", 118.0);
        expectedValues.put("HH", 153.0);
        expectedValues.put("HI", 11121.0);
        expectedValues.put("HK", 0.0);
        expectedValues.put("HN", 126235.0);

        // Amortissements
        expectedValues.put("WE", 0.0);
        expectedValues.put("WF", 0.0);

        // Affectation résultat
        expectedValues.put("ZE", 0.0);
        expectedValues.put("ZB", 0.0);
        expectedValues.put("ZD", 0.0);
        expectedValues.put("ZG", -4519.0);

        for (Map.Entry<String, Double> expectedValue : expectedValues.entrySet()) {
            Repere repere = Repere.DEFINITIONS.get(expectedValue.getKey());
            if (!Objects.equals(expectedValue.getValue(), actual.getMontant(repere).get())) {
                fail(String.format("Montant du repère %s (%s) incorrect. Actual: %f, expected: %f", repere.getRepere(), repere.getNom(), actual.getMontant(repere).get(), expectedValue.getValue()));
            }
        }
    }
}
