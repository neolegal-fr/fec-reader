package fr.neolegal.fec;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Anomalie {
    NatureAnomalie nature;
    Object valeur;
    String Message;
}
