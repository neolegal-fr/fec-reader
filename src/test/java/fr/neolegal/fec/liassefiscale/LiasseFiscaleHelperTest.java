package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.neolegal.fec.FecHelper;

class LiasseFiscaleHelperTest {

    @Test
    void buildLiasseFiscale_reelNormal() {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(
                FecHelper.read(Fixtures.fichier("123456789FEC20500930.txt")), RegimeImposition.REEL_NORMAL);

        assertEquals(RegimeImposition.REEL_NORMAL, liasse.getRegime());
        assertEquals(13, liasse.getFormulaires().size());
        assertEquals(102, montantsJustes(liasse, "123456789FEC20500930-expected.csv"));
    }

    @Test
    void buildLiasseFiscale_reelSimplifie() {
        LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(
                FecHelper.read(Fixtures.fichier("000000000FEC20231231.txt")), RegimeImposition.REEL_SIMPLIFIE);

        assertEquals(RegimeImposition.REEL_SIMPLIFIE, liasse.getRegime());
        assertEquals(6, liasse.getFormulaires().size());
        assertEquals(59, montantsJustes(liasse, "000000000FEC20231231-expected.csv"));
    }

    /** Nombre de repères dont le montant calculé correspond à la valeur attendue. */
    private long montantsJustes(LiasseFiscale liasse, String attendus) {
        Map<String, Double> montants = Fixtures.lireMontants(Fixtures.fichier(attendus));
        return montants.entrySet().stream()
                .filter(attendu -> Math.abs(liasse.getMontant(attendu.getKey()).orElse(0.0) - attendu.getValue()) < 0.5)
                .count();
    }

