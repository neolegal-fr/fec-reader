package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import fr.neolegal.fec.FecHelper;
import technology.tabula.RectangularTextContainer;

public class LiasseFiscaleHelperTest {

    LiasseFiscale liasseReelNormal = LiasseFiscaleHelper
            .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt")),
                    RegimeImposition.REEL_NORMAL);
    LiasseFiscale liasseReelSimplifie = LiasseFiscaleHelper
            .buildLiasseFiscale(FecHelper.read(Path.of("target/test-classes/000000000FEC20231231.txt")),
                    RegimeImposition.REEL_SIMPLIFIE);

    @Test
    void buildLiasseFiscale_reelNormal() {
        assertEquals(12, liasseReelNormal.getFormulaires().size());
    }

    @Test
    void buildLiasseFiscale_reelSimplifie() {
        assertEquals(4, liasseReelSimplifie.getFormulaires().size());
    }

    @Test
    void getMontant_reelNormal() throws IOException {
        CSVParser csvParser = CSVParser.parse(Path.of("target/test-classes/123456789FEC20500930-expected.csv"),
                Charset.forName("UTF-8"), CSVFormat.DEFAULT);

        StringBuilder sb = new StringBuilder();
        for (CSVRecord csvRecord : csvParser) {
            if (csvRecord.size() > 1) {
                String symbole = csvRecord.get(0);
                Repere repere = Repere.DEFINITIONS.get(symbole);
                Double expected = Double.valueOf(csvRecord.get(1));
                Double actual = liasseReelNormal.getMontant(repere).orElse(0.0);
                if (!Objects.equals(expected, actual)) {
                    sb.append(String.format("Montant du repère %s (%s) incorrect. Actual: %f, expected: %f", symbole,
                            repere.getNom(), actual, expected));
                }
            }
        }

        if (sb.length() > 0) {
            fail(sb.toString());
        }
    }

    @Test
    void getMontant_reelSimplifie() throws IOException {
        CSVParser csvParser = CSVParser.parse(Path.of("target/test-classes/000000000FEC20231231-expected.csv"),
                Charset.forName("UTF-8"), CSVFormat.DEFAULT);

        StringBuilder sb = new StringBuilder();
        for (CSVRecord csvRecord : csvParser) {
            if (csvRecord.size() > 1) {
                String symbole = csvRecord.get(0);
                Repere repere = Repere.DEFINITIONS.get(symbole);
                Double expected = Double.valueOf(csvRecord.get(1));
                Double actual = liasseReelSimplifie.getMontant(repere).orElse(0.0);
                if (!Objects.equals(expected, actual)) {
                    sb.append(String.format("Montant du repère %s (%s) incorrect. Actual: %f, expected: %f\r\n",
                            symbole, repere.getNom(), actual, expected));
                }
            }
        }

        if (sb.length() > 0) {
            fail(sb.toString());
        }
    }

    @Test
    void getMontant_reelNormal2() throws IOException {

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
        for (CSVRecord csvRecord : csvParser) {
            if (csvRecord.size() > 1) {
                String symbole = csvRecord.get(0);
                Repere repere = Repere.DEFINITIONS.get(symbole);
                if (repere != null) {
                    Double expected = Double.valueOf(csvRecord.get(1));
                    Double actual = liasse.getMontant(repere).orElse(0.0);
                    if (!Objects.equals(expected, actual)) {
                        sb.append(String.format("Montant du repère %s (%s) incorrect. Actual: %f, expected: %f\r\n",
                                symbole, repere.getNom(), actual, expected));
                    }
                } else {
                    sb.append(String.format("Repère inconnu : %s\r\n", symbole));
                }
            }
        }

        if (sb.length() > 0) {
            fail(sb.toString());
        }

    }

    @Test
    void readLiasseFiscalePDF() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper
                .readLiasseFiscalePDF("target/test-classes/liasse-A.pdf");

        checkParsedLiasse(liasse, "target/test-classes/liasse-A-expected.csv");
    }

}
