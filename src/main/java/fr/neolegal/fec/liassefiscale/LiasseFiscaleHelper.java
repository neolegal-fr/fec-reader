package fr.neolegal.fec.liassefiscale;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pdfbox.pdmodel.PDDocument;

import fr.neolegal.fec.Anomalie;
import fr.neolegal.fec.Fec;
import fr.neolegal.fec.NatureAnomalie;
import fr.neolegal.fec.liassefiscale.controle.ControlesHelper;
import fr.neolegal.fec.liassefiscale.controle.ResultatControle;
import fr.neolegal.fec.liassefiscale.controle.StatutControle;
import fr.neolegal.fec.liassefiscale.pdf.DocumentPdf;
import fr.neolegal.fec.liassefiscale.pdf.ExtracteurAnnexes;
import fr.neolegal.fec.liassefiscale.pdf.ExtracteurMontants;
import fr.neolegal.fec.liassefiscale.pdf.ExtracteurQuadrillage;
import fr.neolegal.fec.liassefiscale.pdf.IdentificateurFormulaire;
import fr.neolegal.fec.liassefiscale.pdf.IdentificationPage;
import fr.neolegal.fec.liassefiscale.pdf.PagePdf;
import net.objecthunter.exp4j.VariableProvider;
import technology.tabula.ObjectExtractor;
import technology.tabula.Table;

/**
 * Construction d'une liasse fiscale, à partir d'un fichier des écritures
 * comptables (FEC) ou d'une liasse au format PDF.
 */
public class LiasseFiscaleHelper {

    private static final Logger LOGGER = Logger.getLogger(LiasseFiscaleHelper.class.getName());

    private LiasseFiscaleHelper() {
    }

    public static LiasseFiscale buildLiasseFiscale(RegimeImposition regime) {
        LiasseFiscale liasse = LiasseFiscale.builder().regime(regime).build();
        for (ModeleFormulaire modele : FormulaireHelper.getModelesFormulaires()) {
            if (Objects.equals(regime, modele.getRegimeImposition())) {
                liasse.getFormulaires().add(Formulaire.builder().modele(modele).build());
            }
        }

        return liasse;
    }

    public static LiasseFiscale buildLiasseFiscale(Fec fec, RegimeImposition regime) {
        LiasseFiscale liasse = buildLiasseFiscale(regime);
        liasse.setSiren(fec.getSiren());
        liasse.setClotureExercice(fec.getClotureExercice());

        VariableProvider provider = new FecVariableProvider(fec, liasse);
        for (Formulaire formulaire : liasse.getFormulaires()) {
            for (Repere repere : formulaire.getAllReperes()) {
                RepereHelper.computeMontantRepereCellule(repere, fec, provider)
                        .ifPresent(montant -> formulaire.setMontant(MontantExtrait.builder()
                                .symbole(repere.getSymbole())
                                .montant(montant)
                                .methode(MethodeExtraction.CALCUL_FEC)
                                .confiance(MethodeExtraction.CALCUL_FEC.getConfianceBase())
                                .build()));
            }
        }

        appliquerControles(liasse);

        return liasse;
    }

    public static LiasseFiscale readLiasseFiscalePDF(String filename) throws IOException {
        return readLiasseFiscalePDF(filename, false);
    }

    /**
     * @param outputDebugFiles écrit à côté du document les fichiers de diagnostic
     *                         de l'extraction
     */
    public static LiasseFiscale readLiasseFiscalePDF(String filename, boolean outputDebugFiles) throws IOException {
        Path fichier = Path.of(filename);
        OptionsLecture options = outputDebugFiles
                ? OptionsLecture.builder().repertoireDiagnostic(repertoire(fichier)).build()
                : OptionsLecture.defaut();
        return lire(fichier, options);
    }

    private static Path repertoire(Path fichier) {
        Path parent = fichier.toAbsolutePath().getParent();
        return parent == null ? Path.of(".") : parent;
    }

    /** Lit une liasse fiscale au format PDF. */
    public static LiasseFiscale lire(Path fichier) throws IOException {
        return lire(fichier, OptionsLecture.defaut());
    }

    public static LiasseFiscale lire(Path fichier, OptionsLecture options) throws IOException {
        try (PDDocument document = PDDocument.load(fichier.toFile())) {
            LiasseFiscale liasse = lire(document, options);
            ecrireDiagnostic(liasse, options, FilenameUtils.removeExtension(fichier.toString()));
            return liasse;
        }
    }

