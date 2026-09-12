package fr.neolegal.fec.liassefiscale;

import java.util.Objects;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Montant d'un repère, accompagné des informations permettant d'apprécier la
 * fiabilité de son extraction.
 */
@Getter
@Builder(toBuilder = true)
public class MontantExtrait implements Comparable<MontantExtrait> {

    /** Symbole du repère (AA, CO, 044...) */
    private final String symbole;

    /** Montant lu, en euros. {@code 0} lorsque la cellule est vide. */
    private final double montant;

    /** true lorsque la cellule a été localisée mais qu'elle ne contient aucune valeur */
    @Builder.Default
    private final boolean celluleVide = false;

    private final MethodeExtraction methode;

    /** Numéro de la page du PDF d'où provient le montant, 0 si non applicable */
    @Builder.Default
    private final int page = 0;

    /** Texte brut de la cellule, tel que lu dans le document */
    private final String texteSource;

    /** true si le texte provient d'une reconnaissance optique */
    @Builder.Default
    private final boolean ocr = false;

    /**
     * Probabilité que le montant soit exact, entre 0 et 1. Elle combine la méthode
     * d'extraction, la qualité de l'alignement dans le tableau et le résultat des
     * contrôles de cohérence comptable portant sur le repère.
     */
    @Setter
    @Builder.Default
    private double confiance = 0.5;

    /** Nombre de contrôles de cohérence satisfaits portant sur ce repère */
    @Setter
    @Builder.Default
    private int controlesSatisfaits = 0;

    /** Nombre de contrôles de cohérence en échec portant sur ce repère */
    @Setter
    @Builder.Default
    private int controlesEnEchec = 0;

    public boolean estFiable() {
        return confiance >= Fiabilite.SEUIL_FIABLE;
    }

    @Override
    public int compareTo(MontantExtrait autre) {
        return Double.compare(confiance, autre == null ? -1 : autre.confiance);
    }

    @Override
    public boolean equals(Object autre) {
        if (!(autre instanceof MontantExtrait)) {
            return false;
        }
        MontantExtrait montantExtrait = (MontantExtrait) autre;
        return Objects.equals(symbole, montantExtrait.symbole) && montant == montantExtrait.montant
                && methode == montantExtrait.methode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbole, montant, methode);
    }

    @Override
    public String toString() {
        return String.format("%s=%.0f (%s, %.0f%%)", symbole, montant, methode, confiance * 100);
    }
}
