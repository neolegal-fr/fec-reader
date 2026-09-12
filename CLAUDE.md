# fec-reader — repères pour l'assistant

Librairie Java publiée sur Maven Central (`fr.neolegal:fec-reader`) qui lit les
fichiers des écritures comptables (FEC) et **extrait les montants des liasses
fiscales au format PDF en mesurant la fiabilité de l'extraction**.

## Commandes

```bash
mvn -o test            # 91 tests, ~1 min
mvn -o -q compile      # compilation seule
mvn -B package         # ce que fait la CI (nécessite le réseau : javadoc, sources)
```

Le mode hors ligne (`-o`) suffit pour compiler et tester ; `package` télécharge le
plugin javadoc au premier appel.

## Conventions

* **Tout est en français** : noms de classes, de méthodes, de variables,
  commentaires et Javadoc. Les classes antérieures à la version 0.3 conservent
  leurs noms anglais (`Formulaire.getMontant`, `LiasseFiscaleHelper.readLiasseFiscalePDF`)
  pour ne pas casser les intégrations : ne pas les renommer.
* Lombok (`@Getter`, `@Builder`, `@Data`) est disponible, en portée `provided`.
* Les commentaires expliquent **pourquoi**, pas quoi : les seuils géométriques et
  les heuristiques doivent être justifiés, ils ont tous été calibrés sur le jeu
  d'essai.
* Dépendances : huit, volontairement. Ne pas en ajouter sans nécessité (Apache
  Tika a été retiré en 0.3.0 au profit d'une détection d'encodage native).

## Architecture de l'extraction PDF

`LiasseFiscaleHelper.lire()` orchestre, dans l'ordre :

1. `pdf/ExtracteurMotsPdf` → mots positionnés (PDFBox), fusion des groupes de
   chiffres, filtrage des textes verticaux des marges ;
2. `pdf/PagePdf` → lignes, puis **rangées** (les lignes sans libellé sont
   rattachées à la ligne de libellé la plus proche : le code d'un repère et son
   montant sont souvent imprimés à des hauteurs légèrement différentes) ;
3. `pdf/IdentificateurFormulaire` → page ↔ modèle de formulaire, par combinaison
   de la référence en en-tête, du titre et de la **proportion des codes du modèle
   présents dans la page** ;
4. `pdf/ExtracteurMontants` → lecture géométrique : la valeur d'un repère est le
   premier montant à droite de son code, dans la colonne suivante. Seconde passe
   par les libellés quand les codes sont illisibles ;
5. `pdf/ExtracteurQuadrillage` → seconde lecture indépendante par la grille du
   tableau (Tabula), quand le document dessine ses bordures. L'algorithme est
   construit par `ExtracteurAnnexes.algorithme()` :
   `neolegalDefaults()` du fork `fr.neolegal:tabula`, complété des tolérances
   d'alignement des bordures calibrées sur le jeu d'essai — `neolegalDefaults()`
   seul ne les porte pas et fait chuter la lecture de 99,6 % à 96,6 % ;
6. `LiasseFiscaleHelper.fusionner()` → accord des deux lectures = confirmation,
   désaccord = montant ramené à 55 % de confiance ;
7. `controle/ControlesHelper` → contrôles de cohérence comptable, qui révisent la
   confiance de chaque montant et alimentent `Fiabilite`.

