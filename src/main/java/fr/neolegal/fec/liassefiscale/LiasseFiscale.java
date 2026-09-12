package fr.neolegal.fec.liassefiscale;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import fr.neolegal.fec.Anomalie;
import fr.neolegal.fec.liassefiscale.controle.ResultatControle;
import fr.neolegal.fec.liassefiscale.controle.StatutControle;
import lombok.Builder;
import lombok.Data;

/* Modélisation de la liasse fiscale, contenant huit tableaux comptables, qui portent les
numéros 2050 (bilan-actif), 2051 (bilan-passif), 2052 et 2053
(compte de résultat, en liste), 2054 (immobilisations), 2055
(amortissements), 2056 (provisions) et 2057 (état des
échéances des créances et des dettes à la clôture de l’exer-
cice)  */
@Data
public class LiasseFiscale {

    String siren;
    LocalDate clotureExercice;
    RegimeImposition regime;
    final List<Formulaire> formulaires = new LinkedList<>();

    /** Résultat des contrôles de cohérence comptable exécutés sur la liasse */
    final List<ResultatControle> controles = new ArrayList<>();

    /** Anomalies rencontrées pendant la lecture du document */
    final List<Anomalie> anomalies = new ArrayList<>();

    /** Probabilité que les montants extraits soient exacts */
    Fiabilite fiabilite;

    @Builder
    public LiasseFiscale(RegimeImposition regime, String siren, LocalDate clotureExercice) {
        this.siren = siren;
        this.clotureExercice = clotureExercice;
        this.regime = regime;
    }

    /**
     * Renvoie le formulaire correspondant au modèle. Si la liasse ne le contient
     * pas, un formulaire vide est renvoyé, sans être ajouté à la liasse.
     *
     * @see #getOrAddFormulaire(ModeleFormulaire)
     */
    public Formulaire getFormulaire(ModeleFormulaire modele) {
        return formulaires.stream().filter(f -> Objects.equals(f.getModele(), modele)).findFirst()
                .orElseGet(() -> Formulaire.builder().modele(modele).build());
    }

    /** Renvoie le formulaire correspondant au modèle, en l'ajoutant si nécessaire. */
    public Formulaire getOrAddFormulaire(ModeleFormulaire modele) {
        return formulaires.stream().filter(f -> Objects.equals(f.getModele(), modele)).findFirst()
                .orElseGet(() -> {
                    Formulaire formulaire = Formulaire.builder().modele(modele).build();
                    formulaires.add(formulaire);
                    return formulaire;
                });
    }

    public Optional<Formulaire> getFormulaire(String identifiant) {
        return formulaires.stream().filter(f -> Objects.equals(f.getIdentifiant(), identifiant)).findFirst();
    }

    public Annexe getAnnexe(NatureAnnexe natureAnnexe) {
        return formulaires.stream().flatMap(f -> f.getAnnexe(natureAnnexe).stream()).findFirst()
                .orElse(Annexe.builder().natureAnnexe(natureAnnexe).build());
    }

    /**
     * Renvoie le montant correspondant au repère passé en paramètre, s'il est
     * connu.
     */
    public Optional<Double> getMontant(String repere) {
        return formulaires.stream().flatMap(f -> f.getMontant(repere).stream()).findFirst();
    }

    public Optional<Double> getMontant(Repere repere) {
        if (Objects.isNull(repere)) {
            return Optional.empty();
        }
        return getMontant(repere.getSymbole());
    }

    public Optional<Repere> getRepere(String symbole) {
        return formulaires.stream().flatMap(f -> f.getRepere(symbole).stream()).findFirst();
    }

    /**
     * Renvoie le détail de l'extraction du montant : méthode employée, page
     * d'origine et probabilité que la valeur soit exacte.
     */
    public Optional<MontantExtrait> getMontantExtrait(String symbole) {
        return formulaires.stream().flatMap(f -> f.getMontantExtrait(symbole).stream())
                .max(Comparator.comparing(MontantExtrait::getConfiance));
    }

    public Optional<MontantExtrait> getMontantExtrait(Repere repere) {
        return Objects.isNull(repere) ? Optional.empty() : getMontantExtrait(repere.getSymbole());
    }

    /** Contrôles de cohérence comptable n'ayant pas été satisfaits. */
    public List<ResultatControle> getControlesEnEchec() {
        return Collections.unmodifiableList(
                controles.stream().filter(c -> c.getStatut() == StatutControle.ECHEC).toList());
    }

    /** Ensemble des montants extraits de la liasse. */
    public List<MontantExtrait> getMontantsExtraits() {
        List<MontantExtrait> montants = new ArrayList<>();
        formulaires.forEach(formulaire -> montants.addAll(formulaire.getMontantsExtraits()));
        return montants;
    }

    /** Probabilité que les montants extraits soient exacts, entre 0 et 1. */
    public double getScoreFiabilite() {
        return fiabilite == null ? 0 : fiabilite.getScore();
    }
}
