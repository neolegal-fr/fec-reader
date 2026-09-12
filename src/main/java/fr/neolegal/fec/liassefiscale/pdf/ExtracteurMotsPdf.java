package fr.neolegal.fec.liassefiscale.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * Extrait les mots d'un document PDF avec leur position.
 * <p>
 * Contrairement à {@link PDFTextStripper}, qui restitue un flux de texte, cet
 * extracteur conserve la géométrie de chaque mot : c'est elle qui permet
 * d'associer un repère de liasse fiscale à la valeur de sa cellule, sans
 * dépendre de la présence d'un quadrillage dans le PDF.
 */
public class ExtracteurMotsPdf extends PDFTextStripper {

    /**
     * Deux glyphes appartiennent au même mot si l'espace qui les sépare est
     * inférieur à cette fraction de la largeur d'une espace.
     */
    private static final float SEUIL_MOT = 0.55f;

    /**
     * Deux groupes de chiffres sont fusionnés en un seul montant (séparateur de
     * milliers) si l'espace qui les sépare est inférieur à cette fraction de la
     * largeur d'une espace.
     */
    private static final float SEUIL_MILLIERS = 1.3f;

    private final Map<Integer, List<TextPosition>> glyphesParPage = new LinkedHashMap<>();

    public ExtracteurMotsPdf() throws IOException {
        super();
        setSortByPosition(false);
    }

    @Override
    protected void processTextPosition(TextPosition texte) {
        String unicode = texte.getUnicode();
        if (unicode == null || unicode.isBlank()) {
            return;
        }
        glyphesParPage.computeIfAbsent(getCurrentPageNo(), p -> new ArrayList<>()).add(texte);
    }

    /**
     * Ne conserve que les glyphes orientés comme la majorité de la page : les
     * libellés imprimés verticalement dans les marges des formulaires
     * s'entrelaceraient sinon avec le texte horizontal.
     */
    static List<TextPosition> filtrerOrientationDominante(List<TextPosition> glyphes) {
        Map<Integer, List<TextPosition>> parOrientation = new LinkedHashMap<>();
        for (TextPosition glyphe : glyphes) {
            parOrientation.computeIfAbsent(Math.round(glyphe.getDir()), dir -> new ArrayList<>()).add(glyphe);
        }
        return parOrientation.values().stream().max(Comparator.comparingInt(List::size)).orElse(List.of());
    }

    /** Extrait les mots de toutes les pages du document. */
    public static Map<Integer, List<MotPdf>> extraire(PDDocument document) throws IOException {
        ExtracteurMotsPdf extracteur = new ExtracteurMotsPdf();
        extracteur.getText(document);

        Map<Integer, List<MotPdf>> resultat = new LinkedHashMap<>();
        for (int page = 1; page <= document.getNumberOfPages(); page++) {
            resultat.put(page, assembler(
                    filtrerOrientationDominante(extracteur.glyphesParPage.getOrDefault(page, List.of())), page));
        }
        return resultat;
    }

    /** Regroupe les glyphes d'une page en mots. */
    static List<MotPdf> assembler(List<TextPosition> glyphes, int page) {
        List<MotPdf> mots = new ArrayList<>();
        if (glyphes.isEmpty()) {
            return mots;
        }

        // Regroupement en lignes : deux glyphes sont sur la même ligne si leurs
        // rectangles se recouvrent verticalement de plus de la moitié
        List<List<TextPosition>> lignes = new ArrayList<>();
        List<TextPosition> tries = new ArrayList<>(glyphes);
        tries.sort(Comparator.comparing(ExtracteurMotsPdf::bas).thenComparing(TextPosition::getXDirAdj));
        for (TextPosition glyphe : tries) {
            List<TextPosition> ligne = lignes.isEmpty() ? null : lignes.get(lignes.size() - 1);
            if (ligne != null && memeLigne(ligne.get(ligne.size() - 1), glyphe)) {
                ligne.add(glyphe);
            } else {
                ligne = new ArrayList<>();
                ligne.add(glyphe);
                lignes.add(ligne);
            }
        }

        for (List<TextPosition> ligne : lignes) {
            ligne.sort(Comparator.comparing(TextPosition::getXDirAdj));
            mots.addAll(assemblerLigne(ligne, page));
        }

        mots.sort(Comparator.comparing(MotPdf::getBas).thenComparing(MotPdf::getGauche));
        return mots;
    }