Les deux lectures sont complémentaires : la géométrie fonctionne sans quadrillage
(la moitié des éditions n'en ont pas), la grille est plus sûre quand elle existe.
**Ne pas supprimer l'une des deux** : chacune rattrape les échecs de l'autre, et
leur désaccord est le principal signal d'alerte.

## Calcul depuis un FEC

`FecHelper.read` produit un `Fec` qui porte ses `SoldesComptes` : les soldes de
chaque compte, calculés une fois, avec et sans les écritures de reprise des
soldes (identifiées par leur journal, pas par la position dans le fichier).
`LiasseFiscaleHelper.buildLiasseFiscale` évalue ensuite les formules
`formuleFEC` des modèles.

Deux pièges à connaître :

* les symboles des formulaires 2033 sont numériques : une référence à un repère
  s'y écrit `REP_310`, sinon `310` est lu comme le nombre 310 ;
* deux préfixes qui se recouvrent dans la même formule (`DEB_43` et `DEB_4387`)
  comptent deux fois le même compte. `RepereTest.checkValiditeRegles` le vérifie
  pour tous les modèles : ne jamais neutraliser ce test.

`VentilationComptes.analyser` recense les comptes dont le solde n'alimente aucun
repère — c'est le premier endroit où regarder quand l'équilibre du bilan n'est
pas vérifié.

## Données

* `src/main/resources/formulaires/*.json` : un modèle par formulaire (repères,
  hiérarchie des sections, formules de calcul depuis le FEC). Le format est celui
  produit par Jackson : **éditer ces fichiers à la main**, ne pas les réécrire
  avec un outil qui en changerait la mise en forme (le diff deviendrait illisible).
  La hiérarchie porte du sens : une section nomme une ligne du tableau, les
  repères qu'elle contient en nomment les colonnes ("Brut", "Amortissements"...).
* `src/main/resources/controles/controles.json` : 63 contrôles de cohérence, sous
  forme `somme(gauche) = somme(droite)` avec tolérance. Un repère peut être
  préfixé de `-`. Tout repère cité doit exister dans un modèle : c'est vérifié par
  `ControleCoherenceTest.reperesDesControles_existentDansLesModeles`.

## Jeu d'essai

22 documents dans `src/test/resources`, dont 6 liasses publiques librement
téléchargeables (provenance dans `SOURCES.md`). Deux natures de fichiers
`*-expected.csv` :

* **référence** : valeurs relevées à la main dans le document (les 14 liasses
  historiques) — c'est sur elles que se mesure la qualité réelle ;
* **non-régression** : instantanés produits par le lecteur puis validés par les
  contrôles de cohérence (les liasses publiques).

`QualiteExtractionTest` fixe un seuil par document. **Ne jamais abaisser un seuil
pour faire passer la construction** : un seuil qui baisse est une régression à
corriger. Les relever après une amélioration est en revanche attendu.

Mesures de référence (version 0.3.0) : 99,0 % de montants non nuls lus exactement
sur les 14 documents à référence indépendante (603 justes, 1 faux, 5 non lus).

## Pièges connus

* Les chemins des ressources de test passent par `target/test-classes` (voir
  `Fixtures`), pas par le classpath.
* PDFBox est en 2.0.x : `PDDocument.load(...)`, et non `Loader.loadPDF(...)`.
* `liasse-2050_7.pdf` n'a pas de table unicode dans ses polices : il est
  illisible sans OCR, et doit rester à une fiabilité de 0.
* Un montant dont les groupes de chiffres sont irréguliers ("39 41560 745120 252")
  provient d'une cellule mal découpée : `MotPdf.parseMontant` le rejette.

## Publication d'une version

Voir la section « Publier une nouvelle version » du README. En résumé : la
publication se fait par le workflow GitHub `release-to-maven-central`
(`gh workflow run release-to-maven-central.yml`), qui détient la clé GPG et les
identifiants Sonatype (secrets de l'organisation `neolegal-fr`). La publication
locale (`./deploys.ps1`) suppose la clé GPG sur la machine.

**État au 12/09/2026** : version 0.3.0 publiée sur Maven Central. La clé de
signature `ed25519/C1B557958CAC9E85` a été prolongée jusqu'au 11/09/2028 et
republiée sur `keys.openpgp.org` (`keyserver.ubuntu.com` refuse les mises à jour
des clés EdDSA et sert encore l'ancienne version, sans conséquence). Le secret
`MAVEN_GPG_PRIVATE_KEY` a été mis à jour au niveau du dépôt ; celui de
l'organisation porte encore la clé expirée.
