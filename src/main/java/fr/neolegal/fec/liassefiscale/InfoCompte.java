package fr.neolegal.fec.liassefiscale;


import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
public class InfoCompte implements Comparable<InfoCompte> {
    final String prefixeNumero;
    final boolean solde; // vrai pour le solde, faux pour la variation au cours de l'exercice

    public InfoCompte(String numero, boolean prefixeNumero) {
        this.prefixeNumero = numero;
        this.solde = prefixeNumero;
    }

    public boolean matches(String numero) {
        return StringUtils.startsWith(numero, prefixeNumero);
    }

    @Override
    public int compareTo(InfoCompte other) {
        if (StringUtils.equalsIgnoreCase(prefixeNumero, other.prefixeNumero)) {
            if (solde == other.solde) {
                return 0;
            }
            return solde && !other.solde ? 1 : -1;
        }

        return prefixeNumero.compareToIgnoreCase(other.prefixeNumero);
    }
}
