package fr.neolegal.fec.liassefiscale;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import net.objecthunter.exp4j.VariableProvider;

@Data
public class Repere {

        public static final CharSequence PREFIXE_SOLDE_COMPTE = "S_";
        public static final CharSequence PREFIXE_DIFF_COMPTE = "D_";

        static VariableProvider variables = new Variables();
        static Map<String, Repere> REPERES = buildDefinitionReperes();

        /** Documentation des calculs : 
         * https://www.lexisnexis.fr/__data/assets/pdf_file/0006/642363/rdi1903.pdf
         */
        private static Map<String, Repere> buildDefinitionReperes() {
                List<Repere> reperes = new LinkedList<>();
                reperes.add(new Repere("AA", "Capital souscrit non appelé", "S_109",
                                Formulaire.DGFIP_2050_BILAN_ACTIF));
                reperes.add(new Repere("DA", "Capital social ou individuel", "S_101 + S_108",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DB", "Primes d'émission, de fusion, d'apport", "S_104",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DC", "Ecarts de réévaluation", "S_105",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DD", "Réserve légale", "S_1061", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DE", "Réserves statutaires ou contractuelles", "S_1063",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DF", "Réserves règlementées", "S_1062 + S_1064",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DG", "Autres réserves", "S_1068", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DH", "Report à nouveau", "S_11", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DI", "Résultat de l'exercice", "S_12",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DJ", "Subvention d'investissement", "S_13",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DK", "Provisions règlementées", "S_14",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DL", "Total (I)", "DA + DB + DC + DD + DE + DF + DG + DH + DI + DJ + DK + HN",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DM", "Produit des émissions de titres participatifs", "S_1671",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DN", "Avances conditionnées", "S_1674",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DO", "Total (II)", "DM + DN", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DP", "Provisions pour risques", "S_151",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DQ", "Provisions pour charges",
                                "S_153 + S_154 + S_155 + S_156 + S_157 + S_158",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("DR", "Total (III)", "DP + DQ", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("EB", "Produits constatés d'avance", "S_487", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("EC", "Total (IV)", "DS + DT + DU + DV + DW + DX + DY + DZ + EA + EB",
                                Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("ED", "Ecarts de conversion passif", "S_477", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("EE", "Total général", "DL + DO + DR + EC + ED", Formulaire.DGFIP_2051_BILAN_PASSIF));
                reperes.add(new Repere("FA", "Ventes de marchandises", "S_707", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FB", "Ventes de marchandises", "S_708", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FC", "Ventes de marchandises", "S_7097", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FD", "Production vendue de biens", "S_701 + S_702 + S_703",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FE", "Production vendue de biens", "S_7091", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FF", "Production vendue de biens", "S_7092", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FJ", "Chiffre d'affaires net", "FA + FD + FG", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FK", "Chiffre d'affaires net", "FB + FE + FH", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FL", "Chiffre d'affaires net", "FJ + FK", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FM", "Production stockée", "S_71", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FN", "Production immobilisée", "S_72", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FO", "Subvention d'exploitation", "S_74", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FP", "Reprises sur amortissements et provisions, transfert de charges", "S_781 + S_791", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FQ", "Autres produits", "S_75", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FR", "Total des produits d'exploitation (I)", "FL + FM + FN + FO + FP + FQ",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FS", "Achats de marchandises", "S_607 + S_6097", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FT", "Variation de stock - MArchandises", "S_6037", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FU", "Achats de matières premières et autres approvisionnements", "S_601 + S_602 + S_6091 + S_6092",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FV", "Variation de stock - Matières premières et approvisionnements", "S_6031 + S_6032", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FW", "Autres achats et charges externes",
                                "S_604 + S_605 + S_606 + S_6094 + S_6095 + S_61 + S_62",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FX", "Impôts, taxes et versements assimilés", "S_63", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FY", "Salaires et traitements", "S_641 + S_644 + S_648",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("FZ", "Charges sociales", "S_645 + S_646 + S_647 + S_648",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GA", "Dotations d'exploitation aux amortissements", "S_6811 + S_6812", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GB", "Dotations d'exploitation aux provisions sur immobilisations", "S_6816", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GC", "Dotations d'exploitation aux provisions sur actif circulant", "S_6817", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GD", "Dotations d'exploitation aux provisions pour risques et charges", "S_6815", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GE", "Autres charges d'exploitation", "S_651 + S_653 + S_654 + S_658",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GF", "Total des charges d'exploitation (II)", "FS + FT + FU + FV + FW + FX + FY + FZ + GA + GB + GC + GD + GE",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GG", "Résultat d'exploitation", "FR - GF",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GH", "Opérations en commun - bénéfice", "S_755",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GI", "Opérations en commun - perte", "S_655",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GJ", "Produits financiers des participants", "S_761",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GK",
                                "Produits des autres valeurs mobilières et créances de l'actif immobilisé",
                                "S_762", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GL", "Autres intérêts et produits assimilés",
                                "S_763 + S_764 + S_765 + S_768", Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GM", "Reprises sur provisions et transferts de charges",
                                "S_786 + S_796",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GN", "Différences positives de change", "S_766",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GO", "Produits nets sur cessions de valeurs mobilières de placement",
                                "S_766",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GP", "Total des produits financiers (V)", "GJ + GK + GL + GM + GN + GO",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GQ", "Dotation financière aux amortissements et provisions", "S_686",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GR", "Intérêts et charges assimilées",
                                "S_661 + S_664 + S_665 + S_668",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GS", "Différences négatives de change", "S_666",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GT", "Charges nettes sur cessions de valeurs mobilières de placement",
                                "S_667",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GU", "Total des charges financières (VI)", "GQ + GR + GS + GT",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GV", "Résultat net financier", "GP - GU",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("GW", "Résultat courant avant impôts", "GG + GH - GI + GV",
                                Formulaire.DGFIP_2052_COMPTE_RESULTAT));
                reperes.add(new Repere("HA", "Produits exceptionnels sur opérations de gestion", "S_771",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HB", "Produits exceptionnels sur opérations en capital",
                                "S_775 + S_777 + S_778", Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HC", "Reprises sur provisions et transferts de charges",
                                "S_787 + S_797",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HD", "Total des produits exceptionnels", "HA + HB + HC",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HE", "Charges exceptionnelles sur opérations de gestion", "S_671",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HF", "Charges exceptionnelles su opérations en capital",
                                "S_675 + S_678",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HG", "Dotations exceptionnelles aux amortissements et provisions",
                                "S_687",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HH", "Total des charges exceptionnelles (VIII)", "HE + HF + HG",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(
                                new Repere("HI", "Résultat net exceptionnel", "HD - HH",
                                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HJ", "Participation des salariés aux résultats de l'entreprise", "S_691",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HK", "Impôts sur les bénéfices", "S_695 + S_698 + S_699",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HL", "Total des produits", "FR + GH + GP + HD",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HM", "Total des charges", "GF + GI + GU + HH + HJ + HK",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("HN", "Résultat de l'exercice", "HL - HM",
                                Formulaire.DGFIP_2053_COMPTE_RESULTAT));
                reperes.add(new Repere("WE", "Amortissements exédentaires", "",
                                Formulaire.DGFIP_2058_A_RESULTAT_FISCAL));
                reperes.add(new Repere("WF", "Autres charges et dépenses somptuaires", "",
                                Formulaire.DGFIP_2058_A_RESULTAT_FISCAL));
                reperes.add(new Repere("ZB", "Réserve légale", "D_1061",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));
                reperes.add(new Repere("ZD", "Autres réserves", "D_1068",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));
                reperes.add(new Repere("ZE", "Dividendes", "D_457",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));
                reperes.add(new Repere("ZF", "Autres répartitions", "",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));
                reperes.add(new Repere("ZG", "Report à nouveau", "D_110",
                                Formulaire.DGFIP_2058_C_AFFECTATION_RESULTAT));

                Map<String, Repere> resultat = new HashMap<String, Repere>();
                reperes.forEach(repere -> resultat.put(repere.getRepere(), repere));
                return resultat;
        }

        public static Repere get(String repere) {
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
        public Repere(String repere, String nom, String expression, Formulaire formulaire) {
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
