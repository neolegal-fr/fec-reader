package fr.neolegal.fec.liassefiscale;

import java.awt.Point;
import org.apache.commons.lang3.ObjectUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "symbole", "nom", "coordonnees", "expression" })
public class Repere implements Comparable<Repere> {
    /** Code alphabétique de 2 caractères en majuscule */
    String symbole;

    /** Nom du repère */
    String nom;

    /** Expression pour le calcul du montant de la ligne */
    @JsonProperty("formuleFEC")
    String expression;

    /** Chemin d'accès à la valeur, relativement à la position du repère */
    Point fromSymbole;

    /** Chemin d'accès à la valeur, relativement à la position du nom */
    Point fromNom;

    @Builder
    public Repere(String symbole, String nom, String expression, Point fromSymbole, Point fromNom) {
        this.symbole = symbole;
        this.expression = expression;
        this.nom = nom;
        this.fromSymbole = fromSymbole;
        this.fromNom = fromNom;
    }

    public Repere() {
    }

    @Override
    public String toString() {
        return symbole;
    }

    @Override
    public int compareTo(Repere other) {
        if (other == this) {
            return 0;
        }

        if (other == null) {
            return 1;
        }

        return ObjectUtils.compare(symbole, other.symbole);
    }
}
