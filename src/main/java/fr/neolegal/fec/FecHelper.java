package fr.neolegal.fec;

import java.nio.file.Path;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import fr.neolegal.fec.liassefiscale.AgregationComptes;

public abstract class FecHelper {

    static String FEC_FILENAME_SEPARATOR = "FEC";
    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private FecHelper() {
    }

    static LocalDate parseDate(String value) {
        return StringUtils.isBlank(value) ? null : LocalDate.parse(value, dateFormatter);
    }

    /**
     * Lit un montant du fichier des écritures comptables.
     * <p>
     * La norme impose deux décimales et n'autorise pas de séparateur de milliers :
     * la virgule comme le point sont donc des séparateurs décimaux. Les espaces
     * (y compris insécables) et le signe en fin de nombre sont tolérés.
     */
    static Double parseDouble(String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String nombre = value.replaceAll("[\\s\\u00a0\\u202f]", "").replace(',', '.');
        boolean negatif = nombre.endsWith("-");
        if (negatif) {
            nombre = nombre.substring(0, nombre.length() - 1);
        }
        if (nombre.isEmpty()) {
            return null;
        }
        try {
            double montant = Double.parseDouble(nombre);
            return negatif ? -montant : montant;
        } catch (NumberFormatException e) {
            throw new ParseException(String.format("Montant illisible : %s", value), 0);
        }
    }

    /**
     * IX. – Le fichier des écritures comptables est nommé selon la nomenclature
     * suivante :
     * SirenFECAAAAMMJJ, où " Siren " est le Siren du contribuable mentionné à
     * l'article L. 47 A et AAAAMMJJ la date de clôture de l'exercice comptable.
     */
    public static Optional<LocalDate> parseClotureExercice(String filename) {
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
    public static Optional<String> parseSiren(String filename) {
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
                .mapToDouble(ecriture -> credit ? ecriture.getCreditOuZero() : ecriture.getDebitOuZero()).sum();
    }

    /**
     * Le numéro d'écriture n'est unique qu'au sein d'un journal : les écritures
     * sont donc dénombrées par couple (journal, numéro).
     */
    static long countEcritures(List<LEC> lignes) {
        return CollectionUtils.emptyIfNull(lignes).stream()
                .map(ecriture -> StringUtils.defaultString(ecriture.getJournalCode()) + '\u0000'
                        + StringUtils.defaultString(ecriture.getEcritureNum()))
                .distinct().count();
    }

    public static Set<String> resolveJournaux(List<LEC> lignes) {
        return CollectionUtils.emptyIfNull(lignes).stream().map(ligne -> ligne.getJournalCode()).distinct()
                .collect(Collectors.toSet());
    }

    /**
     * Agrège les soldes des comptes désignés par l'agrégation.
     *
     * @see SoldesComptes
     */
    public static double computeAgregationComptes(List<LEC> lignes, AgregationComptes agregation) {
        if (lignes.isEmpty()) {
            return 0.0;
        }
        return new SoldesComptes(lignes).agreger(agregation);
    }

    /**
     * Agrège les soldes des comptes désignés par l'agrégation, en s'appuyant sur
     * les soldes déjà calculés du fichier.
     */
    public static double computeAgregationComptes(Fec fec, AgregationComptes agregation) {
        return fec == null ? 0.0 : fec.getSoldes().agreger(agregation);
    }

    /** DAns le cas où le nom du fichier physique ne correspond pas au nom original du fichier,
     * ce dernier est passé en paramètre pour pouvoir en extraire le SIREN et la date de clôture
     */
    public static Fec read(Path file, String originalFileName) {
        FecReader reader = new FecReader();
        try {
            return reader.read(file, originalFileName);
        } catch (Exception e) {
            Logger.getLogger(FecHelper.class.getName()).log(Level.SEVERE,
                    String.format("Lecture du fichier %s impossible : %s", file, e.getMessage()));
            return null;
        }
    }

    public static Fec read(Path file) {        
        return read(file, file.getFileName().toString());
    }
}
