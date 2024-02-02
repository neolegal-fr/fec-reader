package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import fr.neolegal.fec.Fec;
import fr.neolegal.fec.FecHelper;

public class RepereHelperTest {

    final Fec fec = FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt"));

    @Test
    void isLigneRepere_returnFalse_whenEmptyOrNull() {
        assertFalse(RepereHelper.isRepereCellule(RegimeImposition.REEL_NORMAL, null));
        assertFalse(RepereHelper.isRepereCellule(RegimeImposition.REEL_NORMAL, ""));
    }

    @Test
    void isLigneRepere_returnsTrue() {
        assertTrue(RepereHelper.isRepereCellule(RegimeImposition.REEL_NORMAL, "AA"));
        assertTrue(RepereHelper.isRepereCellule(RegimeImposition.REEL_NORMAL, "REP_FL"));
        assertTrue(RepereHelper.isRepereCellule(RegimeImposition.REEL_SIMPLIFIE, "REP_310"));
    }

    @Test
    void isLigneRepere_returnsFalse() {
        assertFalse(RepereHelper.isRepereCellule(RegimeImposition.REEL_NORMAL, "ZZ"));
        assertFalse(RepereHelper.isRepereCellule(RegimeImposition.REEL_NORMAL, "6354"));
        assertFalse(RepereHelper.isRepereCellule(RegimeImposition.REEL_NORMAL, "S_7"));
        assertFalse(RepereHelper.isRepereCellule(RegimeImposition.REEL_NORMAL, "D_7"));
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
        assertTrue(RepereHelper.isNumeroCompte("SLD_1"));
        assertTrue(RepereHelper.isNumeroCompte("DIF_1"));
        assertTrue(RepereHelper.isNumeroCompte("CRD_1"));
        assertTrue(RepereHelper.isNumeroCompte("DEB_1"));
    }

    @Test
    void resolveComptes() {
        assertFalse(RepereHelper.resolveComptes(RegimeImposition.REEL_NORMAL, "FR").isEmpty());
        assertTrue(RepereHelper.resolveComptes(RegimeImposition.REEL_NORMAL, "ZZ").isEmpty());
    }

    @Test
    void computeMontantLigneRepere() {
        assertEquals(Optional.of(41056.0),
                RepereHelper.computeMontantRepereCellule(RegimeImposition.REEL_NORMAL, "DV", fec));
    }

    @Test
    void computeMontantLigneRepere_whenRepereInconnu() {
        assertEquals(Optional.empty(),
                RepereHelper.computeMontantRepereCellule(RegimeImposition.REEL_NORMAL, "YY", fec));
    }
}
