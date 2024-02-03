package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import fr.neolegal.fec.FecHelper;

public class LiasseFiscaleHelperTest {

    @Test
    void buildLiasseFiscale_reelNormal() {
        LiasseFiscale liasseReelNormal = LiasseFiscaleHelper
                .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt")),
                        RegimeImposition.REEL_NORMAL);
        assertEquals(RegimeImposition.REEL_NORMAL, liasseReelNormal.getRegime());
        assertEquals(12, liasseReelNormal.getFormulaires().size());
    }

    @Test
    void buildLiasseFiscale_reelSimplifie() {
        LiasseFiscale liasseReelSimplifie = LiasseFiscaleHelper
                .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/000000000FEC20231231.txt")),
                        RegimeImposition.REEL_SIMPLIFIE);

        assertEquals(RegimeImposition.REEL_SIMPLIFIE, liasseReelSimplifie.getRegime());
        assertEquals(4, liasseReelSimplifie.getFormulaires().size());
    }

    @Test
    void getMontant_reelNormal() throws IOException {
        LiasseFiscale liasseReelNormal = LiasseFiscaleHelper
                .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt")),
                        RegimeImposition.REEL_NORMAL);
        checkParsedLiasse(liasseReelNormal, "target/test-classes/123456789FEC20500930-expected.csv");
    }

    @Test
    void getMontant_reelSimplifie() throws IOException {
        LiasseFiscale liasseReelSimplifie = LiasseFiscaleHelper
                .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/000000000FEC20231231.txt")),
                        RegimeImposition.REEL_SIMPLIFIE);

        checkParsedLiasse(liasseReelSimplifie, "target/test-classes/000000000FEC20231231-expected.csv");
    }

    // @Test
    void getMontant_reelNormalAgricole() throws IOException {

        LiasseFiscale liasse = LiasseFiscaleHelper
                .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/0000000001FEC20220831.txt")),
                        RegimeImposition.REEL_NORMAL);
        assertEquals(12, liasse.getFormulaires().size());

        checkParsedLiasse(liasse, "target/test-classes/0000000001FEC20220831-expected.csv");
    }

    private void checkParsedLiasse(LiasseFiscale liasse, String expectedResultFilePath) throws IOException {
        CSVParser csvParser = CSVParser.parse(Path.of(expectedResultFilePath), Charset.forName("UTF-8"),
                CSVFormat.DEFAULT);

        StringBuilder sb = new StringBuilder();
        int total = 0;
        int success = 0;
        int unknown = 0;
        for (CSVRecord csvRecord : csvParser) {
            if (csvRecord.size() > 1) {
                ++total;
                String symbole = csvRecord.get(0);
                Optional<Repere> match = Repere.get(liasse.getRegime(), symbole);
                if (match.isPresent()) {
                    Repere repere = match.get();
                    Double expected = Double.valueOf(csvRecord.get(1));
                    Double actual = liasse.getMontant(repere).orElse(0.0);
                    if (!Objects.equals(expected, actual)) {
                        sb.append(String.format("Montant du repère %s (%s) incorrect. Actual: %f, expected: %f\r\n",
                                symbole, repere.getNom(), actual, expected));
                    } else {
                        ++success;
                    }
                } else {
                    ++unknown;
                    sb.append(String.format("Repère inconnu : %s\r\n", symbole));
                }
            }
        }

        if (sb.length() > 0) {
            fail(String.format("%.02f%% de réussite, %d erreurs, %d inconnus\r\n", ((float)success / (float)(total-unknown))*100, total - success - unknown, unknown) + sb.toString());
        }

    }

    @Test
    void readLiasseFiscalePDF_A() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper
                .readLiasseFiscalePDF("target/test-classes/liasse-publique-A.pdf");

        checkParsedLiasse(liasse, "target/test-classes/liasse-publique-A-expected.csv");
    }

    @Test
    void readLiasseFiscalePDF_B() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper
                .readLiasseFiscalePDF("target/test-classes/liasse-publique-B.pdf");

        checkParsedLiasse(liasse, "target/test-classes/liasse-publique-B-expected.csv");
    }    

    @Test
    void readLiasseFiscalePDF_C() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper
                .readLiasseFiscalePDF("target/test-classes/liasse-publique-C.pdf");

        checkParsedLiasse(liasse, "target/test-classes/liasse-publique-C-expected.csv");
    }
    
    @Test
    void readLiasseFiscalePDF_D() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper
                .readLiasseFiscalePDF("target/test-classes/liasse-publique-D.pdf");

        checkParsedLiasse(liasse, "target/test-classes/liasse-publique-D-expected.csv");
    }
    
    @Test
    void readLiasseFiscalePDF_E() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper
                .readLiasseFiscalePDF("target/test-classes/liasse-publique-E.pdf");

        checkParsedLiasse(liasse, "target/test-classes/liasse-publique-E-expected.csv");
    }
}
