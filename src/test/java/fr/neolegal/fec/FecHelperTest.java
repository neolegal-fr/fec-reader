package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
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
    void parseCLotureExercice() {
        assertEquals(Optional.of(LocalDate.of(2050, 9, 30)),
                FecHelper.parseClotureExercice("123456789FEC20500930.txt"));
    }

}
