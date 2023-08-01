package fr.neolegal.fec.liassefiscale;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;

import lombok.Builder;
import lombok.Data;

@Data
public class TableauComptable {
    String nom;
    String numero;
    String cerfa;
    Map<LigneRepere, Double> lignes = new HashMap<>();

    @Builder
    public TableauComptable(String nom, String numero, String cerfa, Map<LigneRepere, Double> lignes) {
        this.nom = nom;
        this.numero = numero;
        this.cerfa = cerfa;
        this.lignes = ObjectUtils.firstNonNull(lignes, new HashMap<>());
    }

    public TableauComptable(String nom, String numero, String cerfa) {
        this(nom, numero, cerfa, null);
    }

    public Optional<Double> getMontant(String repere) {
        LigneRepere ligne = LigneRepere.get(repere);
        if (Objects.isNull(ligne)) {
            return Optional.empty();
        }
        return Optional.ofNullable(lignes.get(ligne));
    }
}
