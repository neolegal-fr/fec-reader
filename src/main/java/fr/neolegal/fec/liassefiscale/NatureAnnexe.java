package fr.neolegal.fec.liassefiscale;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
public enum NatureAnnexe {
    PROVISIONS("PROVISIONS INSCRITES AU BILAN"),
    REINTEGRATIONS("REINTEGRATIONS"),
    DEDUCTIONS("Déductions"),
    PRODUITS_ET_CHARGES_EXCEPTIONNELS("Produits et charges exceptionnels"),
    PRODUITS_ET_CHARGES_ANTERIEURS("produits et charges sur exercices"),
    INCONNUE(null);

    String intitule;

    NatureAnnexe(String intitule) {
        this.intitule = intitule;
    }

    public static Optional<NatureAnnexe> resolve(String header) {
        boolean formulaireReferenceFound = NatureFormulaire.resolve(header, false).isPresent();

        // Si l'en-tête contient une formulation comme "Formulaire obligatoire (article
        // 38 sexdecies RB de l'annexe III au Code Général des Impôts)"),
        // ce n'est pas une annexe mais un formulaire
        if (formulaireReferenceFound && StrUtils.containsIgnoreCase(header, "de l'annexe")) {
            return Optional.empty();
        }

        boolean annexeKeywordFound = StrUtils.containsIgnoreCase(header, "annexe");

        // Le titre d'une annexe reprend souvent le titre du formulaire
        // associé, et ses identifiants, mais pas systématiquement
        // Pour considérer qu'on est face à une annexe, on cherche soit une référence à
        // un formulaire
        // avec également le mot "annexe", soit un intitulé d'annexe sans référence à un
        // formulaire
        for (NatureAnnexe candidat : values()) {
            if (StringUtils.containsIgnoreCase(header, candidat.getIntitule())) {
                if (!formulaireReferenceFound || annexeKeywordFound) {
                    return Optional.of(candidat);
                }
            }
        }

        if (annexeKeywordFound) {
            // C'est une annexe, mais on ne sait pas de quelle nature
            return Optional.of(NatureAnnexe.INCONNUE);
        }

        // Aucune correspondance trouvée
        return Optional.empty();
    }
}
