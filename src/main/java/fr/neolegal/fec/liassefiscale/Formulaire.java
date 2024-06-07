package fr.neolegal.fec.liassefiscale;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.trim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Objects.nonNull;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import lombok.Builder;
import lombok.Data;

@Data
public class Formulaire {
    final ModeleFormulaire modele;
    final Set<Repere> cachedReperes;
    Map<String, Double> valeurs = new HashMap<>();
    List<Annexe> annexes = new LinkedList<>();

    @Builder
    public Formulaire(ModeleFormulaire modele, Map<String, Double> valeurs, List<Annexe> annexes) {
        this.modele = modele;
        this.cachedReperes = modele.getAllReperes();
        this.valeurs = ObjectUtils.firstNonNull(valeurs, new HashMap<>());
        this.annexes = ObjectUtils.firstNonNull(annexes, new LinkedList<>());
    }

    public Double getMontant(String repere, Double defaultMontant) {
        return getMontant(repere).orElse(defaultMontant);
    }

    public void setMontant(Repere repere, Double montant) {
        setMontant(repere.getSymbole(), montant);
    }

    public void setMontant(String repere, Double montant) {
        valeurs.put(repere, montant);
    }

    public Optional<Double> getMontant(String symboleRepere) {
        return Optional.ofNullable(valeurs.get(symboleRepere));
    }

    Set<Repere> getAllReperes() {
        return cachedReperes;
    }

    public Optional<Repere> getRepere(String symbole) {
        return getAllReperes().stream().filter(repere -> equalsIgnoreCase(trim(symbole), repere.getSymbole()))
                .findFirst();
    }

    public Optional<Repere> getRepereByNomIfNavigationDefined(String nom) {
        return getAllReperes().stream().filter(repere -> nonNull(repere.getFromNom()))
                .filter(repere -> StrUtils.containsIgnoreCase(nom.replaceAll("[^a-zA-Z]", ""), repere.getNom().replaceAll("[^a-zA-Z]", "")))
                .findFirst();
    }

    long nbMontantsNonNull() {
        return valeurs.values().stream().filter(montant -> montant != null && montant != 0.0).count();
    }

    public Optional<Annexe> getAnnexe(NatureAnnexe natureAnnexe) {
        return annexes.stream().filter(annexe -> annexe.getNatureAnnexe() == natureAnnexe).findFirst();
    }

    public Annexe addAnnexe(NatureAnnexe natureAnnexe) {
        Annexe annexe = Annexe.builder().natureAnnexe(natureAnnexe).build();
        annexes.add(annexe);
        return annexe;
    }

    public Annexe getOrAddAnnexe(NatureAnnexe natureAnnexe) {
        return annexes.stream().filter(annexe -> annexe.getNatureAnnexe() == natureAnnexe).findFirst()
                .orElseGet(() -> addAnnexe(natureAnnexe));
    }
}
