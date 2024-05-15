package fr.neolegal.fec.liassefiscale;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static java.util.Objects.isNull;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import fr.neolegal.fec.Fec;
import net.objecthunter.exp4j.VariableProvider;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Rectangle;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

public class LiasseFiscaleHelper {

    private LiasseFiscaleHelper() {
    }

    public static LiasseFiscale buildLiasseFiscale(RegimeImposition regime) {
        LiasseFiscale liasse = LiasseFiscale.builder().regime(regime).build();
        for (NatureFormulaire formulaire : regime.formulaires()) {
            liasse.getFormulaires().add(buildFormulaire(formulaire));
        }

        return liasse;
    }

    public static LiasseFiscale buildLiasseFiscale(Fec fec, RegimeImposition regime) {

        LiasseFiscale liasse = buildLiasseFiscale(regime);
        liasse.setSiren(fec.getSiren());
        liasse.setClotureExercice(fec.getClotureExercice());

        VariableProvider provider = new FecVariableProvider(fec, regime);
        for (Formulaire formulaire : liasse.getFormulaires()) {
            for (Repere repere : formulaire.getChamps().keySet()) {
                Double montant = RepereHelper.computeMontantRepereCellule(repere, fec, provider).orElse(null);
                formulaire.getChamps().put(repere, montant);
            }
        }

        return liasse;
    }

    public static Formulaire buildFormulaire(NatureFormulaire nature) {
        Formulaire formulaire = new Formulaire(nature);

        Collection<Repere> reperes = Repere.DEFINITIONS.getOrDefault(nature, Map.of()).values();
        for (Repere repere : reperes) {
            formulaire.getChamps().put(repere, null);
        }
        return formulaire;
    }

    public static LiasseFiscale readLiasseFiscalePDF(String filename) throws IOException {
        LiasseFiscale liasse = LiasseFiscale.builder().build();
        // Détermination empirique des distances entre les lignes et les colonnes des
        // tableaux des liasses fiscales
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm()
                .withMaxGapBetweenAlignedHorizontalRulings(30)
                .withMaxGapBetweenAlignedVerticalRulings(15)
                .withMinSpacingBetweenRulings(10f);

        List<Table> docTables = new LinkedList<>();
        try (InputStream in = new FileInputStream(filename);
                PDDocument document = PDDocument.load(in);
                ObjectExtractor extractor = new ObjectExtractor(document)) {
            PageIterator pi = extractor.extract();
            while (pi.hasNext()) {
                Page page = pi.next();
                String header = extractPageHeader(page);
                Optional<NatureFormulaire> formulaireHeaderMatch = NatureFormulaire.resolve(header);
                Optional<NatureAnnexe> annexeHeaderMatch = resolveNatureAnnexe(page);
                if (formulaireHeaderMatch.isPresent() && annexeHeaderMatch.isEmpty()) {
                    NatureFormulaire natureFormulaire = formulaireHeaderMatch.get();
                    if (liasse.getRegime() == null) {
                        liasse.setRegime(natureFormulaire.getRegimeImposition());
                    }
                    List<Table> pageTables = sea.extract(page);
                    Optional<Table> tableMatch = pageTables.stream()
                            .max(Comparator.comparing(Table::getRowCount));
                    if (tableMatch.isPresent()) {
                        Table table = tableMatch.get();
                        docTables.add(table);
                        Formulaire formulaire = parseFormulaire(table, natureFormulaire);

                        // On peut à tort croire qu'une page correspond à un formulaire (ex : annexe),
                        // et le trouver deux fois dans la liasse. Dans cette situation,
                        // le formulaire avec le plus de valeurs renseignées est sélectionné
                        Formulaire existingFormulaire = liasse.getFormulaires().stream()
                                .filter(f -> f.getNature().equals(natureFormulaire)).findFirst().orElse(null);
                        if (existingFormulaire != null) {
                            if (existingFormulaire.nbMontantsNonNull() < formulaire.nbMontantsNonNull()) {
                                liasse.getFormulaires().remove(existingFormulaire);
                                liasse.getFormulaires().add(formulaire);
                            }
                        } else {
                            liasse.getFormulaires().add(formulaire);
                        }

                        if (StringUtils.isEmpty(liasse.getSiren()) && natureFormulaire.containsSiren()) {
                            liasse.setSiren(parseSiren(table, page).orElse(null));
                        }
                        if (isNull(liasse.getClotureExercice()) && natureFormulaire.containsClotureExercice()) {
                            liasse.setClotureExercice(parseClotureExercice(table).orElse(null));
                        }
                    }
                } else if (formulaireHeaderMatch.isPresent() && annexeHeaderMatch.isPresent()) {
                    List<Table> pageTables = sea.extract(page);
                    Optional<Table> tableMatch = pageTables.stream()
                            .max(Comparator.comparing(Table::getRowCount));
                    if (tableMatch.isPresent()) {
                        Table table = tableMatch.get();
                        docTables.add(table);
                        NatureAnnexe natureAnnexe = annexeHeaderMatch.get();
                        List<? extends List<String>> lignes = parseAnnexe(table, natureAnnexe, false);
                        liasse.getFormulaire(formulaireHeaderMatch.get()).getOrAddAnnexe(annexeHeaderMatch.get())
                                .getLignes().addAll(lignes);
                    }
                }
            }
        }
        // writeTablesAsSvg(docTables, "tables.html");
        return liasse;
    }

