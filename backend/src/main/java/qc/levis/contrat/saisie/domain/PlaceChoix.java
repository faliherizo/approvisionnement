package qc.levis.contrat.saisie.domain;

/** État des places d'un choix : capacité (nulle si illimitée), fiches qui l'occupent, places restantes. */
public record PlaceChoix(String champ, String libelleChamp, String choix, Integer capacite, int inscrits) {

    public Integer restantes() {
        return capacite == null ? null : Math.max(0, capacite - inscrits);
    }

    public boolean complet() {
        return capacite != null && inscrits >= capacite;
    }
}
