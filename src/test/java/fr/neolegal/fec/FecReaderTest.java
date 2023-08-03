package fr.neolegal.fec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.junit.jupiter.api.Test;

import fr.neolegal.fec.liassefiscale.Repere;

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

        assertEquals(12, fec.getJournaux().size());

        assertEquals(1, fec.getAnomalies().size());
        assertEquals(NatureAnomalie.LIGNES_VIDES, fec.getAnomalies().get(0).getNature());
        assertEquals(10756, fec.getAnomalies().get(0).getValeur());

    }

    @Test
    void test() {
        Path path = Path.of("d:/tmp/regles-calcul-reperes.csv");

        try (FileWriter fileWriter = new FileWriter(path.toString());
                CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.MYSQL )) {

            // Écriture de l'en-tête du fichier CSV
            csvPrinter.printRecord("Repère", "Formulaire", "Intitulé", "Règle de calcul");

            List<Repere> reperes = new LinkedList<>(Repere.REPERES.values());
            Collections.sort(reperes);
            for (Repere repere : reperes) {
                csvPrinter.printRecord(repere.getRepere(), repere.getFormulaire(), repere.getNom(), repere.getExpression());
            }

            System.out.println("Fichier CSV créé avec succès !");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
