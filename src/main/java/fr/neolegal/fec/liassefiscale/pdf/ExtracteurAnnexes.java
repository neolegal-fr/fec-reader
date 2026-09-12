package fr.neolegal.fec.liassefiscale.pdf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import fr.neolegal.fec.liassefiscale.NatureAnnexe;
import fr.neolegal.fec.liassefiscale.StrUtils;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

/**
 * Extraction du contenu libre des annexes (détail des réintégrations, des
 * provisions, des charges exceptionnelles...).
 * <p>
 * Contrairement aux montants des repères, ces tableaux n'ont pas de structure
 * connue à l'avance : leur lecture s'appuie sur le quadrillage du document.
 */
public class ExtracteurAnnexes {

    private ExtracteurAnnexes() {
    }

    /** Algorithme de détection des tableaux, calibré sur les liasses fiscales. */
    public static SpreadsheetExtractionAlgorithm algorithme() {
        return new SpreadsheetExtractionAlgorithm()
                .withMaxGapBetweenAlignedHorizontalRulings(30)
                .withMaxGapBetweenAlignedVerticalRulings(15)
                .withMinColumnWidth(9f)
                .withMinRowHeight(9f);
    }

    /** Plus grand tableau de la page, au sens du nombre de lignes. */
    public static Optional<Table> plusGrandTableau(Page page) {
        return algorithme().extract(page).stream().max(Comparator.comparing(Table::getRowCount));
    }

    @SuppressWarnings("rawtypes")
    public static List<? extends List<String>> extraire(Table table, NatureAnnexe natureAnnexe,
            boolean rechercherTitre) {
        List<List<String>> resultat = new LinkedList<>();

        List<List<RectangularTextContainer>> lignes = table.getRows();
        if (lignes.isEmpty()) {
            return resultat;
        }

        int indexEntete = 0;
        if (rechercherTitre) {
            for (int i = 0; i < lignes.size() && indexEntete == 0; ++i) {
                // On cherche la première ligne contenant le nom de l'annexe :
                // les données sont sur les lignes suivantes
                for (RectangularTextContainer<?> cellule : lignes.get(i)) {
                    if (StrUtils.containsIgnoreCase(cellule.getText().trim(), natureAnnexe.getIntitule())) {
                        indexEntete = i;
                    }
                }
            }

            // La ligne d'en-tête peut être composée de cellules fusionnées : on cherche la
            // première ligne suivante qui reprend la structure du tableau
            List<RectangularTextContainer> entete = lignes.get(indexEntete);
            float debutLigne = entete.get(0).getLeft();
            while ((indexEntete + 1) < lignes.size() && lignes.get(indexEntete + 1).get(0).getLeft() != debutLigne) {
                ++indexEntete;
            }
        }

        List<RectangularTextContainer> precedente = null;
        boolean encoreDesDonnees = true;
        for (int i = indexEntete + 1; i < lignes.size() && encoreDesDonnees; ++i) {
            List<RectangularTextContainer> ligne = lignes.get(i);
            List<String> valeurs = new ArrayList<>();
            boolean ligneVide = true;
            for (RectangularTextContainer<?> cellule : ligne) {
                String texte = cellule.getText().trim();
                ligneVide = ligneVide && texte.isEmpty();
                valeurs.add(texte);
            }

            encoreDesDonnees = !ligneVide && (precedente == null || memeStructure(ligne, precedente));
            if (encoreDesDonnees) {
                precedente = ligne;
                resultat.add(valeurs);
            }
        }

        return resultat;
    }

    @SuppressWarnings("rawtypes")
    private static boolean memeStructure(List<RectangularTextContainer> ligne,
            List<RectangularTextContainer> autre) {
        if (ligne == null || autre == null || ligne.size() != autre.size()) {
            return false;
        }

        for (int i = 0; i < ligne.size(); ++i) {
            if (ligne.get(i).getWidth() != autre.get(i).getWidth()) {
                return false;
            }
        }
        return true;
    }
}
