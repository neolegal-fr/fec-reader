package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class FecReaderTest {

    @Test
    void read() throws FileNotFoundException, IOException {
        FecReader reader = new FecReader();

        Path path = Path.of("target/test-classes/123456789FEC20500930.txt");
        Fec fec = reader.read(path);
        assertEquals("123456789", fec.getSiren());
        assertEquals(LocalDate.of(2050, 9, 30), fec.getClotureExercice());

        assertEquals(10756, fec.getEcritures().size());

        assertEquals(1, fec.getAnomalies().size());
        assertEquals(NatureAnomalie.LIGNES_VIDES, fec.getAnomalies().get(0).getNature());
        assertEquals(10756, fec.getAnomalies().get(0).getValeur());
    }

    @Test
    void parseSiren_whenEmpty() {
        assertTrue(FecReader.parseSiren("").isEmpty());
    }

    @Test
    void parseSiren() {
        assertEquals(Optional.of("123456789"), FecReader.parseSiren("123456789FEC20500930.txt"));
    }

    @Test
    void parseCLotureExercice_whenEmpty() {
        assertTrue(FecReader.parseClotureExercice("").isEmpty());
    }

    @Test
    void parseCLotureExercice() {
        assertEquals(Optional.of(LocalDate.of(2050, 9, 30)), FecReader.parseClotureExercice("123456789FEC20500930.txt"));
    }

}
