package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import fr.neolegal.fec.liassefiscale.LigneRepereHelper;

public class LigneRepereHelperTest {

    @Test
    void resolveComptes_FR() {
        assertEquals(Set.of("781", "701", "7091", "702", "71", "703", "72", "74", "75", "707", "791", "708"),
                LigneRepereHelper.resolveComptes("FR"));
    }

    @Test
    void resolveComptes_DL() {
        assertEquals(Set.of("101", "104", "105", "1061", "1062", "1063", "1064", "1068", "108", "11", "12", "13", "14"),
                LigneRepereHelper.resolveComptes("DL"));
    }

}
