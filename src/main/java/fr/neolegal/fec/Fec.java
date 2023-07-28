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
    final double chiffreAffaires;
    final double reserveLegale;
    final double autresReserves;
    final double reportANouveau;
    final double capitauxPropres; // DL
    final double totalBilan;
    final double produitExploitation; // FR
    final double chargesExploitation; // GF
    final double chargesPersonnel; // FY
    final double achatsMarchandises; // FS
    final double chargesSociales; // FZ
    final double resultatExploitation; // GG

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
        this.chiffreAffaires = FecHelper.computeSoldeCompte(lignes, PCG.VENTES_DE_PRODUITS);
        this.reserveLegale = FecHelper.computeSoldeCompte(lignes, PCG.RESERVE_LEGALE);
        this.autresReserves = FecHelper.computeSoldeCompte(lignes, PCG.AUTRES_RESERVES);
        this.reportANouveau = FecHelper.computeSoldeReportANouveau(lignes);
        this.capitauxPropres = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("DL"));
        this.totalBilan = FecHelper.computeTotalBilan(lignes);
        this.produitExploitation = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("FR"));
        this.chargesExploitation = FecHelper.computeSoldeComptesByNumero(lignes, LignesReperesHelper.resolveComptes("GF"));
        this.chargesPersonnel = FecHelper.computeSoldeComptes(lignes,
                Set.of(PCG.TRAITEMENTS_ET_SALAIRES, PCG.REMUNERATION_EXPLOLITANT, PCG.AUTRES_CHARGES_PERSONNEL));
        this.chargesSociales = FecHelper.computeSoldeComptesByNumero(lignes,
                Set.of("645", "646", "647", "648"));
        this.achatsMarchandises = FecHelper.computeSoldeComptes(lignes,
                Set.of(PCG.ACHATS_MARCHANDISES, PCG.RABAIS_ET_RISTOURNES));
        this.resultatExploitation = FecHelper.computeSoldeComptesByNumero(lignes,
                Set.of("645", "646", "647", "648"));
    }
}
