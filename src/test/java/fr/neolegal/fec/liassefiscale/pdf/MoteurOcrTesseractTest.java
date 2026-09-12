package fr.neolegal.fec.liassefiscale.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class MoteurOcrTesseractTest {

    /** Format de sortie "tsv" de tesseract */
    private static final String TSV = String.join("\n",
            "level\tpage_num\tblock_num\tpar_num\tline_num\tword_num\tleft\ttop\twidth\theight\tconf\ttext",
            "5\t1\t1\t1\t1\t1\t100\t200\t40\t20\t96.5\tAA",
            "5\t1\t1\t1\t1\t2\t300\t200\t80\t20\t95.1\t1 234",
            "5\t1\t1\t1\t1\t3\t500\t200\t10\t20\t12.0\t",
            "4\t1\t1\t1\t2\t0\t0\t0\t0\t0\t-1\t");

    @Test
    void parseTsv() {
        // Une image rendue à 300 points par pouce se ramène aux points PDF par 72/300
        List<MotPdf> mots = MoteurOcrTesseract.parseTsv(TSV, 3, 72f / 300f);

        assertEquals(2, mots.size());
        assertEquals("AA", mots.get(0).getTexte());
        assertEquals(3, mots.get(0).getPage());
        assertEquals(24.0f, mots.get(0).getGauche(), 0.01);
        assertEquals(48.0f, mots.get(0).getHaut(), 0.01);
        assertEquals(52.8f, mots.get(0).getBas(), 0.01);
        assertTrue(mots.get(1).estMontant());
        assertEquals(1234.0, mots.get(1).getMontant());
    }

    @Test
    void parseTsv_sortieIllisible() {
        assertTrue(MoteurOcrTesseract.parseTsv("", 1, 1f).isEmpty());
        assertTrue(MoteurOcrTesseract.parseTsv("entête\nligne incomplète", 1, 1f).isEmpty());
    }
}
