package fr.neolegal.fec.liassefiscale.controle;

import java.util.Set;

import lombok.Builder;
import lombok.Getter;

/** Résultat de l'exécution d'un contrôle de cohérence sur une liasse. */
@Getter
@Builder
public class ResultatControle {

    private final String identifiant;
    private final String libelle;
    private final StatutControle statut;

    /** Somme des repères de la partie gauche */
    private final double valeurGauche;

    /** Somme des repères de la partie droite */
    private final double valeurDroite;

    /** Repères impliqués dans le contrôle */
    private final Set<String> reperes;

    /** Repères du contrôle qui n'ont pas été extraits du document */
    private final Set<String> reperesManquants;

    public double getEcart() {
        return valeurGauche - valeurDroite;
    }

    public boolean estEnEchec() {
        return statut == StatutControle.ECHEC;
    }

    @Override
    public String toString() {
        if (statut == StatutControle.NON_APPLICABLE) {
            return String.format("%s : non applicable (%s)", identifiant, libelle);
        }
        return String.format("%s : %s (%s) %.0f vs %.0f, écart %.0f", identifiant, statut, libelle, valeurGauche,
                valeurDroite, getEcart());
    }
}