    /**
     * Lit une liasse fiscale au format PDF depuis un flux, sans passer par un
     * fichier temporaire.
     */
    public static LiasseFiscale lire(InputStream flux, OptionsLecture options) throws IOException {
        try (PDDocument document = PDDocument.load(flux)) {
            return lire(document, options);
        }
    }

    static LiasseFiscale lire(PDDocument document, OptionsLecture options) throws IOException {
        LiasseFiscale liasse = LiasseFiscale.builder().build();
        DocumentPdf documentPdf = DocumentPdf.charger(document, options);
        Set<ModeleFormulaire> modeles = FormulaireHelper.getModelesFormulaires();

        List<IdentificationPage> identifications = new ArrayList<>();
        for (PagePdf page : documentPdf.getPages()) {
            if (page.estVide()) {
                liasse.getAnomalies().add(new Anomalie(NatureAnomalie.PAGE_ILLISIBLE, page.getNumero(),
                        String.format("La page %d ne contient aucun texte exploitable", page.getNumero())));
                continue;
            }
            IdentificateurFormulaire.identifier(page, modeles).ifPresent(identifications::add);
        }

        try (ObjectExtractor tableaux = new ObjectExtractor(document)) {
            for (IdentificationPage identification : identifications) {
                // Une page identifiée comme annexe ne comporte pas de repères : si la
                // plupart des codes du modèle y figurent, c'est le formulaire lui-même
                if (identification.estAnnexe() && identification.getTauxReperes() < 0.5) {
                    continue;
                }
                lireFormulaire(liasse, documentPdf, tableaux, identification);
            }

            liasse.setRegime(resoudreRegime(identifications));
            liasse.setSiren(resoudreSiren(documentPdf, identifications).orElse(null));
            liasse.setClotureExercice(resoudreClotureExercice(documentPdf, identifications).orElse(null));

            if (options.isExtractionAnnexes()) {
                lireAnnexes(tableaux, liasse, identifications);
            }
        }

        for (Formulaire formulaire : liasse.getFormulaires()) {
            if (formulaire.getMontantsExtraits().isEmpty() && !formulaire.getAllReperes().isEmpty()) {
                liasse.getAnomalies().add(new Anomalie(NatureAnomalie.FORMULAIRE_VIDE, formulaire.getIdentifiant(),
                        String.format("Aucun montant n'a pu être lu dans le formulaire %s",
                                formulaire.getIdentifiant())));
            }
        }

        if (options.isControlesCoherence()) {
            appliquerControles(liasse);
        }

        return liasse;
    }

    /**
     * Lit les montants d'une page de formulaire par deux méthodes indépendantes :
     * la lecture géométrique, qui fonctionne sur toutes les éditions, et la lecture
     * du quadrillage, plus sûre mais réservée aux documents qui dessinent les
     * bordures de leurs cellules. Leur accord vaut confirmation ; leur désaccord
     * désigne un montant à vérifier.
     */
    private static void lireFormulaire(LiasseFiscale liasse, DocumentPdf documentPdf, ObjectExtractor tableaux,
            IdentificationPage identification) {
        PagePdf page = documentPdf.getPage(identification.getPage());
        ModeleFormulaire modele = identification.getModele();
        Formulaire formulaire = liasse.getOrAddFormulaire(modele);

        Map<String, MontantExtrait> montants = new LinkedHashMap<>();
        for (MontantExtrait montant : ExtracteurMontants.extraire(page, modele)) {
            montants.put(montant.getSymbole(), montant);
        }

        plusGrandTableau(tableaux, identification.getPage()).ifPresent(tableau -> {
            for (MontantExtrait grille : ExtracteurQuadrillage.extraire(tableau, modele, page.getNumero())) {
                montants.merge(grille.getSymbole(), grille, LiasseFiscaleHelper::fusionner);
            }
        });

        for (MontantExtrait montant : montants.values()) {
            Optional<MontantExtrait> existant = formulaire.getMontantExtrait(montant.getSymbole());
            if (existant.isEmpty() || existant.get().getConfiance() < montant.getConfiance()) {
                formulaire.setMontant(montant);
            }
        }
    }

