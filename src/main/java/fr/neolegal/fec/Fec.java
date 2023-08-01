package fr.neolegal.fec;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import lombok.Builder;
import lombok.Getter;

/** Fichier des écitures comptables */
@Getter
public class Fec {
    final String siren;
    final LocalDate clotureExercice;
    final List<LEC> lignes;
    final long nombreEcritures;
    final List<Anomalie> anomalies;
    final Set<String> journaux;

    @Builder
    public Fec(List<LEC> lignes, String siren, LocalDate clotureExercice, List<Anomalie> anomalies) {
        this.lignes = ObjectUtils.firstNonNull(lignes, new LinkedList<>());
        this.siren = siren;
        this.clotureExercice = clotureExercice;
        this.anomalies = ObjectUtils.firstNonNull(anomalies, new LinkedList<>());
        this.nombreEcritures = FecHelper.countEcritures(lignes);
        this.journaux = FecHelper.resolveJournaux(lignes);
    }
}
