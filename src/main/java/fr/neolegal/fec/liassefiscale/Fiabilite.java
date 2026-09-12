package fr.neolegal.fec.liassefiscale;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.neolegal.fec.liassefiscale.controle.ResultatControle;
import fr.neolegal.fec.liassefiscale.controle.StatutControle;
import lombok.Getter;

/**
 * Indicateur de fiabilité d'une extraction : probabilité que les montants lus
 * soient exacts, assortie des éléments qui la justifient.
 * <p>
 * Elle combine trois familles d'indices :
 * <ul>
 * <li>la <b>couverture</b> : proportion des repères du formulaire effectivement
 * localisés dans le document ;</li>
 * <li>la <b>confiance d'extraction</b> : qualité de la lecture de chaque
 * cellule (code du repère lu ou libellé reconnu, alignement des colonnes,
 * recours à l'OCR) ;</li>
 * <li>la <b>cohérence comptable</b> : vérification des totaux, sous-totaux et
 * égalités structurelles de la liasse.</li>
 * </ul>
 */
@Getter
public class Fiabilite {

    /** Au-delà de ce score, un montant est considéré comme fiable */
    public static final double SEUIL_FIABLE = 0.80;

    private final double score;
    private final double couverture;
    private final double confianceMoyenne;
    private final double tauxCoherence;
    private final int nombreMontants;
    private final int nombreControles;
    private final int nombreControlesEnEchec;
    private final Map<String, String> details;

    Fiabilite(double score, double couverture, double confianceMoyenne, double tauxCoherence, int nombreMontants,
            int nombreControles, int nombreControlesEnEchec, Map<String, String> details) {
        this.score = score;
        this.couverture = couverture;
        this.confianceMoyenne = confianceMoyenne;
        this.tauxCoherence = tauxCoherence;
        this.nombreMontants = nombreMontants;
        this.nombreControles = nombreControles;
        this.nombreControlesEnEchec = nombreControlesEnEchec;
        this.details = Collections.unmodifiableMap(details);
    }

    public boolean estFiable() {
        return score >= SEUIL_FIABLE;
    }

    /** Calcule la fiabilité d'un ensemble de montants et de contrôles. */
    public static Fiabilite calculer(List<MontantExtrait> montants, List<ResultatControle> controles,
            int nombreReperesAttendus) {
        int nombreMontants = montants.size();
        double couverture = nombreReperesAttendus == 0 ? 0
                : Math.min(1.0, (double) nombreMontants / nombreReperesAttendus);
        double confianceMoyenne = montants.stream().mapToDouble(MontantExtrait::getConfiance).average().orElse(0);

        long applicables = controles.stream().filter(c -> c.getStatut() != StatutControle.NON_APPLICABLE).count();
        long satisfaits = controles.stream().filter(c -> c.getStatut() == StatutControle.SATISFAIT).count();
        double tauxCoherence = applicables == 0 ? -1 : (double) satisfaits / applicables;

        // Sans contrôle applicable, seule la qualité de lecture est mesurable ; la
        // cohérence comptable est le signal le plus discriminant lorsqu'elle existe.
        double score;
        if (tauxCoherence < 0) {
            score = confianceMoyenne * (0.6 + 0.4 * couverture);
        } else {
            score = (0.45 * confianceMoyenne + 0.45 * tauxCoherence + 0.10 * couverture);
        }
        score = Math.max(0, Math.min(1, score));

        Map<String, String> details = new LinkedHashMap<>();
        details.put("couverture", String.format("%.0f%% des repères du modèle localisés (%d/%d)", couverture * 100,
                nombreMontants, nombreReperesAttendus));
        details.put("confiance", String.format("confiance moyenne de lecture : %.0f%%", confianceMoyenne * 100));
        details.put("coherence", applicables == 0 ? "aucun contrôle de cohérence applicable"
                : String.format("%d/%d contrôles de cohérence satisfaits", satisfaits, applicables));

        return new Fiabilite(score, couverture, confianceMoyenne, Math.max(tauxCoherence, 0), nombreMontants,
                (int) applicables, (int) (applicables - satisfaits), details);
    }

    @Override
    public String toString() {
        return String.format("fiabilité %.0f%% (couverture %.0f%%, lecture %.0f%%, cohérence %.0f%%)", score * 100,
                couverture * 100, confianceMoyenne * 100, tauxCoherence * 100);
    }
}
