package fr.neolegal.fec.liassefiscale;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ObjectUtils;

import lombok.Builder;
import lombok.Data;
import net.objecthunter.exp4j.VariableProvider;

@Data
public class Repere implements Comparable<Repere> {

    public static final CharSequence PREFIXE_SOLDE_COMPTE = "S_";
    public static final CharSequence PREFIXE_DIFF_COMPTE = "D_";

    static VariableProvider variables = new Variables();
    public static Map<String, Repere> DEFINITIONS = loadDefinitionsReperes();

    /**
     * Documentation des calculs :
     * https://www.lexisnexis.fr/__data/assets/pdf_file/0006/642363/rdi1903.pdf
     * 
     * @throws IOException
     */
    private static Map<String, Repere> loadDefinitionsReperes() {
        Map<String, Repere> resultat = new HashMap<String, Repere>();
        int lineIndex = 1;
        try {
            List<Repere> reperes = new LinkedList<>();
            InputStream is = Repere.class.getClassLoader().getResourceAsStream("definitions-reperes.csv");

            CSVParser csvParser = CSVParser.parse(is, Charset.forName("UTF-8"), CSVFormat.MYSQL);
            for (CSVRecord csvRecord : csvParser) {
                if ((lineIndex > 1) && (csvRecord.size() > 0)) {
                    // La première ligne est obligatoirement une ligne d'en-tête
                    Repere repere = new Repere(csvRecord.get(0), csvRecord.get(2), csvRecord.get(3),
                            Formulaire.fromIdentifiant(csvRecord.get(1)));
                    reperes.add(repere);
                }
                ++lineIndex;
            }

            reperes.forEach(repere -> resultat.put(repere.getRepere(), repere));

        } catch (Exception e) {
            Logger.getLogger(Repere.class.getName()).log(Level.SEVERE,
                    String.format("Erreur lors du chargement des définitions de repères à la ligne %d", lineIndex), e);
        }

        return resultat;
    }

    public static Repere get(String repere) {
        return DEFINITIONS.get(repere);
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
        this.expression = expression;
        this.nom = nom;
        this.formulaire = formulaire;
    }

    @Override
    public String toString() {
        return repere;
    }

    @Override
    public int compareTo(Repere other) {
        if (other == this) {
            return 0;
        }

        if (other == null) {
            return 1;
        }

        int result = ObjectUtils.compare(repere, other.repere);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(formulaire, other.formulaire);
    }
}
