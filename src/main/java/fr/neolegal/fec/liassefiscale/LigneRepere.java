package fr.neolegal.fec.liassefiscale;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import net.objecthunter.exp4j.VariableProvider;

@Data
public class LigneRepere {

    public static final CharSequence PREFIXE_VARIABLE_COMPTE = "PCG_";

    static VariableProvider variables = new Variables();
    static Map<String, LigneRepere> REPERES = buildDefinitionReperes();

    private static Map<String, LigneRepere> buildDefinitionReperes() {
        List<LigneRepere> reperes = new LinkedList<>();
        reperes.add(new LigneRepere("AA", "", "PCG_109"));
        reperes.add(new LigneRepere("DA", "Capital social ou individuel", "PCG_101 + PCG_108"));
        reperes.add(new LigneRepere("DB", "Primes d'émission, de fusion, d'apport", "PCG_104"));
        reperes.add(new LigneRepere("DC", "Ecarts de réévaluation", "PCG_105"));
        reperes.add(new LigneRepere("DD", "Réserve légale", "PCG_1061"));
        reperes.add(new LigneRepere("DE", "Réserves statutaires ou contractuelles", "PCG_1063"));
        reperes.add(new LigneRepere("DF", "Réserves règlementées", "PCG_1062 + PCG_1064"));
        reperes.add(new LigneRepere("DG", "Autres réserves", "PCG_1068"));
        reperes.add(new LigneRepere("DH", "Report à nouveau", "PCG_11"));
        reperes.add(new LigneRepere("DI", "Résultat de l'exercice", "PCG_12"));
        reperes.add(new LigneRepere("DJ", "Subvention d'investissement", "PCG_13"));
        reperes.add(new LigneRepere("DK", "Provisions règlementées", "PCG_14"));
        reperes.add(new LigneRepere("DL", "Total (I)", "DA + DB + DC + DD + DE + DF + DG + DH + DI + DJ + DK"));
        reperes.add(new LigneRepere("DM", "Produit des émissions de titres participatifs", "PCG_1671"));
        reperes.add(new LigneRepere("DN", "Avances conditionnées", "PCG_1674"));
        reperes.add(new LigneRepere("DO", "Total (II)", "DM + DN"));
        reperes.add(new LigneRepere("DP", "Provisions pour risques", "PCG_151"));
        reperes.add(new LigneRepere("DQ", "Provisions pour charges", "PCG_153 + PCG_154 + PCG_155 + PCG_156 + PCG_157 + PCG_158"));
        reperes.add(new LigneRepere("DR", "", "DP + DQ"));
        reperes.add(new LigneRepere("EB", "", "PCG_487"));
        reperes.add(new LigneRepere("EC", "", "DS + DT + DU + DV + DW + DX + DY + DZ + EA + EB"));
        reperes.add(new LigneRepere("ED", "", "PCG_477"));
        reperes.add(new LigneRepere("EE", "", "DL + DO + DR + EC + ED"));
        reperes.add(new LigneRepere("FA", "", "PCG_707"));
        reperes.add(new LigneRepere("FB", "", "PCG_708"));
        reperes.add(new LigneRepere("FC", "", "PCG_7097"));
        reperes.add(new LigneRepere("FD", "", "PCG_701 + PCG_702 + PCG_703"));
        reperes.add(new LigneRepere("FE", "", "PCG_7091"));
        reperes.add(new LigneRepere("FF", "", "PCG_7092"));
        reperes.add(new LigneRepere("FJ", "", "FA + FD + FG"));
        reperes.add(new LigneRepere("FK", "", "FB + FE + FH"));
        reperes.add(new LigneRepere("FL", "", "FJ + FK"));
        reperes.add(new LigneRepere("FM", "", "PCG_71"));
        reperes.add(new LigneRepere("FN", "", "PCG_72"));
        reperes.add(new LigneRepere("FO", "", "PCG_74"));
        reperes.add(new LigneRepere("FP", "", "PCG_781 + PCG_791"));
        reperes.add(new LigneRepere("FQ", "", "PCG_75"));
        reperes.add(new LigneRepere("FR", "", "FL + FM + FN + FO + FP + FQ"));
        reperes.add(new LigneRepere("FS", "", "PCG_607 + PCG_6097"));
        reperes.add(new LigneRepere("FT", "", "PCG_6037"));
        reperes.add(new LigneRepere("FU", "", "PCG_601 + PCG_602 + PCG_6091 + PCG_6092"));
        reperes.add(new LigneRepere("FV", "", "PCG_6031 + PCG_6032"));
        reperes.add(new LigneRepere("FW", "", "PCG_604 + PCG_605 + PCG_606 + PCG_6094 + PCG_6095 + PCG_61 + PCG_62"));
        reperes.add(new LigneRepere("FX", "", "PCG_63"));
        reperes.add(new LigneRepere("FY", "", "PCG_641 + PCG_644 + PCG_648"));
        reperes.add(new LigneRepere("FZ", "", "PCG_645 + PCG_646 + PCG_647 + PCG_648"));
        reperes.add(new LigneRepere("GA", "", "PCG_6811 + PCG_6812"));
        reperes.add(new LigneRepere("GB", "", "PCG_6816"));
        reperes.add(new LigneRepere("GC", "", "PCG_6817"));
        reperes.add(new LigneRepere("GD", "", "PCG_6815"));
        reperes.add(new LigneRepere("GE", "", "PCG_651 + PCG_653 + PCG_654 + PCG_658"));
        reperes.add(new LigneRepere("GF", "", "FS + FT + FU + FV + FW + FX + FY + FZ + GA + GB + GC + GD + GE"));
        reperes.add(new LigneRepere("GG", "Résultat d'exploitation", "FR - GF"));
        reperes.add(new LigneRepere("GH", "Opérations en commun - bénéfice", "PCG_755"));
        reperes.add(new LigneRepere("GI", "Opérations en commun - perte", "PCG_655"));
        reperes.add(new LigneRepere("GJ", "Produits financiers des participants", "PCG_761"));
        reperes.add(new LigneRepere("GK", "Produits des autres valeurs mobilières et créances de l'actif immobilisé", "PCG_762"));
        reperes.add(new LigneRepere("GL", "Autres intérêts et produits assimilés", "PCG_763 + PCG_764 + PCG_765 + PCG_768"));
        reperes.add(new LigneRepere("GM", "Reprises sur provisions et transferts de charges", "PCG_786 + PCG_796"));
        reperes.add(new LigneRepere("GN", "Différences positives de change", "PCG_766"));
        reperes.add(new LigneRepere("GO", "Produits nets sur cessions de valeurs mobilières de placement", "PCG_766"));
        reperes.add(new LigneRepere("GP", "Total des produits financiers (V)", "GJ + GK + GL + GM + GN + GO"));
        reperes.add(new LigneRepere("GQ", "Dotation financière aux amortissements et provisions", "PCG_686"));
        reperes.add(new LigneRepere("GR", "Intérêts et charges assimilées", "PCG_661 + PCG_664 + PCG_665 + PCG_668"));
        reperes.add(new LigneRepere("GS", "Différences négatives de change", "PCG_666"));
        reperes.add(new LigneRepere("GT", "Charges nettes sur cessions de valeurs mobilières de placement", "PCG_667"));
        reperes.add(new LigneRepere("GU", "Total des charges financières (VI)", "GQ + GR + GS + GT"));
        reperes.add(new LigneRepere("GV", "Résultat net financier", "GP - GU"));
        reperes.add(new LigneRepere("GW", "Résultat courant avant impôts", "GG + GH - GI + GV"));

        Map<String, LigneRepere> resultat = new HashMap<String, LigneRepere>();
        reperes.forEach(repere -> resultat.put(repere.getRepere(), repere));
        return resultat;
    }

    public static LigneRepere get(String repere) {
        return REPERES.get(repere);
    }

    /** Code alphabétique de 2 caractères en majuscule */
    final String repere;

    /** Nom du repère */
    final String nom;

    /** Expression pour le calcul du montant de la ligne */
    final String expression;

    @Builder
    public LigneRepere(String repere, String nom, String expression) {
        this.repere = repere;
        // ExpressionBuilder builder = new ExpressionBuilder(expression);
        // builder.variables(variables);
        // this.expression = builder.build();
        this.expression = expression;
        this.nom = nom;
    }

    @Override
    public String toString() {
        return repere;
    }
}
