package fr.neolegal.fec.liassefiscale;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
public class Annexe {
    NatureAnnexe natureAnnexe;
    NatureFormulaire natureFormulaire;

    List<List<String>> lignes = new ArrayList<>();
    
    @Builder
    public Annexe(NatureAnnexe natureAnnexe, NatureFormulaire natureFormulaire) {
        this.natureAnnexe = natureAnnexe;
        this.natureFormulaire = natureFormulaire;
    }

    public String getCellule(int indexLigne, int indexColonne) {
        return getLignes().get(indexLigne).get(indexColonne);
    }
}
