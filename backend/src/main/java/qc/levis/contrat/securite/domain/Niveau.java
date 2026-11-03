package qc.levis.contrat.securite.domain;

/**
 * Niveaux de classification de l'information (ordonnés). Taxonomie imposée par l'appel d'offres CHU ;
 * l'AFFECTATION d'un niveau à un champ reste une donnée de configuration.
 */
public enum Niveau {
    PUBLIC, INTERNE, CONFIDENTIEL, SECRET;

    public static Niveau depuis(String code) {
        if (code == null) return PUBLIC;
        try { return Niveau.valueOf(code.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { return PUBLIC; }
    }
}
