package fr.neolegal.fec.liassefiscale.pdf;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * Moteur d'OCR s'appuyant sur l'exécutable <a href=
 * "https://github.com/tesseract-ocr/tesseract">tesseract</a> installé sur la
 * machine. Il est utilisé si l'exécutable est présent dans le PATH (ou désigné
 * par la propriété système {@code fec.tesseract.path}).
 * <p>
 * Le format de sortie TSV fournit la position et le taux de confiance de chaque
 * mot, ce qui permet de pondérer la fiabilité des montants reconnus.
 */
public class MoteurOcrTesseract implements MoteurOcr {

    private static final Logger LOGGER = Logger.getLogger(MoteurOcrTesseract.class.getName());

    private final String executable;
    private final String langue;

    public MoteurOcrTesseract() {
        this(System.getProperty("fec.tesseract.path", "tesseract"), "fra");
    }

    public MoteurOcrTesseract(String executable, String langue) {
        this.executable = executable;
        this.langue = langue;
    }

    @Override
    public boolean estDisponible() {
        try {
            Process process = new ProcessBuilder(executable, "--version").redirectErrorStream(true).start();
            boolean termine = process.waitFor(10, TimeUnit.SECONDS);
            return termine && process.exitValue() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public List<MotPdf> reconnaitre(BufferedImage image, int page, float echelle) {
        Path repertoire = null;
        try {
            repertoire = Files.createTempDirectory("fec-ocr");
            File source = repertoire.resolve("page.png").toFile();
            ImageIO.write(image, "png", source);
            String base = repertoire.resolve("page").toString();

            Process process = new ProcessBuilder(executable, source.getAbsolutePath(), base,
                    "-l", langue, "--psm", "6", "tsv").redirectErrorStream(true).start();
            if (!process.waitFor(120, TimeUnit.SECONDS) || process.exitValue() != 0) {
                LOGGER.log(Level.WARNING, "Échec de la reconnaissance optique de la page {0}", page);
                return List.of();
            }
            return parseTsv(Files.readString(Path.of(base + ".tsv"), StandardCharsets.UTF_8), page, echelle);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Reconnaissance optique impossible : " + e.getMessage());
            return List.of();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return List.of();
        } finally {
            supprimer(repertoire);
        }
    }

    /** Convertit la sortie TSV de tesseract en mots positionnés. */
    static List<MotPdf> parseTsv(String tsv, int page, float echelle) {
        List<MotPdf> mots = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(tsv))) {
            String ligne = reader.readLine(); // en-tête
            while ((ligne = reader.readLine()) != null) {
                String[] champs = ligne.split("\t");
                if (champs.length < 12) {
                    continue;
                }
                String texte = champs[11].trim();
                if (texte.isEmpty()) {
                    continue;
                }
                float gauche = Float.parseFloat(champs[6]) * echelle;
                float haut = Float.parseFloat(champs[7]) * echelle;
                float largeur = Float.parseFloat(champs[8]) * echelle;
                float hauteur = Float.parseFloat(champs[9]) * echelle;
                mots.add(new MotPdf(texte, gauche, gauche + largeur, haut, haut + hauteur, hauteur * 0.5f, page));
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Sortie TSV illisible : " + e.getMessage());
        }
        return mots;
    }

    private static void supprimer(Path repertoire) {
        if (repertoire == null) {
            return;
        }
        try (var fichiers = Files.walk(repertoire)) {
            fichiers.sorted((a, b) -> b.getNameCount() - a.getNameCount()).forEach(fichier -> {
                try {
                    Files.deleteIfExists(fichier);
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Fichier temporaire non supprimé : {0}", fichier);
                }
            });
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Répertoire temporaire non supprimé : {0}", repertoire);
        }
    }
}
