package fr.neolegal.fec;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import lombok.Builder;
import lombok.Data;

/**
 * Modélisation d'une ligne du fichier des écritures comptables, conformément à
 * la norme définie par
 * l’article A.47 A-1 du livre des procédures fiscales.
 * https://www.legifrance.gouv.fr/codes/article_lc/LEGIARTI000027804775
 */
@Data
public class LEC {
    final String journalCode; // Col 1
    final String journalLib; // Col 2
    final String ecritureNum; // Col 3
    final LocalDate ecritureDate; // Col 4
    final String compteNum; // Col 5
    final String compteLib; // Col 6
    final String compAuxNum; // Col 7
    final String compAuxLib; // Col 8
    final String pieceRef; // Col 9
    final LocalDate pieceDate; // Col 10
    final String ecritureLib; // Col 11
    final Double debit; // Col 12
    final Double credit; // Col 13
    final String ecritureLet; // Col 14
    final LocalDate dateLet; // Col 15
    final LocalDate validDate; // Col 16
    final Double montantdevise; // Col 17
    final String idevise; // Col 18
    final LocalDate dateRglt; // Col 19
    final String modeRglt; // Col 20
    final String natOp; // Col 21
    final String idClient; // Col 22

    final List<Anomalie> anomalies;

    @Builder
    public LEC(String journalCode,
            String journalLib,
            String ecritureNum,
            LocalDate ecritureDate,
            String compteNum,
            String compteLib,
            String compAuxNum,
            String compAuxLib,
            String pieceRef,
            LocalDate pieceDate,
            String ecritureLib,
            Double debit,
            Double credit,
            String ecritureLet,
            LocalDate dateLet,
            LocalDate validDate,
            Double montantdevise,
            String idevise,
            LocalDate dateRglt,
            String modeRglt,
            String natOp,
            String idClient,
            List<Anomalie> anomalies) {
        this.journalCode = journalCode;
        this.journalLib = journalLib;
        this.ecritureNum = ecritureNum;
        this.ecritureDate = ecritureDate;
        this.compteNum = compteNum;
        this.compteLib = compteLib;
        this.compAuxNum = compAuxNum;
        this.compAuxLib = compAuxLib;
        this.pieceRef = pieceRef;
        this.pieceDate = pieceDate;
        this.ecritureLib = ecritureLib;
        this.debit = debit;
        this.credit = credit;
        this.ecritureLet = ecritureLet;
        this.dateLet = dateLet;
        this.validDate = validDate;
        this.montantdevise = montantdevise;
        this.idevise = idevise;
        this.dateRglt = dateRglt;
        this.modeRglt = modeRglt;
        this.natOp = natOp;
        this.idClient = idClient;
        this.anomalies = ObjectUtils.firstNonNull(anomalies, new LinkedList<>());
    }
}