    /** Rapproche les montants lus par les deux méthodes d'extraction. */
    static MontantExtrait fusionner(MontantExtrait geometrique, MontantExtrait grille) {
        if (geometrique.getMontant() == grille.getMontant()
                && geometrique.isCelluleVide() == grille.isCelluleVide()) {
            // Deux lectures indépendantes concordent
            return geometrique.toBuilder().confiance(Math.min(0.98, geometrique.getConfiance() + 0.05)).build();
        }

        // Les deux lectures ne diffèrent que par le signe : la lecture géométrique
        // conserve l'espacement d'origine, qui distingue la parenthèse d'un montant
        // négatif de celle qui referme une incise du libellé
        if (geometrique.getMontant() == -grille.getMontant() && geometrique.getMontant() != 0) {
            return geometrique.toBuilder().confiance(0.70).build();
        }

        // Les deux lectures divergent : le quadrillage, lorsqu'il existe, désigne la
        // cellule sans ambiguïté. La valeur reste douteuse jusqu'aux contrôles de
        // cohérence comptable.
        MontantExtrait retenu = grille.getMethode() == MethodeExtraction.QUADRILLAGE ? grille : geometrique;
        return retenu.toBuilder().confiance(0.55).build();
    }

    /** Plus grand tableau reconnu dans une page, s'il en existe un. */
    private static Optional<Table> plusGrandTableau(ObjectExtractor extracteur, int page) {
        try {
            return ExtracteurAnnexes.plusGrandTableau(extracteur.extract(page));
        } catch (RuntimeException e) {
            LOGGER.log(Level.FINE, "Aucun tableau détecté page {0}", page);
            return Optional.empty();
        }
    }

    /**
     * Exécute les contrôles de cohérence comptable et ajuste la confiance accordée
     * à chaque montant en fonction de leur résultat.
     */
    static void appliquerControles(LiasseFiscale liasse) {
        List<ResultatControle> resultats = ControlesHelper.executer(liasse);
        liasse.getControles().clear();
        liasse.getControles().addAll(resultats);

        for (ResultatControle resultat : resultats) {
            if (resultat.getStatut() == StatutControle.NON_APPLICABLE) {
                continue;
            }
            for (String symbole : resultat.getReperes()) {
                liasse.getMontantExtrait(symbole).ifPresent(montant -> {
                    if (resultat.getStatut() == StatutControle.SATISFAIT) {
                        montant.setControlesSatisfaits(montant.getControlesSatisfaits() + 1);
                    } else {
                        montant.setControlesEnEchec(montant.getControlesEnEchec() + 1);
                    }
                });
            }
            if (resultat.getStatut() == StatutControle.ECHEC) {
                liasse.getAnomalies().add(new Anomalie(NatureAnomalie.INCOHERENCE_COMPTABLE, resultat.getEcart(),
                        String.format("%s : écart de %.0f €", resultat.getLibelle(), resultat.getEcart())));
            }
        }

        List<MontantExtrait> montants = liasse.getMontantsExtraits();
        montants.forEach(montant -> montant.setConfiance(
                ajusterConfiance(montant.getConfiance(), montant.getControlesSatisfaits(),
                        montant.getControlesEnEchec())));

        int reperesAttendus = liasse.getFormulaires().stream()
                .mapToInt(formulaire -> formulaire.getAllReperes().size()).sum();
        liasse.setFiabilite(Fiabilite.calculer(montants, resultats, reperesAttendus));
    }

    /**
     * Révise la probabilité qu'un montant soit exact au vu des contrôles de
     * cohérence auxquels il participe : un montant impliqué dans un total juste
     * est très probablement exact, un montant impliqué dans un total faux ne l'est
     * pas.
     */
    static double ajusterConfiance(double confiance, int satisfaits, int echecs) {
        double resultat = confiance;
        for (int i = 0; i < echecs; i++) {
            resultat *= 0.55;
        }
        for (int i = 0; i < satisfaits; i++) {
            resultat = 1 - (1 - resultat) * 0.45;
        }
        return Math.max(0.02, Math.min(0.995, resultat));
    }

