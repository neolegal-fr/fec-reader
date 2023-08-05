package fr.neolegal.fec.liassefiscale;

import javax.annotation.Nonnull;

import lombok.Getter;

@Getter
public enum AgregateurCompte {
    SOLDE("SLD", true), DIFFERENCE("DIF", false), CREDIT("CRD", true), DEBIT("DEB", true);

    @Nonnull
    final String prefixe;
    @Nonnull
    final boolean repriseSoldeIncluded; // Vrai pour le calcul de la variation sur l'année, qui doit ignorer la première ligne de la reprise à nouveau

    AgregateurCompte(String prefixe, boolean repriseANouveauIncluded) {
        this.prefixe = prefixe + "_";
        this.repriseSoldeIncluded = repriseANouveauIncluded;
    }
}
