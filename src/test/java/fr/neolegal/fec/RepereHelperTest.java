package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;

import fr.neolegal.fec.liassefiscale.InfoCompte;
import fr.neolegal.fec.liassefiscale.RepereHelper;

public class RepereHelperTest {

    @Test
    void resolveComptes_FR() {
        Set<InfoCompte> expected = new TreeSet<>();
        for (String numCompte : Set.of("781", "701", "7091", "702", "71", "703", "72", "74", "75", "707", "791", "708")) {
            expected.add(new InfoCompte(numCompte, true));
        }
        assertEquals(expected,
                RepereHelper.resolveComptes("FR"));
    }

    @Test
    void resolveComptes_DL() {
        Set<InfoCompte> expected = new TreeSet<>();
        for (String numCompte : Set.of("101", "104", "105", "1061", "1062", "1063", "1064", "1068", "108", "11", "12", "13", "14")) {
            expected.add(new InfoCompte(numCompte, true));
        }
        assertEquals(expected,
                RepereHelper.resolveComptes("DL"));
    }

}