    private static Optional<NatureAnnexe> resolveNatureAnnexe(Page page) throws IOException {
        // On cherche d'abord dans l'en-tête de la page
        Optional<NatureAnnexe> result = NatureAnnexe.resolve(extractPageHeader(page));
        if (result.isPresent() && result.get() == NatureAnnexe.INCONNUE) {
            // C'est une annexe, mais on ne sait pas de quelle nature
            // On va plus loin et analyse le corps du document
            String pageText = extractPageText(page);
            Optional<NatureAnnexe> deeperResolution = NatureAnnexe.resolve(pageText);
            return deeperResolution.or(() -> result);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    static Optional<LocalDate> parseClotureExercice(Table table) {
        for (List<RectangularTextContainer> row : table.getRows()) {
            for (RectangularTextContainer<?> cell : row) {
                String text = cell.getText();
                Optional<LocalDate> match = parseClotureExercice(text);
                if (match.isPresent()) {
                    return match;
                }
            }
        }

        return Optional.empty();
    }

    static Optional<LocalDate> parseClotureExercice(String text) {
        final String regex = "(clos le|c l o s   l e)([\\s,:]*)(.+)";
        // Il peut y avoir les dates de cloture des exercices N, et N-1, on s'assure de
        // matcher le N
        Pattern pattern = Pattern.compile(".*N[,\\s]+" + regex,
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        String date = "";
        if (matcher.matches()) {
            date = matcher.group(3);
            date = StringUtils.left(date.replaceAll("[^\\d]", ""), 8);
        } else {

            // PAs de correspondance, il n'y a peut être pas de N
            pattern = Pattern.compile(".*" + regex,
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            matcher = pattern.matcher(text);
            if (matcher.matches()) {
                date = matcher.group(3);
                date = StringUtils.left(date.replaceAll("[^\\d]", ""), 8);
            }
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(date.length() == 8 ? "ddMMyyyy" : "ddMMyy");
            return Optional.of(LocalDate.parse(date, formatter));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    static Optional<String> parseSiren(Table table, Page page) throws IOException {
        String tableText = extractTableText(table, page);
        return parseSiren(tableText).or(() -> {
            try {
                return parseSiren(extractPageText(page));
            } catch (IOException e) {
                return Optional.empty();
            }
        });
    }

    static Optional<String> parseSiren(String text) {
        Pattern sirenPattern = Pattern.compile(".*(SIREN|SIRET|S I R E N|S I R E T)[^\\d]{0,5}(.+)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = sirenPattern.matcher(text);
        if (matcher.matches()) {
            String siren = StringUtils.left(matcher.group(2), 18);
            siren = StringUtils.left(siren.replaceAll("[^\\d]", ""), 9);
            return Optional.of(siren);
        }

        return Optional.empty();
    }

    @SuppressWarnings("rawtypes")
    private static String getRowText(Page page, List<RectangularTextContainer> row, String delimiter) {

        List<RectangularTextContainer> cells = row.stream().filter(cell -> cell.getArea() > 0.0)
                .collect(Collectors.toList());
        Rectangle rowArea = cells.size() > 0 ? cells.get(0) : new Rectangle();
        for (RectangularTextContainer<?> cell : cells) {
            rowArea.setTop(Math.min(rowArea.getTop(), cell.getTop()));
            rowArea.setLeft(Math.min(rowArea.getLeft(), cell.getLeft()));
            rowArea.setBottom(Math.max(rowArea.getBottom(), cell.getBottom()));
            rowArea.setRight(Math.max(rowArea.getRight(), cell.getRight()));
        }
        return page.getText(rowArea).stream().map(te -> te.getText()).collect(Collectors.joining(delimiter));
    }

    @SuppressWarnings("rawtypes")
    private static Formulaire parseFormulaire(Table table, NatureFormulaire natureFormulaire) {
        Formulaire formulaire = buildFormulaire(natureFormulaire);
        for (Repere repere : formulaire.reperes()) {
            boolean found = false;
            for (List<RectangularTextContainer> row : table.getRows()) {
                if (found) {
                    break;
                }
                for (RectangularTextContainer<?> cell : row) {
                    String text = cell.getText().trim();
                    if (found) {
                        double montant = parseNumber(text);
                        formulaire.setMontant(repere, montant);
                        break;
                    }

                    if (StringUtils.equalsIgnoreCase(repere.getSymbole(), text)) {
                        found = true;
                    }
                }
            }
        }

        for (NatureAnnexe natureAnnexe : natureFormulaire.getAnnexes()) {
            formulaire.getOrAddAnnexe(natureAnnexe).getLignes()
                    .addAll(parseAnnexe(table, natureAnnexe, true));
        }

        return formulaire;
    }

    public static double parseNumber(String text) {
        text = text.trim();
        boolean isNegative = text.startsWith("(") || text.endsWith(")");
        text = text.replaceAll("[\\s\\(\\)]", "");

        double number = NumberUtils.toDouble(text, 0.0);
        if (isNegative && number != 0.0) {
            number = -number;
        }

        return number;
    }

    @SuppressWarnings("rawtypes")
    private static List<? extends List<String>> parseAnnexe(Table table, NatureAnnexe natureAnnexe,
            boolean searchTitle) {
        List<List<String>> resultat = new LinkedList<>();

        List<List<RectangularTextContainer>> rows = table.getRows();
        if (rows.isEmpty()) {
            return resultat;
        }

        int headerRowIndex = 0;
        if (searchTitle) {
            for (int i = 0; i < rows.size() && headerRowIndex == 0; ++i) {
                // On cherche la première ligne contenant le nom de l'annexe
                // Les données seront présentes sur les lignes suivantes
                for (RectangularTextContainer<?> cell : rows.get(i)) {
                    String text = cell.getText().trim();
                    if (StrUtils.containsIgnoreCase(text, natureAnnexe.getIntitule())) {
                        headerRowIndex = i;
                    }
                }
            }

            // La ligne d'en-tête peut être composée de cellules fusionnées
            // On cherche la prochaine ligne commençant au
            List<RectangularTextContainer> headerRow = rows.get(headerRowIndex);
            float rowStartPos = headerRow.get(0).getLeft();
            while ((headerRowIndex + 1) < rows.size() && rows.get(headerRowIndex + 1).get(0).getLeft() != rowStartPos) {
                ++headerRowIndex;
            }
        }

        List<RectangularTextContainer> prevRow = null;
        boolean moreData = true;
        for (int i = headerRowIndex + 1; i < rows.size() && moreData; ++i) {
            List<RectangularTextContainer> row = rows.get(i);
            List<String> ligne = new LinkedList<>();
            boolean emptyRow = true;
            for (RectangularTextContainer<?> cell : row) {
                String text = cell.getText().trim();
                emptyRow = emptyRow && text.isEmpty();
                ligne.add(text);
            }

            moreData = !emptyRow && (isNull(prevRow) || sameRowStructure(row, prevRow));
            if (moreData) {
                prevRow = row;
                resultat.add(ligne);
            }
        }

        return resultat;
    }

    @SuppressWarnings("rawtypes")
    private static boolean sameRowStructure(List<RectangularTextContainer> row,
            List<RectangularTextContainer> other) {
        if (isNull(row) || isNull(other) || row.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < row.size(); ++i) {
            RectangularTextContainer<?> cell = row.get(i);
            RectangularTextContainer<?> otherCell = other.get(i);
            if (cell.getWidth() != otherCell.getWidth()) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    private static String extractTableText(Table table, Page page) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (List<RectangularTextContainer> row : table.getRows()) {
            sb.append(getRowText(page, row, ""));
            sb.append("\r\n");
        }
        return sb.toString();
    }

    private static String extractPageText(Page page) throws IOException {
        PDFTextStripper reader = new PDFTextStripper();
        reader.setStartPage(page.getPageNumber());
        reader.setEndPage(page.getPageNumber());
        return reader.getText(page.getPDDoc());
    }

    private static String extractPageHeader(Page page) throws IOException {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        Rectangle rect = new Rectangle(0, 0, page.width, (int) ((double) page.height * 0.08));
        stripper.addRegion("top", rect);

        stripper.extractRegions(page.getPDPage());

        return stripper.getTextForRegion("top");
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    private static void writeTablesAsSvg(List<Table> tables, String htmlFileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        int i = 1;
        for (Table table : tables) {
            sb.append("<h1>Table " + i + "</h1>");
            sb.append(String.format(Locale.US,
                    "<svg width=\"100%%\" viewbox=\"0 0 %s %s\" xmlns=\"http://www.w3.org/2000/svg\">",
                    table.getWidth() + 50.0, table.getHeight() + 50.0));
            for (List<RectangularTextContainer> row : table.getRows()) {
                for (RectangularTextContainer<?> cell : row) {
                    if (true /* cell.height > 10 && cell.width > 10 */) {
                        String text = cell.getText();
                        sb.append("<g>");
                        sb.append(String.format(Locale.US,
                                "<rect width=\"%f\" height=\"%f\" x=\"%f\" y=\"%f\" rx=\"2\" ry=\"2\" fill=\"white\" stroke=\"blue\"/>",
                                cell.width, cell.height, cell.x, cell.y));
                        sb.append(String.format(Locale.US,
                                "<text x=\"%f\" y=\"%f\" font-family=\"Verdana\" font-size=\"8\">%s</text>",
                                cell.x + 2, cell.y + cell.height - 3,
                                text));
                        sb.append("</g>");
                    }
                }
            }

            sb.append("</svg><br>");
            ++i;
        }
        sb.append("</body></html>");

        String html = sb.toString();

        BufferedWriter writer = new BufferedWriter(new FileWriter(htmlFileName));
        writer.write(html);
        writer.close();
    }

}
