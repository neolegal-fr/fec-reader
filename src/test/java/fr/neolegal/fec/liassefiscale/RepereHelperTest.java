package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import fr.neolegal.fec.Fec;
import fr.neolegal.fec.FecHelper;

public class RepereHelperTest {

    final Fec fec = FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt"));

    @Test
    void isLigneRepere_returnFalse_whenEmptyOrNull() {
        assertFalse(RepereHelper.isLigneRepere(null));
        assertFalse(RepereHelper.isLigneRepere(""));
    }

    @Test
    void isLigneRepere_returnsTrue() {
        assertTrue(RepereHelper.isLigneRepere("AA"));
        assertTrue(RepereHelper.isLigneRepere("oo"));
    }

    @Test
    void isLigneRepere_returnsFalse() {
        assertFalse(RepereHelper.isLigneRepere("ZZ"));
        assertFalse(RepereHelper.isLigneRepere("6354"));
        assertFalse(RepereHelper.isLigneRepere("S_7"));
        assertFalse(RepereHelper.isLigneRepere("D_7"));
    }

    @Test
    void isNumeroCompte_returnFalse_whenEmptyOrNull() {
        assertFalse(RepereHelper.isNumeroCompte(null));
        assertFalse(RepereHelper.isNumeroCompte(""));
    }

    @Test
    void isNumeroCompte_returnFalse() {
        assertFalse(RepereHelper.isNumeroCompte("AA"));
        assertFalse(RepereHelper.isNumeroCompte("A1"));
    }

    @Test
    void isNumeroCompte_returnTrue() {
        assertTrue(RepereHelper.isNumeroCompte("S_1"));
        assertTrue(RepereHelper.isNumeroCompte("D_1"));
    }

    @Test
    void resolveTermes() {
        assertFalse(RepereHelper.resolveTermes("FR").isEmpty());
        assertTrue(RepereHelper.resolveTermes("ZZ").isEmpty());
    }

    @Test
    void computeMontantLigneRepere() {
        assertEquals(249858.0, RepereHelper.computeMontantLigneRepere("FY", fec));
    }
}
