package fr.neolegal.fec.liassefiscale;

import lombok.Getter;

@Getter
public enum NatureFormulaire {
    DGFIP_2050_BILAN_ACTIF(2050, "2050-SD", "15949 * 05", "BILAN - ACTIF", RegimeImposition.REEL_NORMAL, true, true),
    DGFIP_2051_BILAN_PASSIF(2051, "2051-SD", "15949 * 05", "BILAN - PASSIF", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2052_COMPTE_RESULTAT(2052, "2052-SD", "15949 * 05", "COMPTE DE RESULTAT DE L'EXERCICE (en liste)", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2053_COMPTE_RESULTAT(2053, "2053-SD", "15949 * 05", "COMPTE DE RESULTAT DE L'EXERCICE (suite)", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2054_IMMOBILISATIONS(2054, "2054-SD", "15949 * 05", "IMMOBILISATIONS", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2054_ECARTS_REEVALUATION(2054, "2054 bis-SD", "15949 * 05", "TABLEAU DES ECARTS DE REEVALUATION SUR IMMOBILISATIONS AMORTISSABLES", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2055_AMORTISSEMENTS(2055, "2055-SD", "15949 * 05", "IMMOBILISATIONS", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2056_PROVISIONS(2056, "2056-SD", "15949 * 05", "PROVISIONS INSCRITES AU BILAN", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2057_CREANCES(2057, "2057-SD", "15949 * 05", "ETAT DES ECHEANCES DES CREANCES ET DES DETTES A LA CLOTURE DE L'EXERCICE", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2058_A_RESULTAT_FISCAL(2058, "2058-A-SD", "15949 * 05", "DETERMINATION DU RESULTAT FISCAL", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2058_B_DEFICITS(2058, "2058-B-SD", "15949 * 05", "DEFICITS, INDEMNITES POUR CONGES A PAYER ET PROVISIONS NON DEDUCTIBLES", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2058_C_AFFECTATION_RESULTAT(2058, "2058-C-SD", "15949 * 05", "TABLEAU D'AFFECTTION DU RESULTAT ET RENSEIGNEMENTS DIVERS", RegimeImposition.REEL_NORMAL, false, false),
    DGFIP_2033_A_BILAN_SIMPLIFIE(2033, "2033-A-SD", "15948 * 05", "BILAN SIMPLIFIE", RegimeImposition.REEL_SIMPLIFIE, true, true),
    DGFIP_2033_B_COMPTE_RESULTAT_SIMPLIFIE(2033, "2033-B-SD", "15948 * 05", "COMPTE DE RESULTAT SIMPLIFIE", RegimeImposition.REEL_SIMPLIFIE, false, false),
    DGFIP_2033_C_IMMOBILISATIONS_AMORTISSEMENTS(2033, "2033-C-SD", "15948 * 05", "IMMOBILISATIONS - AMORTISSEMENTS - PLUS-VALUES - MOINS-VALUES", RegimeImposition.REEL_SIMPLIFIE, false, false),
    DGFIP_2033_D_PROVISIONS_AMORTISSEMENTS(2033, "2033-D-SD", "15948 * 05", "RELEVE DES PROVISIONS - AMORTISSEMENTS DEROGATOIRES - DEFICITS", RegimeImposition.REEL_SIMPLIFIE, false, false),
    DGFIP_2139_A_BILAN_SIMPLIFIE(2139, "2139-A-SD", "15946 * 05", "BILAN SIMPLIFIÉ", RegimeImposition.REEL_SIMPLIFIE_AGRICOLE, true, true),
    DGFIP_2139_B_COMPTE_RESULTAT_SIMPLIFIE(2139, "2139-B-SD", "15946 * 05", "COMPTE DE RESULTAT SIMPLIFIÉ DE L'EXERCICE", RegimeImposition.REEL_SIMPLIFIE_AGRICOLE, false, false),
    DGFIP_2139_BENEFICES_AGRICOLES(2139, "2139-SD", "11144 * 25", "BÉNÉFICES AGRICOLES - RÉGIME DU BÉNÉFICE RÉEL SIMPLIFIÉ", RegimeImposition.REEL_SIMPLIFIE_AGRICOLE, false, false);

    Integer numero;
    String identifiant;
    String cerfa;
    String titre;
    RegimeImposition regimeImposition;
    boolean containsSiren;
    boolean containsClotureExercice;

    NatureFormulaire(Integer numero, String identifiant, String cerfa, String titre, RegimeImposition regimeImposition, boolean containsSiren, boolean containsClotureExercice) {
        this.numero = numero;
        this.identifiant = identifiant;
        this.cerfa = cerfa;
        this.titre = titre;
        this.regimeImposition = regimeImposition;
        this.containsSiren = containsSiren;
        this.containsClotureExercice = containsClotureExercice;
    }

    @Override
    public String toString() {
        return identifiant;
    }

    public static NatureFormulaire fromIdentifiant(String string) {
        for (NatureFormulaire formulaire : NatureFormulaire.values()) {
            if (formulaire.identifiant.equals(string)) {
                return formulaire;
            }
        }
        return null;
    }

    public boolean containsSiren() {
        return containsSiren;
    }

    public boolean containsClotureExercice() {
        return containsClotureExercice;
    }
}
