package fr.neolegal.fec.liassefiscale.pdf;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;

import fr.neolegal.fec.liassefiscale.MethodeExtraction;
import fr.neolegal.fec.liassefiscale.ModeleFormulaire;
import fr.neolegal.fec.liassefiscale.MontantExtrait;
import fr.neolegal.fec.liassefiscale.Repere;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;

/**
 * Extraction des montants à partir du quadrillage du tableau.
 * <p>
 * Lorsque le document dessine les bordures de ses cellules, la grille reconnue
 * donne directement la cellule voisine du code d'un repère. Cette lecture est
 * très sûre, mais inopérante sur les nombreuses éditions dépourvues de
 * bordures : elle complète la lecture géométrique de
 * {@link ExtracteurMontants} et la confirme.
 */
public class ExtracteurQuadrillage {

    private ExtracteurQuadrillage() {
    }

    @SuppressWarnings("rawtypes")
    public static List<MontantExtrait> extraire(Table table, ModeleFormulaire modele, int page) {
        Map<String, Repere> parSymbole = new HashMap<>();
        modele.getAllReperes().forEach(repere -> parSymbole.put(repere.getSymbole().toUpperCase(), repere));
        List<Repere> designesParLeurNom = modele.getAllReperes().stream()
                .filter(repere -> repere.getFromNom() != null).toList();

        List<MontantExtrait> montants = new ArrayList<>();
        List<List<RectangularTextContainer>> lignes = table.getRows();

        for (int indexLigne = 0; indexLigne < lignes.size(); ++indexLigne) {
            List<RectangularTextContainer> ligne = lignes.get(indexLigne);
            for (int indexColonne = 0; indexColonne < ligne.size(); ++indexColonne) {
                String texte = texte(ligne.get(indexColonne));
                Point origine = new Point(indexColonne, indexLigne);

                Optional<Repere> repere = Optional.ofNullable(parSymbole.get(texte.toUpperCase()));
                Point navigation = repere.map(Repere::getFromSymbole).orElse(null);
                if (repere.isEmpty()) {
                    repere = rechercherParNom(designesParLeurNom, texte);
                    navigation = repere.map(Repere::getFromNom).orElse(null);
                }
                if (repere.isEmpty()) {
                    continue;
                }

                lire(table, origine, navigation, repere.get(), page).ifPresent(montants::add);
            }
        }

        return montants;
    }

    /**
     * Certains modèles désignent la cellule à lire à partir du libellé de la ligne
     * plutôt que du code du repère.
     */
    private static Optional<Repere> rechercherParNom(List<Repere> reperes, String texte) {
        if (texte.isEmpty()) {
            return Optional.empty();
        }
        String libelle = MotPdf.normalise(texte);
        return reperes.stream().filter(repere -> libelle.contains(MotPdf.normalise(repere.getNom()))).findFirst();
    }

    @SuppressWarnings("rawtypes")
    private static Optional<MontantExtrait> lire(Table table, Point origine, Point navigation, Repere repere,
            int page) {
        Point position = new Point(origine);
        Point deplacement = ObjectUtils.firstNonNull(navigation, new Point(1, 0));
        position.translate(deplacement.x, deplacement.y);

        List<List<RectangularTextContainer>> lignes = table.getRows();
        if (position.y < 0 || position.y >= lignes.size() || position.x < 0
                || position.x >= lignes.get(position.y).size()) {
            return Optional.empty();
        }

        String texte = lignes.get(position.y).get(position.x).getText();
        Optional<Double> montant = MotPdf.parseMontant(texte);
        MontantExtrait.MontantExtraitBuilder builder = MontantExtrait.builder().symbole(repere.getSymbole())
                .page(page);
        if (montant.isEmpty()) {
            if (!texte.isBlank()) {
                // La cellule contient autre chose qu'un montant : la grille est mal reconnue
                return Optional.empty();
            }
            return Optional.of(builder.methode(MethodeExtraction.CELLULE_VIDE).montant(0).celluleVide(true)
                    .confiance(MethodeExtraction.CELLULE_VIDE.getConfianceBase() - 0.05).build());
        }

        return Optional.of(builder.methode(MethodeExtraction.QUADRILLAGE).montant(montant.get())
                .texteSource(texte.trim()).confiance(MethodeExtraction.QUADRILLAGE.getConfianceBase()).build());
    }

    private static String texte(RectangularTextContainer<?> cellule) {
        return cellule.getText().replaceAll("\\s", "");
    }
}
