package fr.neolegal.fec.liassefiscale;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class Annexe {
    NatureAnnexe natureAnnexe;
    ModeleFormulaire modeleFormulaire;

    List<List<String>> lignes = new ArrayList<>();
    
    @Builder
    public Annexe(NatureAnnexe natureAnnexe, ModeleFormulaire modeleFormulaire) {
        this.natureAnnexe = natureAnnexe;
        this.modeleFormulaire = modeleFormulaire;
    }

    public String getCellule(int indexLigne, int indexColonne) {
        return getLignes().get(indexLigne).get(indexColonne);
    }
}
