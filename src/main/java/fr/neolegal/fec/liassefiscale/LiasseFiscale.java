package fr.neolegal.fec.liassefiscale;

import java.time.LocalDate;

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
    final TableauComptable bilanActif;
    final TableauComptable bilanPassif;
    final TableauComptable compteDeResultat;
    final TableauComptable compteDeResultatEnListe;
    final TableauComptable immobilisations;
    final TableauComptable amortissements;
    final TableauComptable provisions;
    final TableauComptable creancesEtDettes;

    @Builder
    public LiasseFiscale(String siren, LocalDate clotureExercice, TableauComptable bilanActif,
            TableauComptable bilanPassif, TableauComptable compteDeResultat, TableauComptable compteDeResultatEnListe,
            TableauComptable immobilisations, TableauComptable amortissements, TableauComptable provisions,
            TableauComptable creancesEtDettes) {
        this.siren = siren;
        this.clotureExercice = clotureExercice;
        this.bilanActif = bilanActif;
        this.bilanPassif = bilanPassif;
        this.compteDeResultat = compteDeResultat;
        this.compteDeResultatEnListe = compteDeResultatEnListe;
        this.immobilisations = immobilisations;
        this.amortissements = amortissements;
        this.provisions = provisions;
        this.creancesEtDettes = creancesEtDettes;
    }
}
