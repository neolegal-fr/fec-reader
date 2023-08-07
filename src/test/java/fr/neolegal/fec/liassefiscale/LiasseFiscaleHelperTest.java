package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import fr.neolegal.fec.FecHelper;

public class LiasseFiscaleHelperTest {

    LiasseFiscale liasseReelNormal = LiasseFiscaleHelper
            .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt")), RegimeImposition.REEL_NORMAL);
    LiasseFiscale liasseReelSimplifie = LiasseFiscaleHelper
            .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/000000000FEC20231231.txt")), RegimeImposition.REEL_SIMPLIFIE);

    @Test
    void buildLiasseFiscale_reelNormal() {
        assertEquals(12, liasseReelNormal.getFormulaires().size());
    }

    @Test
    void buildLiasseFiscale_reelSimplifie() {
        assertEquals(4, liasseReelSimplifie.getFormulaires().size());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/123456789FEC20500930-expected.csv")
    void getMontant_reelNormal(String repere, Double expectedValue) {
        assertEquals(expectedValue, liasseReelNormal.getMontant(repere).get());
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/000000000FEC20231231-expected.csv")
    void getMontant_reelSimplifie(String repere, Double expectedValue) {
        assertEquals(expectedValue, liasseReelSimplifie.getMontant(repere).get());
    }

}
