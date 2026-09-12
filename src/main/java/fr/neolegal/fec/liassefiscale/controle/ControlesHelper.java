package fr.neolegal.fec.liassefiscale.controle;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import fr.neolegal.fec.liassefiscale.Formulaire;
import fr.neolegal.fec.liassefiscale.LiasseFiscale;

/**
 * Chargement et exécution des contrôles de cohérence comptable.
 * <p>
 * Les contrôles sont décrits dans {@code /controles/controles.json} : ils
 * vérifient les totaux et sous-totaux de chaque formulaire, ainsi que les
 * égalités entre formulaires (équilibre du bilan, report du résultat...). Un
 * contrôle en échec signale soit une erreur de lecture, soit une incohérence du
 * document d'origine : dans les deux cas, les montants concernés ne peuvent pas
 * être considérés comme fiables.
 */
public class ControlesHelper {

    private static final Logger LOGGER = Logger.getLogger(ControlesHelper.class.getName());

    private static final String RESSOURCE = "/controles/controles.json";

    private static final List<ControleCoherence> CONTROLES = charger();

    private ControlesHelper() {
    }

    public static List<ControleCoherence> getControles() {
        return CONTROLES;
    }

    static List<ControleCoherence> charger() {
        try (InputStream is = ControlesHelper.class.getResourceAsStream(RESSOURCE)) {
            if (is == null) {
                LOGGER.log(Level.WARNING, "Contrôles de cohérence introuvables : {0}", RESSOURCE);
                return List.of();
            }
            ObjectMapper mapper = new ObjectMapper();
            CollectionType type = mapper.getTypeFactory().constructCollectionType(List.class,
                    ControleCoherence.class);
            List<ControleCoherence> controles = mapper.readValue(is, type);
            return Collections.unmodifiableList(controles);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Chargement des contrôles de cohérence impossible : " + e.getMessage());
            return List.of();
        }
    }

    /** Exécute les contrôles applicables à la liasse. */
    public static List<ResultatControle> executer(LiasseFiscale liasse) {
        Set<String> formulaires = new LinkedHashSet<>();
        for (Formulaire formulaire : liasse.getFormulaires()) {
            if (formulaire.getIdentifiant() != null) {
                formulaires.add(formulaire.getIdentifiant());
            }
        }

        List<ResultatControle> resultats = new ArrayList<>();
        for (ControleCoherence controle : CONTROLES) {
            if (controle.getFormulaires().isEmpty() || formulaires.containsAll(controle.getFormulaires())) {
                resultats.add(executer(controle, liasse));
            }
        }
        return resultats;
    }

    static ResultatControle executer(ControleCoherence controle, LiasseFiscale liasse) {
        Set<String> manquants = new LinkedHashSet<>();
        boolean toutNul = true;
        boolean gaucheConnue = false;

        double valeurGauche = 0;
        for (String membre : controle.getGauche()) {
            String symbole = ControleCoherence.symbole(membre);
            Double montant = liasse.getMontant(symbole).orElse(null);
            if (montant == null) {
                manquants.add(symbole);
            } else {
                gaucheConnue = true;
                toutNul = toutNul && montant == 0.0;
                valeurGauche += ControleCoherence.signe(membre) * montant;
            }
        }

        double valeurDroite = 0;
        for (String membre : controle.getDroite()) {
            String symbole = ControleCoherence.symbole(membre);
            Double montant = liasse.getMontant(symbole).orElse(null);
            if (montant == null) {
                manquants.add(symbole);
            } else {
                toutNul = toutNul && montant == 0.0;
                valeurDroite += ControleCoherence.signe(membre) * montant;
            }
        }

        ResultatControle.ResultatControleBuilder builder = ResultatControle.builder()
                .identifiant(controle.getIdentifiant())
                .libelle(controle.getLibelle())
                .valeurGauche(valeurGauche)
                .valeurDroite(valeurDroite)
                .reperes(controle.getReperes())
                .reperesManquants(manquants);

        // Un contrôle n'apporte d'information que si le total est connu et que les
        // montants ne sont pas tous nuls
        if (!gaucheConnue || toutNul) {
            return builder.statut(StatutControle.NON_APPLICABLE).build();
        }

        boolean satisfait = Math.abs(valeurGauche - valeurDroite) <= controle.getTolerance();
        return builder.statut(satisfait ? StatutControle.SATISFAIT : StatutControle.ECHEC).build();
    }
}
