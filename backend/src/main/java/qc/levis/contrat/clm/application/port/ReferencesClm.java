package qc.levis.contrat.clm.application.port;

import java.util.List;

/** Listes de référence de l'organisation courante (valeurs de la plateforme, personnalisées par l'organisation). */
public interface ReferencesClm {

    /** Valeur active d'une liste ; {@code detail} : complément propre à la liste (ex. mots-clés de classification). */
    record Valeur(String code, String libelle, String detail) {
    }

    List<Valeur> valeurs(String liste);
}
