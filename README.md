# fec-reader

Librairie JAVA de :

* lecture des fichiers des écritures comptables (FEC) conformes aux normes codifiées à l’article A.47 A-1 du livre des procédures fiscales ;
* calcul des montants de la liasse fiscale à partir d'un FEC (bilan, compte de résultat, etc.) ;
* **extraction des données des liasses fiscales au format PDF, avec mesure de la fiabilité de l'extraction**.

La librairie lit les fichiers FEC au format plat (CSV séparé par tabulation ou `|`, pas le format XML) et les liasses fiscales PDF comportant une couche texte. Les liasses scannées sont prises en charge si un moteur de reconnaissance optique est branché (voir plus bas).

Régimes et formulaires reconnus : réel normal (2050 à 2059), réel simplifié (2033-A à 2033-E), bénéfices agricoles au réel simplifié (2139-A, 2139-B) et au réel normal (2144 à 2151), sociétés immobilières (2072-S).

## Utilisation

### Maven

```xml
<dependency>
    <groupId>fr.neolegal</groupId>
    <artifactId>fec-reader</artifactId>
    <version>0.3.0</version>
</dependency>
```

### Lecture d'un fichier FEC

```java
Fec fec = FecHelper.read(Path.of("123456789FEC20500930.txt"));
System.out.printf("%d lignes, %d écritures, société %s, exercice clos le %s%n",
        fec.getLignes().size(), fec.getNombreEcritures(), fec.getSiren(), fec.getClotureExercice());
```

### Calcul de la liasse fiscale à partir d'un FEC

```java
Fec fec = FecHelper.read(Path.of("123456789FEC20500930.txt"));
LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(fec, RegimeImposition.REEL_NORMAL);

Formulaire compteResultat = liasse.getFormulaire("2052-SD").orElseThrow();
compteResultat.getMontantsExtraits()
        .forEach(montant -> System.out.printf("%s : %.02f €%n", montant.getSymbole(), montant.getMontant()));
```

### Fiabilité de la liasse calculée depuis un FEC

Le calcul de la liasse à partir d'un FEC repose sur des formules qui désignent les comptes par leur numéro. Un plan comptable qui s'écarte de la nomenclature attendue — un fournisseur en 4082 là où la formule attend 4081, un stock en 302 là où elle attend 31 — verrait son solde disparaître de la liasse. Deux mécanismes le rendent visible :

```java
LiasseFiscale liasse = LiasseFiscaleHelper.buildLiasseFiscale(fec, RegimeImposition.REEL_NORMAL);

// Les contrôles de cohérence s'appliquent aussi aux montants calculés
liasse.getControlesEnEchec().forEach(System.out::println);

// Comptes dont le solde n'alimente aucun repère
VentilationComptes ventilation = VentilationComptes.analyser(fec, liasse);
System.out.println(ventilation);
// 6 comptes non affectés, 61237.51 € (0,9 % des soldes)
ventilation.getComptesNonAffectes().forEach((compte, solde) ->
        System.out.printf("%s : %.2f €%n", compte, solde));
```

Les comptes non affectés sont également signalés dans `liasse.getAnomalies()`, avec la nature `COMPTE_NON_AFFECTE`.

Sur les quatre fichiers FEC du jeu d'essai, l'équilibre du bilan est désormais vérifié exactement pour deux d'entre eux, à 5 € près pour le troisième ; le quatrième présente un écart de 25 244 € entièrement expliqué par six comptes hors nomenclature, que la ventilation désigne nommément.

### Lecture d'une liasse fiscale au format PDF

```java
LiasseFiscale liasse = LiasseFiscaleHelper.lire(Path.of("liasse.pdf"));

System.out.println(liasse.getSiren());              // 776550766
System.out.println(liasse.getClotureExercice());    // 2024-04-30
System.out.println(liasse.getRegime());             // REEL_NORMAL
System.out.println(liasse.getMontant("CO"));        // total général de l'actif brut
System.out.println(liasse.getMontant("HN"));        // résultat de l'exercice
```

La lecture accepte également un flux, ce qui évite d'écrire un fichier temporaire lorsque le document provient d'un téléversement :

```java
try (InputStream flux = requete.getInputStream()) {
    LiasseFiscale liasse = LiasseFiscaleHelper.lire(flux, OptionsLecture.defaut());
}
```

### Fiabilité de l'extraction

