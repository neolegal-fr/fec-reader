package fr.neolegal.fec.liassefiscale.pdf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import fr.neolegal.fec.liassefiscale.MethodeExtraction;
import fr.neolegal.fec.liassefiscale.ModeleFormulaire;
import fr.neolegal.fec.liassefiscale.MontantExtrait;
import fr.neolegal.fec.liassefiscale.Repere;
import fr.neolegal.fec.liassefiscale.pdf.DispositionFormulaire.LigneModele;

/**
 * Extraction des montants d'une page de formulaire.
 * <p>
 * Les formulaires de liasse fiscale font précéder chaque cellule chiffrée du
 * code du repère correspondant : la valeur est donc le premier montant situé à
 * droite du code, dans la colonne qui le suit. Cette lecture géométrique ne
 * dépend ni du quadrillage du tableau (absent de nombreuses éditions) ni de
 * l'ordre de restitution du texte par le PDF.
 * <p>
 * Lorsque les codes des repères ne sont pas lisibles (imprimés sous forme
 * d'image, par exemple), une seconde passe reconnaît les libellés des lignes du
 * formulaire et lit les valeurs dans l'ordre des colonnes.
 */
public class ExtracteurMontants {

    /**
     * Part de la largeur de la page réservée aux libellés : aucune colonne de
     * valeurs ne commence avant.
     */
    private static final float MARGE_LIBELLES = 0.20f;

    /** Similarité minimale entre le libellé lu et celui du modèle */
    private static final double SEUIL_LIBELLE = 0.80;

    private ExtracteurMontants() {
    }

    public static List<MontantExtrait> extraire(PagePdf page, ModeleFormulaire modele) {
        Map<String, Repere> parSymbole = new LinkedHashMap<>();
        for (Repere repere : modele.getAllReperes()) {
            parSymbole.put(repere.getSymbole(), repere);
        }

        List<Ancre> ancres = localiserAncres(page, parSymbole);
        List<ColonneValeurs> colonnes = colonnes(page, ancres);

        Map<String, MontantExtrait> montants = new LinkedHashMap<>();
        for (MontantExtrait montant : extraireParRepere(page, ancres, colonnes)) {
            montants.merge(montant.getSymbole(), montant, ExtracteurMontants::meilleur);
        }
        for (MontantExtrait montant : extraireParLibelle(page, modele, colonnes, montants.keySet())) {
            montants.merge(montant.getSymbole(), montant, ExtracteurMontants::meilleur);
        }

        return new ArrayList<>(montants.values());
    }

    /**
     * Colonnes de valeurs de la page, les codes des repères étant exclus : ceux des
     * formulaires 2033 sont numériques et seraient pris pour des montants.
     */
    private static List<ColonneValeurs> colonnes(PagePdf page, List<Ancre> ancres) {
        Set<MotPdf> motsAncres = new HashSet<>();
        ancres.forEach(ancre -> motsAncres.add(ancre.mot));
        List<float[]> colonnesCodes = colonnesCodes(ancres);
        Set<MotPdf> dansUnTexte = montantsDansUnTexte(page);
        List<MotPdf> mots = page.getMots().stream()
                .filter(mot -> !motsAncres.contains(mot))
                .filter(mot -> !dansColonneDeCodes(mot, colonnesCodes))
                .filter(mot -> !dansUnTexte.contains(mot))
                .toList();
        return ColonneValeurs.detecter(mots, page.getLargeur());
    }

    /**
     * Emprises horizontales des colonnes de codes de repères. Les appels de note et
     * les renvois qui s'y trouvent ne sont pas des montants : les ignorer évite de
     * réunir deux colonnes de valeurs de part et d'autre.
     */
    private static List<float[]> colonnesCodes(List<Ancre> ancres) {
        List<float[]> colonnes = new ArrayList<>();
        for (Ancre ancre : ancres) {
            float[] existante = colonnes.stream()
                    .filter(colonne -> ancre.mot.getGauche() <= colonne[1] && ancre.mot.getDroite() >= colonne[0])
                    .findFirst().orElse(null);
            if (existante == null) {
                colonnes.add(new float[] { ancre.mot.getGauche(), ancre.mot.getDroite() });
            } else {
                existante[0] = Math.min(existante[0], ancre.mot.getGauche());
                existante[1] = Math.max(existante[1], ancre.mot.getDroite());
            }
        }
        return colonnes;
    }

