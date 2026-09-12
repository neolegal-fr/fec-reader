package fr.neolegal.fec.liassefiscale.controle;

/** Résultat d'un contrôle de cohérence comptable. */
public enum StatutControle {
    /** L'égalité comptable est vérifiée */
    SATISFAIT,
    /** L'égalité comptable n'est pas vérifiée : au moins un montant est erroné */
    ECHEC,
    /** Les repères nécessaires n'ont pas été extraits, ou sont tous nuls */
    NON_APPLICABLE
}
