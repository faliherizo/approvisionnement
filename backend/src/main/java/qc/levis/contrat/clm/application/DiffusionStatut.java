package qc.levis.contrat.clm.application;

import java.util.Map;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Diffusion d'une transition de trousse aux systèmes externes concernés : retour de statut vers le dossier Salesforce
 * d'origine (ST-12) et rappel au client d'intégration qui l'a créée (addenda 1). Chacun décide s'il est concerné.
 */
@Service
public class DiffusionStatut {

    private final SalesforceService salesforce;
    private final RappelsExternesService rappels;

    public DiffusionStatut(SalesforceService salesforce, RappelsExternesService rappels) {
        this.salesforce = salesforce;
        this.rappels = rappels;
    }

    public void enfiler(Trousse t, String evenement, long sequence, Map<String, Object> donnees) {
        salesforce.enfiler(t, evenement, sequence, donnees);
        rappels.enfiler(t, evenement, sequence, donnees);
    }
}
