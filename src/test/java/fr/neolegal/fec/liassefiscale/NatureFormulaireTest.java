package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class NatureFormulaireTest {
    
    @Test
    void resolve_whenNull() {
        assertEquals(Optional.empty(), NatureFormulaire.resolve(null));
    }

    @Test
    void resolve_whenEmpty() {
        assertEquals(Optional.empty(), NatureFormulaire.resolve(""));
    }    

    @Test
    void resolve_whenIdentifiantComplet() {
        assertEquals(Optional.of(NatureFormulaire.DGFIP_2139_A_BILAN_SIMPLIFIE), NatureFormulaire.resolve("2139-A-SD"));
    }    

    @Test
    void resolve_whenIdentifiantIncompletNoDgFip() {
        assertEquals(Optional.empty(), NatureFormulaire.resolve("2139 A"));
    }    

    @Test
    void resolve_whenIdentifiantIncompletWithDgFip() {
        assertEquals(Optional.of(NatureFormulaire.DGFIP_2139_A_BILAN_SIMPLIFIE), NatureFormulaire.resolve("dgfip N° 2139 A"));
    }    

    @Test
    void resolve_whenNumeroWithDgFip() {
        assertEquals(Optional.of(NatureFormulaire.DGFIP_2139_A_BILAN_SIMPLIFIE), NatureFormulaire.resolve("N° 2139"));
    }    
}
