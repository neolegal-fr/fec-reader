package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import fr.neolegal.fec.FecHelper;

public class LiasseFiscaleHelperTest {

    LiasseFiscale liasse = LiasseFiscaleHelper
            .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt")));

    @Test
    void buildLiasseFiscale() {
        assertEquals(liasse.getFormulaires().size(), Formulaire.values().length);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/123456789FEC20500930-expected.csv")
    void getMontant(String repere, Double expectedValue) throws FileNotFoundException, IOException {
        assertEquals(expectedValue, liasse.getMontant(repere).get());
    }
}
