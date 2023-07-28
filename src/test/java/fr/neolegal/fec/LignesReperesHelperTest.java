package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

public class LignesReperesHelperTest {

    @Test
    void resolveComptes_FR() {
        assertEquals(Set.of("781", "71", "72", "74", "75", "707", "791", "708"),
                LignesReperesHelper.resolveComptes("FR"));
    }

    @Test
    void resolveComptes_DL() {
        assertEquals(Set.of("11", "12", "13", "101", "14", "104", "105", "1068", "108", "1064", "1063", "1062"),
                LignesReperesHelper.resolveComptes("DL"));
    }

}
