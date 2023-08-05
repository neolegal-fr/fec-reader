package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import fr.neolegal.fec.FecHelper;

public class LiasseFiscaleHelperTest {

    LiasseFiscale liasse1 = LiasseFiscaleHelper
            .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt")));
    LiasseFiscale liasse2 = LiasseFiscaleHelper
            .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/000000000FEC20231231.txt")));

    @Test
    void buildLiasseFiscale() {
        assertEquals(liasse1.getFormulaires().size(), Formulaire.values().length);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/123456789FEC20500930-expected.csv")
    void getMontant_liasse1(String repere, Double expectedValue) {
        assertEquals(expectedValue, liasse1.getMontant(repere).get());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/000000000FEC20231231-expected.csv")
    void getMontant_liasse2(String repere, Double expectedValue) {
        assertEquals(expectedValue, liasse2.getMontant(repere).get());
    }

}
