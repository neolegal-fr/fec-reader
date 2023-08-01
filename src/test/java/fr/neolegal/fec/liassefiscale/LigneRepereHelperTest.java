package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LigneRepereHelperTest {
    @Test
    void isLigneRepere_returnFalse_whenEmptyOrNull() {
        assertFalse(LigneRepereHelper.isLigneRepere(null));
        assertFalse(LigneRepereHelper.isLigneRepere(""));
    }

    @Test
    void isLigneRepere_returnsTrue() {
        assertTrue(LigneRepereHelper.isLigneRepere("AA"));
        assertTrue(LigneRepereHelper.isLigneRepere("oo"));
    }

    @Test
    void isLigneRepere_returnsFalse() {
        assertFalse(LigneRepereHelper.isLigneRepere("ZZ"));
        assertFalse(LigneRepereHelper.isLigneRepere("6354"));
        assertFalse(LigneRepereHelper.isLigneRepere("PCG_7"));
    }

    @Test
    void isNumeroCompte_returnFalse_whenEmptyOrNull() {
        assertFalse(LigneRepereHelper.isNumeroCompte(null));
        assertFalse(LigneRepereHelper.isNumeroCompte(""));
    }

    @Test
    void isNumeroCompte_returnFalse() {
        assertFalse(LigneRepereHelper.isNumeroCompte("AA"));
        assertFalse(LigneRepereHelper.isNumeroCompte("A1"));
    }

    @Test
    void parseNumeroCompte() {

    }
}
