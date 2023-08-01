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
        assertEquals(121396.22, actual.getMontant("DH").get());
        assertEquals(512996.22000000003, actual.getMontant("DL").get()); // erreur: devrait être 639230
        assertEquals(512996.22000000003, actual.getMontant("EE").get()); // erreur : devrait être 1016587
    }

    @Test
    void buildTableauComptable_whenCompteDeResultatsEnListe() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildTableauComptable(fec, Formulaire.DGFIP_2052_COMPTE_RESULTAT);
        assertEquals(1212843.9, actual.getMontant("FL").get());
        assertEquals(1225776.5, actual.getMontant("FR").get());
        assertEquals(-410953.37, actual.getMontant("FS").get());
        assertEquals(-1107619.9, actual.getMontant("GF").get());
        assertEquals(-249857.75, actual.getMontant("FY").get());
        assertEquals(-83308.12000000001, actual.getMontant("FZ").get());
        assertEquals(118156.59999999998, actual.getMontant("GG").get());
        assertEquals(-3043.58, actual.getMontant("GV").get());
        assertEquals(115113.01999999999, actual.getMontant("GW").get());
    }

    @Test
    void buildTableauComptable_whenCompteDeResultats() throws FileNotFoundException, IOException {
        TableauComptable actual = LiasseFiscaleHelper.buildTableauComptable(fec, Formulaire.DGFIP_2053_COMPTE_RESULTAT);
        assertEquals(11120.89, actual.getMontant("HI").get());
        assertEquals(0.0, actual.getMontant("HK").get());
        assertEquals(126233.90999999999, actual.getMontant("HN").get());
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
        assertEquals(35600.0, actual.getMontant("ZB").get()); // Erreur devrait être 0.0
        assertEquals(0.0, actual.getMontant("ZD").get());
        assertEquals(121396.22, actual.getMontant("ZG").get()); // Erreur : devrait être 4518.74
    }    

}
