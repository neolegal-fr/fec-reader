package fr.neolegal.fec;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

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

}
