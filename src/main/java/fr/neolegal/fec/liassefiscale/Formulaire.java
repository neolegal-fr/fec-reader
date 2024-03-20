package fr.neolegal.fec.liassefiscale;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import lombok.Builder;
import lombok.Data;

@Data
public class Formulaire {
    NatureFormulaire nature;
    Map<Repere, Double> champs = new HashMap<>();
    List<Annexe> annexes = new LinkedList<>();

    @Builder
    public Formulaire(NatureFormulaire nature, Map<Repere, Double> champs, List<Annexe> annexes) {
        this.nature = nature;
        this.champs = ObjectUtils.firstNonNull(champs, new HashMap<>());
        this.annexes = ObjectUtils.firstNonNull(annexes, new LinkedList<>());
    }

    public Formulaire(NatureFormulaire formulaire) {
        this(formulaire, null, null);
    }

    public Double getMontant(String repere, Double defaultMontant) {
        return getMontant(repere).orElse(defaultMontant);
    }

    public void setMontant(Repere repere, Double montant) {
        champs.put(repere, montant);
    }

    public Optional<Double> getMontant(String symboleRepere) {
        return Repere.get(nature, symboleRepere).map(repere -> champs.get(repere));
    }

    Set<Repere> reperes() {
        return champs.keySet();
    }

    long nbMontantsNonNull() {
        return champs.values().stream().filter(montant -> montant != null && montant != 0.0).count();
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
