package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.neolegal.fec.NatureAnomalie;
import fr.neolegal.fec.liassefiscale.controle.ControleCoherence;
import fr.neolegal.fec.liassefiscale.controle.ControlesHelper;
import fr.neolegal.fec.liassefiscale.controle.ResultatControle;
import fr.neolegal.fec.liassefiscale.controle.StatutControle;

/**
 * Les contrôles de cohérence comptable sont le principal indicateur de
 * fiabilité : ils vérifient que les montants extraits se recomposent
 * correctement (totaux, sous-totaux, équilibre du bilan, report du résultat).
 */
class ControleCoherenceTest {

    @Test
    void controlesCharges() {
        List<ControleCoherence> controles = ControlesHelper.getControles();
        assertTrue(controles.size() >= 60, "les contrôles de cohérence doivent être chargés");
        controles.forEach(controle -> {
            assertFalse(controle.getIdentifiant().isBlank());
            assertFalse(controle.getLibelle().isBlank());
            assertFalse(controle.getReperes().isEmpty());
        });
    }

    @Test
    void liasseCoherente() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-publique-jo-776550766.pdf"));

        List<ResultatControle> applicables = liasse.getControles().stream()
                .filter(resultat -> resultat.getStatut() != StatutControle.NON_APPLICABLE).toList();
        assertTrue(applicables.size() >= 25,
                "une liasse complète doit permettre d'exécuter la plupart des contrôles : " + applicables.size());
        assertEquals(List.of(), liasse.getControlesEnEchec());
        assertTrue(liasse.getScoreFiabilite() > 0.90, "fiabilité " + liasse.getScoreFiabilite());
    }

    @Test
    void liasseIncoherente_estSignalee() throws IOException {
        // Liasse dont les codes des repères sont imprimés sous forme d'image : les
        // montants ne peuvent être rattachés qu'aux libellés des lignes, ce qui est
        // beaucoup moins sûr. Les contrôles de cohérence le détectent.
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2050_2.pdf"));

        assertFalse(liasse.getControlesEnEchec().isEmpty());
        assertTrue(liasse.getScoreFiabilite() < 0.70, "fiabilité " + liasse.getScoreFiabilite());
        assertTrue(liasse.getAnomalies().stream()
                .anyMatch(anomalie -> anomalie.getNature() == NatureAnomalie.INCOHERENCE_COMPTABLE));
    }

    @Test
    void documentIllisible_estSignale() throws IOException {
        // Les polices de ce document ne comportent pas de table de correspondance
        // vers l'unicode : son texte est illisible sans reconnaissance optique
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2050_7.pdf"));

        assertEquals(0.0, liasse.getScoreFiabilite());
        assertTrue(liasse.getAnomalies().stream()
                .anyMatch(anomalie -> anomalie.getNature() == NatureAnomalie.PAGE_ILLISIBLE));
    }

    @Test
    void documentSansLiasseFiscale_neProduitAucunFormulaire() throws IOException {
        // Comptes annuels d'association au format libre : aucun formulaire de liasse
        // fiscale ne doit être reconnu, plutôt que des montants inventés
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("comptes-annuels-sans-liasse.pdf"));

        assertTrue(liasse.getFormulaires().isEmpty());
        assertTrue(liasse.getMontantsExtraits().isEmpty());
        assertEquals(0.0, liasse.getScoreFiabilite());
        assertEquals(null, liasse.getSiren());
    }

    @Test
    void liasseVierge_neProduitPasDeMontants() throws IOException {
        // Formulaires officiels non renseignés : toutes les cellules sont vides, et
        // les rares nombres imprimés sur le formulaire (appels de note, millésime) ne
        // doivent pas être présentés comme des montants fiables
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-vierge-2050.pdf"));

        assertFalse(liasse.getFormulaires().isEmpty(), "les formulaires vierges restent reconnus");
        List<MontantExtrait> nonNuls = liasse.getMontantsExtraits().stream()
                .filter(montant -> montant.getMontant() != 0).toList();
        assertTrue(nonNuls.size() <= 10, "montants lus à tort : " + nonNuls);
        assertTrue(nonNuls.stream().noneMatch(MontantExtrait::estFiable), "montants lus à tort : " + nonNuls);
        assertEquals(List.of(), liasse.getControlesEnEchec());
    }

    @Test
    void liassePubliqueInconnue_estIntegralementCoherente() throws IOException {
        // Liasse réelle n'ayant servi à aucun réglage du lecteur
        LiasseFiscale liasse = LiasseFiscaleHelper
                .lire(Fixtures.fichier("liasse-publique-union-champagne-2023.pdf"));

        assertEquals("780397410", liasse.getSiren());
        assertEquals(java.time.LocalDate.of(2023, 5, 31), liasse.getClotureExercice());
        assertEquals(List.of(), liasse.getControlesEnEchec());
        assertTrue(liasse.getScoreFiabilite() > 0.95, "fiabilité " + liasse.getScoreFiabilite());
    }

    @Test
    void quadrillageMalReconnu_estSignaleParUnControle() throws IOException {
        // Le quadrillage du bilan actif de ce document est mal découpé : un montant
        // est rattaché au mauvais repère, ce que détecte le contrôle des totaux
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-publique-jo-332108877.pdf"));

        assertEquals(1, liasse.getControlesEnEchec().size());
        assertTrue(liasse.getMontantExtrait("AC").orElseThrow().getConfiance() < 0.5);
        assertTrue(liasse.getScoreFiabilite() > 0.85);
    }

    @Test
    void confianceDesMontants_estRehausseeParLesControles() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-publique-jo-776550766.pdf"));

        MontantExtrait total = liasse.getMontantExtrait("CO").orElseThrow();
        assertEquals(MethodeExtraction.REPERE, total.getMethode());
        assertTrue(total.getControlesSatisfaits() >= 2);
        assertTrue(total.getConfiance() > 0.97, "confiance " + total.getConfiance());
        assertTrue(total.estFiable());
    }

    @Test
    void ajusterConfiance() {
        // Un montant vérifié par plusieurs contrôles devient quasi certain
        assertTrue(LiasseFiscaleHelper.ajusterConfiance(0.90, 2, 0) > 0.97);
        // Un montant impliqué dans un contrôle en échec devient douteux
        assertTrue(LiasseFiscaleHelper.ajusterConfiance(0.90, 0, 1) < 0.55);
        assertEquals(0.90, LiasseFiscaleHelper.ajusterConfiance(0.90, 0, 0), 0.001);
    }

    @Test
    void controleNonApplicable_lorsqueLesMontantsSontInconnus() {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_NORMAL);

        assertTrue(liasse.getControles().stream()
                .allMatch(resultat -> resultat.getStatut() == StatutControle.NON_APPLICABLE));
    }

    @Test
    void liasseCalculeeDepuisUnFec_estControlee() {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(
                fr.neolegal.fec.FecHelper.read(Fixtures.fichier("123456789FEC20500930.txt")),
                RegimeImposition.REEL_NORMAL);

        // Les contrôles s'appliquent aussi aux montants calculés depuis le FEC
        assertTrue(liasse.getControles().stream()
                .anyMatch(resultat -> resultat.getStatut() != StatutControle.NON_APPLICABLE));
    }

    @Test
    void reperesDesControles_existentDansLesModeles() {
        LiasseFiscale reelNormal = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_NORMAL);
        LiasseFiscale reelSimplifie = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_SIMPLIFIE);
        LiasseFiscale agricole = LiasseFiscaleHelper.buildLiasseFiscale(RegimeImposition.REEL_SIMPLIFIE_AGRICOLE);

        for (ControleCoherence controle : ControlesHelper.getControles()) {
            for (String symbole : controle.getReperes()) {
                assertTrue(
                        reelNormal.getRepere(symbole).isPresent() || reelSimplifie.getRepere(symbole).isPresent()
                                || agricole.getRepere(symbole).isPresent(),
                        String.format("le repère %s du contrôle %s n'existe dans aucun modèle de formulaire",
                                symbole, controle.getIdentifiant()));
            }
        }
    }
}
