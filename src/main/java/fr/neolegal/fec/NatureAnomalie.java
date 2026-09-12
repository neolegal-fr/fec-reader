package fr.neolegal.fec;

public enum NatureAnomalie {
    SIREN,
    /** Page du document non exploitable : absence de couche texte */
    PAGE_ILLISIBLE,
    /** Formulaire reconnu mais aucun montant extrait */
    FORMULAIRE_VIDE,
    /** Contrôle de cohérence comptable non satisfait */
    INCOHERENCE_COMPTABLE,
    /** Numéro SIREN invalide (clé de contrôle) */
    SIREN_INVALIDE,
    CLOTURE_EXERCICE, LIGNES_VIDES, LIGNES_INVALIDES

}
