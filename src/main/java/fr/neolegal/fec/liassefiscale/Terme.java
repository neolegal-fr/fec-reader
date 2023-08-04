package fr.neolegal.fec.liassefiscale;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * Modélise un terme du calcul du montant d'un repère. Un Terme est
 * la combinaison d'un numéro de compte du PCG, et d'un indicateur de solde ou de
 * variation.
 */
@Data
public class Terme implements Comparable<Terme> {
    final String prefixeNumero;
    final boolean solde; // vrai pour le solde, faux pour la variation au cours de l'exercice

    public Terme(String numero, boolean prefixeNumero) {
        this.prefixeNumero = numero;
        this.solde = prefixeNumero;
    }

    public boolean matches(String numero) {
        return StringUtils.startsWith(numero, prefixeNumero);
    }

    @Override
    public int compareTo(Terme other) {
        if (StringUtils.equalsIgnoreCase(prefixeNumero, other.prefixeNumero)) {
            if (solde == other.solde) {
                return 0;
            }
            return solde && !other.solde ? 1 : -1;
        }

        return prefixeNumero.compareToIgnoreCase(other.prefixeNumero);
    }
}
