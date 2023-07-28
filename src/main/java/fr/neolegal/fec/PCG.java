package fr.neolegal.fec;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

/** Liste des numéros de comptes du Plan Comptable Général (PCG) */
@Getter
public enum PCG {
    // Classe 1 : Comptes de capitaux
    CAPITAUX(1),
    CAPITAL(101),
    EXPLOITANT(108),
    PRIMES_CAPITAL_SOCIAL(104),
    ECARTS_REEVALUATION(105),
    RESERVE_LEGALE(1061),
    RESERVES_STATUTAIRES(1063),
    RESERVES_REGLEMENTEES(1064),
    AUTRES_RESERVES(1068),
    REPORT_A_NOUVEAU_CREDITEUR(110),
    REPORT_A_NOUVEAU_DEBITEUR(119),
    RESULTAT_BENEFICE(120),
    RESULTAT_PERTE(129),
    SUBVENTIONS_INVESTISSEMENT(13),
    SUBVENTIONS_INVESTISSEMENT_INSCRITES_AU_COMPTE_DE_RESULTAT(139),
    PROVISIONS_REGLEMENTEES(14),
    EMPRUNTS(164),

    // Classe 2 : Comptes d'immobilisations
    IMMOBILISATIONS(2),

    // Classe 3 : Comptes de stocks et en-cours
    STOCKS(3),

    // Classe 4 : Comptes de Tiers
    TIERS(4),

    // Classe 5 : Comptes financiers
    FINANCIERS(5),

    // Classe 6 : Comptes de charges
    CHARGES(6),
    ACHATS(60),
    ACHATS_MARCHANDISES(607),
    RABAIS_ET_RISTOURNES(609),
    CHARGES_EXTERNES(61),
    AUTRES_CHARGES_EXTERNES(62),
    IMPOTS(63),
    CHARGES_PERSONNEL(64),
    TRAITEMENTS_ET_SALAIRES(641),
    REMUNERATION_EXPLOLITANT(644),
    CHARGES_SECURITE_SOCIALE(645),
    AUTRES_CHARGES_PERSONNEL(648),
    AUTRES_CHARGES_GESTION_COURANTE(65),

    // Classe 7 : Comptes de produits
    PRODUITS(7),
    VENTES_DE_PRODUITS(70),
    PRODUCTION_STOCKEE(71),
    PRODUCTION_IMMOBILISEE(72),
    SUBVENTIONS_EXPLOITATION(74),
    AUTRES_PRODUITS_DE_GESTION(75),
    PRODUITS_FINANCIERS(76),
    PRODUITS_EXCEPTIONNELS(77),
    REPRISES_SUR_AMORTISSEMENTS(78),
    TRANSFERTS_CHARGES(79),

    // Classe 8 : Comptes spéciaux
    SPECIAUX(8);

    final int code;
    final String prefix;

    PCG(int code) {
        this.code = code;
        this.prefix = Integer.toString(code);
    }

    boolean matches(String compte) {
        return StringUtils.startsWith(compte, prefix);
    }
}