    private static List<MotPdf> assemblerLigne(List<TextPosition> ligne, int page) {
        List<MotPdf> mots = new ArrayList<>();
        StringBuilder texte = new StringBuilder();
        float gauche = 0;
        float droite = 0;
        float haut = 0;
        float bas = 0;
        float taille = 0;
        for (TextPosition glyphe : ligne) {
            float debut = glyphe.getXDirAdj();
            float fin = debut + glyphe.getWidthDirAdj();
            boolean nouveauMot = texte.length() == 0 || (debut - droite) > SEUIL_MOT * espace(glyphe)
                    || codeAccoleAUnMontant(texte, glyphe, debut - droite);
            if (nouveauMot) {
                if (texte.length() > 0) {
                    mots.add(new MotPdf(texte.toString(), gauche, droite, haut, bas, taille, page));
                }
                texte.setLength(0);
                gauche = debut;
                haut = haut(glyphe);
                bas = bas(glyphe);
                taille = espace(glyphe);
            } else {
                haut = Math.min(haut, haut(glyphe));
                bas = Math.max(bas, bas(glyphe));
            }
            texte.append(glyphe.getUnicode());
            droite = Math.max(droite, fin);
        }
        if (texte.length() > 0) {
            mots.add(new MotPdf(texte.toString(), gauche, droite, haut, bas, taille, page));
        }

        return fusionnerMontants(mots, page);
    }

    /**
     * Fusionne les groupes de chiffres séparés par une espace fine ("1 234 567"
     * restitué en trois mots) ainsi que les signes isolés qui précèdent ou suivent
     * un montant.
     */
    private static List<MotPdf> fusionnerMontants(List<MotPdf> mots, int page) {
        List<MotPdf> resultat = new ArrayList<>();
        for (MotPdf mot : mots) {
            MotPdf precedent = resultat.isEmpty() ? null : resultat.get(resultat.size() - 1);
            if (precedent != null && fusionnable(precedent, mot)) {
                resultat.set(resultat.size() - 1, new MotPdf(precedent.getTexte() + " " + mot.getTexte(),
                        precedent.getGauche(), mot.getDroite(),
                        Math.min(precedent.getHaut(), mot.getHaut()), Math.max(precedent.getBas(), mot.getBas()),
                        precedent.getLargeurEspace(), page));
            } else {
                resultat.add(mot);
            }
        }
        return resultat;
    }

    private static boolean fusionnable(MotPdf gauche, MotPdf droite) {
        float ecart = droite.getGauche() - gauche.getDroite();
        if (ecart > SEUIL_MILLIERS * Math.max(gauche.getLargeurEspace(), 1f)) {
            return false;
        }
        String g = gauche.getTexte().trim();
        String d = droite.getTexte().trim();
        // Le dernier groupe de chiffres peut porter la parenthèse ou le signe qui
        // marque un montant négatif : "(1 234 567)" est restitué en trois mots
        String chiffres = d.endsWith(")") || d.endsWith("-") ? d.substring(0, d.length() - 1) : d;
        boolean groupeMilliers = gauche.estMontant() && chiffres.length() == 3 && estEntier(chiffres);
        boolean signePrefixe = (g.equals("-") || g.equals("(") || g.equals("–")) && droite.estMontant();
        boolean signeSuffixe = (d.equals("-") || d.equals(")")) && gauche.estMontant();
        return groupeMilliers || signePrefixe || signeSuffixe;
    }

    /**
     * Sur certaines éditions, la valeur d'une cellule et le code du repère suivant
     * sont restitués d'un seul tenant ("146 949 166 VZ") : la transition d'un
     * chiffre vers une majuscule les sépare, sans disjoindre pour autant les codes
     * qui commencent par un chiffre ("1A"), dont les caractères sont accolés.
     */
    private static boolean codeAccoleAUnMontant(CharSequence texte, TextPosition glyphe, float ecart) {
        char precedent = texte.charAt(texte.length() - 1);
        String unicode = glyphe.getUnicode();
        return Character.isDigit(precedent) && !unicode.isEmpty() && Character.isUpperCase(unicode.charAt(0))
                && ecart > 0.15f * espace(glyphe);
    }

    private static boolean estEntier(String texte) {
        if (texte.isEmpty()) {
            return false;
        }
        for (int i = 0; i < texte.length(); i++) {
            if (!Character.isDigit(texte.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean memeLigne(TextPosition precedent, TextPosition glyphe) {
        float haut1 = haut(precedent);
        float bas1 = bas(precedent);
        float haut2 = haut(glyphe);
        float bas2 = bas(glyphe);
        float recouvrement = Math.min(bas1, bas2) - Math.max(haut1, haut2);
        float hauteurMin = Math.max(0.1f, Math.min(bas1 - haut1, bas2 - haut2));
        return recouvrement / hauteurMin > 0.5f;
    }

    private static float bas(TextPosition glyphe) {
        return glyphe.getYDirAdj();
    }

    private static float haut(TextPosition glyphe) {
        return glyphe.getYDirAdj() - Math.max(glyphe.getHeightDir(), 1f);
    }

    private static float espace(TextPosition glyphe) {
        float largeur = glyphe.getWidthOfSpace();
        if (largeur <= 0.1f || Float.isNaN(largeur)) {
            largeur = Math.max(glyphe.getHeightDir(), 1f) * 0.5f;
        }
        return largeur;
    }
}
