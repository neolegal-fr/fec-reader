package fr.neolegal.fec.liassefiscale;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * Modélise l'aggrégation des données d'un ou plusieurs comptes, que ce soit le solde initial, la
 * variation au cours de l'exercice, le montant créditeur ou débiteur.
 */
@Data
public class AgregationComptes implements Comparable<AgregationComptes> {
    final String prefixNumeroCompte;
    final AgregateurCompte agregateur;

    public AgregationComptes(String prefixNumeroCompte, AgregateurCompte aggregateur) {
        this.prefixNumeroCompte = prefixNumeroCompte;
        this.agregateur = aggregateur;
    }

    public boolean matches(String numeroCompte) {
        return StringUtils.startsWith(numeroCompte, prefixNumeroCompte);
    }

    @Override
    public int compareTo(AgregationComptes other) {
        int result = prefixNumeroCompte.compareToIgnoreCase(other.prefixNumeroCompte);
        if (result != 0) {
            return result;
        }

        return agregateur.compareTo(other.agregateur);
    }
}
