package fr.neolegal.fec.liassefiscale;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public enum NatureFormulaire {
    DGFIP_2050_BILAN_ACTIF(2050, null, "2050-SD", "15949", "BILAN - ACTIF", RegimeImposition.REEL_NORMAL, true,
            true, null),
    DGFIP_2051_BILAN_PASSIF(2051, null, "2051-SD", "15949", "BILAN - PASSIF", RegimeImposition.REEL_NORMAL, false,
            false, null),
    DGFIP_2052_COMPTE_RESULTAT(2052, null, "2052-SD", "15949", "COMPTE DE RESULTAT DE L'EXERCICE (en liste)",
            RegimeImposition.REEL_NORMAL, false, false, null),
    DGFIP_2053_COMPTE_RESULTAT(2053, null, "2053-SD", "15949", "COMPTE DE RESULTAT DE L'EXERCICE (suite)",
            RegimeImposition.REEL_NORMAL, false, false,
            Set.of(NatureAnnexe.PRODUITS_ET_CHARGES_EXCEPTIONNELS,
                    NatureAnnexe.PRODUITS_ET_CHARGES_ANTERIEURS)),
    DGFIP_2054_IMMOBILISATIONS(2054, null, "2054-SD", "15949", "IMMOBILISATIONS", RegimeImposition.REEL_NORMAL,
            false, false, null),
    DGFIP_2054_ECARTS_REEVALUATION(2054, "bis", "2054 bis-SD", "15949",
            "TABLEAU DES ECARTS DE REEVALUATION SUR IMMOBILISATIONS AMORTISSABLES", RegimeImposition.REEL_NORMAL, false,
            false, null),
    DGFIP_2055_AMORTISSEMENTS(2055, null, "2055-SD", "15949", "IMMOBILISATIONS", RegimeImposition.REEL_NORMAL,
            false, false, null),
    DGFIP_2056_PROVISIONS(2056, null, "2056-SD", "15949", "PROVISIONS INSCRITES AU BILAN",
            RegimeImposition.REEL_NORMAL, false, false, null),
    DGFIP_2057_CREANCES(2057, null, "2057-SD", "15949",
            "ETAT DES ECHEANCES DES CREANCES ET DES DETTES A LA CLOTURE DE L'EXERCICE", RegimeImposition.REEL_NORMAL,
            false, false, null),
    DGFIP_2058_A_RESULTAT_FISCAL(2058, "A", "2058-A-SD", "15949", "DETERMINATION DU RESULTAT FISCAL",
            RegimeImposition.REEL_NORMAL, false, false, null),
    DGFIP_2058_B_DEFICITS(2058, "B", "2058-B-SD", "15949",
            "DEFICITS, INDEMNITES POUR CONGES A PAYER ET PROVISIONS NON DEDUCTIBLES", RegimeImposition.REEL_NORMAL,
            false, false, null),
    DGFIP_2058_C_AFFECTATION_RESULTAT(2058, "C", "2058-C-SD", "15949",
            "TABLEAU D'AFFECTATION DU RESULTAT ET RENSEIGNEMENTS DIVERS", RegimeImposition.REEL_NORMAL, false, false,
            null),
    DGFIP_2059_E_DETERMINATION_EFFECTIFS(2059, "E", "2059-E-SD", null,
            "DÉTERMINATION DES EFFECTIFS ET DE LA VALEUR AJOUTÉE", RegimeImposition.REEL_NORMAL, false, false,
            null),            
    DGFIP_2033_A_BILAN_SIMPLIFIE(2033, "A", "2033-A-SD", "15948", "BILAN SIMPLIFIÉ",
            RegimeImposition.REEL_SIMPLIFIE, true, true, null),
    DGFIP_2033_B_COMPTE_RESULTAT_SIMPLIFIE(2033, "B", "2033-B-SD", "15948", "COMPTE DE RESULTAT SIMPLIFIE",
            RegimeImposition.REEL_SIMPLIFIE, false, false, null),
    DGFIP_2033_C_IMMOBILISATIONS_AMORTISSEMENTS(2033, "C", "2033-C-SD", "15948",
            "IMMOBILISATIONS - AMORTISSEMENTS - PLUS-VALUES - MOINS-VALUES", RegimeImposition.REEL_SIMPLIFIE, false,
            false, null),
    DGFIP_2033_D_PROVISIONS_AMORTISSEMENTS(2033, "D", "2033-D-SD", "15948",
            "RELEVE DES PROVISIONS - AMORTISSEMENTS DEROGATOIRES - DEFICITS", RegimeImposition.REEL_SIMPLIFIE, false,
            false, null),
    DGFIP_2139_A_BILAN_SIMPLIFIE(2139, "A", "2139-A-SD", "11145", "BILAN SIMPLIFIÉ",
            RegimeImposition.REEL_SIMPLIFIE_AGRICOLE, true, true, null),
    DGFIP_2139_B_COMPTE_RESULTAT_SIMPLIFIE(2139, "B", "2139-B-SD", "11146",
            "COMPTE DE RESULTAT SIMPLIFIÉ DE L'EXERCICE", RegimeImposition.REEL_SIMPLIFIE_AGRICOLE, false, true, null),
    DGFIP_2072_COMPTE_RESULTAT_SIMPLIFIE(2072, "B", "2072-S-SD", "10338",
            "DÉCLARATION DES SOCIÉTÉS IMMOBILIÈRES NON SOUMISES A L’IMPÔT SUR LES SOCIÉTÉS",
            RegimeImposition.REEL_SIMPLIFIE, false, true, Set.of(NatureAnnexe.DETERMINATION_REVENUS_SOCIETE_IMMOBILIERE,
                    NatureAnnexe.ASSOCIES_USUFRUITIERS_REPARTITION_RESULTAT));

    Integer numero;
    String page;
    String identifiant;
    String cerfa;
    String titre;
    RegimeImposition regimeImposition;
    boolean containsSiren;
    boolean containsClotureExercice;
    Set<NatureAnnexe> annexes = new HashSet<>();

    NatureFormulaire(Integer numero, String page, String identifiant, String cerfa, String titre,
            RegimeImposition regimeImposition, boolean containsSiren, boolean containsClotureExercice,
            Set<NatureAnnexe> annexes) {
        this.numero = numero;
        this.page = page;
        this.identifiant = identifiant;
        this.cerfa = cerfa;
        this.titre = titre;
        this.regimeImposition = regimeImposition;
        this.containsSiren = containsSiren;
        this.containsClotureExercice = containsClotureExercice;
        this.annexes = ObjectUtils.firstNonNull(annexes, Set.of());
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

    public static Optional<NatureFormulaire> resolve(String header) {
        return resolve(header, true);
    }

    public static Optional<NatureFormulaire> resolve(String header, boolean checkTitre) {
        if (StringUtils.isBlank(header)) {
            return Optional.empty();
        }

        // Si l'en-tête contient l'identifiant exact, bingo
        for (NatureFormulaire formulaire : NatureFormulaire.values()) {
            if (StrUtils.containsIgnoreCase(header, formulaire.getIdentifiant())) {
                return Optional.of(formulaire);
            }
        }

        List<NatureFormulaire> formulairesAvecPage = Stream.of(NatureFormulaire.values())
                .filter(f -> f.getPage() != null)
                .collect(Collectors.toList());
        // C'est un formulaire, sans identifiant exact connu.
        // Recherche de variations autour du numéro de formulaire avec la page
        for (NatureFormulaire formulaire : formulairesAvecPage) {
            String regex = String.format(".*(%d[\\s\\-]?%s).*", formulaire.getNumero(), formulaire.getPage());
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(header);

            if (matcher.matches()) {
                return Optional.of(formulaire);
            }
        }

        // puis test des formulaires sans numéro de page
        List<NatureFormulaire> formulairesSansPage = Stream.of(NatureFormulaire.values())
                .filter(f -> f.getPage() == null)
                .collect(Collectors.toList());
        for (NatureFormulaire formulaire : formulairesSansPage) {
            if (StringUtils.contains(header, formulaire.getNumero().toString())) {
                return Optional.of(formulaire);
            }
        }

        // Test des formulaires avec un numéro de page, mais sans
        // préciser le numéro de page
        for (NatureFormulaire formulaire : formulairesAvecPage) {
            if (StringUtils.contains(header, formulaire.getNumero().toString())) {
                return Optional.of(formulaire);
            }
        }

        // Recherche du titre
        if (checkTitre) {
            for (NatureFormulaire formulaire : values()) {
                if (StrUtils.containsIgnoreCase(header, formulaire.getTitre())) {
                    return Optional.of(formulaire);
                }
            }
        }

        // Aucune correspondance trouvée
        return Optional.empty();
    }
}
