package fr.neolegal.fec.liassefiscale;

public enum Formulaire {
    DGFIP_2050_BILAN_ACTIF("2050-SD", "15949 * 05", "BILAN - ACTIF"),
    DGFIP_2051_BILAN_PASSIF("2051-SD", "15949 * 05", "BILAN - PASSIF"),
    DGFIP_2052_COMPTE_RESULTAT("2052-SD", "15949 * 05", "COMPTE DE RESULTAT DE L'EXERCICE (en liste)"),
    DGFIP_2053_COMPTE_RESULTAT("2053-SD", "15949 * 05", "COMPTE DE RESULTAT DE L'EXERCICE (suite)"),
    DGFIP_2054_IMMOBILISATIONS("2054-SD", "15949 * 05", "IMMOBILISATIONS"),
    DGFIP_2054_ECARTS_REEVALUATION("2054 bis-SD", "15949 * 05", "TABLEAU DES ECARTS DE REEVALUATION SUR IMMOBILISATIONS AMORTISSABLES"),
    DGFIP_2055_AMORTISSEMENTS("2055-SD", "15949 * 05", "IMMOBILISATIONS"),
    DGFIP_2056_PROVISIONS("2056-SD", "15949 * 05", "PROVISIONS INSCRITES AU BILAN"),
    DGFIP_2057_CREANCES("2057-SD", "15949 * 05", "ETAT DES ECHEANCES DES CREANCES ET DES DETTES A LA CLOTURE DE L'EXERCICE"),
    DGFIP_2058_A_RESULTAT_FISCAL("2058-A-SD", "15949 * 05", "DETERMINATION DU RESULTAT FISCAL"),
    DGFIP_2058_B_DEFICITS("2058-B-SD", "15949 * 05", "DEFICITS, INDEMNITES POUR CONGES A PAYER ET PROVISIONS NON DEDUCTIBLES"),
    DGFIP_2058_C_AFFECTATION_RESULTAT("2058-C-SD", "15949 * 05", "TABLEAU D'AFFECTTION DU RESULTAT ET RENSEIGNEMENTS DIVERS");

    String identifiant;
    String cerfa;
    String titre;

    Formulaire(String identifiant, String cerfa, String titre) {
        this.identifiant = identifiant;
        this.cerfa = cerfa;
        this.titre = titre;
    }

    @Override
    public String toString() {
        return identifiant;
    }

    public static Formulaire fromIdentifiant(String string) {
        for (Formulaire formulaire : Formulaire.values()) {
            if (formulaire.identifiant.equals(string)) {
                return formulaire;
            }
        }
        return null;
    }
}
