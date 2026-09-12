package fr.neolegal.fec.liassefiscale.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class MotPdfTest {

    @Test
    void parseMontant_entier() {
        assertEquals(Optional.of(1.0), MotPdf.parseMontant("1"));
        assertEquals(Optional.of(1234567.0), MotPdf.parseMontant("1234567"));
    }

    @Test
    void parseMontant_separateursDeMilliers() {
        assertEquals(Optional.of(1033701.0), MotPdf.parseMontant("1 033 701"));
        assertEquals(Optional.of(1033701.0), MotPdf.parseMontant("1 033 701"));
        assertEquals(Optional.of(1033701.0), MotPdf.parseMontant("1.033.701"));
    }

    @Test
    void parseMontant_decimales() {
        assertEquals(Optional.of(1000.25), MotPdf.parseMontant("1 000,25"));
        assertEquals(Optional.of(1000.25), MotPdf.parseMontant("1000.25"));
        assertEquals(Optional.of(12345.67), MotPdf.parseMontant("12.345,67"));
    }

    @Test
    void parseMontant_negatif() {
        assertEquals(Optional.of(-1000.0), MotPdf.parseMontant("(1 000)"));
        assertEquals(Optional.of(-10170931.0), MotPdf.parseMontant("10 170 931)"));
        assertEquals(Optional.of(-1000.0), MotPdf.parseMontant("-1 000"));
        assertEquals(Optional.of(-1000.0), MotPdf.parseMontant("1 000-"));
    }

    @Test
    void parseMontant_refuseCeQuiNEstPasUnMontant() {
        assertTrue(MotPdf.parseMontant("").isEmpty());
        assertTrue(MotPdf.parseMontant(null).isEmpty());
        assertTrue(MotPdf.parseMontant("AA").isEmpty());
        assertTrue(MotPdf.parseMontant("31/12/2019").isEmpty());
        assertTrue(MotPdf.parseMontant("12 %").isEmpty());
        assertTrue(MotPdf.parseMontant("2050-SD").isEmpty());
    }

    @Test
    void normalise() {
        assertEquals("BILANACTIF", MotPdf.normalise("Bilan  actif"));
        assertEquals("CREANCES", MotPdf.normalise("Créances"));
        assertEquals("", MotPdf.normalise(null));
    }
}
