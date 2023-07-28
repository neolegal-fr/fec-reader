package fr.neolegal.fec;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import lombok.Builder;
import lombok.Getter;

/** Fichier des écitures comptables */
@Getter
public class Fec {
    final String siren;
    final LocalDate clotureExercice;
    final List<EcritureComptable> ecritures;
    final List<Anomalie> anomalies;

    @Builder
    public Fec(List<EcritureComptable> ecritures, String siren, LocalDate clotureExercice, List<Anomalie> anomalies) {
        this.ecritures = ObjectUtils.firstNonNull(ecritures, new LinkedList<>());
        this.siren = siren;
        this.clotureExercice = clotureExercice;
        this.anomalies = ObjectUtils.firstNonNull(anomalies, new LinkedList<>());
    }
}
