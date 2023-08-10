package fr.neolegal.fec.liassefiscale;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.neolegal.fec.Fec;
import net.objecthunter.exp4j.VariableProvider;

public class LiasseFiscaleHelper {

    private LiasseFiscaleHelper() {
    }

    public static LiasseFiscale buildLiasseFiscale(RegimeImposition regime) {
        LiasseFiscale liasse = LiasseFiscale.builder().build();
        for (NatureFormulaire formulaire : NatureFormulaire.values()) {
            if (formulaire.getRegimeImposition() == regime) {
                liasse.getFormulaires().add(buildFormulaire(formulaire));
            }
        }

        return liasse;
    }

    public static LiasseFiscale buildLiasseFiscale(Fec fec, RegimeImposition regime) {

        LiasseFiscale liasse = buildLiasseFiscale(regime);
        liasse.setSiren(fec.getSiren());
        liasse.setClotureExercice(fec.getClotureExercice());

        VariableProvider provider = new FecVariableProvider(fec);
        for (Formulaire formulaire : liasse.getFormulaires()) {
            for (Repere repere : formulaire.getChamps().keySet()) {
                Double montant = RepereHelper.computeMontantLigneRepere(repere, fec, provider).orElse(null);
                formulaire.getChamps().put(repere, montant);
            }
        }

        return liasse;
    }

    public static Formulaire buildFormulaire(NatureFormulaire nature) {
        Formulaire tableau = new Formulaire(nature);

        List<Repere> reperes = Repere.DEFINITIONS.values().stream()
                .filter(ligne -> Objects.equals(ligne.getFormulaire(), nature)).collect(Collectors.toList());
        for (Repere repere : reperes) {
            tableau.getChamps().put(repere, null);
        }
        return tableau;
    }
}
