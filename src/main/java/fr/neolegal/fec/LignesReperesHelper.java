package fr.neolegal.fec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class LignesReperesHelper {
    
    /* Décrits pour chaque ligne repère de la liasse fiscale les numéros de comptes à utiliser poru son calcul, ou les autres numéro de lignes sur lesquelles elle s'appuie 
     * D'après https://www.lexisnexis.fr/__data/assets/pdf_file/0006/642363/rdi1903.pdf
    */
    static Map<String, Set<String>> dependances = buildDependances();

    private LignesReperesHelper() {
    }

    static Map<String, Set<String>> buildDependances() {
        Map<String, Set<String>> dependances = new HashMap<>();
        dependances.put("DA", Set.of("101", "108"));
        dependances.put("DB", Set.of("104"));
        dependances.put("DC", Set.of("105"));
        dependances.put("DD", Set.of("1061"));
        dependances.put("DE", Set.of("1063"));
        dependances.put("DF", Set.of("1062", "1064" ));
        dependances.put("DG", Set.of("1068"));
        dependances.put("DH", Set.of("11"));
        dependances.put("DI", Set.of("12"));
        dependances.put("DJ", Set.of("13"));
        dependances.put("DK", Set.of("14"));
        dependances.put("DL", Set.of("DA", "DB", "DC", "DD", "DE", "DF", "DG", "DH", "DI", "DJ", "DK"));
        dependances.put("DM", Set.of("1671"));
        dependances.put("DN", Set.of("1674"));
        dependances.put("DO", Set.of("DM", "DN"));
        dependances.put("DP", Set.of("151"));
        dependances.put("DQ", Set.of("153", "154", "155", "156", "157", "158"));
        dependances.put("DR", Set.of("DP", "DQ"));
        dependances.put("DS", Set.of());
        dependances.put("DT", Set.of());
        dependances.put("DU", Set.of());
        dependances.put("DV", Set.of());
        dependances.put("DW", Set.of());
        dependances.put("DX", Set.of());
        dependances.put("DY", Set.of());
        dependances.put("DZ", Set.of());
        dependances.put("EA", Set.of());
        dependances.put("EB", Set.of("487"));
        dependances.put("EC", Set.of("DS", "DT", "DU", "DV", "DW", "DX", "DY", "DZ", "EA", "EB"));
        dependances.put("ED", Set.of("477"));
        dependances.put("EE", Set.of("DL", "DO", "DR", "EC", "ED"));
        dependances.put("FA", Set.of("707"));
        dependances.put("FB", Set.of("708"));
        dependances.put("FC", Set.of("7097"));
        dependances.put("FD", Set.of("701", "702", "703"));
        dependances.put("FE", Set.of("7091"));
        dependances.put("FF", Set.of("7092"));
        dependances.put("FJ", Set.of("FA", "FD", "FG"));
        dependances.put("FK", Set.of("FB", "FE", "FH"));
        dependances.put("FL", Set.of("FJ", "FK"));
        dependances.put("FM", Set.of("71"));
        dependances.put("FN", Set.of("72"));
        dependances.put("FO", Set.of("74"));
        dependances.put("FP", Set.of("781", "791"));
        dependances.put("FQ", Set.of("75"));
        dependances.put("FR", Set.of("FL", "FM", "FN", "FO", "FP", "FQ"));
        dependances.put("FS", Set.of("607", "6097"));
        dependances.put("FT", Set.of("6037"));
        dependances.put("FU", Set.of("601", "602", "6091", "6092"));
        dependances.put("FV", Set.of("6031", "6032"));
        dependances.put("FW", Set.of("604", "605", "606", "6094", "6095", "6096", "61", "62"));
        dependances.put("FX", Set.of("63"));
        dependances.put("FY", Set.of("641", "644", "648"));
        dependances.put("FZ", Set.of("645", "646", "647", "648"));
        dependances.put("GA", Set.of("6811", "6812"));
        dependances.put("GB", Set.of("6816"));
        dependances.put("GC", Set.of("6817"));
        dependances.put("GD", Set.of("6815"));
        dependances.put("GE", Set.of("651", "653", "654", "658"));
        dependances.put("GF", Set.of("FS", "FT", "FU", "FV", "FW", "FX", "FY", "FZ", "GA", "GB", "GC", "GD", "GE"));
        return dependances;
    }
    
    public static Set<String> resolveComptes(String repereLigneOrNumeroCompte) {
        if (StringUtils.isNumeric(repereLigneOrNumeroCompte)) {
            return Set.of(repereLigneOrNumeroCompte);
        }

        Set<String> resultat = dependances.getOrDefault(repereLigneOrNumeroCompte, Set.of());
        return resultat.stream().flatMap(code -> resolveComptes(code).stream()).collect(Collectors.toSet());
    }
}
