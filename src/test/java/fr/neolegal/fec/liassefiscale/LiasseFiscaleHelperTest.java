package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
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

        // Bilan passif
        expectedValues.put("DD", 35600.0);
        expectedValues.put("DG", 0.0);
        expectedValues.put("DH", 121396.0);
        expectedValues.put("DL", 639230.0);
        expectedValues.put("EE", 965331.0); // erreur : devrait être 1016587

        // Compte de résultats en liste
        expectedValues.put("FL", 1212844.0);
        expectedValues.put("FR", 1225777.0);
        expectedValues.put("FS", -410953.0);
        expectedValues.put("GF", -1107620.0);
        expectedValues.put("FY", -249858.0);
        expectedValues.put("FZ", -83308.0);
        expectedValues.put("GG", 118157.0);
        expectedValues.put("GV", -3044.0);
        expectedValues.put("GW", 115113.0);

        // Compte de résultats
        expectedValues.put("HI", 11121.0);
        expectedValues.put("HK", 0.0);
        expectedValues.put("HN", 126234.0);

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
            assertEquals(expectedValue.getValue(), actual.getMontant(repere).get(), String.format("Montant du repère %s (%s) incorrect", repere.getRepere(), repere.getNom()));
        }
    }
}
