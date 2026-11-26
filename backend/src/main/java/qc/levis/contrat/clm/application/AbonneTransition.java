package qc.levis.contrat.clm.application;

import java.util.Map;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Système externe informé d'une transition de trousse (Salesforce, client d'intégration, Microsoft 365…). Chaque abonné
 * décide s'il est concerné et inscrit son envoi dans la transaction de la transition ; la livraison suit, avec reprises.
 * Ajouter un système = ajouter un abonné, sans toucher à {@link DiffusionStatut}.
 */
public interface AbonneTransition {

    void enfiler(Trousse t, String evenement, long sequence, Map<String, Object> donnees);
}
