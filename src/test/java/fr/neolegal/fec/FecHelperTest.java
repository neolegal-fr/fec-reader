package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    void parseDouble() throws java.text.ParseException {
        // La norme impose deux décimales sans séparateur de milliers : la virgule
        // comme le point sont des séparateurs décimaux
        assertEquals(1888.31, FecHelper.parseDouble("1888,31"));
        assertEquals(1888.31, FecHelper.parseDouble("1888.31"));
        assertEquals(-1888.31, FecHelper.parseDouble("-1888,31"));
        assertEquals(-1888.31, FecHelper.parseDouble("1888,31-"));
        assertEquals(1888.31, FecHelper.parseDouble(" 1 888,31 "));
        assertEquals(0.0, FecHelper.parseDouble("0,00"));
        assertEquals(null, FecHelper.parseDouble(""));
        assertEquals(null, FecHelper.parseDouble(null));
    }

    @Test
    void parseDouble_montantIllisible() {
        assertThrows(java.text.ParseException.class, () -> FecHelper.parseDouble("mille euros"));
    }

    @Test
    void countEcritures_parJournalEtNumero() {
        // Le numéro d'écriture n'est unique qu'au sein d'un journal
        List<LEC> lignes = new LinkedList<>();
        lignes.add(LEC.builder().journalCode("VEN").ecritureNum("1").build());
        lignes.add(LEC.builder().journalCode("VEN").ecritureNum("1").build());
        lignes.add(LEC.builder().journalCode("ACH").ecritureNum("1").build());
        assertEquals(2, FecHelper.countEcritures(lignes));
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
