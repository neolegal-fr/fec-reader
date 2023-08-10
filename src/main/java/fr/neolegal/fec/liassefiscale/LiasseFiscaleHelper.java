package fr.neolegal.fec.liassefiscale;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.neolegal.fec.Fec;

public class LiasseFiscaleHelper {

    private LiasseFiscaleHelper() {
    }

    public static LiasseFiscale buildLiasseFiscale(Fec fec, RegimeImposition regime) {
        LiasseFiscale liasse = LiasseFiscale.builder().siren(fec.getSiren()).clotureExercice(fec.getClotureExercice())
                .build();
        for (NatureFormulaire formulaire : NatureFormulaire.values()) {
            if (formulaire.getRegimeImposition() == regime) {
                liasse.getFormulaires().add(buildFormulaire(fec, formulaire));
            }            
        }
        
        return liasse;
    }

    public static Formulaire buildFormulaire(Fec fec, NatureFormulaire nature) {
        Formulaire tableau = new Formulaire(nature);

        List<Repere> reperes = Repere.DEFINITIONS.values().stream()
                .filter(ligne -> Objects.equals(ligne.getFormulaire(), nature)).collect(Collectors.toList());
        for (Repere repere : reperes) {

            Double montant = RepereHelper.computeMontantLigneRepere(repere, fec).orElse(null);
            tableau.getChamps().put(repere, montant);
        }
        return tableau;
    }
}