Chaque montant est accompagné de la méthode qui a permis de le lire et d'une probabilité qu'il soit exact. La liasse porte un score de fiabilité global, la liste des contrôles de cohérence comptable exécutés et les anomalies rencontrées.

```java
LiasseFiscale liasse = LiasseFiscaleHelper.lire(Path.of("liasse.pdf"));

// Score global : probabilité que les montants extraits soient exacts
System.out.println(liasse.getFiabilite());
// fiabilité 93% (couverture 88%, lecture 96%, cohérence 100%)

// Détail d'un montant
MontantExtrait total = liasse.getMontantExtrait("CO").orElseThrow();
System.out.printf("%s = %.0f lu page %d par %s, confiance %.0f%% (%d contrôles satisfaits)%n",
        total.getSymbole(), total.getMontant(), total.getPage(), total.getMethode(),
        total.getConfiance() * 100, total.getControlesSatisfaits());

// Contrôles non satisfaits : ils désignent les montants à vérifier
liasse.getControlesEnEchec().forEach(System.out::println);
// 2052-ca-net : ECHEC (Chiffre d'affaires net = France + Exportations) 2540003 vs 283797, écart 2256206

// Montants à faire contrôler par un humain
liasse.getMontantsExtraits().stream().filter(montant -> !montant.estFiable()).forEach(System.out::println);
```

Le score de confiance d'un montant combine :

