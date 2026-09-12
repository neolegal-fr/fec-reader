package fr.neolegal.fec.liassefiscale.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;

import fr.neolegal.fec.liassefiscale.OptionsLecture;
import lombok.Getter;

/**
 * Représentation d'un document PDF sous forme de pages de mots positionnés.
 * <p>
 * Les pages dépourvues de couche texte exploitable sont soumises au moteur de
 * reconnaissance optique, lorsqu'un moteur est disponible.
 */
@Getter
public class DocumentPdf {

    private static final Logger LOGGER = Logger.getLogger(DocumentPdf.class.getName());

    /** En deçà de ce nombre de mots, une page est considérée sans couche texte */
    private static final int MINIMUM_MOTS = 20;

    private final List<PagePdf> pages;
    private final boolean ocrUtilise;

    private DocumentPdf(List<PagePdf> pages, boolean ocrUtilise) {
        this.pages = Collections.unmodifiableList(pages);
        this.ocrUtilise = ocrUtilise;
    }

    public static DocumentPdf charger(PDDocument document, OptionsLecture options) throws IOException {
        Map<Integer, List<MotPdf>> motsParPage = ExtracteurMotsPdf.extraire(document);

        List<Integer> pagesSansTexte = new ArrayList<>();
        for (Map.Entry<Integer, List<MotPdf>> entree : motsParPage.entrySet()) {
            if (entree.getValue().size() < MINIMUM_MOTS) {
                pagesSansTexte.add(entree.getKey());
            }
        }

        boolean ocrUtilise = false;
        if (!pagesSansTexte.isEmpty()) {
            Optional<MoteurOcr> moteur = options.resoudreMoteurOcr();
            if (moteur.isPresent()) {
                ocrUtilise = reconnaitre(document, moteur.get(), pagesSansTexte, motsParPage);
            } else {
                LOGGER.log(Level.INFO, "{0} page(s) sans couche texte et aucun moteur OCR disponible",
                        pagesSansTexte.size());
            }
        }

        List<PagePdf> pages = new ArrayList<>();
        for (int numero = 1; numero <= document.getNumberOfPages(); numero++) {
            PDPage page = document.getPage(numero - 1);
            PDRectangle boite = page.getCropBox();
            boolean pivotee = (page.getRotation() / 90) % 2 != 0;
            float largeur = pivotee ? boite.getHeight() : boite.getWidth();
            float hauteur = pivotee ? boite.getWidth() : boite.getHeight();
            pages.add(new PagePdf(numero, largeur, hauteur, motsParPage.getOrDefault(numero, List.of()),
                    ocrUtilise && pagesSansTexte.contains(numero)));
        }

        return new DocumentPdf(pages, ocrUtilise);
    }

    private static boolean reconnaitre(PDDocument document, MoteurOcr moteur, List<Integer> pages,
            Map<Integer, List<MotPdf>> motsParPage) {
        PDFRenderer renderer = new PDFRenderer(document);
        float echelle = 72f / moteur.getResolution();
        boolean utilise = false;
        for (Integer numero : pages) {
            try {
                BufferedImage image = renderer.renderImageWithDPI(numero - 1, moteur.getResolution());
                List<MotPdf> mots = moteur.reconnaitre(image, numero, echelle);
                if (!mots.isEmpty()) {
                    motsParPage.put(numero, mots);
                    utilise = true;
                }
            } catch (IOException | RuntimeException e) {
                LOGGER.log(Level.WARNING, String.format("Reconnaissance optique de la page %d impossible : %s",
                        numero, e.getMessage()));
            }
        }
        return utilise;
    }

    public PagePdf getPage(int numero) {
        return pages.get(numero - 1);
    }
}