    /** Identification de l'entreprise et de l'exercice, pour chaque document du jeu d'essai. */
    @ParameterizedTest
    @CsvSource(nullValues = "-", value = {
            "liasse-2033.pdf,                   437641699, 2022-12-31, REEL_SIMPLIFIE",
            "liasse-2050_1.pdf,                 303195192, 2019-12-31, REEL_NORMAL",
            "liasse-2050_2.pdf,                 558501912, 2019-12-31, REEL_NORMAL",
            "liasse-2050_3.pdf,                 529770646, 2017-12-31, REEL_NORMAL",
            "liasse-2050_4.pdf,                 523128205, 2019-03-31, REEL_NORMAL",
            "liasse-2050_5.pdf,                 402207153, 2015-12-31, REEL_NORMAL",
            "liasse-2050_6.pdf,                 449207133, 2015-12-31, REEL_NORMAL",
            "liasse-2050_8.pdf,                 451209852, 2018-12-31, REEL_NORMAL",
            // Liasse ne portant pas de numéro SIREN
            "liasse-2050_9.pdf,                 -,         2022-12-31, REEL_NORMAL",
            "liasse-2139_1.pdf,                 891369951, 2021-12-31, REEL_SIMPLIFIE_AGRICOLE",
            "liasse-2139_2.pdf,                 524166816, 2016-08-31, REEL_SIMPLIFIE_AGRICOLE",
            "liasse-2145-réel-agricole.pdf,     348614793, 2014-06-30, REEL_NORMAL_AGRICOLE",
            "liasse-publique-atlantic-2023.pdf, 562053173, 2023-12-31, REEL_NORMAL",
            "liasse-publique-jo-352383855.pdf,  352383855, 2019-12-31, REEL_NORMAL",
            "liasse-publique-jo-776550766.pdf,  776550766, 2024-04-30, REEL_NORMAL",
            "liasse-publique-res-2018.pdf,      423379338, 2018-10-31, REEL_NORMAL" })
    void lire_identificationDuDeclarant(String document, String siren, LocalDate cloture, RegimeImposition regime)
            throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier(document));

        assertEquals(siren, liasse.getSiren());
        assertEquals(cloture, liasse.getClotureExercice());
        assertEquals(regime, liasse.getRegime());
    }

    @Test
    void lire_bilanActif() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2050_1.pdf"));

        assertEquals("303195192", liasse.getSiren());
        assertEquals(RegimeImposition.REEL_NORMAL, liasse.getRegime());
        assertEquals(LocalDate.of(2019, 12, 31), liasse.getClotureExercice());
        assertEquals(2040287.0, liasse.getMontant("BJ").orElseThrow());
        assertEquals(1302306.0, liasse.getMontant("BK").orElseThrow());
        assertEquals(4905816.0, liasse.getMontant("CO").orElseThrow());
        assertEquals(181997.0, liasse.getMontant("HN").orElseThrow());
        assertTrue(liasse.getScoreFiabilite() > 0.9);
    }

    @Test
    void lire_liassePubliqueComplete() throws IOException {
        // Liasse TDFC complète (2050 à 2059-E), déposée au Journal officiel
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-publique-jo-776550766.pdf"));

        assertEquals("776550766", liasse.getSiren());
        assertEquals(LocalDate.of(2024, 4, 30), liasse.getClotureExercice());
        assertEquals(RegimeImposition.REEL_NORMAL, liasse.getRegime());
        assertEquals(13, liasse.getFormulaires().size());

        // Valeurs relevées dans le document
        assertEquals(6089763.0, liasse.getMontant("CO").orElseThrow(), "total général brut de l'actif");
        assertEquals(623108.0, liasse.getMontant("1A").orElseThrow(), "total des amortissements");
        assertEquals(5466655.0, liasse.getMontant("EE").orElseThrow(), "total du passif");
        assertEquals(-27184.0, liasse.getMontant("HN").orElseThrow(), "résultat de l'exercice");
        assertEquals(1675478.0, liasse.getMontant("FL").orElseThrow(), "chiffre d'affaires net");
    }

    @Test
    void lire_bilanSimplifie() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2033.pdf"));

        assertEquals("437641699", liasse.getSiren());
        assertEquals(RegimeImposition.REEL_SIMPLIFIE, liasse.getRegime());
        assertEquals(LocalDate.of(2022, 12, 31), liasse.getClotureExercice());
        assertEquals(2911114.0, liasse.getMontant("110").orElseThrow());
        assertEquals(32718.0, liasse.getMontant("310").orElseThrow());
    }

    @Test
    void lire_beneficeAgricole() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2139_2.pdf"));

        assertEquals("524166816", liasse.getSiren());
        assertEquals(RegimeImposition.REEL_SIMPLIFIE_AGRICOLE, liasse.getRegime());
        assertEquals(LocalDate.of(2016, 8, 31), liasse.getClotureExercice());
        assertEquals(474266.0, liasse.getMontant("BR").orElseThrow());
        assertEquals(44290.0, liasse.getMontant("FJ").orElseThrow());
    }

    @Test
    void lire_reelNormalAgricole() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2145-réel-agricole.pdf"));

        assertEquals("348614793", liasse.getSiren());
        assertEquals(RegimeImposition.REEL_NORMAL_AGRICOLE, liasse.getRegime());
        assertEquals(LocalDate.of(2014, 6, 30), liasse.getClotureExercice());
    }

    @Test
    void lire_reperesImprimesEnImage() throws IOException {
        // Les codes des repères de cette liasse sont des images : les montants sont
        // rattachés aux libellés des lignes, avec une confiance moindre
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2050_2.pdf"));

        assertEquals(1033701.0, liasse.getMontant("CX").orElseThrow());
        assertEquals(30511542.0, liasse.getMontant("AF").orElseThrow());
        assertEquals(MethodeExtraction.LIBELLE, liasse.getMontantExtrait("AF").orElseThrow().getMethode());
        assertFalse(liasse.getMontantExtrait("AF").orElseThrow().estFiable());
    }

    @Test
    void lire_annexes() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2050_2.pdf"));

        Annexe provisions = liasse.getAnnexe(NatureAnnexe.PROVISIONS);
        assertEquals(2, provisions.getLignes().size());
        assertEquals("Créances rattachées à des participations", provisions.getCellule(0, 0));

        Annexe exceptionnels = liasse.getAnnexe(NatureAnnexe.PRODUITS_ET_CHARGES_EXCEPTIONNELS);
        assertEquals(13, exceptionnels.getLignes().size());
        assertEquals("Valeur comptable des immobilisations incorporelles cédées", exceptionnels.getCellule(0, 0));

        Annexe reintegrations = liasse.getAnnexe(NatureAnnexe.REINTEGRATIONS);
        assertEquals(4, reintegrations.getLignes().size());
        assertEquals("CICE", reintegrations.getCellule(3, 0));
    }

    @Test
    void lire_depuisUnFlux() throws IOException {
        try (InputStream flux = Files.newInputStream(Fixtures.fichier("liasse-2050_1.pdf"))) {
            LiasseFiscale liasse = LiasseFiscaleHelper.lire(flux, OptionsLecture.defaut());
            assertEquals("303195192", liasse.getSiren());
        }
    }

    @Test
    void lire_ecritureDesFichiersDeDiagnostic(@TempDir Path repertoire) throws IOException {
        LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2050_1.pdf"),
                OptionsLecture.builder().repertoireDiagnostic(repertoire).build());

        assertTrue(Files.exists(repertoire.resolve("liasse-2050_1.csv")));
        assertTrue(Files.exists(repertoire.resolve("liasse-2050_1-montants.csv")));
        assertTrue(Files.exists(repertoire.resolve("liasse-2050_1-controles.csv")));
        assertTrue(Files.readString(repertoire.resolve("liasse-2050_1-montants.csv")).contains("CO,4905816.00"));
    }

    @Test
    void readLiasseFiscalePDF_apiHistorique() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper
                .readLiasseFiscalePDF("target/test-classes/liasse-2050_3.pdf");

        assertNotNull(liasse);
        assertEquals("529770646", liasse.getSiren());
        assertEquals(LocalDate.of(2017, 12, 31), liasse.getClotureExercice());
    }

    @Test
    void parseSiren() {
        // Bloc de texte multiligne
        assertEquals(Optional.of("303195192"), LiasseFiscaleHelper.parseSiren(
                "2\r\nNuméro SIRET*  30319519200032*Néant\r\nExercice N clos le,31122019Brut\r\n"));

        // Retour à la ligne entre le libellé et la valeur
        assertEquals(Optional.of("303195192"), LiasseFiscaleHelper.parseSiren("Numéro SIRET*  \r\n30319519200032"));

        // SIREN et pas SIRET
        assertEquals(Optional.of("303195192"), LiasseFiscaleHelper.parseSiren("Numéro SIREN*  30319519200032"));

        // Chiffres saisis dans des cases pré-imprimées
        assertEquals(Optional.of("303195192"),
                LiasseFiscaleHelper.parseSiren("N u m é r o S I R E N *  3 0 3 1 9 5 1 9 2 0 0 0 3 2"));

        // Pas de numéro SIREN renseigné
        assertEquals(Optional.of(""),
                LiasseFiscaleHelper.parseSiren("*Numéro SIRET*Néant \r\nExercice N clos le,31/12/2022"));

        // Pas de champ SIREN
        assertEquals(Optional.empty(), LiasseFiscaleHelper.parseSiren(""));
    }

    @Test
    void estSirenValide() {
        assertTrue(LiasseFiscaleHelper.estSirenValide("303195192"));
        assertTrue(LiasseFiscaleHelper.estSirenValide("356000000"));
        assertFalse(LiasseFiscaleHelper.estSirenValide("303195193"));
        assertFalse(LiasseFiscaleHelper.estSirenValide("30319519"));
        assertFalse(LiasseFiscaleHelper.estSirenValide(null));
    }

    @Test
    void parseClotureExercice() {
        assertTrue(LiasseFiscaleHelper.parseClotureExercice("").isEmpty());

        // Bloc de texte multiligne
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper.parseClotureExercice(
                "2\r\nNuméro SIRET*  30319519200032*Néant\r\nExercice N clos le,31122019Brut\r\n"));

        // Retour à la ligne entre le libellé et la valeur
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)),
                LiasseFiscaleHelper.parseClotureExercice("Exercice N clos le,\r\n31122019"));

        // Séparateurs de date
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)),
                LiasseFiscaleHelper.parseClotureExercice("Exercice N clos le,\r\n31/12/2019"));

        // Espaces entre les caractères
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper
                .parseClotureExercice("E x e r c i c e   N   c l o s   l e   , \r\n 3 1 1 2 2 0 1 9"));

        // Exercices N et N-1 dans la même cellule
        assertEquals(Optional.of(LocalDate.of(2021, 12, 31)), LiasseFiscaleHelper
                .parseClotureExercice("Exercice N, clos le :31/12/2021Exercice N-1, clos le :08/11/2020"));

        // Date de clôture sur six positions
        assertEquals(Optional.of(LocalDate.of(2016, 8, 31)),
                LiasseFiscaleHelper.parseClotureExercice("EXERCICE CLOS LE 310816"));
    }

    @Test
    void parseNumber() {
        assertEquals(0.0, LiasseFiscaleHelper.parseNumber(""));
        assertEquals(1.0, LiasseFiscaleHelper.parseNumber("1"));
        assertEquals(1000.25, LiasseFiscaleHelper.parseNumber("1 000.25"));
        assertEquals(-1000.0, LiasseFiscaleHelper.parseNumber(" (1 000)"));
        assertEquals(0.0, LiasseFiscaleHelper.parseNumber(" (0)"));
    }

    @Test
    void parseDate() {
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper.parseDate("31/12/2019"));
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper.parseDate("31-12-2019"));
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper.parseDate("2019/12/31"));
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper.parseDate("2019-12-31"));
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper.parseDate("31/12/19"));
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper.parseDate("311219"));
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper.parseDate("31122019"));
        assertEquals(Optional.of(LocalDate.of(2019, 12, 31)), LiasseFiscaleHelper.parseDate(" 31/12/2019 a"));
        assertEquals(Optional.of(LocalDate.of(2021, 12, 31)),
                LiasseFiscaleHelper.parseDate("31/12/2021ExerciceN-1closle08/11/2020"));
    }
}
