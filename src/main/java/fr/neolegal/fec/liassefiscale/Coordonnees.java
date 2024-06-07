package fr.neolegal.fec.liassefiscale;

import lombok.Data;

/**
 * Les coordonnées d'une cellule dans un formulaire, relatives à un repère, en
 * décalant de x cellules vers la gauche ou la droite, et y cellules vers le
 * haut ou le bas
 */
@Data
public class Coordonnees {

    String repere;
    Integer x;
    Integer y;
}
