package tn.formini.entities.Users;

public enum Gouvernorat {
    TUNIS("Tunis"),
    ARIANA("Ariana"),
    BEN_AROUS("Ben Arous"),
    MANOUBA("Manouba"),
    NABEUL("Nabeul"),
    ZAGHOUAN("Zaghouan"),
    BIZERTE("Bizerte"),
    BEJA("Béja"),
    JENDOUBA("Jendouba"),
    LE_KEF("Le Kef"),
    SILIANA("Siliana"),
    SOUSE("Sousse"),
    MONASTIR("Monastir"),
    MAHDIA("Mahdia"),
    KAIROUAN("Kairouan"),
    KASSERINE("Kasserine"),
    SIDI_BOUZID("Sidi Bouzid"),
    GAFSA("Gafsa"),
    TOZEUR("Tozeur"),
    KEbili("Kébili"),
    GABES("Gabès"),
    MEDENINE("Médenine"),
    TATAOUINE("Tataouine");

    private final String displayName;

    Gouvernorat(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public static Gouvernorat fromDisplayName(String displayName) {
        for (Gouvernorat gouvernorat : Gouvernorat.values()) {
            if (gouvernorat.getDisplayName().equalsIgnoreCase(displayName)) {
                return gouvernorat;
            }
        }
        return null;
    }
}
