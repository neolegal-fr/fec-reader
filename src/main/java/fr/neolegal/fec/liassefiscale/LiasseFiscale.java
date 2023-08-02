package fr.neolegal.fec.liassefiscale;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;

/* Modélisation de la liasse fiscale, contenant huit tableaux comptables, qui portent les
numéros 2050 (bilan-actif), 2051 (bilan-passif), 2052 et 2053
(compte de résultat, en liste), 2054 (immobilisations), 2055
(amortissements), 2056 (provisions) et 2057 (état des
échéances des créances et des dettes à la clôture de l’exer-
cice)  */
@Data
public class LiasseFiscale {

    final String siren;
    final LocalDate clotureExercice;
    final List<TableauComptable> formulaires = new LinkedList<>();

    @Builder
    public LiasseFiscale(String siren, LocalDate clotureExercice) {
        this.siren = siren;
        this.clotureExercice = clotureExercice;
    }

    public TableauComptable get(Formulaire formulaire) {
        return formulaires.stream().filter(tableau -> tableau.getFormulaire() == formulaire).findFirst().orElse(TableauComptable.builder().build());
    }
}