1. **la méthode de lecture** : code du repère lu dans le document (le cas général), libellé de la ligne reconnu (lorsque les codes sont imprimés sous forme d'image), reconnaissance optique, calcul depuis le FEC ;
2. **la qualité de la géométrie** : le montant est-il bien dans la colonne attendue ;
3. **les contrôles de cohérence comptable** qui portent sur le repère : un montant qui participe à des totaux justes est presque certainement exact, un montant impliqué dans un total faux ne l'est pas.

Calibration observée sur le jeu d'essai (repères dont la valeur attendue est connue) :

| Confiance annoncée | Montants | Taux d'exactitude observé |
|---|---|---|
| 90 % à 99 % | 812 | 99,3 % |
| 80 % à 89 % | 524 | 100 % |
| moins de 80 % | 38 | 94,7 % |

Le score est volontairement prudent : il sert à désigner les montants à faire vérifier. Un montant dont les deux méthodes de lecture divergent est ramené à 55 % même si la valeur retenue est finalement exacte, tant qu'aucun contrôle de cohérence ne vient la confirmer.

### Contrôles de cohérence comptable

63 contrôles vérifient la recomposition des totaux et les égalités structurelles de la liasse : totaux et sous-totaux de chaque formulaire, équilibre du bilan (actif net = passif), report du résultat du compte de résultat au bilan, composition du chiffre d'affaires, etc. Ils sont décrits dans [`src/main/resources/controles/controles.json`](src/main/resources/controles/controles.json) et sont exécutés aussi bien sur une liasse lue depuis un PDF que sur une liasse calculée depuis un FEC.

```json
{
  "identifiant": "bilan-equilibre",
  "formulaires": [ "2050-SD", "2051-SD" ],
  "libelle": "Équilibre du bilan : total de l'actif net = total du passif",
  "gauche": [ "CO", "-1A" ],
  "droite": [ "EE" ],
  "tolerance": 2
}
```

Un contrôle dont les repères n'ont pas été extraits, ou dont tous les montants sont nuls, est déclaré `NON_APPLICABLE` : il n'entre pas dans le calcul du taux de cohérence.

### Liasses scannées : reconnaissance optique

Les pages dépourvues de couche texte exploitable (documents scannés, PDF dont les polices ne comportent pas de table de correspondance vers l'unicode) sont soumises à un moteur d'OCR, s'il en existe un.

* Si l'exécutable [`tesseract`](https://github.com/tesseract-ocr/tesseract) est installé sur la machine, il est utilisé automatiquement (`MoteurOcrTesseract`). Le chemin de l'exécutable peut être précisé par la propriété système `fec.tesseract.path`.
* Tout autre moteur, y compris un service externe (Mistral OCR, DataLeon, Azure Document Intelligence...), s'intègre en implémentant l'interface `MoteurOcr` et en la déclarant dans `META-INF/services/fr.neolegal.fec.liassefiscale.pdf.MoteurOcr`, ou en la passant directement :

```java
OptionsLecture options = OptionsLecture.builder().moteurOcr(monMoteur).build();
LiasseFiscale liasse = LiasseFiscaleHelper.lire(Path.of("liasse-scannee.pdf"), options);
```

Les montants issus de l'OCR sont marqués (`MontantExtrait.isOcr()`) et leur confiance est minorée ; les contrôles de cohérence restent le juge de paix.

### Options de lecture et diagnostic

```java
OptionsLecture options = OptionsLecture.builder()
        .ocrAutorise(true)                              // reconnaissance optique des pages sans texte
        .controlesCoherence(true)                       // contrôles comptables et calcul de la fiabilité
        .extractionAnnexes(true)                        // contenu libre des annexes
        .repertoireDiagnostic(Path.of("/tmp/diag"))     // fichiers de diagnostic de l'extraction
        .build();
```

Le répertoire de diagnostic reçoit, pour chaque document lu :

* `<document>.csv` : les montants extraits, au format `repère,montant` ;
* `<document>-montants.csv` : le détail de l'extraction (méthode, confiance, page, texte lu) ;
* `<document>-controles.csv` : le résultat de chaque contrôle de cohérence.

## Comment fonctionne l'extraction PDF

Les formulaires de liasse fiscale font précéder chaque cellule chiffrée du **code du repère** correspondant (`AA`, `CO`, `044`...). Le lecteur exploite cette régularité :

1. **Lecture positionnée** : les mots du PDF sont extraits avec leur géométrie, puis regroupés en lignes et en rangées. Les textes imprimés verticalement dans les marges sont écartés, les groupes de chiffres d'un même montant (`1 033 701`) sont réunis, les montants négatifs entre parenthèses sont reconnus.
2. **Identification des formulaires** : chaque page est rapprochée d'un modèle de formulaire en combinant la référence imprimée en en-tête (`N° 2050`, `2058-A`), la similarité du titre et la proportion des codes du modèle présents dans la page. Ce dernier indice évite de confondre la page « Bilan actif » d'une plaquette de présentation avec le formulaire 2050-SD.
3. **Reconstitution des colonnes** : les colonnes de montants sont retrouvées par projection horizontale des nombres de la page, quel que soit leur alignement, sans dépendre du quadrillage du tableau — de nombreuses éditions n'en ont pas.
4. **Lecture des cellules** : la valeur d'un repère est le premier montant situé à sa droite, dans la colonne qui suit son code. Si cette colonne ne contient rien sur la rangée, la cellule est déclarée vide (et non pas absente).
5. **Repli sur les libellés** : pour les repères dont le code n'a pas été trouvé (codes imprimés sous forme d'image), le libellé de la ligne est rapproché de celui du modèle et les valeurs sont lues dans l'ordre des colonnes, avec une confiance moindre.
6. **Seconde lecture par le quadrillage** : lorsque le document dessine les bordures de ses cellules, la grille du tableau est reconnue et les valeurs sont relues indépendamment. L'accord des deux lectures confirme le montant ; leur désaccord le signale.
7. **Contrôles de cohérence** : les totaux sont recomposés, ce qui valide ou invalide les montants extraits et alimente le score de fiabilité.

Le numéro SIREN et la date de clôture sont recherchés sur toutes les pages du document, puis retenus à la majorité ; la clé de contrôle du SIREN (algorithme de Luhn) départage les candidats.

## Qualité mesurée

Le jeu d'essai compte 22 documents : 14 liasses historiques du dépôt, 6 liasses **publiques** librement téléchargeables, un jeu de formulaires vierges et des comptes annuels d'association dépourvus de liasse fiscale. La classe de test `QualiteExtractionTest` mesure à chaque construction le nombre de montants correctement lus et échoue en cas de régression.

Sur les 14 documents dont les montants attendus ont été relevés indépendamment du lecteur :

* **99,0 %** des montants non nuls sont lus exactement (603 sur 609), contre 97,0 % pour le moteur précédent ;
* 1 montant erroné et 5 montants non lus, tous signalés par un score de confiance faible ;
* un document n'est pas lisible sans OCR (`liasse-2050_7.pdf`, polices sans table unicode) : il obtient une fiabilité de 0, ce qui le signale au lieu de produire des valeurs fausses.

Deux cas limites sont vérifiés en plus : un jeu de formulaires vierges ne doit produire aucun montant fiable, et des comptes annuels présentés au format libre (règlement ANC 2018-06, sans formulaire 2050) ne doivent faire reconnaître aucun formulaire plutôt que d'inventer des valeurs.

La provenance et l'URL de chaque document public sont documentées dans [`src/test/resources/SOURCES.md`](src/test/resources/SOURCES.md). La dernière liasse ajoutée (`liasse-publique-union-champagne-2023.pdf`) n'a servi à aucun réglage du lecteur : elle est lue avec 29 contrôles de cohérence satisfaits sur 29 et une fiabilité de 97 %.

## Environnement de développement

1. Installer un JDK 17 et Maven, puis VS Code avec les extensions Java
2. `mvn test` exécute l'ensemble des tests, dont la mesure de qualité de l'extraction

Pour ajouter un formulaire, déposer sa description dans `src/main/resources/formulaires/` ; pour ajouter un contrôle de cohérence, compléter `src/main/resources/controles/controles.json`. Le test `ControleCoherenceTest.reperesDesControles_existentDansLesModeles` vérifie que tous les repères cités par un contrôle existent dans un modèle.

## Publier une nouvelle version

Le numéro de version porté par `pom.xml` sur la branche `main` est toujours un `-SNAPSHOT` : c'est la publication qui en retire le suffixe, puis incrémente le numéro pour la suivante.

### Par GitHub Actions (recommandé)

Le workflow [`release-to-maven-central`](.github/workflows/release-to-maven-central.yml) détient la clé de signature et les identifiants Sonatype. Il se déclenche à la demande :

```bash
gh workflow run release-to-maven-central.yml     # ou : onglet Actions > release-to-maven-central > Run workflow
gh run watch                                     # suivre l'exécution
```

Il enchaîne : retrait du suffixe `-SNAPSHOT`, construction et signature GPG, dépôt sur Maven Central (publication automatique, `autoPublish`), puis incrément du numéro de version et `commit` sur `main`.

Secrets attendus dans le dépôt GitHub : `MAVEN_GPG_PRIVATE_KEY`, `MAVEN_GPG_PASSPHRASE`, `OSS_SONATYPE_USERNAME`, `OSS_SONATYPE_PASSWORD` (les deux derniers sont le *user token* engendré depuis <https://central.sonatype.com/account>).

### Depuis un poste de développement

Nécessite la clé de signature sur la machine :

1. installer gnupg (<https://gpg4win.org/download.html> sous Windows) ;
2. importer dans Kleopatra le certificat de signature des binaires (clé publique **et** clé privée) ;
3. créer un *user token* depuis <https://central.sonatype.com/account> et le déclarer dans `~/.m2/settings.xml` (`C:\Users\[utilisateur]\.m2\settings.xml`) pour le serveur d'identifiant `central`, ainsi que les propriétés `gpg.executable` et `gpg.passphrase` ;
4. publier :

```powershell
./deploys.ps1        # mvn clean versions:set -DremoveSnapshot deploy
```

5. incrémenter ensuite le numéro de version et le committer :

```bash
mvn release:update-versions
git commit -am "Passage en <nouvelle version>-SNAPSHOT"
```

L'artefact apparaît sur <https://central.sonatype.com/artifact/fr.neolegal/fec-reader> en quelques minutes, puis sur `repo1.maven.org` sous une demi-heure.

### Renouveler la clé de signature

Maven Central refuse les binaires signés avec une clé expirée, et `gpg` refuse purement et simplement de signer : la publication échoue alors sur `gpg: no default secret key`. La clé de publication du projet est `ed25519/C1B557958CAC9E85` (`Nicolas Riousset <nicolas@neolegal.fr>`), **valable jusqu'au 11 septembre 2028**. Pour la prolonger le moment venu, depuis le poste qui détient la clé privée :

```powershell
gpg --list-secret-keys --keyid-format=long                      # vérifier l'échéance
gpg --quick-set-expire 810AFE65D46C95280CE70B57C1B557958CAC9E85 2y
gpg --quick-set-expire 810AFE65D46C95280CE70B57C1B557958CAC9E85 2y '*'   # les sous-clés
```

Prolonger l'échéance ne remplace pas la clé : même empreinte, même identité, seule la date de l'auto-signature change.

Il faut ensuite **republier la clé publique**, faute de quoi la validation Sonatype échoue :

```powershell
gpg --armor --export 810AFE65D46C95280CE70B57C1B557958CAC9E85 > cle-publique.asc
curl -X POST -H "Content-Type: application/json" `
     --data-binary "@corps.json" https://keys.openpgp.org/vks/v1/upload   # corps.json : {"keytext": "<contenu de cle-publique.asc>"}
```

`keyserver.ubuntu.com` ignore les mises à jour des clés EdDSA : il continue de servir l'ancienne version, sans conséquence tant que la clé est à jour sur `keys.openpgp.org`.

Enfin, mettre à jour le secret qui porte la clé **privée** pour le workflow :

```powershell
gpg --armor --export-secret-keys 810AFE65D46C95280CE70B57C1B557958CAC9E85 | gh secret set MAVEN_GPG_PRIVATE_KEY --repo neolegal-fr/fec-reader
```

Le secret existe aux deux niveaux : celui du dépôt, mis à jour le 12/09/2026, l'emporte sur celui de l'organisation `neolegal-fr`, qui porte encore la clé expirée et reste à corriger pour les autres dépôts qui l'utiliseraient (il faut pour cela le droit `admin:org`).

Le workflow vérifie l'état de la clé avant de construire, et s'arrête immédiatement avec un message explicite si elle est absente, expirée ou révoquée.

## Journal des versions

### 0.3.1

* Mise à jour de `fr.neolegal:tabula` en 1.2.0 : la détection des tableaux s'initialise par `SpreadsheetExtractionAlgorithm.neolegalDefaults()`, qui porte désormais l'intégralité du réglage — tolérances d'alignement des bordures, autocomplétion des cellules et tolérance de débordement du texte.

Fiabilité du calcul de la liasse à partir d'un fichier des écritures comptables :


* les écritures de reprise des soldes sont reconnues par leur journal, quel que soit son code et sa position dans le fichier — l'heuristique précédente (le numéro de la première écriture du fichier) excluait jusqu'à l'intégralité des écritures sur certains fichiers ;
* les soldes des comptes sont calculés une seule fois à la lecture du fichier, au lieu d'être recalculés par chaque formule ;
* les montants ne sont plus arrondis à chaque étape intermédiaire mais au moment d'être inscrits dans le formulaire : les totaux ne dérivent plus de quelques euros ;
* correction des formules de calcul : total de l'actif circulant amputé des charges constatées d'avance, total des amortissements et totaux du bilan simplifié absents, résultat fiscal déduit de soldes de comptes inexistants, terme d'agrégation sans numéro de compte ;
* ventilation des soldes atypiques (fournisseur débiteur, salarié débiteur, client créditeur), qui disparaissaient de la liasse, et des familles de comptes de gestion non couvertes ;
* nouveau contrôle `VentilationComptes` : les comptes dont le solde n'alimente aucun repère sont recensés et signalés comme anomalies.

Sur le jeu d'essai, l'équilibre du bilan passe d'un écart de 571 666 € à un écart nul sur le fichier de référence, et les écarts résiduels des autres fichiers sont intégralement expliqués par des comptes hors nomenclature, désormais désignés nommément.

### 0.3.0

* Nouveau moteur d'extraction des liasses PDF : la lecture géométrique du document, qui ne dépend plus de la présence d'un quadrillage, est confrontée à la lecture du quadrillage lorsqu'il existe. Les liasses sans bordures, aux colonnes non alignées ou dont les codes de repères sont imprimés sous forme d'image sont désormais lues, et le taux de lecture exacte passe de 97,0 % à 99,0 % sur le jeu d'essai.
* Mesure de la fiabilité de l'extraction : confiance par montant, score global, 63 contrôles de cohérence comptable, anomalies.
* Prise en charge des liasses scannées par un moteur d'OCR enfichable (`MoteurOcr`), avec une implémentation s'appuyant sur `tesseract`.
* Lecture depuis un flux (`InputStream`), options de lecture, fichiers de diagnostic écrits dans un répertoire dédié (et non plus à côté du document lu).
* Correction de la lecture des montants du FEC comportant un séparateur décimal anglo-saxon, du dénombrement des écritures (unique par journal), de la lecture des fichiers dont les lignes se terminent par `\r\r\n`.
* Suppression de la dépendance à Apache Tika (détection d'encodage désormais native) et à `jakarta.annotation` : l'empreinte de la librairie se réduit à huit dépendances directes.
* API conservée : `LiasseFiscaleHelper.readLiasseFiscalePDF`, `buildLiasseFiscale`, `LiasseFiscale.getMontant`, `FecHelper.read` fonctionnent à l'identique.
* Jeu d'essai porté à 22 documents, dont six liasses publiques librement téléchargeables, un jeu de formulaires vierges et des comptes annuels sans liasse fiscale.
