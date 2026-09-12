package fr.neolegal.fec.liassefiscale;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** Accès aux documents de test et à leurs montants attendus. */
public class Fixtures {

    /** Les ressources de test sont recopiées dans le répertoire de compilation */
    private static final Path RESSOURCES = Path.of("target/test-classes");

    private Fixtures() {
    }

    public static Path fichier(String nom) {
        return RESSOURCES.resolve(nom);
    }

    /** Liasses au format PDF accompagnées d'un fichier de montants attendus. */
    public static List<Path> liassesPdf() {
        try (Stream<Path> fichiers = Files.list(RESSOURCES)) {
            List<Path> liasses = new ArrayList<>(fichiers
                    .filter(fichier -> fichier.getFileName().toString().endsWith(".pdf"))
                    .filter(fichier -> Files.exists(montantsAttendusPath(fichier)))
                    .sorted().toList());
            return liasses;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path montantsAttendusPath(Path liasse) {
        return Path.of(liasse.toString().replaceAll("\\.pdf$", "-expected.csv"));
    }

    /** Montants attendus pour une liasse, par symbole de repère. */
    public static Map<String, Double> montantsAttendus(Path liasse) {
        return lireMontants(montantsAttendusPath(liasse));
    }

    /** Montants attendus décrits par un fichier {@code -expected.csv}. */
    public static Map<String, Double> lireMontants(Path fichier) {
        Map<String, Double> montants = new LinkedHashMap<>();
        try {
            for (String ligne : Files.readAllLines(fichier, StandardCharsets.UTF_8)) {
                String[] colonnes = ligne.trim().split(",");
                if (colonnes.length >= 2) {
                    montants.put(colonnes[0], Double.parseDouble(colonnes[1]));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return montants;
    }
}
