package fr.neolegal.fec;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/** Fichier des écitures comptables */
@Getter
@Setter
public class Fec {
    String siren;
    LocalDate clotureExercice;
    final List<LEC> lignes;
    final long nombreEcritures;
    final List<Anomalie> anomalies;
    final Set<String> journaux;

    /** Soldes des comptes, calculés une seule fois pour l'ensemble des écritures */
    final SoldesComptes soldes;

    @Builder
    public Fec(List<LEC> lignes, String siren, LocalDate clotureExercice, List<Anomalie> anomalies) {
        this.lignes = ObjectUtils.firstNonNull(lignes, new LinkedList<>());
        this.siren = siren;
        this.clotureExercice = clotureExercice;
        this.anomalies = ObjectUtils.firstNonNull(anomalies, new LinkedList<>());
        this.nombreEcritures = FecHelper.countEcritures(this.lignes);
        this.journaux = FecHelper.resolveJournaux(this.lignes);
        this.soldes = new SoldesComptes(this.lignes);
    }
}
