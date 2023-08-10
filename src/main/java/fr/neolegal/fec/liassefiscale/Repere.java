package fr.neolegal.fec.liassefiscale;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ObjectUtils;

import lombok.Builder;
import lombok.Data;

@Data
public class Repere implements Comparable<Repere> {
   
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
                    String symbole = csvRecord.get(0);
                    NatureFormulaire formulaire = NatureFormulaire.fromIdentifiant(csvRecord.get(1));
                    String nom = csvRecord.get(2);
                    String expression = csvRecord.size() > 3 ? csvRecord.get(3) : null;
                    Repere repere = new Repere(symbole, nom, expression,
                            formulaire);
                    reperes.add(repere);
                }
                ++lineIndex;
            }

            reperes.forEach(repere -> resultat.put(repere.getSymbole(), repere));

        } catch (Exception e) {
            Logger.getLogger(Repere.class.getName()).log(Level.SEVERE,
                    String.format("Erreur lors du chargement des définitions de repères à la ligne %d", lineIndex), e);
        }

        return resultat;
    }

    public static Repere get(String repere) {
        return DEFINITIONS.get(repere);
    }

    final NatureFormulaire formulaire;

    /** Code alphabétique de 2 caractères en majuscule */
    final String symbole;

    /** Nom du repère */
    final String nom;

    /** Expression pour le calcul du montant de la ligne */
    final String expression;

    @Builder
    public Repere(String repere, String nom, String expression, NatureFormulaire formulaire) {
        this.symbole = repere;
        this.expression = expression;
        this.nom = nom;
        this.formulaire = formulaire;
    }

    @Override
    public String toString() {
        return symbole;
    }

    @Override
    public int compareTo(Repere other) {
        if (other == this) {
            return 0;
        }

        if (other == null) {
            return 1;
        }

        int result = ObjectUtils.compare(symbole, other.symbole);
        if (result != 0) {
            return result;
        }

        return ObjectUtils.compare(formulaire, other.formulaire);
    }
}
