package fr.neolegal.fec.liassefiscale;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class NatureAnnexeTest {

    @Test
    void resolve_whenContientAnnexeSansEtreUneAnnexe() {
        assertEquals(Optional.empty(),
                NatureAnnexe.resolve("article 38 sexdecies RB de l'annexe III au Code Général des Impôts 2139-A-SD"));
    }

    @Test
    void resolve_whenAnnexeInconnueSansRefFormulaire() {
        assertEquals(Optional.of(NatureAnnexe.INCONNUE), NatureAnnexe.resolve("Annexe"));
    }

    @Test
    void resolve_whenAnnexeInconnueAvecRefFormulaire() {
        // La combinaison du mot clé "annexe" et d'une référence à un formulaire
        // Suffit à considérer que c'est une annexe, même en l'absence d'un identifiant d'annexe
        assertEquals(Optional.of(NatureAnnexe.INCONNUE), NatureAnnexe.resolve("Annexe 2139-A-SD"));
    }

    @Test
    void resolve_whenIntituleAnnexeAvecRefFormulaireMaisSansMotAnnexe() {
        // Bien qu'il contienne l'identifiant d'une annexe, l'en-tête est celle d'un
        // formulaire, pas d'une annexe
        assertEquals(Optional.empty(), NatureAnnexe.resolve(NatureAnnexe.PROVISIONS.getIntitule() + " 2139-A-SD"));
    }

    @Test
    void resolve_whenIntituleAnnexeSansRefFormulaireNotMotAnnexe() {
        // En l'absence de référence à un formulaire, et du mot clé "annexe", la
        // présence de l'identifiant suffit à considérer la page comme une annexe
        assertEquals(Optional.of(NatureAnnexe.PROVISIONS), NatureAnnexe.resolve("Provisions pour hausse des prix"));
    }

}
