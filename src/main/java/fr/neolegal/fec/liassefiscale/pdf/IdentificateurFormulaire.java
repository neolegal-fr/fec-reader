package fr.neolegal.fec.liassefiscale.pdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import fr.neolegal.fec.liassefiscale.ModeleFormulaire;
import fr.neolegal.fec.liassefiscale.NatureAnnexe;
import fr.neolegal.fec.liassefiscale.Repere;

/**
 * Reconnaissance du formulaire présenté par une page de liasse fiscale.
 * <p>
 * Trois indices sont combinés : la référence du formulaire (numéro CERFA et
 * indice de page) présente dans l'en-tête, la similarité du titre, et la
 * proportion des repères du modèle effectivement présents dans la page. Ce
 * dernier indice est le plus discriminant : il distingue par exemple la page
 * "BILAN ACTIF" d'une plaquette de présentation du formulaire 2050-SD qui porte
 * le même titre.
 */
public class IdentificateurFormulaire {

    /** Références de formulaire : 2050, 2058-A, 2054 bis... */
    private static final Pattern REFERENCE = Pattern
            .compile("(2\\d{3})[^A-Z0-9]{0,2}(BIS|TER|QUATER|[A-HS])|(2\\d{3})");

    private static final double SEUIL_TITRE = 0.75;

    private IdentificateurFormulaire() {
    }

    public static Optional<IdentificationPage> identifier(PagePdf page, Collection<ModeleFormulaire> modeles) {
        if (page.estVide()) {
            return Optional.empty();
        }

        Set<String> referencesEntete = referencesFormulaire(page.getEntete());
        Set<String> referencesPage = referencesFormulaire(page.getTexte());
        Set<String> jetons = jetonsIsoles(page);

        IdentificationPage meilleure = null;
        for (ModeleFormulaire modele : modeles) {
            IdentificationPage identification = evaluer(page, modele, referencesEntete, referencesPage, jetons);
            if (identification != null && (meilleure == null || identification.getScore() > meilleure.getScore())) {
                meilleure = identification;
            }
        }

        if (meilleure == null) {
            return Optional.empty();
        }

        // Une annexe libre reprend le titre et la référence du formulaire auquel elle
        // se rattache : elle ne doit pas être confondue avec le formulaire lui-même
        Optional<NatureAnnexe> annexe = resoudreNatureAnnexe(page);
        if (annexe.isPresent()) {
            meilleure = meilleure.toBuilder().natureAnnexe(annexe.get()).build();
        }

        return Optional.of(meilleure);
    }

    private static IdentificationPage evaluer(PagePdf page, ModeleFormulaire modele, Set<String> referencesEntete,
            Set<String> referencesPage, Set<String> jetons) {
        double scoreReference = scoreReference(modele, referencesEntete);
        if (scoreReference == 0) {
            scoreReference = scoreReference(modele, referencesPage) / 2;
        }

        double scoreTitre = Similarite.calculerAvecTroncature(titreEntete(page, modele), modele.getNom()) >= SEUIL_TITRE
                ? 1.5
                : 0;

        Set<Repere> reperes = modele.getAllReperes();
        long presents = reperes.stream().map(Repere::getSymbole).filter(jetons::contains).count();
        double tauxReperes = reperes.isEmpty() ? 0 : (double) presents / reperes.size();
        // Un formulaire ne comportant que quelques repères atteindrait trop facilement
        // un taux de 100 % : le nombre de codes effectivement reconnus pondère le score
        double scoreReperes = 4 * tauxReperes * Math.min(1.0, presents / 10.0);

        double score = scoreReference + scoreTitre + scoreReperes;
        boolean retenu = (scoreReference >= 3 && score >= 3)
                || (scoreReference > 0 && scoreReperes >= 1.0)
                || (scoreTitre > 0 && scoreReperes >= 2.0);
        if (!retenu) {
            return null;
        }

        return IdentificationPage.builder().page(page.getNumero()).modele(modele).score(score)
                .tauxReperes(tauxReperes).build();
    }

