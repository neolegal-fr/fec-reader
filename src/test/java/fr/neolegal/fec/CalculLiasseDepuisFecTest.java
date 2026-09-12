package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.neolegal.fec.liassefiscale.LiasseFiscale;
import fr.neolegal.fec.liassefiscale.LiasseFiscaleHelper;
import fr.neolegal.fec.liassefiscale.RegimeImposition;
import fr.neolegal.fec.liassefiscale.VentilationComptes;
import fr.neolegal.fec.liassefiscale.controle.ResultatControle;
import fr.neolegal.fec.liassefiscale.controle.StatutControle;

/**
 * Qualité du calcul d'une liasse fiscale à partir d'un fichier des écritures
 * comptables, mesurée par les contrôles de cohérence comptable.
 */
class CalculLiasseDepuisFecTest {

    private static Path fichier(String nom) {
        return Path.of("target/test-classes", nom);
    }

    /**
     * Les écritures de reprise des soldes doivent être reconnues quel que soit le
     * code du journal qui les porte et sa position dans le fichier.
     */
    @ParameterizedTest
    @CsvSource({
            "123456789FEC20500930.txt,  ANO",  // journal en tête de fichier
            "000000000FEC20231231.txt,  AD",   // journal en fin de fichier, code inattendu
            "0000000001FEC20220831.txt, ANO",
            "111111111FEC20221231.TXT,  AN" }) // journal au milieu du fichier
    void journauxDeRepriseDesSoldes(String nom, String journalAttendu) {
        Fec fec = FecHelper.read(fichier(nom));

        assertEquals(Set.of(journalAttendu), fec.getSoldes().getJournauxRepriseSoldes());
    }

    /**
     * Le bilan calculé depuis un fichier complet doit s'équilibrer : c'est la
     * première preuve que les soldes ont été correctement ventilés.
     */
    @ParameterizedTest
    @CsvSource({
            "123456789FEC20500930.txt, REEL_NORMAL",
            "111111111FEC20221231.TXT, REEL_NORMAL" })
    void bilanEquilibre(String nom, RegimeImposition regime) {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(FecHelper.read(fichier(nom)), regime);

        assertEquals(List.of(), liasse.getControlesEnEchec());
        assertTrue(liasse.getScoreFiabilite() > 0.90, "fiabilité " + liasse.getScoreFiabilite());
    }

    @Test
    void tousLesComptesSontVentiles() {
        Fec fec = FecHelper.read(fichier("123456789FEC20500930.txt"));
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(fec, RegimeImposition.REEL_NORMAL);

        VentilationComptes ventilation = VentilationComptes.analyser(fec, liasse);
        assertEquals(0, ventilation.getComptesNonAffectes().size(), ventilation.toString());
        assertEquals(0.0, ventilation.getMontantNonAffecte(), 0.005);
    }

    /**
     * Un plan comptable qui s'écarte de la nomenclature attendue doit être
     * signalé, et non silencieusement ignoré.
     */
    @Test
    void comptesNonVentiles_sontSignales() {
        Fec fec = FecHelper.read(fichier("0000000001FEC20220831.txt"));
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(fec, RegimeImposition.REEL_NORMAL);

        VentilationComptes ventilation = VentilationComptes.analyser(fec, liasse);
        assertTrue(ventilation.getComptesNonAffectes().containsKey("408226"),
                "le compte 408226 n'est repris par aucune formule : " + ventilation);
        assertTrue(ventilation.getPartNonAffectee() < 0.02, "part non affectée " + ventilation);

        assertTrue(liasse.getAnomalies().stream()
                .anyMatch(anomalie -> anomalie.getNature() == NatureAnomalie.COMPTE_NON_AFFECTE));
    }

    /**
     * Le résultat calculé doit correspondre au solde des comptes de charges et de
     * produits du fichier.
     */
    @Test
    void resultatConformeAuFichier() {
        Fec fec = FecHelper.read(fichier("123456789FEC20500930.txt"));
        double resultat = 0;
        for (LEC ligne : fec.getLignes()) {
            char classe = ligne.getCompteNum().charAt(0);
            if (classe == '6' || classe == '7') {
                resultat += ligne.getCreditOuZero() - ligne.getDebitOuZero();
            }
        }

        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(fec, RegimeImposition.REEL_NORMAL);

        assertEquals(Math.round(resultat), liasse.getMontant("HN").orElseThrow());
        assertEquals(liasse.getMontant("HN"), liasse.getMontant("DI"), "le résultat est reporté au bilan");
    }

    @Test
    void controlesApplicables() {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(
                FecHelper.read(fichier("123456789FEC20500930.txt")), RegimeImposition.REEL_NORMAL);

        List<ResultatControle> applicables = liasse.getControles().stream()
                .filter(resultat -> resultat.getStatut() != StatutControle.NON_APPLICABLE).toList();
        assertTrue(applicables.size() >= 20, "contrôles applicables : " + applicables.size());
    }
}
