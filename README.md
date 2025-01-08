# fec-reader
Librairie JAVA de :
* lecture des fichiers des écritures comptables conformes aux normes codifiées à l’article A.47 A-1 du livre des procédures fiscales.
* calcul des montants de la liasse fiscale (tableaux comptables bilan-actif, passif, etc définis dans le formulaire DGFiP N° 2050-SD 2023, cerfa N° 15949 * 05)
* extraction des données des formulaires de liasse fiscale en PDF

La librairie ne supporte que les fichiers FEC en format plat (CSV), pas le format XML, et les liasses fiscales en PDF (pas les liasses scannées).


## Environnement de développement

1. Installer VS Code avec les extensions Java
2. Installer gnupg pour pouvoir publier une nouvelle version de la librairie sur les repos Maven : https://gpg4win.org/download.html
3. Importer dans Kleopatra le certificat de signature des binaires (clé publique et clé privée / secret)
4. Créer un user token depuis https://central.sonatype.com/account et le paramétrer dans "C:\Users\[username]]\.m2\settings.xml", pour le server avec l'id "central"
5. Exécuter "./deploy.ps1", ou "mvn clean deploy"


## Utilisation
### Maven

Pour utiliser la librairie avec Maven, ajouter la dépendance:
```
<dependency>
    <groupId>fr.neolegal</groupId>
    <artifactId>fec-reader</artifactId>
    <version>0.1.15</version>
</dependency>
```

### Lecture d'un fichier FEC

La classe FecReader est responsable de la lecture des fichiers. La classe Fec contient la liste des écritures lues.
```
  Fec fec = FecHelper.read(Path.of("123456789FEC20500930.txt"));
  Logger.getAnonymousLogger().info(String.format("Le fichier contient %d lignes représentant %d écritures comptables de la société %s pour l'exercice clos le %s", fec.getLignes().size(), fec.getNombreEcritures(), fec.getSiren(), fec.getClotureExercice()));
```

### Calcul des montants des repères de la liasse fiscale

```
  Fec fec = FecHelper.read(Path.of("123456789FEC20500930.txt"));
  TableauComptable compteResulat = liasse.getFormulaire(Formulaire.DGFIP_2052_COMPTE_RESULTAT);
  Logger logger = Logger.getAnonymousLogger();
  logger.info(String.format("Compte de résultat pour l'exercice clos le %s de la société %s:", liasse.getClotureExercice(), liasse.getSiren()));
  for (Map.Entry<Repere, Double> ligne : compteResulat.getLignes().entrySet()) {
      logger.info(String.format("%s :  %.02f €", ligne.getKey().getNom(), ligne.getValue()));
  }        
```
