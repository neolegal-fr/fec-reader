package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Mesure la qualité de l'extraction sur l'ensemble des liasses de test et
 * garantit l'absence de régression.
 * <p>
 * Le seuil retenu pour chaque document est le nombre de montants correctement
 * lus à la date de la dernière mise à jour de ce test : une modification du
 * lecteur qui dégrade l'un d'eux fait échouer la construction.
 */
class QualiteExtractionTest {

    /** Nombre minimal de montants non nuls correctement lus, par document */
    private static final Map<String, Integer> SEUILS = new LinkedHashMap<>();
    static {
        SEUILS.put("liasse-2033.pdf", 63);
        SEUILS.put("liasse-2050_1.pdf", 89);
        SEUILS.put("liasse-2050_2.pdf", 3);
        SEUILS.put("liasse-2050_3.pdf", 63);
        SEUILS.put("liasse-2050_4.pdf", 13);
        SEUILS.put("liasse-2050_5.pdf", 7);
        SEUILS.put("liasse-2050_6.pdf", 21);
        // Document dépourvu de couche texte : illisible sans reconnaissance optique
        SEUILS.put("liasse-2050_7.pdf", 0);
        SEUILS.put("liasse-2050_8.pdf", 97);
        SEUILS.put("liasse-2050_9.pdf", 149);
        SEUILS.put("liasse-2072-S-SD.pdf", 4);
        SEUILS.put("liasse-2139_1.pdf", 18);
        SEUILS.put("liasse-2139_2.pdf", 57);
        SEUILS.put("liasse-2145-réel-agricole.pdf", 19);
        SEUILS.put("liasse-publique-atlantic-2023.pdf", 229);
        SEUILS.put("liasse-publique-jo-332108877.pdf", 150);
        SEUILS.put("liasse-publique-union-champagne-2023.pdf", 81);
        SEUILS.put("liasse-publique-jo-352383855.pdf", 141);
        SEUILS.put("liasse-publique-jo-776550766.pdf", 153);
    }

    /** Nombre maximal de montants lus avec une valeur erronée, tous documents confondus */
    private static final int MAXIMUM_ERREURS = 1;

    @Test
    void qualiteExtraction() throws IOException {
        StringBuilder rapport = new StringBuilder(
                String.format("%-40s %6s %6s %6s %8s%n", "document", "justes", "faux", "absents", "fiabilité"));
        int totalErreurs = 0;
        boolean regression = false;

        for (Path liasse : Fixtures.liassesPdf()) {
            LiasseFiscale lue = LiasseFiscaleHelper.lire(liasse);
            int justes = 0;
            int faux = 0;
            int absents = 0;
            for (Map.Entry<String, Double> attendu : Fixtures.montantsAttendus(liasse).entrySet()) {
                if (attendu.getValue() == 0.0 || lue.getRepere(attendu.getKey()).isEmpty()) {
                    // Les cellules vides et les repères absents des modèles de formulaires
                    // ne renseignent pas sur la qualité de la lecture
                    continue;
                }
                Optional<Double> montant = lue.getMontant(attendu.getKey());
                if (montant.isEmpty()) {
                    ++absents;
                } else if (Math.abs(montant.get() - attendu.getValue()) < 0.5) {
                    ++justes;
                } else {
                    ++faux;
                }
            }

            String nom = liasse.getFileName().toString();
            totalErreurs += faux;
            int seuil = SEUILS.getOrDefault(nom, 0);
            regression |= justes < seuil;
            rapport.append(String.format("%-40s %6d %6d %6d %7.0f%%%s%n", nom, justes, faux, absents,
                    lue.getScoreFiabilite() * 100, justes < seuil ? " RÉGRESSION, attendu " + seuil : ""));
        }

        rapport.append(String.format("%d montants erronés (maximum toléré : %d)%n", totalErreurs, MAXIMUM_ERREURS));
        assertTrue(!regression && totalErreurs <= MAXIMUM_ERREURS, rapport.toString());
    }
}
