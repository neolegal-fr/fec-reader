package fr.neolegal.fec.liassefiscale.pdf;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

/** Ensemble des mots d'une page situés sur une même ligne de base. */
@Getter
public class LignePdf {

    private final List<MotPdf> mots;
    private final float haut;
    private final float bas;
    private final String texte;
    /** Texte de la ligne sans accent, sans espace et en majuscules */
    private final String texteNormalise;

    public LignePdf(List<MotPdf> mots) {
        this.mots = Collections.unmodifiableList(mots);
        this.haut = (float) mots.stream().mapToDouble(MotPdf::getHaut).min().orElse(0);
        this.bas = (float) mots.stream().mapToDouble(MotPdf::getBas).max().orElse(0);
        this.texte = mots.stream().map(MotPdf::getTexte).collect(Collectors.joining(" "));
        this.texteNormalise = MotPdf.normalise(this.texte);
    }

    public float getMilieuY() {
        return (haut + bas) / 2f;
    }

    /** Index du mot dans la ligne, ou -1 */
    public int indexOf(MotPdf mot) {
        return mots.indexOf(mot);
    }

    @Override
    public String toString() {
        return String.format("%.1f: %s", haut, texte);
    }
}