    private static RegimeImposition resoudreRegime(List<IdentificationPage> identifications) {
        Map<RegimeImposition, Double> scores = new LinkedHashMap<>();
        for (IdentificationPage identification : identifications) {
            RegimeImposition regime = identification.getModele().getRegimeImposition();
            if (regime != null) {
                scores.merge(regime, identification.getScore(), Double::sum);
            }
        }
        return scores.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Le numéro SIREN est répété sur chaque formulaire : la valeur retenue est
     * celle qui apparaît sur le plus grand nombre de pages.
     */
    static Optional<String> resoudreSiren(DocumentPdf document, List<IdentificationPage> identifications) {
        Map<String, Integer> candidats = new LinkedHashMap<>();
        for (IdentificationPage identification : identifications) {
            PagePdf page = document.getPage(identification.getPage());
            parseSiren(page.getTexte()).filter(siren -> siren.length() == 9)
                    .ifPresent(siren -> candidats.merge(siren, 1, Integer::sum));
        }
        Optional<String> valide = candidats.entrySet().stream()
                .filter(entree -> estSirenValide(entree.getKey()))
                .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
        return valide.or(() -> candidats.entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey));
    }

    /** Date de clôture la plus fréquemment mentionnée sur les formulaires. */
    static Optional<LocalDate> resoudreClotureExercice(DocumentPdf document,
            List<IdentificationPage> identifications) {
        Map<LocalDate, Integer> candidats = new LinkedHashMap<>();
        for (IdentificationPage identification : identifications) {
            // La date de clôture est rappelée sur la plupart des formulaires : la
            // retenir à la majorité évite de dépendre de la mise en page de l'un d'eux
            PagePdf page = document.getPage(identification.getPage());
            Pair<Boolean, LocalDate> resultat = parseClotureExerciceInterne(page.getTexte());
            if (Boolean.TRUE.equals(resultat.getKey()) && estDatePlausible(resultat.getValue())) {
                int poids = identification.getModele().isContainsClotureExercice() ? 2 : 1;
                candidats.merge(resultat.getValue(), poids, Integer::sum);
            }
        }
        return candidats.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }

    static boolean estDatePlausible(LocalDate date) {
        return date != null && date.getYear() >= 1980 && date.getYear() <= LocalDate.now().getYear() + 1;
    }

    /**
     * Vérifie la clé de contrôle (algorithme de Luhn) du numéro SIREN.
     */
    public static boolean estSirenValide(String siren) {
        if (siren == null || siren.length() != 9 || !siren.chars().allMatch(Character::isDigit)) {
            return false;
        }
        // La Poste fait exception à l'algorithme de Luhn
        if ("356000000".equals(siren)) {
            return true;
        }
        int total = 0;
        for (int i = 0; i < 9; i++) {
            int chiffre = siren.charAt(8 - i) - '0';
            if (i % 2 == 1) {
                chiffre *= 2;
                if (chiffre > 9) {
                    chiffre -= 9;
                }
            }
            total += chiffre;
        }
        return total % 10 == 0;
    }

    private static void lireAnnexes(ObjectExtractor extracteur, LiasseFiscale liasse,
            List<IdentificationPage> identifications) {
        for (IdentificationPage identification : identifications) {
            ModeleFormulaire modele = identification.getModele();
            Set<NatureAnnexe> annexesModele = identification.estAnnexe()
                    ? Set.of(identification.getNatureAnnexe())
                    : new LinkedHashSet<>(emptyIfNull(modele.getAnnexes()));
            if (annexesModele.isEmpty()) {
                continue;
            }

            Optional<Table> tableau = plusGrandTableau(extracteur, identification.getPage());
            if (tableau.isEmpty()) {
                continue;
            }

            Formulaire formulaire = liasse.getOrAddFormulaire(modele);
            for (NatureAnnexe nature : annexesModele) {
                formulaire.getOrAddAnnexe(nature).getLignes()
                        .addAll(ExtracteurAnnexes.extraire(tableau.get(), nature, !identification.estAnnexe()));
            }
        }
    }

    static Optional<LocalDate> parseClotureExercice(String texte) {
        Pair<Boolean, LocalDate> resultat = parseClotureExerciceInterne(texte);
        return Boolean.TRUE.equals(resultat.getKey()) ? Optional.ofNullable(resultat.getValue()) : Optional.empty();
    }