    /**
     * Nombres cités dans une phrase ("total des lignes 5+24-25+27") : encadrés de
     * mots, ils ne sont pas les valeurs d'une cellule et ne constituent donc pas
     * une colonne.
     */
    private static Set<MotPdf> montantsDansUnTexte(PagePdf page) {
        Set<MotPdf> montants = new HashSet<>();
        for (LignePdf rangee : page.getRangees()) {
            List<MotPdf> mots = rangee.getMots();
            for (int i = 0; i < mots.size(); i++) {
                MotPdf mot = mots.get(i);
                float espace = 2 * Math.max(mot.getLargeurEspace(), 1f);
                boolean texteAvant = i > 0 && estUnMot(mots.get(i - 1))
                        && mot.getGauche() - mots.get(i - 1).getDroite() < espace;
                boolean texteApres = i + 1 < mots.size() && estUnMot(mots.get(i + 1))
                        && mots.get(i + 1).getGauche() - mot.getDroite() < espace;
                if (mot.estMontant() && texteAvant && texteApres) {
                    montants.add(mot);
                }
            }
        }
        return montants;
    }

    /** Un mot de la langue, par opposition à un montant ou à un code de repère. */
    private static boolean estUnMot(MotPdf mot) {
        return !mot.estMontant() && mot.getTexte().chars().filter(Character::isLetter).count() >= 2;
    }

    private static boolean dansColonneDeCodes(MotPdf mot, List<float[]> colonnesCodes) {
        // Un montant de plus de trois caractères déborde de la colonne des codes : il
        // s'agit d'une valeur, quand bien même son centre s'y trouverait
        if (mot.getTexte().trim().length() > 3) {
            return false;
        }
        return colonnesCodes.stream()
                .anyMatch(colonne -> mot.getGauche() >= colonne[0] - 1 && mot.getDroite() <= colonne[1] + 1);
    }

    /** Colonnes de valeurs de la page, pour le diagnostic de l'extraction. */
    public static List<ColonneValeurs> colonnes(PagePdf page, ModeleFormulaire modele) {
        Map<String, Repere> parSymbole = new LinkedHashMap<>();
        modele.getAllReperes().forEach(repere -> parSymbole.put(repere.getSymbole(), repere));
        return colonnes(page, localiserAncres(page, parSymbole));
    }

    static MontantExtrait meilleur(MontantExtrait gauche, MontantExtrait droite) {
        return gauche.getConfiance() >= droite.getConfiance() ? gauche : droite;
    }

    /** Ancre : code de repère localisé dans la page. */
    private static class Ancre {
        final MotPdf mot;
        final Repere repere;
        final LignePdf ligne;
        final int index;

        Ancre(MotPdf mot, Repere repere, LignePdf ligne, int index) {
            this.mot = mot;
            this.repere = repere;
            this.ligne = ligne;
            this.index = index;
        }
    }

    private static List<MontantExtrait> extraireParRepere(PagePdf page, List<Ancre> ancres,
            List<ColonneValeurs> colonnes) {
        List<MontantExtrait> montants = new ArrayList<>();
        for (int i = 0; i < ancres.size(); i++) {
            Ancre ancre = ancres.get(i);
            Ancre suivante = i + 1 < ancres.size() && ancres.get(i + 1).ligne == ancre.ligne ? ancres.get(i + 1)
                    : null;
            montants.add(lireCellule(page, colonnes, ancre, suivante));
        }
        return montants;
    }

