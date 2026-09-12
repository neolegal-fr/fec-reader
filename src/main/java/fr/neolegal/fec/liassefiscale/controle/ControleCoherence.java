package fr.neolegal.fec.liassefiscale.controle;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contrôle de cohérence comptable : égalité entre la somme des repères de
 * gauche et la somme des repères de droite.
 * <p>
 * Un repère peut être préfixé de {@code -} pour être soustrait. Ces contrôles
 * reproduisent les vérifications effectuées par l'administration fiscale sur
 * les liasses télétransmises (totaux, sous-totaux, équilibre du bilan,
 * cohérence entre formulaires).
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ControleCoherence {

    /** Tolérance par défaut, en euros, pour absorber les arrondis */
    public static final double TOLERANCE_DEFAUT = 1.0;

    private String identifiant;

    /** Identifiants des formulaires concernés, pour n'exécuter que les contrôles utiles */
    private List<String> formulaires = List.of();

    private String libelle;

    /** Repères de la partie gauche de l'égalité */
    private List<String> gauche = List.of();

    /** Repères de la partie droite de l'égalité */
    private List<String> droite = List.of();

    private Double tolerance;

    public double getTolerance() {
        return tolerance == null ? TOLERANCE_DEFAUT : tolerance;
    }

    /** Ensemble des repères utilisés par le contrôle, sans les signes. */
    @JsonIgnore
    public Set<String> getReperes() {
        Set<String> reperes = new LinkedHashSet<>();
        gauche.forEach(repere -> reperes.add(symbole(repere)));
        droite.forEach(repere -> reperes.add(symbole(repere)));
        return reperes;
    }

    static String symbole(String repere) {
        return repere.startsWith("-") || repere.startsWith("+") ? repere.substring(1) : repere;
    }

    static double signe(String repere) {
        return repere.startsWith("-") ? -1 : 1;
    }

    @Override
    public String toString() {
        return identifiant + " : " + libelle;
    }
}
