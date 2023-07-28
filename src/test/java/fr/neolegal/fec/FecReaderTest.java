package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

public class FecReaderTest {

    @Test
    void read() throws FileNotFoundException, IOException {
        FecReader reader = new FecReader();

        Path path = Path.of("target/test-classes/123456789FEC20500930.txt");
        Fec fec = reader.read(path);
        assertEquals("123456789", fec.getSiren());
        assertEquals(LocalDate.of(2050, 9, 30), fec.getClotureExercice());

        assertEquals(10756, fec.getLignes().size());
        assertEquals(4001, fec.getNombreEcritures());
        assertEquals(35600.0, fec.getReserveLegale());
        assertEquals(1212843.9, fec.getChiffreAffaires());
        assertEquals(121396.22, fec.getReportANouveau());
        assertEquals(0.0, fec.getAutresReserves());

        assertEquals(-83308.12000000001, fec.getChargesSociales());
        assertEquals(-410953.37, fec.getAchatsMarchandises());
        assertEquals(-249857.75, fec.getChargesPersonnel());
        
        assertEquals(-1107619.9, fec.getChargesExploitation());
        assertEquals(1225776.5, fec.getProduitExploitation());
        assertEquals(512996.22000000003, fec.getCapitauxPropres()); // problème : devrait être 639230.0 d'après la liasse fiscale

        assertEquals(12, fec.getJournaux().size());

        assertEquals(1, fec.getAnomalies().size());
        assertEquals(NatureAnomalie.LIGNES_VIDES, fec.getAnomalies().get(0).getNature());
        assertEquals(10756, fec.getAnomalies().get(0).getValeur());

    }
}