    /**
     * Localise les codes de repères dans la page, en écartant les mots qui leur
     * ressemblent : les codes sont alignés dans quelques colonnes étroites, un mot
     * isolé hors de ces colonnes n'est pas un code.
     */
    private static List<Ancre> localiserAncres(PagePdf page, Map<String, Repere> parSymbole) {
        List<Ancre> candidates = new ArrayList<>();
        for (LignePdf ligne : page.getRangees()) {
            List<MotPdf> mots = ligne.getMots();
            for (int index = 0; index < mots.size(); index++) {
                MotPdf mot = mots.get(index);
                Repere repere = parSymbole.get(mot.getTexte().trim());
                if (repere != null) {
                    candidates.add(new Ancre(mot, repere, ligne, index));
                }
            }
        }

        return filtrerParColonnes(candidates);
    }

    private static List<Ancre> filtrerParColonnes(List<Ancre> candidates) {
        TreeMap<Float, List<Ancre>> colonnes = new TreeMap<>();
        for (Ancre ancre : candidates) {
            Map.Entry<Float, List<Ancre>> proche = colonnes.floorEntry(ancre.mot.getGauche());
            Map.Entry<Float, List<Ancre>> suivante = colonnes.ceilingEntry(ancre.mot.getGauche());
            Float cle = null;
            if (proche != null && ancre.mot.getGauche() - proche.getKey() <= 4f) {
                cle = proche.getKey();
            } else if (suivante != null && suivante.getKey() - ancre.mot.getGauche() <= 4f) {
                cle = suivante.getKey();
            }
            colonnes.computeIfAbsent(cle == null ? ancre.mot.getGauche() : cle, x -> new ArrayList<>()).add(ancre);
        }

        boolean colonnesFiables = colonnes.values().stream().anyMatch(ancres -> ancres.size() >= 3);
        List<Ancre> retenues = new ArrayList<>();
        for (List<Ancre> ancres : colonnes.values()) {
            if (!colonnesFiables || ancres.size() >= 2) {
                retenues.addAll(ancres);
            }
        }

        // Les ancres sont ordonnées par rangée puis de gauche à droite : la valeur
        // d'un repère se trouve entre son code et le code suivant de la rangée
        retenues.sort(Comparator.comparing((Ancre ancre) -> ancre.ligne.getHaut())
                .thenComparing(ancre -> ancre.mot.getGauche()));
        return retenues;
    }

    /**
     * Lit la valeur associée à une ancre : le premier montant situé à sa droite,
     * dans la colonne qui la suit immédiatement, avant le repère suivant.
     */
    private static MontantExtrait lireCellule(PagePdf page, List<ColonneValeurs> colonnes, Ancre ancre,
            Ancre suivante) {
        float borneDroite = suivante == null ? Float.MAX_VALUE : suivante.mot.getGauche();
        // La valeur est lue dans la première colonne située à droite du code
        ColonneValeurs colonne = colonnes.stream()
                .filter(c -> c.getGauche() > ancre.mot.getDroite() - 1)
                .findFirst().orElse(null);

        MotPdf valeur = null;
        boolean colonneRespectee = false;
        List<MotPdf> mots = ancre.ligne.getMots();
        for (int i = ancre.index + 1; i < mots.size() && mots.get(i).getGauche() < borneDroite; i++) {
            MotPdf mot = mots.get(i);
            if (!mot.estMontant()) {
                continue;
            }
            if (colonne == null || colonne.contient(mot)) {
                valeur = mot;
                colonneRespectee = colonne != null;
                break;
            }
            // Le montant déborde de la colonne attendue : il appartient à une autre
            // cellule, celle du repère est donc vide
            if (mot.getGauche() > colonne.getDroite()) {
                break;
            }
        }

        MontantExtrait.MontantExtraitBuilder builder = MontantExtrait.builder()
                .symbole(ancre.repere.getSymbole())
                .page(page.getNumero())
                .ocr(page.isIssueOcr());

        if (valeur == null) {
            return builder.methode(MethodeExtraction.CELLULE_VIDE).montant(0).celluleVide(true)
                    .confiance(confiance(MethodeExtraction.CELLULE_VIDE.getConfianceBase(), page)).build();
        }

        double confiance = MethodeExtraction.REPERE.getConfianceBase() + (colonneRespectee ? 0.05 : -0.10);
        return builder.methode(MethodeExtraction.REPERE).montant(valeur.getMontant())
                .texteSource(valeur.getTexte()).confiance(confiance(confiance, page)).build();
    }

