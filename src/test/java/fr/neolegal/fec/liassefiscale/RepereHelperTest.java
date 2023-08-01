package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RepereHelperTest {
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
}
