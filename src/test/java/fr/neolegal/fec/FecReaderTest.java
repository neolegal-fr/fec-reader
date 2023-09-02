package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class FecReaderTest {

    @Test
    void read_tabSeparated() throws FileNotFoundException, IOException {
        /** Lecture d'un FEC avec des tabultations comme séparateur*/
        FecReader reader = new FecReader();

        Path path = Path.of("target/test-classes/123456789FEC20500930.txt");
        Fec fec = reader.read(path);
        assertEquals("123456789", fec.getSiren());
        assertEquals(LocalDate.of(2050, 9, 30), fec.getClotureExercice());

        assertEquals(10756, fec.getLignes().size());
        assertEquals(4001, fec.getNombreEcritures());

        assertEquals(12, fec.getJournaux().size());

        assertEquals(1, fec.getAnomalies().size());
        assertEquals(NatureAnomalie.LIGNES_VIDES, fec.getAnomalies().get(0).getNature());
        assertEquals(10756, fec.getAnomalies().get(0).getValeur());

    }

    @Test
    void read_fixedSizeColumns() throws FileNotFoundException, IOException {
        /** Lecture d'un FEC avec le caractère '|' comme séparateur, et des colonnes de taille fixe, paddées par des espaces*/
        FecReader reader = new FecReader();

        Path path = Path.of("target/test-classes/111111111FEC20221231.TXT");
        Fec fec = reader.read(path);
        assertEquals("111111111", fec.getSiren());
        assertEquals(LocalDate.of(2022, 12, 31), fec.getClotureExercice());

        assertEquals(934, fec.getLignes().size());
        assertEquals(248, fec.getNombreEcritures());

        assertEquals(9, fec.getJournaux().size());

        assertEquals(0, fec.getAnomalies().size());
    }

    
}
