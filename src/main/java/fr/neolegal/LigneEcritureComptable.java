package fr.neolegal;

import java.util.Date;

import lombok.Data;

/** Modélisation d'une ligne du fichier des écritures comptables, conformément à la norme définie par 
 *  l’article A.47 A-1 du livre des procédures fiscales. 
 * https://www.legifrance.gouv.fr/codes/article_lc/LEGIARTI000027804775
 */
@Data
public class LigneEcritureComptable {
    String journalCode;
    String journalLib;
    Long ecritureNum;
    Date ecritureDate;
    Long compteNum;
    String compteLib;
    String compAuxNum;
    String compAuxLib;
    String pieceRef;
    Date pieceDate;
    String ecritureLib;
    Double debit;
    Double credit;
    String ecritureLet;
    Date dateLet;
    Date validDate;
    String montantdevise;
    String idevise;
    Date dateRglt;
    String modeRglt;
    String natOp;
    String idClient;
}
