package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.neolegal.fec.liassefiscale.pdf.MotPdf;
import fr.neolegal.fec.liassefiscale.pdf.MoteurOcr;

/**
 * Vérifie le recours à la reconnaissance optique pour les pages dépourvues de
 * couche texte, au moyen d'un moteur d'OCR simulé : le document utilisé est une
 * liasse dont les polices ne comportent pas de table de correspondance vers
 * l'unicode, et dont le texte est donc illisible autrement.
 */
class LectureOcrTest {

    /** Restitue une page 2050 simplifiée, quelle que soit l'image reçue. */
    private static class MoteurOcrSimule implements MoteurOcr {

        int appels = 0;

        @Override
        public List<MotPdf> reconnaitre(BufferedImage image, int page, float echelle) {
            ++appels;
            if (page != 3) {
                return List.of();
            }
            List<MotPdf> mots = new ArrayList<>();
            mots.add(mot("BILAN - ACTIF", 200, 300, 30, page));
            mots.add(mot("N° 2050", 450, 500, 30, page));
            String[][] lignes = {
                    { "Concessions, brevets", "AF", "1 000", "AG", "100" },
                    { "Constructions", "AP", "2 000", "AQ", "200" },
                    { "Autres immobilisations", "AT", "3 000", "AU", "300" },
                    { "TOTAL (II)", "BJ", "6 000", "BK", "600" } };
            float y = 200;
            for (String[] ligne : lignes) {
                mots.add(mot(ligne[0], 50, 150, y, page));
                mots.add(mot(ligne[1], 220, 235, y, page));
                mots.add(mot(ligne[2], 310, 350, y, page));
                mots.add(mot(ligne[3], 380, 395, y, page));
                mots.add(mot(ligne[4], 470, 500, y, page));
                y += 20;
            }
            return mots;
        }

        private static MotPdf mot(String texte, float gauche, float droite, float bas, int page) {
            return new MotPdf(texte, gauche, droite, bas - 8, bas, 2.5f, page);
        }
    }

    @Test
    void lecture_sansMoteurOcr() throws IOException {
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2050_7.pdf"),
                OptionsLecture.builder().ocrAutorise(false).build());

        assertTrue(liasse.getFormulaires().isEmpty());
        assertEquals(0.0, liasse.getScoreFiabilite());
    }

    @Test
    void lecture_avecMoteurOcr() throws IOException {
        MoteurOcrSimule moteur = new MoteurOcrSimule();
        LiasseFiscale liasse = LiasseFiscaleHelper.lire(Fixtures.fichier("liasse-2050_7.pdf"),
                OptionsLecture.builder().moteurOcr(moteur).build());

        assertTrue(moteur.appels > 0, "le moteur d'OCR doit être sollicité pour les pages sans texte");
        assertEquals(1000.0, liasse.getMontant("AF").orElseThrow());
        assertEquals(600.0, liasse.getMontant("BK").orElseThrow());

        MontantExtrait montant = liasse.getMontantExtrait("AF").orElseThrow();
        assertTrue(montant.isOcr());
        assertEquals(3, montant.getPage());
        // La reconnaissance optique est moins sûre que la lecture de la couche texte
        assertTrue(montant.getConfiance() < 0.95, "confiance " + montant.getConfiance());
    }
}
