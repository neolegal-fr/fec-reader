# Provenance des liasses fiscales de test

Les fichiers `liasse-publique-*.pdf` sont des documents **publiés officiellement**,
librement téléchargeables. Ils servent de jeu d'essai reproductible pour mesurer la
qualité de l'extraction sur des documents que n'importe qui peut se procurer.

| Fichier | Origine | URL |
|---|---|---|
| `liasse-publique-jo-776550766.pdf` | Comptes annuels déposés au Journal officiel (association recevant plus de 153 000 € de subventions publiques, art. L. 612-4 du code de commerce) — liasse TDFC complète 2050 à 2059-E + 2065, exercice clos le 30/04/2024 | https://www.journal-officiel.gouv.fr/telechargements/ASSOCIATIONS/DCA/PDF/2024/3004/776550766_30042024.pdf |
| `liasse-publique-jo-352383855.pdf` | Comptes annuels déposés au Journal officiel, exercice clos le 31/12/2019 | https://www.journal-officiel.gouv.fr/telechargements/ASSOCIATIONS/DCA/PDF/2019/3112/352383855_31122019.pdf |
| `liasse-publique-atlantic-2023.pdf` | Liasse fiscale publiée par la préfecture de Saône-et-Loire (dossier d'enquête publique), déclaration BIC 2023, formulaires 2050 à 2059-G | https://www.saone-et-loire.gouv.fr/contenu/telechargement/29705/254490/file/liasse%20fiscale%20-%20atlantic%20sfdt%20D%C3%A9claration%20BIC%202023%20(2050%20%C3%A0%202059G).pdf |
| `liasse-publique-res-2018.pdf` | Liasse fiscale publiée par la préfecture de l'Aisne, exercice clos le 31/10/2018 | https://www.aisne.gouv.fr/content/download/31208/203652/file/liasse_fiscale_RES.pdf |
| `liasse-publique-jo-332108877.pdf` | Comptes annuels déposés au Journal officiel, exercice clos le 31/12/2015 : le quadrillage de son bilan actif est mal reconnu, un contrôle de cohérence reste en échec | https://www.journal-officiel.gouv.fr/telechargements/ASSOCIATIONS/DCA/PDF/2015/3112/332108877_31122015.pdf |
| `liasse-publique-union-champagne-2023.pdf` | Liasse fiscale publiée par l'Union Champagne sur son site institutionnel, exercice clos le 31/05/2023 (formulaires 2050 à 2059-G) | https://www.union-champagne.fr/content/104/documents/REEL%20NORMAL%20DGI%20N%C2%B0%202050%20%C3%A0%202059G%20-%20Liasse%202050.pdf |
| `liasse-vierge-2050.pdf` | Formulaires vierges de la liasse réel normal publiés par la DGFiP (millésime 2026) : sert à vérifier qu'un document non renseigné ne produit pas de montants | https://www.impots.gouv.fr/sites/default/files/formulaires/2050-liasse/2026/2050-liasse_5320.pdf |
| `comptes-annuels-sans-liasse.pdf` | Comptes annuels d'association présentés selon le règlement ANC 2018-06, **sans liasse fiscale** : sert à vérifier que le lecteur ne reconnaît aucun formulaire plutôt que d'en inventer | https://www.journal-officiel.gouv.fr/telechargements/ASSOCIATIONS/DCA/PDF/2021/3112/804964849_31122021.pdf |

Sur les dépôts de comptes annuels du Journal officiel, la plupart des associations
publient des états financiers au format libre (règlement ANC 2018-06) et non la
liasse fiscale : seuls les dépôts qui contiennent les formulaires 2050 et suivants
sont exploitables par cette librairie.

Les autres fichiers (`liasse-2050_*.pdf`, `liasse-2033.pdf`, `liasse-2139_*.pdf`,
`liasse-2145-*.pdf`, `liasse-2072-*.pdf`) sont des documents anonymisés ou déjà
présents dans le dépôt avant la constitution de ce jeu d'essai public.

## Fichiers `*-expected.csv`

Ils contiennent les montants attendus pour chaque repère, au format
`symbole,montant`. Deux natures de fichiers coexistent :

* les fichiers de **référence**, dont les valeurs ont été relevées manuellement
  dans le document ;
* les fichiers de **non-régression**, produits par une exécution du lecteur puis
  contrôlés par les vérifications de cohérence comptable (totaux, équilibre du
  bilan, report du résultat). Ils contiennent également les repères dont la
  cellule est vide (montant `0.00`).

La classe `QualiteExtractionTest` mesure le taux de lecture exacte sur l'ensemble
de ces fichiers et échoue en cas de régression.
