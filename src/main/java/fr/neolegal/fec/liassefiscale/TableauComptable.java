package fr.neolegal.fec.liassefiscale;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import lombok.Builder;
import lombok.Data;

@Data
public class TableauComptable {
    Formulaire formulaire;
     Map<Repere, Double> lignes = new HashMap<>();

    @Builder
    public TableauComptable(Formulaire formulaire, Map<Repere, Double> lignes) {
        this.formulaire = formulaire;
        this.lignes = ObjectUtils.firstNonNull(lignes, new HashMap<>());
    }

    public TableauComptable(Formulaire formulaire) {
        this(formulaire, null);
    }

    public Double getMontant(String repere, Double defaultMontant) {
        return getMontant(repere).orElse(defaultMontant);
    }

    public Optional<Double> getMontant(String repere) {
        Repere ligne = Repere.get(repere);
        if (Objects.isNull(ligne)) {
            return Optional.empty();
        }
        return Optional.ofNullable(lignes.get(ligne));
    }

    Set<Repere> reperes() {
        return lignes.keySet();
    }
}