    /**
     * Recherche le titre dans l'en-tête : les titres sont écrits sur une ou deux
     * lignes, parfois entrecoupés de la référence du formulaire.
     */
    private static String titreEntete(PagePdf page, ModeleFormulaire modele) {
        String meilleur = "";
        double meilleureSimilarite = 0;
        List<LignePdf> lignes = page.getLignes();
        for (int i = 0; i < lignes.size() && lignes.get(i).getBas() <= page.getHauteur() * 0.2; i++) {
            for (int taille = 1; taille <= 2 && i + taille <= lignes.size(); taille++) {
                String candidat = String.join(" ", lignes.subList(i, i + taille).stream().map(LignePdf::getTexte)
                        .toList());
                double similarite = Similarite.calculerAvecTroncature(candidat, modele.getNom());
                if (similarite > meilleureSimilarite) {
                    meilleureSimilarite = similarite;
                    meilleur = candidat;
                }
            }
        }
        return meilleur;
    }

    private static double scoreReference(ModeleFormulaire modele, Set<String> references) {
        if (modele.getNumero() == null) {
            return 0;
        }
        String numero = modele.getNumero().toString();
        Set<String> suffixes = suffixes(modele);
        boolean numeroSeulPresent = references.contains(numero);

        for (String suffixe : suffixes) {
            if (!suffixe.isEmpty() && references.contains(numero + suffixe)) {
                return 4.0;
            }
        }

        if (numeroSeulPresent) {
            // Le numéro est présent sans indice de page : la référence est certaine pour
            // un formulaire sans indice, ambiguë pour les autres
            return suffixes.stream().allMatch(String::isEmpty) ? 3.5 : 1.5;
        }

        return 0;
    }

    /** Indices de page possibles du modèle : "A", "BIS"... */
    static Set<String> suffixes(ModeleFormulaire modele) {
        Set<String> suffixes = new LinkedHashSet<>();
        if (StringUtils.isNotBlank(modele.getPage())) {
            suffixes.add(MotPdf.normalise(modele.getPage()));
        }
        String identifiant = MotPdf.normalise(modele.getIdentifiant());
        String numero = modele.getNumero() == null ? "" : modele.getNumero().toString();
        if (!numero.isEmpty() && identifiant.startsWith(numero)) {
            String reste = identifiant.substring(numero.length()).replace("-SD", "").replace("SD", "");
            suffixes.add(reste.replace("-", ""));
        }
        suffixes.remove("");
        if (suffixes.isEmpty()) {
            suffixes.add("");
        }
        return suffixes;
    }

    /** Références de formulaire présentes dans un texte : "2050", "2058A"... */
    static Set<String> referencesFormulaire(String texte) {
        Set<String> references = new LinkedHashSet<>();
        Matcher matcher = REFERENCE.matcher(MotPdf.normalise(texte));
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                references.add(matcher.group(1) + matcher.group(2));
                references.add(matcher.group(1));
            } else {
                references.add(matcher.group(3));
            }
        }
        return references;
    }

    /**
     * Mots de la page susceptibles d'être des codes de repère : un code est un mot
     * isolé, écrit en majuscules ou en chiffres.
     */
    static Set<String> jetonsIsoles(PagePdf page) {
        Set<String> jetons = new LinkedHashSet<>();
        for (MotPdf mot : page.getMots()) {
            String texte = mot.getTexte().trim();
            if (texte.length() >= 2 && texte.length() <= 3 && texte.equals(texte.toUpperCase())) {
                jetons.add(texte);
            }
        }
        return jetons;
    }

    private static Optional<NatureAnnexe> resoudreNatureAnnexe(PagePdf page) {
        Optional<NatureAnnexe> resultat = NatureAnnexe.resolve(page.getEntete());
        if (resultat.isPresent() && resultat.get() == NatureAnnexe.INCONNUE) {
            return NatureAnnexe.resolve(page.getTexte()).or(() -> resultat);
        }
        return resultat;
    }

    /** Regroupe les identifications par modèle de formulaire. */
    public static Map<ModeleFormulaire, List<IdentificationPage>> regrouper(List<IdentificationPage> pages) {
        Map<ModeleFormulaire, List<IdentificationPage>> resultat = new HashMap<>();
        for (IdentificationPage identification : pages) {
            resultat.computeIfAbsent(identification.getModele(), m -> new ArrayList<>()).add(identification);
        }
        return resultat;
    }
}
