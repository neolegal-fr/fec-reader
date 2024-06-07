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
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_NORMAL);
        assertFalse(RepereHelper.isRepereCellule(liasse, null));
        assertFalse(RepereHelper.isRepereCellule(liasse, ""));
    }

    @Test
    void isLigneRepere_returnsTrue() {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_NORMAL);
        assertTrue(RepereHelper.isRepereCellule(liasse, "AA"));        
        assertTrue(RepereHelper.isRepereCellule(liasse, "REP_FL"));

        liasse = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_SIMPLIFIE);
        assertTrue(RepereHelper.isRepereCellule(liasse, "REP_310"));
    }

    @Test
    void isLigneRepere_returnsFalse() {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_NORMAL);
        assertFalse(RepereHelper.isRepereCellule(liasse, "ZZ"));
        assertFalse(RepereHelper.isRepereCellule(liasse, "6354"));
        assertFalse(RepereHelper.isRepereCellule(liasse, "S_7"));
        assertFalse(RepereHelper.isRepereCellule(liasse, "D_7"));
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
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_NORMAL);
        assertFalse(RepereHelper.resolveComptes(liasse, "FR").isEmpty());
        assertTrue(RepereHelper.resolveComptes(liasse, "ZZ").isEmpty());
    }

    @Test
    void computeMontantLigneRepere() {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_NORMAL);
        assertEquals(Optional.of(41056.0),
                RepereHelper.computeMontantRepereCellule(liasse, "DV", fec));
    }

    @Test
    void computeMontantLigneRepere_whenRepereInconnu() {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_NORMAL);
        assertEquals(Optional.empty(),
                RepereHelper.computeMontantRepereCellule(liasse, "YY", fec));
    }
}
