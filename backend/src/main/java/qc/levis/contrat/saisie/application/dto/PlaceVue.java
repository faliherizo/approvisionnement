package qc.levis.contrat.saisie.application.dto;

import qc.levis.contrat.saisie.domain.PlaceChoix;

/** Places d'un choix telles que l'interface les affiche : capacité, inscrits, restantes et complet. */
public record PlaceVue(String champ, String libelleChamp, String choix, Integer capacite, int inscrits, Integer restantes,
                       boolean complet) {
    public static PlaceVue de(PlaceChoix p) {
        return new PlaceVue(p.champ(), p.libelleChamp(), p.choix(), p.capacite(), p.inscrits(), p.restantes(), p.complet());
    }
}