    /**
     * @return une paire indiquant si la date de clôture a été cherchée avec succès,
     *         et la date trouvée le cas échéant
     */
    static Pair<Boolean, LocalDate> parseClotureExerciceInterne(String texte) {
        final String regex = "(clos le|c l o s   l e)([\\s,:]*?)(.+)";
        // Il peut y avoir les dates de clôture des exercices N et N-1 : on s'assure de
        // retenir celle de l'exercice N
        Pattern pattern = Pattern.compile(".*N[,\\s]+" + regex,
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Matcher matcher = pattern.matcher(texte);
        String candidat = "";
        if (matcher.matches()) {
            candidat = matcher.group(3);
        } else {
            // Pas de correspondance : l'exercice n'est peut-être pas désigné par "N"
            pattern = Pattern.compile(".*?" + regex,
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            matcher = pattern.matcher(texte);
            if (!matcher.matches()) {
                return Pair.of(false, null);
            }
            candidat = matcher.group(3);
        }

        Optional<LocalDate> match = parseDate(candidat, false);
        if (match.isPresent()) {
            return Pair.of(true, match.get());
        }

        // Parfois la date se retrouve avant le libellé, ou beaucoup plus loin, à cause
        // de l'ordre de restitution du texte : on élargit la recherche à tout le texte,
        // en exigeant un séparateur de date pour limiter les faux positifs
        return Pair.of(true, parseDate(texte, true).orElse(null));
    }

    public static Optional<LocalDate> parseDate(String str) {
        return parseDate(str, false);
    }

    static Optional<LocalDate> parseDate(String str, boolean separateursObligatoires) {
        // Les dates saisies dans des cases pré-imprimées sont restituées chiffre par
        // chiffre : les espaces qui les séparent sont supprimés. Les retours à la
        // ligne sont conservés, pour ne pas souder la date au texte qui la précède
        str = str.replaceAll("(?<=\\d)[ \\t\\u00a0\\u202f]+(?=\\d)", "");
        String pattern = null;

        Map<String, String> formats = new LinkedHashMap<>();
        formats.put("\\d{4}/\\d{2}/\\d{2}", "yyyy/MM/dd");
        formats.put("\\d{4}-\\d{2}-\\d{2}", "yyyy-MM-dd");
        formats.put("\\d{2}/\\d{2}/\\d{4}", "dd/MM/yyyy");
        formats.put("\\d{2}-\\d{2}-\\d{4}", "dd-MM-yyyy");
        formats.put("\\d{2}/\\d{2}/\\d{2}", "dd/MM/yy");
        formats.put("\\d{2}-\\d{2}-\\d{2}", "dd-MM-yy");
        if (!separateursObligatoires) {
            formats.put("\\d{8}", "ddMMyyyy");
            formats.put("\\d{6}", "ddMMyy");
        }

        String candidat = null;
        int distance = Integer.MAX_VALUE;
        for (Map.Entry<String, String> entree : formats.entrySet()) {
            Pattern p = Pattern.compile("(.*?)(" + entree.getKey() + ").*",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            Matcher m = p.matcher(str);
            if (m.matches() && m.group(1).length() < distance) {
                distance = m.group(1).length();
                candidat = m.group(2);
                pattern = entree.getValue();
            }
        }
        if (isNull(pattern) || isNull(candidat)) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDate.parse(candidat, DateTimeFormatter.ofPattern(pattern)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /** Suite de chiffres, éventuellement saisis dans des cases pré-imprimées */
    private static final Pattern SUITE_DE_CHIFFRES = Pattern.compile("\\d(?:[\\s.\\-/]?\\d)*");

    /** Distance maximale, en caractères, entre le libellé et le numéro */
    private static final int PORTEE_SIREN = 120;

    /**
     * Recherche le numéro SIREN dans le texte d'une page, à partir du libellé qui
     * le désigne.
     * <p>
     * Le numéro est cherché de part et d'autre du libellé : selon les éditions, il
     * est imprimé à sa droite, au-dessous, ou dans des cases pré-imprimées qui le
     * placent avant lui dans l'ordre de lecture. Seules les suites de 9 chiffres
     * (SIREN) ou de 14 chiffres (SIRET) sont retenues.
     *
     * @return le numéro SIREN, une chaîne vide si le libellé est présent sans
     *         numéro, ou {@link Optional#empty()} si le libellé est absent
     */
    static Optional<String> parseSiren(String texte) {
        if (isBlank(texte)) {
            return Optional.empty();
        }
        Matcher libelle = Pattern.compile("(SIREN|SIRET|S\\s?I\\s?R\\s?E\\s?[NT])", Pattern.CASE_INSENSITIVE)
                .matcher(texte);
        if (!libelle.find()) {
            return Optional.empty();
        }

        String meilleur = "";
        int meilleureDistance = Integer.MAX_VALUE;
        do {
            int debut = libelle.start();
            int fin = libelle.end();
            Matcher nombres = SUITE_DE_CHIFFRES.matcher(texte);
            while (nombres.find()) {
                String chiffres = nombres.group().replaceAll("\\D", "");
                if (chiffres.length() != 9 && chiffres.length() != 14) {
                    continue;
                }
                int distance = nombres.start() >= fin ? nombres.start() - fin : debut - nombres.end();
                if (distance < 0 || distance > PORTEE_SIREN) {
                    continue;
                }
                String siren = chiffres.substring(0, 9);
                boolean valide = estSirenValide(siren);
                if (distance < meilleureDistance || (valide && !estSirenValide(meilleur))) {
                    meilleureDistance = valide ? distance : distance + PORTEE_SIREN;
                    meilleur = siren;
                }
            }
        } while (libelle.find());

        return Optional.of(meilleur);
    }

    public static double parseNumber(String texte) {
        if (texte == null) {
            return 0.0;
        }
        texte = texte.trim();
        boolean negatif = texte.startsWith("(") || texte.endsWith(")");
        texte = texte.replaceAll("[\\s\\(\\)]", "");

        double nombre = NumberUtils.toDouble(texte, 0.0);
        if (negatif && nombre != 0.0) {
            nombre = -nombre;
        }

        return nombre;
    }

    private static void ecrireDiagnostic(LiasseFiscale liasse, OptionsLecture options, String prefixe)
            throws IOException {
        if (options.getRepertoireDiagnostic() == null) {
            return;
        }
        String base = FilenameUtils.getName(prefixe);
        Path repertoire = options.getRepertoireDiagnostic();
        Files.createDirectories(repertoire);

        StringBuilder montants = new StringBuilder("repere,montant,methode,confiance,page,source\r\n");
        StringBuilder valeurs = new StringBuilder();
        for (Formulaire formulaire : liasse.getFormulaires()) {
            Set<Repere> reperes = new TreeSet<>(formulaire.getAllReperes());
            for (Repere repere : reperes) {
                Optional<MontantExtrait> montant = formulaire.getMontantExtrait(repere.getSymbole());
                if (montant.isEmpty()) {
                    continue;
                }
                valeurs.append(String.format(Locale.US, "%s,%.2f%n", repere.getSymbole(),
                        montant.get().getMontant()).replace("\n", "\r\n"));
                montants.append(String.format(Locale.US, "%s,%.2f,%s,%.2f,%d,\"%s\"%n", repere.getSymbole(),
                        montant.get().getMontant(), montant.get().getMethode(), montant.get().getConfiance(),
                        montant.get().getPage(),
                        Objects.toString(montant.get().getTexteSource(), "")).replace("\n", "\r\n"));
            }
        }
        Files.writeString(repertoire.resolve(base + ".csv"), valeurs.toString(), StandardCharsets.UTF_8);
        Files.writeString(repertoire.resolve(base + "-montants.csv"), montants.toString(), StandardCharsets.UTF_8);

        StringBuilder controles = new StringBuilder("controle,statut,gauche,droite,ecart,libelle\r\n");
        for (ResultatControle resultat : liasse.getControles()) {
            controles.append(String.format(Locale.US, "%s,%s,%.2f,%.2f,%.2f,\"%s\"%n", resultat.getIdentifiant(),
                    resultat.getStatut(), resultat.getValeurGauche(), resultat.getValeurDroite(),
                    resultat.getEcart(), resultat.getLibelle()).replace("\n", "\r\n"));
        }
        Files.writeString(repertoire.resolve(base + "-controles.csv"), controles.toString(), StandardCharsets.UTF_8);
    }

    /** Trie les montants du plus fiable au moins fiable. */
    public static List<MontantExtrait> parFiabilite(LiasseFiscale liasse) {
        List<MontantExtrait> montants = liasse.getMontantsExtraits();
        montants.sort(Comparator.comparing(MontantExtrait::getConfiance).reversed());
        return montants;
    }
}