    private static double confiance(double confiance, PagePdf page) {
        return Math.max(0, Math.min(1, page.isIssueOcr() ? confiance * 0.8 : confiance));
    }

    /**
     * Seconde passe : reconnaissance des lignes par leur libellé, pour les repères
     * dont le code n'a pas été trouvé.
     */
    private static List<MontantExtrait> extraireParLibelle(PagePdf page, ModeleFormulaire modele,
            List<ColonneValeurs> toutesColonnes, Set<String> dejaExtraits) {
        List<ColonneValeurs> colonnes = colonnesPrincipales(toutesColonnes, page.getLargeur());
        if (colonnes.isEmpty()) {
            return List.of();
        }

        DispositionFormulaire disposition = DispositionFormulaire.of(modele);
        float debutValeurs = colonnes.get(0).getGauche();

        List<MontantExtrait> montants = new ArrayList<>();
        for (LignePdf ligne : page.getRangees()) {
            String libelle = libelleLigne(ligne, debutValeurs);
            LigneModele ligneModele = disposition.rechercherLigne(libelle, SEUIL_LIBELLE);
            if (ligneModele == null) {
                continue;
            }

            List<Repere> reperes = ligneModele.getReperes();
            boolean utile = reperes.stream().anyMatch(repere -> !dejaExtraits.contains(repere.getSymbole()));
            if (!utile) {
                continue;
            }

            for (int index = 0; index < reperes.size() && index < colonnes.size(); index++) {
                Repere repere = reperes.get(index);
                if (dejaExtraits.contains(repere.getSymbole())) {
                    continue;
                }
                Optional<MotPdf> valeur = valeurDansColonne(ligne, colonnes.get(index));
                double confiance = MethodeExtraction.LIBELLE.getConfianceBase();
                MontantExtrait.MontantExtraitBuilder builder = MontantExtrait.builder()
                        .symbole(repere.getSymbole()).page(page.getNumero()).ocr(page.isIssueOcr());
                if (valeur.isPresent()) {
                    montants.add(builder.methode(MethodeExtraction.LIBELLE).montant(valeur.get().getMontant())
                            .texteSource(valeur.get().getTexte()).confiance(confiance(confiance, page)).build());
                } else {
                    montants.add(builder.methode(MethodeExtraction.CELLULE_VIDE).montant(0).celluleVide(true)
                            .confiance(confiance(confiance - 0.1, page)).build());
                }
            }
        }
        return montants;
    }

    /**
     * Colonnes du tableau principal de la page : une valeur isolée peut former une
     * colonne parasite (numéro de page, renvoi), que le faible nombre de ses
     * valeurs distingue des colonnes du tableau.
     */
    static List<ColonneValeurs> colonnesPrincipales(List<ColonneValeurs> colonnes, float largeurPage) {
        int maximum = colonnes.stream().mapToInt(ColonneValeurs::getNombreValeurs).max().orElse(0);
        return colonnes.stream()
                .filter(colonne -> colonne.getNombreValeurs() >= Math.max(3, maximum / 4))
                .filter(colonne -> colonne.getGauche() >= MARGE_LIBELLES * largeurPage)
                .toList();
    }

    private static Optional<MotPdf> valeurDansColonne(LignePdf ligne, ColonneValeurs colonne) {
        return ligne.getMots().stream().filter(MotPdf::estMontant).filter(colonne::contient).findFirst();
    }

    /** Texte de la ligne situé à gauche de la première colonne de valeurs. */
    static String libelleLigne(LignePdf ligne, float debutValeurs) {
        StringBuilder libelle = new StringBuilder();
        for (MotPdf mot : ligne.getMots()) {
            if (mot.getDroite() >= debutValeurs) {
                break;
            }
            if (libelle.length() > 0) {
                libelle.append(' ');
            }
            libelle.append(mot.getTexte());
        }
        return libelle.toString();
    }
}
