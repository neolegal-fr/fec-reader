package fr.neolegal.fec.liassefiscale.pdf;

import fr.neolegal.fec.liassefiscale.ModeleFormulaire;
import fr.neolegal.fec.liassefiscale.NatureAnnexe;
import lombok.Builder;
import lombok.Getter;

/** Nature d'une page de liasse fiscale, telle que reconnue par le lecteur. */
@Getter
@Builder(toBuilder = true)
public class IdentificationPage {

    private final int page;
    private final ModeleFormulaire modele;

    /** Renseigné lorsque la page est une annexe libre rattachée au formulaire */
    private final NatureAnnexe natureAnnexe;

    /** Score de reconnaissance du formulaire */
    private final double score;

    /** Proportion des repères du modèle présents dans le texte de la page */
    private final double tauxReperes;

    public boolean estAnnexe() {
        return natureAnnexe != null;
    }
}
