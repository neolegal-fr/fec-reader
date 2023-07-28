package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class FecHelperTest {

    @Test
    void parseSiren_whenEmpty() {
        assertTrue(FecHelper.parseSiren("").isEmpty());
    }

    @Test
    void parseSiren() {
        assertEquals(Optional.of("123456789"), FecHelper.parseSiren("123456789FEC20500930.txt"));
    }

    @Test
    void parseCLotureExercice_whenEmpty() {
        assertTrue(FecHelper.parseClotureExercice("").isEmpty());
    }

    @Test
    void parseClotureExercice() {
        assertEquals(Optional.of(LocalDate.of(2050, 9, 30)),
                FecHelper.parseClotureExercice("123456789FEC20500930.txt"));
    }

    @Test
    void computeTotalJournal_whenEmpty() {
        assertEquals(0.0, FecHelper.computeTotalJournal(List.of(), "VEN", true));

    }

    @Test
    void computeTotalJournal() {
        List<LEC> ecritures = new LinkedList<>();
        ecritures.add(LEC.builder().journalCode("VEN").credit(1000.0).build());
        ecritures.add(LEC.builder().journalCode("VEN").credit(500.0).build());
        assertEquals(1500.0, FecHelper.computeTotalJournal(ecritures, "VEN", true));
    }
}
