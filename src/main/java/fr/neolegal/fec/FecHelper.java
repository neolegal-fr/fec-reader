package fr.neolegal.fec;

import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import fr.neolegal.fec.liassefiscale.AgregationComptes;

public abstract class FecHelper {

    static String FEC_FILENAME_SEPARATOR = "FEC";
    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    static NumberFormat numberFormat = NumberFormat.getInstance(Locale.FRANCE);

    private FecHelper() {
    }

    static LocalDate parseDate(String value) {
        return StringUtils.isBlank(value) ? null : LocalDate.parse(value, dateFormatter);
    }

    static Double parseDouble(String value) throws ParseException {
        return StringUtils.isBlank(value) ? null : numberFormat.parse(value).doubleValue();
    }

    /**
     * IX. – Le fichier des écritures comptables est nommé selon la nomenclature
     * suivante :
     * SirenFECAAAAMMJJ, où " Siren " est le Siren du contribuable mentionné à
     * l'article L. 47 A et AAAAMMJJ la date de clôture de l'exercice comptable.
     */
    static Optional<LocalDate> parseClotureExercice(String filename) {
        filename = FilenameUtils.removeExtension(filename);

        if (StringUtils.isBlank(filename)) {
            return Optional.empty();
        }

        String[] parts = filename.split("(?i)" + FEC_FILENAME_SEPARATOR);
        if (parts.length != 2) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDate.parse(parts[1], dateFormatter));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * IX. – Le fichier des écritures comptables est nommé selon la nomenclature
     * suivante :
     * SirenFECAAAAMMJJ, où " Siren " est le Siren du contribuable mentionné à
     * l'article L. 47 A et AAAAMMJJ la date de clôture de l'exercice comptable.
     */
    static Optional<String> parseSiren(String filename) {
        if (StringUtils.isBlank(filename)) {
            return Optional.empty();
        }

        String[] parts = filename.split("(?i)" + FEC_FILENAME_SEPARATOR);
        if (parts.length != 2) {
            return Optional.empty();
        }

        return Optional.of(parts[0]);
    }

    static double computeTotalJournal(List<LEC> lignes, String journalCode, boolean credit) {
        return CollectionUtils.emptyIfNull(lignes).stream().filter(
                ecriture -> StringUtils.equalsIgnoreCase(ecriture.getJournalCode(), journalCode))
                .mapToDouble(ecriture -> credit ? ecriture.getCredit() : ecriture.getDebit()).sum();
    }

    static long countEcritures(List<LEC> lignes) {
        return CollectionUtils.emptyIfNull(lignes).stream().map(ecriture -> ecriture.getEcritureNum()).distinct()
                .count();
    }

    public static Set<String> resolveJournaux(List<LEC> lignes) {
        return CollectionUtils.emptyIfNull(lignes).stream().map(ligne -> ligne.getJournalCode()).distinct()
                .collect(Collectors.toSet());
    }

    public static double computeAgregationComptes(List<LEC> lignes, AgregationComptes agregation) {
        if (lignes.isEmpty()) {
            return 0.0;
        }

        /**
         * Livre des procédures fiscales : Section III : Modalités d'exercice du droit
         * de contrôle:
         * Pour chaque exercice, les premiers numéros d'écritures comptables du fichier
         * correspondent aux écritures de reprise des soldes de l'exercice antérieur
         */
        String numEcritureRepriseSolde = lignes.stream().findFirst().map(lec -> lec.getEcritureNum()).orElse("");

        /**
         * Lors du calcul de la variation d'un compte, on doit ignorer la première ligne
         * du fichier, qui reprend le solde de l'exercice précédent
         */
        Map<String, Double> comptes = new HashMap<>();
        for (LEC ligne : lignes) {
            boolean includeLigne = agregation.matches(ligne.getCompteNum())
                    && (agregation.getAgregateur().isRepriseSoldeIncluded()
                            || !StringUtils.equalsIgnoreCase(numEcritureRepriseSolde, ligne.getEcritureNum()));
            if (includeLigne) {
                comptes.put(ligne.getCompteNum(), comptes.getOrDefault(ligne.getCompteNum(), 0.0)
                        + (ligne.getCredit() - ligne.getDebit()));
            }
        }

        switch (agregation.getAgregateur()) {
            case CREDIT:
                comptes = comptes.entrySet().stream().filter(entry -> entry.getValue() > 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                break;
            case DEBIT:
                comptes = comptes.entrySet().stream().filter(entry -> entry.getValue() < 0)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                break;
            case DIFFERENCE:
            case SOLDE:
            default:
                break;

        }
        return comptes.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    public static Fec read(Path file) {
        FecReader reader = new FecReader();
        try {
            return reader.read(file);
        } catch (Exception e) {
            return null;
        }
    }
}
