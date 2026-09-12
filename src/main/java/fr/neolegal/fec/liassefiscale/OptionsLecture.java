package fr.neolegal.fec.liassefiscale;

import java.nio.file.Path;
import java.util.Optional;
import java.util.ServiceLoader;

import fr.neolegal.fec.liassefiscale.pdf.MoteurOcr;
import fr.neolegal.fec.liassefiscale.pdf.MoteurOcrTesseract;
import lombok.Builder;
import lombok.Getter;

/** Options de lecture d'une liasse fiscale au format PDF. */
@Getter
@Builder(toBuilder = true)
public class OptionsLecture {

    private static final OptionsLecture DEFAUT = OptionsLecture.builder().build();

    /**
     * Moteur de reconnaissance optique utilisé pour les pages sans couche texte.
     * Si aucun moteur n'est fourni, le premier moteur déclaré via
     * {@link ServiceLoader} est utilisé, à défaut {@link MoteurOcrTesseract} s'il
     * est installé sur la machine.
     */
    private final MoteurOcr moteurOcr;

    /** Autorise le recours à la reconnaissance optique (activée par défaut). */
    @Builder.Default
    private final boolean ocrAutorise = true;

    /**
     * Répertoire dans lequel écrire les fichiers de diagnostic (rendu des pages
     * analysées, montants extraits et contrôles de cohérence). Aucun fichier n'est
     * écrit si le répertoire n'est pas renseigné.
     */
    private final Path repertoireDiagnostic;

    /** Exécute les contrôles de cohérence comptable (activés par défaut). */
    @Builder.Default
    private final boolean controlesCoherence = true;

    /**
     * Extrait le contenu des annexes libres (détail des réintégrations, des
     * provisions...), activé par défaut.
     */
    @Builder.Default
    private final boolean extractionAnnexes = true;

    public static OptionsLecture defaut() {
        return DEFAUT;
    }

    /** Résout le moteur d'OCR à utiliser, s'il en existe un d'utilisable. */
    public Optional<MoteurOcr> resoudreMoteurOcr() {
        if (!ocrAutorise) {
            return Optional.empty();
        }
        if (moteurOcr != null) {
            return Optional.of(moteurOcr);
        }
        for (MoteurOcr moteur : ServiceLoader.load(MoteurOcr.class)) {
            if (moteur.estDisponible()) {
                return Optional.of(moteur);
            }
        }
        MoteurOcr tesseract = new MoteurOcrTesseract();
        return tesseract.estDisponible() ? Optional.of(tesseract) : Optional.empty();
    }
}
