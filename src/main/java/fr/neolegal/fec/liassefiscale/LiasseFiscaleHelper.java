package fr.neolegal.fec.liassefiscale;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import fr.neolegal.fec.Fec;

public class LiasseFiscaleHelper {

    public final static Character MIN_REPERE = 'A';
    public final static Character MAX_REPERE = 'O';
    public final static int MIN_NUM_COMPTE = 1;
    public final static int MAX_NUM_COMPTE = 7999;
    public final static String REPERE_REGEX = "(?i)([" + MIN_REPERE + "-" + MAX_REPERE + "][A-Z])";

    private LiasseFiscaleHelper() {
    }

    static LiasseFiscale buildLiasseFiscale(Fec fec) {
        LiasseFiscale liasse = LiasseFiscale.builder().siren(fec.getSiren()).clotureExercice(fec.getClotureExercice())
                .build();
        for (Formulaire formulaire : Formulaire.values()) {
            liasse.getFormulaires().add(buildTableauComptable(fec, formulaire));
        }
        
        return liasse;
    }

    static TableauComptable buildTableauComptable(Fec fec, Formulaire formulaire) {
        TableauComptable tableau = new TableauComptable(formulaire);

        List<Repere> reperes = Repere.REPERES.values().stream()
                .filter(ligne -> Objects.equals(ligne.getFormulaire(), formulaire)).collect(Collectors.toList());
        for (Repere repere : reperes) {

            double montant = RepereHelper.computeMontantLigneRepere(repere, fec);
            tableau.getLignes().put(repere, montant);
        }
        return tableau;
    }
}
