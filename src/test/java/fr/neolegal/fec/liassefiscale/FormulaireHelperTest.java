package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class FormulaireHelperTest {
    @Test
    void getModeleFormulaireFromIdentifiant() {
        Optional<ModeleFormulaire> actual = FormulaireHelper.getModeleFormulaireFromIdentifiant("2033-A-SD");
        assertEquals("2033-A-SD", actual.get().getIdentifiant());
    }

    @Test
    void resolveModeleFormulaire_whenNull() {
        assertEquals(Optional.empty(), FormulaireHelper.resolveModeleFormulaire(null));
    }

    @Test
    void resolveModeleFormulaire_whenEmpty() {
        assertEquals(Optional.empty(), FormulaireHelper.resolveModeleFormulaire(""));
    }    

    @Test
    void resolveModeleFormulaire_whenIdentifiantComplet() {
        Optional<ModeleFormulaire> actual = FormulaireHelper.resolveModeleFormulaire("2139-A-SD");
        assertEquals("2139-A-SD", actual.get().getIdentifiant());
    }    

    @Test
    void resolveModeleFormulaire_whenIdentifiantIncompletNoDgFip() {
        Optional<ModeleFormulaire> actual = FormulaireHelper.resolveModeleFormulaire("2139 A");
        assertEquals("2139-A-SD", actual.get().getIdentifiant());
    }    

    @Test
    void resolveModeleFormulaire_whenIdentifiantIncompletWithDgFip() {
        Optional<ModeleFormulaire> actual = FormulaireHelper.resolveModeleFormulaire("dgfip N° 2139 A");
        assertEquals("2139-A-SD", actual.get().getIdentifiant());
    }    

    @Test
    void resolveModeleFormulaire_whenNumeroWithDgFip() {
        Optional<ModeleFormulaire> actual = FormulaireHelper.resolveModeleFormulaire("N° 2139");
        assertEquals("2139-A-SD", actual.get().getIdentifiant());
    }    

    @Test
    void resolveModeleFormulaire_whenTitre() {
        Optional<ModeleFormulaire> actual = FormulaireHelper.resolveModeleFormulaire("--- Bilan simplifié --- ");
        assertEquals("2033-A-SD", actual.get().getIdentifiant());
    }
}
