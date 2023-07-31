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
    final double chiffreAffaires; // FL
    final double reserveLegale; // DD
    final double autresReserves; // DG
    final double reportANouveau;
    final double capitauxPropres; // DL
    final double totalBilan; // EE
    final double produitExploitation; // FR
    final double chargesExploitation; // GF
    final double salairesEtTraitements; // FY
    final double achatsMarchandises; // FS
    final double chargesSociales; // FZ
    final double resultatExploitation; // GG
    final double resultatFinancier; // GV

    @Builder
    public Fec(List<LEC> lignes, String siren, LocalDate clotureExercice, List<Anomalie> anomalies) {
        this.lignes = ObjectUtils.firstNonNull(lignes, new LinkedList<>());
        this.siren = siren;
        this.clotureExercice = clotureExercice;
        this.anomalies = ObjectUtils.firstNonNull(anomalies, new LinkedList<>());
        this.nombreEcritures = FecHelper.countEcritures(lignes);
        this.journaux = FecHelper.resolveJournaux(lignes);

        /** Documentation des calculs : 
         * https://www.lexisnexis.fr/__data/assets/pdf_file/0006/642363/rdi1903.pdf
         */
        this.chiffreAffaires = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("FL"));
        this.reserveLegale = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("DD"));
        this.autresReserves = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("DG"));
        this.reportANouveau = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("DH"));
        this.capitauxPropres = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("DL"));
        this.totalBilan = FecHelper.computeTotalBilan(lignes);
        this.produitExploitation = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("FR"));
        this.chargesExploitation = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("GF"));
        this.salairesEtTraitements = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("FY"));
        this.chargesSociales = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("FZ"));;
        this.resultatFinancier = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("GV"));;
        this.achatsMarchandises = FecHelper.computeSoldeComptes(lignes,
                Set.of(PCG.ACHATS_MARCHANDISES, PCG.RABAIS_ET_RISTOURNES));
        this.resultatExploitation = FecHelper.computeSoldeComptesByNumero(lignes,
                Set.of("645", "646", "647", "648"));
    }
}
