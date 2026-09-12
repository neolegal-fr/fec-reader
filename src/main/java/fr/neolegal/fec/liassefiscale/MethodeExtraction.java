package fr.neolegal.fec.liassefiscale;

import lombok.Getter;

/** Méthode ayant permis d'obtenir le montant d'un repère. */
@Getter
public enum MethodeExtraction {

    /** Le code du repère a été lu dans le PDF, la valeur est celle de sa cellule */
    REPERE(0.90),

    /** Le libellé de la ligne a été reconnu, la valeur est lue dans la colonne attendue */
    LIBELLE(0.62),

    /** La valeur a été lue dans le quadrillage du tableau (méthode historique) */
    QUADRILLAGE(0.70),

    /** La cellule du repère a été localisée mais elle est vide */
    CELLULE_VIDE(0.80),

    /** Le montant a été calculé à partir du fichier des écritures comptables */
    CALCUL_FEC(0.95),

    /** Le montant a été déduit d'un contrôle de cohérence (total ou sous-total) */
    DEDUCTION(0.75);

    /** Confiance de base associée à la méthode, avant pondération */
    private final double confianceBase;

    MethodeExtraction(double confianceBase) {
        this.confianceBase = confianceBase;
    }
}
