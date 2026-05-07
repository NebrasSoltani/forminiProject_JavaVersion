package tn.formini.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class TunisiaGovernorates {

    private static final String[] VALUES = {
        "Ariana",
        "Béja",
        "Ben Arous",
        "Bizerte",
        "Gabès",
        "Gafsa",
        "Jendouba",
        "Kairouan",
        "Kasserine",
        "Kébili",
        "Le Kef",
        "Mahdia",
        "La Manouba",
        "Médenine",
        "Monastir",
        "Nabeul",
        "Sfax",
        "Sidi Bouzid",
        "Siliana",
        "Sousse",
        "Tataouine",
        "Tozeur",
        "Tunis",
        "Zaghouan"
    };

    private TunisiaGovernorates() {
    }

    public static ObservableList<String> asObservableList() {
        return FXCollections.observableArrayList(VALUES);
    }
}
