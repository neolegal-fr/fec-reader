package fr.neolegal.fec.liassefiscale;

import static java.util.Objects.isNull;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Section implements Comparable<Section> {
    String nom;
    Set<Section> sections;
    Set<Repere> reperes = new TreeSet<>();

    public Section(String nom, Set<Repere> reperes, Set<Section> sections) {
        this.nom = nom;
        this.reperes = ObjectUtils.firstNonNull(reperes, new TreeSet<>());
        this.sections = sections;
    }

    public Section(String nom) {
        this(nom, null, null);
    }

    public Section() {
        this(null, null, null);
    }

    public Section addSection(String nomSection) {
        if (isNull(sections)) {
            sections = new TreeSet<>();
        }
        return getSection(nomSection).orElseGet(() -> {
            Section newSection = new Section(nomSection);
            sections.add(newSection);
            return newSection;
        });
    }

    @Override
    public int compareTo(Section other) {
        if (other == this) {
            return 0;
        }

        if (other == null) {
            return 1;
        }

        return StringUtils.compareIgnoreCase(nom, other.nom);
    }

    public Optional<Section> getSection(String nomSection) {
        if (isNull(sections)) {
            return Optional.empty();
        }
        return sections.stream().filter(s -> StringUtils.equalsIgnoreCase(s.getNom(), nomSection)).findFirst();
    }

    public void addRepere(Repere repere) {
        if (isNull(reperes)) {
            reperes = new TreeSet<>();
        }
        reperes.add(repere);
    }

    public Set<Repere> getAllReperes() {
        Set<Repere> result = new TreeSet<>();
        result.addAll(reperes);
        for (Section section : emptyIfNull(sections)) {
            result.addAll(section.getAllReperes());
        }
        return result;
    }

}
