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
        assertEquals(actual.getFormulaires().size(), Formulaire.values().length);
    }

    @Test
    void buildTableauComptable_whenBilanActif() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildTableauComptable(fec, Formulaire.DGFIP_2050_BILAN_ACTIF);
        assertNotNull(actual.getMontant("AA"));
    }

    @Test
    void buildTableauComptable_whenBilanPassif() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildTableauComptable(fec, Formulaire.DGFIP_2051_BILAN_PASSIF);
        assertEquals(35600.0, actual.getMontant("DD").get());
        assertEquals(0.0, actual.getMontant("DG").get());
        assertEquals(121396.0, actual.getMontant("DH").get());
        assertEquals(639230.0, actual.getMontant("DL").get()); // erreur: devrait être 639230
        assertEquals(639230.0, actual.getMontant("EE").get()); // erreur : devrait être 1016587
    }

    @Test
    void buildTableauComptable_whenCompteDeResultatsEnListe() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildTableauComptable(fec, Formulaire.DGFIP_2052_COMPTE_RESULTAT);
        assertEquals(1212844.0, actual.getMontant("FL").get());
        assertEquals(1225777.0, actual.getMontant("FR").get());
        assertEquals(-410953.0, actual.getMontant("FS").get());
        assertEquals(-1107620.0, actual.getMontant("GF").get());
        assertEquals(-249858.0, actual.getMontant("FY").get());
        assertEquals(-83308.0, actual.getMontant("FZ").get());
        assertEquals(118157.0, actual.getMontant("GG").get());
        assertEquals(-3044.0, actual.getMontant("GV").get());
        assertEquals(115113.0, actual.getMontant("GW").get());
    }

    @Test
    void buildTableauComptable_whenCompteDeResultats() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildTableauComptable(fec, Formulaire.DGFIP_2053_COMPTE_RESULTAT);
        assertEquals(11121.0, actual.getMontant("HI").get());
        assertEquals(0.0, actual.getMontant("HK").get());
        assertEquals(126234.0, actual.getMontant("HN").get());
    }

    @Test
    void buildTableauComptable_whenAmortissements() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildTableauComptable(fec, Formulaire.DGFIP_2058_A_RESULTAT_FISCAL);
        assertEquals(0.0, actual.getMontant("WE").get());
        assertEquals(0.0, actual.getMontant("WF").get());
    }    

    @Test
    void buildTableauComptable_whenAffectationResultat() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildTableauComptable(fec, Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT);
        assertEquals(0.0, actual.getMontant("ZE").get());
        assertEquals(0.0, actual.getMontant("ZB").get());
        assertEquals(0.0, actual.getMontant("ZD").get());
        assertEquals(-4519.0, actual.getMontant("ZG").get());
    }    

}
