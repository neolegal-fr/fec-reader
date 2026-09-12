package fr.neolegal.fec.liassefiscale.pdf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import fr.neolegal.fec.liassefiscale.ModeleFormulaire;
import fr.neolegal.fec.liassefiscale.Repere;
import fr.neolegal.fec.liassefiscale.Section;
import fr.neolegal.fec.liassefiscale.StrUtils;
import lombok.Getter;

/**
 * Disposition d'un formulaire : regroupement des repères par ligne du tableau et
 * ordre de leurs colonnes.
 * <p>
 * Elle est déduite de la hiérarchie du modèle : une section porte le libellé
 * d'une ligne, et les repères qu'elle contient en désignent les colonnes
 * ("Montant brut", "Amortissements", "Net"...). Cette disposition permet de
 * localiser une valeur à partir du libellé de sa ligne lorsque le code du
 * repère n'est pas lisible dans le PDF (codes imprimés sous forme d'image, par
 * exemple).
 */
@Getter
public class DispositionFormulaire {

    private static final Map<String, DispositionFormulaire> CACHE = new ConcurrentHashMap<>();

    /** Libellés de colonnes, par ordre d'apparition dans les formulaires */
    private static final List<String> COLONNES = List.of(
            "MONTANTBRUT", "VALEURBRUTE", "BRUT", "FRANCE",
            "AMORTISSEMENT", "AMORTISSEMENTS", "PROVISIONS", "DEPRECIATIONS", "EXPORTATION", "EXPORTATIONS",
            "A1ANAUPLUS", "DOTATIONSDELEXERCICE",
            "NET", "APLUSDUNAN", "REPRISESSURLEXERCICE");

    /** Lignes du formulaire, indexées par leur libellé normalisé */
    private final Map<String, LigneModele> lignes;

    private final Map<String, Integer> colonneParRepere;

    private DispositionFormulaire(Map<String, LigneModele> lignes, Map<String, Integer> colonneParRepere) {
        this.lignes = lignes;
        this.colonneParRepere = colonneParRepere;
    }

    /** Une ligne du tableau : un libellé et les repères de ses colonnes. */
    @Getter
    public static class LigneModele {
        private final String libelle;
        private final List<Repere> reperes = new ArrayList<>();

        LigneModele(String libelle) {
            this.libelle = libelle;
        }
    }

    public static DispositionFormulaire of(ModeleFormulaire modele) {
        return CACHE.computeIfAbsent(modele.getIdentifiant(), identifiant -> construire(modele));
    }

    private static DispositionFormulaire construire(ModeleFormulaire modele) {
        Map<String, LigneModele> lignes = new LinkedHashMap<>();
        collecter(modele, null, lignes);

        Map<String, Integer> colonnes = new LinkedHashMap<>();
        for (LigneModele ligne : lignes.values()) {
            ligne.reperes.sort(Comparator.comparingInt((Repere repere) -> rang(repere, ligne))
                    .thenComparing(Repere::getSymbole));
            for (int i = 0; i < ligne.reperes.size(); i++) {
                colonnes.put(ligne.reperes.get(i).getSymbole(), i);
            }
        }

        return new DispositionFormulaire(lignes, colonnes);
    }

    private static void collecter(Section section, String libelleSection, Map<String, LigneModele> lignes) {
        for (Repere repere : section.getReperes()) {
            String libelle = estLibelleColonne(repere.getNom()) && libelleSection != null ? libelleSection
                    : repere.getNom();
            if (libelle == null || libelle.isBlank()) {
                continue;
            }
            lignes.computeIfAbsent(MotPdf.normalise(libelle), cle -> new LigneModele(libelle)).reperes.add(repere);
        }

        Set<Section> sections = section.getSections();
        if (sections != null) {
            for (Section sousSection : sections) {
                collecter(sousSection, sousSection.getNom(), lignes);
            }
        }
    }

    /** Position de la colonne du repère dans sa ligne. */
    private static int rang(Repere repere, LigneModele ligne) {
        String nom = MotPdf.normalise(repere.getNom());
        int index = COLONNES.indexOf(nom);
        if (index >= 0) {
            return index;
        }

        // Le repère porte le libellé de la ligne : c'est soit la première colonne
        // (lorsque les autres colonnes sont des amortissements), soit la colonne de
        // total (lorsque les autres colonnes la composent, France + Exportations)
        boolean colonnesComposantes = ligne.reperes.stream().map(autre -> MotPdf.normalise(autre.getNom()))
                .anyMatch(autre -> autre.startsWith("FRANCE") || autre.startsWith("EXPORT"));
        return colonnesComposantes ? COLONNES.size() + 1 : -1;
    }

    static boolean estLibelleColonne(String nom) {
        return COLONNES.contains(MotPdf.normalise(nom));
    }

    /** Index de la colonne du repère dans sa ligne, 0 pour la première. */
    public int getColonne(Repere repere) {
        return colonneParRepere.getOrDefault(repere.getSymbole(), 0);
    }

    /** Recherche la ligne du modèle dont le libellé correspond au texte. */
    public LigneModele rechercherLigne(String libelle, double seuil) {
        String normalise = MotPdf.normalise(libelle);
        if (normalise.length() < 6) {
            return null;
        }
        LigneModele meilleure = null;
        double meilleureSimilarite = seuil;
        double suivante = 0;
        for (LigneModele ligne : lignes.values()) {
            double similarite = Similarite.calculerAvecTroncature(normalise, ligne.getLibelle());
            if (similarite > meilleureSimilarite) {
                suivante = meilleureSimilarite;
                meilleureSimilarite = similarite;
                meilleure = ligne;
            } else if (similarite > suivante) {
                suivante = similarite;
            }
        }
        // Un libellé ambigu (deux lignes aussi proches l'une que l'autre) est écarté
        return meilleureSimilarite - suivante >= 0.05 ? meilleure : null;
    }

    static String normaliserLibelle(String libelle) {
        return MotPdf.normalise(StrUtils.stripAccents(libelle));
    }
}
