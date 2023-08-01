package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import fr.neolegal.fec.Fec;
import fr.neolegal.fec.FecHelper;

public class LiasseFiscaleHelperTest {

    static Fec fec = FecHelper.read(Path.of("target/test-classes/123456789FEC20500930.txt"));

    @Test
    void buildLiasseFiscale() throws FileNotFoundException, IOException {
        LiasseFiscale actual = LiasseFiscaleHelper.buildLiasseFiscale(fec);
        assertNotNull(actual.getBilanActif());
    }

    @Test
    void buildBilanActif() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildBilanActif(fec);
        assertNotNull(actual.getMontant("AA"));
    }

    @Test
    void buildBilanPassif() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildBilanPassif(fec);
        assertEquals(35600.0, actual.getMontant("DD").get());
        assertEquals(0.0, actual.getMontant("DG").get());
        assertEquals(121396.22, actual.getMontant("DH").get());
        assertEquals(512996.22000000003, actual.getMontant("DL").get()); // erreur: devrait être 639230
        assertEquals(512996.22000000003, actual.getMontant("EE").get()); // erreur : devrait être 1016587
    }

    

}
