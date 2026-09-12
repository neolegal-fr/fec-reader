package fr.neolegal.fec.liassefiscale.pdf;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Reconnaissance optique de caractères, utilisée en dernier recours pour les
 * pages dépourvues de couche texte (liasses scannées, PDF images).
 * <p>
 * L'implémentation est laissée au choix de l'intégrateur : moteur local
 * ({@link MoteurOcrTesseract}) ou service externe (Mistral OCR, DataLeon,
 * Azure Document Intelligence...). Les implémentations déclarées dans
 * {@code META-INF/services/fr.neolegal.fec.liassefiscale.pdf.MoteurOcr} sont
 * découvertes automatiquement.
 */
public interface MoteurOcr {

    /**
     * Reconnaît les mots présents dans l'image d'une page.
     *
     * @param image    image de la page, rendue à {@code resolution} points par
     *                 pouce
     * @param page     numéro de la page dans le document, à partir de 1
     * @param echelle  facteur de conversion des pixels de l'image vers les points
     *                 PDF (72 / resolution) : les mots renvoyés doivent être
     *                 positionnés en points PDF
     * @return les mots reconnus, positionnés dans le repère de la page PDF
     */
    List<MotPdf> reconnaitre(BufferedImage image, int page, float echelle);

    /** Indique si le moteur est utilisable dans l'environnement courant. */
    default boolean estDisponible() {
        return true;
    }

    /** Résolution de rendu des pages, en points par pouce. */
    default int getResolution() {
        return 300;
    }

    /**
     * Confiance moyenne accordée aux montants lus par ce moteur, entre 0 et 1.
     * Elle pondère le score de fiabilité des valeurs extraites par OCR.
     */
    default double getFiabilite() {
        return 0.75;
    }
}
