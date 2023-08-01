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
                reperes.add(new LigneRepere("AA", "Capital souscrit non appelé", "PCG_109",
                                Formulaire.DGFIP_2050_BILAN_ACTIF));
                reperes.add(new LigneRepere("DA", "Capital social ou individuel", "PCG_101 + PCG_108",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DB", "Primes d'émission, de fusion, d'apport", "PCG_104",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DC", "Ecarts de réévaluation", "PCG_105",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DD", "Réserve légale", "PCG_1061", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DE", "Réserves statutaires ou contractuelles", "PCG_1063",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DF", "Réserves règlementées", "PCG_1062 + PCG_1064",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DG", "Autres réserves", "PCG_1068", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DH", "Report à nouveau", "PCG_11", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DI", "Résultat de l'exercice", "PCG_12",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DJ", "Subvention d'investissement", "PCG_13",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DK", "Provisions règlementées", "PCG_14",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DL", "Total (I)", "DA + DB + DC + DD + DE + DF + DG + DH + DI + DJ + DK",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DM", "Produit des émissions de titres participatifs", "PCG_1671",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DN", "Avances conditionnées", "PCG_1674",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DO", "Total (II)", "DM + DN", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DP", "Provisions pour risques", "PCG_151",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DQ", "Provisions pour charges",
                                "PCG_153 + PCG_154 + PCG_155 + PCG_156 + PCG_157 + PCG_158",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("DR", "", "DP + DQ", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("EB", "", "PCG_487", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("EC", "", "DS + DT + DU + DV + DW + DX + DY + DZ + EA + EB",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("ED", "", "PCG_477", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("EE", "", "DL + DO + DR + EC + ED", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new LigneRepere("FA", "", "PCG_707", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FB", "", "PCG_708", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FC", "", "PCG_7097", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FD", "", "PCG_701 + PCG_702 + PCG_703",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FE", "", "PCG_7091", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FF", "", "PCG_7092", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FJ", "", "FA + FD + FG", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FK", "", "FB + FE + FH", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FL", "", "FJ + FK", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FM", "", "PCG_71", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FN", "", "PCG_72", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FO", "", "PCG_74", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FP", "", "PCG_781 + PCG_791", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FQ", "", "PCG_75", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FR", "", "FL + FM + FN + FO + FP + FQ",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FS", "", "PCG_607 + PCG_6097", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FT", "", "PCG_6037", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FU", "", "PCG_601 + PCG_602 + PCG_6091 + PCG_6092",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FV", "", "PCG_6031 + PCG_6032", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FW", "",
                                "PCG_604 + PCG_605 + PCG_606 + PCG_6094 + PCG_6095 + PCG_61 + PCG_62",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FX", "", "PCG_63", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FY", "", "PCG_641 + PCG_644 + PCG_648",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("FZ", "", "PCG_645 + PCG_646 + PCG_647 + PCG_648",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GA", "", "PCG_6811 + PCG_6812", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GB", "", "PCG_6816", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GC", "", "PCG_6817", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GD", "", "PCG_6815", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GE", "", "PCG_651 + PCG_653 + PCG_654 + PCG_658",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GF", "", "FS + FT + FU + FV + FW + FX + FY + FZ + GA + GB + GC + GD + GE",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GG", "Résultat d'exploitation", "FR - GF",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GH", "Opérations en commun - bénéfice", "PCG_755",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GI", "Opérations en commun - perte", "PCG_655",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GJ", "Produits financiers des participants", "PCG_761",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GK",
                                "Produits des autres valeurs mobilières et créances de l'actif immobilisé",
                                "PCG_762", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GL", "Autres intérêts et produits assimilés",
                                "PCG_763 + PCG_764 + PCG_765 + PCG_768", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GM", "Reprises sur provisions et transferts de charges",
                                "PCG_786 + PCG_796",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GN", "Différences positives de change", "PCG_766",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GO", "Produits nets sur cessions de valeurs mobilières de placement",
                                "PCG_766",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GP", "Total des produits financiers (V)", "GJ + GK + GL + GM + GN + GO",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GQ", "Dotation financière aux amortissements et provisions", "PCG_686",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GR", "Intérêts et charges assimilées",
                                "PCG_661 + PCG_664 + PCG_665 + PCG_668",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GS", "Différences négatives de change", "PCG_666",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GT", "Charges nettes sur cessions de valeurs mobilières de placement",
                                "PCG_667",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GU", "Total des charges financières (VI)", "GQ + GR + GS + GT",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GV", "Résultat net financier", "GP - GU",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("GW", "Résultat courant avant impôts", "GG + GH - GI + GV",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HA", "Produits exceptionnels sur opérations de gestion", "PCG_771",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HB", "Produits exceptionnels sur opérations en capital",
                                "PCG_775 + PCG_777 + PCG_778", Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HC", "Reprises sur provisions et transferts de charges",
                                "PCG_787 + PCG_797",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HD", "Total des produits exceptionnels", "HA + HB + HC",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HE", "Charges exceptionnelles sur opérations de gestion", "PCG_671",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HF", "Charges exceptionnelles su opérations en capital",
                                "PCG_675 + PCG_678",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HG", "Dotations exceptionnelles aux amortissements et provisions",
                                "PCG_687",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HH", "Total des charges exceptionnelles (VIII)", "HE + HF + HG",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(
                                new LigneRepere("HI", "Résultat net exceptionnel", "HD - HH",
                                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HJ", "Participation des salariés aux résultats de l'entreprise", "PCG_691",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HK", "Impôts sur les bénéfices", "PCG_695 + PCG_698 + PCG_699",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HL", "Total des produits", "FR + GH + GP + HD",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HM", "Total des charges", "GF + GI + GU + HH + HJ + HK",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("HN", "Résultat de l'exercice", "HL - HM",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new LigneRepere("WE", "Amortissements exédentaires", "",
                                Formulaire.DGFIP_2058_A_RESULTAT_FISCAL));
                reperes.add(new LigneRepere("WF", "Autres charges et dépenses somptuaires", "",
                                Formulaire.DGFIP_2058_A_RESULTAT_FISCAL));
                reperes.add(new LigneRepere("ZB", "Réserve légale", "PCG_1061",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));
                reperes.add(new LigneRepere("ZD", "Autres réserves", "PCG_1068",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));
                reperes.add(new LigneRepere("ZE", "Dividendes", "PCG_457",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));
                reperes.add(new LigneRepere("ZF", "Autres répartitions", "",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));
                reperes.add(new LigneRepere("ZG", "Report à nouveau", "PCG_110",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));

                Map<String, LigneRepere> resultat = new HashMap<String, LigneRepere>();
                reperes.forEach(repere -> resultat.put(repere.getRepere(), repere));
                return resultat;
        }

        public static LigneRepere get(String repere) {
                return REPERES.get(repere);
        }

        final Formulaire formulaire;

        /** Code alphabétique de 2 caractères en majuscule */
        final String repere;

        /** Nom du repère */
        final String nom;

        /** Expression pour le calcul du montant de la ligne */
        final String expression;

        @Builder
        public LigneRepere(String repere, String nom, String expression, Formulaire formulaire) {
                this.repere = repere;
                // ExpressionBuilder builder = new ExpressionBuilder(expression);
                // builder.variables(variables);
                // this.expression = builder.build();
                this.expression = expression;
                this.nom = nom;
                this.formulaire = formulaire;
        }

        @Override
        public String toString() {
                return repere;
        }
}
