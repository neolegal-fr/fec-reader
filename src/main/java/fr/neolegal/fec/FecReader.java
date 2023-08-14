package fr.neolegal.fec;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;

/**
 * Lecture d'un fichier de écritures comptables conforme à la norme définie par
 * le Livre des procédures fiscales : Section III : Modalités d'exercice du
 * droit de contrôle
 * https://www.legifrance.gouv.fr/codes/article_lc/LEGIARTI000027804775/
 */
public class FecReader {

    static Character TAB_SEPARATOR = '\t';
    static Character PIPE_SEPARATOR = '|';    

    public FecReader() {
    }

    /**
     * Les zones sont obligatoirement séparées par une tabulation ou le caractère
     * "|" ;
     */
    Optional<Character> guessSeparator(Path path, Charset charset) throws IOException {
        try (Scanner scanner = new Scanner(path, charset)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int tabCount = StringUtils.countMatches(line, TAB_SEPARATOR);
                int pipeCount = StringUtils.countMatches(line, PIPE_SEPARATOR);
                if (tabCount > 0 || pipeCount > 0) {
                    return Optional.of(tabCount > pipeCount ? TAB_SEPARATOR : PIPE_SEPARATOR);
                }
            }
        }
        return Optional.empty();
    }

    CharsetMatch guessCharset(Path path) throws IOException {
        CharsetDetector detector = new CharsetDetector();
        try (FileInputStream inputStream = new FileInputStream(path.toFile())) {
            detector.setText(inputStream.readAllBytes());
            return detector.detect();
        }
    }

    Optional<CSVFormat> guessCsvFormat(Path path, Charset charset) throws IOException {
        return guessSeparator(path, charset).map(delimiter -> CSVFormat.newFormat(delimiter));
    }

    public Fec read(Path file) throws FileNotFoundException, IOException {
        return read(file, file.getFileName().toString());
    }

    public Fec read(Path path, String originalFileName) throws FileNotFoundException, IOException {
        Fec.FecBuilder builder = Fec.builder();

        List<Anomalie> anomalies = new LinkedList<>();
        CharsetMatch charsetMatch = guessCharset(path);
        Charset charset = Charset.forName(charsetMatch.getName());
        CSVFormat format = guessCsvFormat(path, charset).orElseThrow(() -> new InvalidParameterException(
                "Aucun séparateur de zone détecté. Les zones sont obligatoirement séparées par une tabulation ou le caractère '|'"));
        
        Optional<String> sirenMatch = FecHelper.parseSiren(originalFileName);
        sirenMatch.ifPresentOrElse(siren -> builder.siren(siren),
                () -> anomalies.add(new Anomalie(NatureAnomalie.SIREN, originalFileName,
                        String.format("Format du nom de fichier incorrect, numéro SIREN non trouvé : %s", originalFileName))));

        Optional<LocalDate> clotureExerciceMatch = FecHelper.parseClotureExercice(originalFileName);
        clotureExerciceMatch.ifPresentOrElse(cloture -> builder.clotureExercice(cloture),
                () -> anomalies.add(new Anomalie(NatureAnomalie.CLOTURE_EXERCICE, originalFileName,
                        String.format(
                                "Format du nom de fichier incorrect, date de clôture de l'exercice non trouvée : %s",
                                originalFileName))));

        // Les enregistrements sont séparés par le caractère de contrôle Retour chariot
        // et/ ou Fin de ligne ;
        CSVParser csvParser = CSVParser.parse(path, charset, format);
        int lineCount = 0;
        int emptyLineCount = 0;
        int lineParsingErrorCount = 0;
        List<LEC> lignes = new LinkedList<>();
        for (CSVRecord csvRecord : csvParser) {
            if (lineCount > 0) {
                // La première ligne est obligatoirement une ligne d'en-tête
                if (csvRecord.size() > 1) {
                    try {
                        lignes.add(parseLigne(csvRecord));
                    } catch (ParseException e) {
                        ++lineParsingErrorCount;
                    }
                } else {
                    ++emptyLineCount;
                }
            }
            ++lineCount;
        }
        
        if (emptyLineCount > 0) {
            anomalies.add(new Anomalie(NatureAnomalie.LIGNES_VIDES, emptyLineCount, String.format("%d lignes vides dans le fichier", emptyLineCount)));
        }
        if (lineParsingErrorCount > 0) {
            anomalies.add(new Anomalie(NatureAnomalie.LIGNES_INVALIDES, lineParsingErrorCount, String.format("%d lignes n'ont pas pu être lues", lineParsingErrorCount)));
        }

        builder.lignes(lignes);
        builder.anomalies(anomalies);

        return builder.build();
    }

    static String getValueOrNull(CSVRecord ligne, int colIndex) {
        return ligne.size() > colIndex ? ligne.get(colIndex) : null;
    }

    /**
     * Extrait du Livre des procédures fiscales : Section III : Modalités d'exercice
     * du droit de contrôle, article VII
     * Le fichier est constitué des écritures après opérations d'inventaire, hors
     * écritures de centralisation et hors écritures de solde des comptes de charges
     * et de produits. Il comprend les écritures de reprise des soldes de l'exercice
     * antérieur et contient, pour chaque écriture, l'ensemble des données
     * comptables figurant dans le système informatisé comptable de l'entreprise,
     * les dix-huit premières informations devant obligatoirement correspondre, dans
     * l'ordre, à celles listées dans le tableau
     * @throws ParseException
     */
    private LEC parseLigne(CSVRecord ligne) throws ParseException {
        List<Anomalie> anomalies = new LinkedList<>();

        if (ligne.size() < 18) {
            anomalies.add(new Anomalie(NatureAnomalie.LIGNES_INVALIDES, ligne.size(),String.format("La ligne ne contient que %d valeurs au lieu des 18 attendues", ligne.size())));
        }

        LEC.LECBuilder builder = LEC.builder();
        int colIndex = 0;
        builder.journalCode(getValueOrNull(ligne, colIndex++));
        builder.journalLib(getValueOrNull(ligne, colIndex++));
        builder.ecritureNum(getValueOrNull(ligne, colIndex++));
        builder.ecritureDate(FecHelper.parseDate(getValueOrNull(ligne, colIndex++)));
        builder.compteNum(getValueOrNull(ligne, colIndex++));
        builder.compteLib(getValueOrNull(ligne, colIndex++));
        builder.compAuxNum(getValueOrNull(ligne, colIndex++));
        builder.compAuxLib(getValueOrNull(ligne, colIndex++));
        builder.pieceRef(getValueOrNull(ligne, colIndex++));
        builder.pieceDate(FecHelper.parseDate(getValueOrNull(ligne, colIndex++)));
        builder.ecritureLib(getValueOrNull(ligne, colIndex++));
        builder.debit(FecHelper.parseDouble(getValueOrNull(ligne, colIndex++)));
        builder.credit(FecHelper.parseDouble(getValueOrNull(ligne, colIndex++)));
        builder.ecritureLet(getValueOrNull(ligne, colIndex++));
        builder.dateLet(FecHelper.parseDate(getValueOrNull(ligne, colIndex++)));
        builder.validDate(FecHelper.parseDate(getValueOrNull(ligne, colIndex++)));
        builder.montantdevise(FecHelper.parseDouble(getValueOrNull(ligne, colIndex++)));
        builder.idevise(getValueOrNull(ligne, colIndex++));
        builder.dateRglt(FecHelper.parseDate(getValueOrNull(ligne, colIndex++)));
        builder.modeRglt(getValueOrNull(ligne, colIndex++));
        builder.natOp(getValueOrNull(ligne, colIndex++));
        builder.idClient(getValueOrNull(ligne, colIndex++));
        builder.anomalies(anomalies);
        return builder.build();
    }
}
