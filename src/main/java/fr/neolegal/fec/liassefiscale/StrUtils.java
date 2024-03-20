package fr.neolegal.fec.liassefiscale;

import java.text.Normalizer;

import org.apache.commons.lang3.StringUtils;

public class StrUtils {
    public static String stripAccents(String input) {
        return input == null ? null
                : Normalizer.normalize(input, Normalizer.Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    public static boolean containsIgnoreCase(String str, String search) {
        if (str == null || search == null) {
            return false;
        }

        str = stripAccents(str);
        return StringUtils.containsIgnoreCase(str, stripAccents(search));
    }

    public static boolean containsAnyIgnoreCase(String str, String... searchStrs) {
        if (str == null || searchStrs == null) {
            return false;
        }

        str = stripAccents(str);
        for (String search : searchStrs) {
            if (StringUtils.containsIgnoreCase(str, stripAccents(search))) {
                return true;
            }
        }
        return false;
    }
}
