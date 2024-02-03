package fr.neolegal.fec.liassefiscale;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import fr.neolegal.fec.Fec;
import net.objecthunter.exp4j.VariableProvider;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
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
        SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm(10);

        try (InputStream in = new FileInputStream(filename);
                PDDocument document = PDDocument.load(in);
                ObjectExtractor extractor = new ObjectExtractor(document)) {
            PageIterator pi = extractor.extract();
            while (pi.hasNext()) {
                Page page = pi.next();
                Optional<NatureFormulaire> match = resolveNatureFormulaire(page);
                if (match.isPresent()) {
                    NatureFormulaire natureFormulaire = match.get();
                    liasse.setRegime(natureFormulaire.getRegimeImposition());
                    List<Table> tables = sea.extract(page);
                    writeTablesAsSvg(tables, String.format("tables-page-%d.html", page.getPageNumber()));
                    Optional<Table> tableMatch = tables.stream()
                            .max(Comparator.comparing(Table::getRowCount));
                    if (tableMatch.isPresent()) {
                        Formulaire formulaire = parseFormulaire(tableMatch.get(), natureFormulaire);
                        liasse.getFormulaires().add(formulaire);
                    }
                }
            }
        }
        return liasse;
    }

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
                    if (found && cell.getWidth() > 10 && cell.getHeight() > 10 ) {
                        boolean isNegative = text.startsWith("(");
                        text =  text.replaceAll(" ()", "");

                        double montant = NumberUtils.toDouble(text, 0.0);
                        if (isNegative) {
                            montant = -montant;
                        }
                        formulaire.setMontant(repere, montant);
                        break;
                    }

                    if (StringUtils.equalsIgnoreCase(repere.getSymbole(), text)) {
                        found = true;
                    }
                }
            }
        }
        return formulaire;
    }

    private static Optional<NatureFormulaire> resolveNatureFormulaire(Page page) throws IOException {

        PDFTextStripper reader = new PDFTextStripper();
        reader.setStartPage(page.getPageNumber());
        reader.setEndPage(page.getPageNumber());
        String pageText = reader.getText(page.getPDDoc());

        if (!StringUtils.containsAnyIgnoreCase(pageText, "DGFiP", "N°")) {
            return Optional.empty();
        }
        // if (page.getText().stream()
        // .noneMatch(textElem -> StringUtils.containsAnyIgnoreCase(textElem.getText(),
        // "DGFiP", "N°"))) {
        // return Optional.empty();
        // }

        for (NatureFormulaire formulaire : NatureFormulaire.values()) {
            if (StringUtils.containsIgnoreCase(pageText, formulaire.getNumero().toString())) {
                return Optional.of(formulaire);
            }
        }

        return Optional.empty();
    }

    private static void writeTablesAsSvg(List<Table> tables, String htmlFileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        int i = 1;
        for (Table table : tables) {
            sb.append("<h1>Table " + i + "</h1>");
            sb.append(String.format(Locale.US,
                    "<svg width=\"100%%\" viewbox=\"0 0 %s %s\" xmlns=\"http://www.w3.org/2000/svg\">",
                    table.getWidth() + 50.0, table.getHeight() + 10.0));
            for (List<RectangularTextContainer> row : table.getRows()) {
                for (RectangularTextContainer<?> cell : row) {
                    if (true /*cell.height > 10 && cell.width > 10*/) {
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
