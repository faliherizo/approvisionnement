package qc.levis.contrat.clm.infrastructure;

import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ConnexionsSalesforce;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.domain.ConnexionSalesforce;

/**
 * Port Salesforce des cas d'utilisation : chaque appel suit le mode de la connexion de l'organisation en cours (API
 * réelle ou simulation), de sorte que deux organisations puissent relever de deux instances différentes.
 */
@Component
@Primary
public class SalesforceParOrganisation implements PortSalesforce {

    private final ConnexionsSalesforce connexions;
    private final SalesforceRest rest;
    private final SalesforceSimulation simulation;

    public SalesforceParOrganisation(ConnexionsSalesforce connexions, SalesforceRest rest, SalesforceSimulation simulation) {
        this.connexions = connexions;
        this.rest = rest;
        this.simulation = simulation;
    }

    private PortSalesforce adaptateur() {
        return connexions.courante().mode() == ConnexionSalesforce.Mode.REST ? rest : simulation;
    }

    @Override
    public Optional<Dossier> lireDossier(String identifiantOuNumero) {
        return adaptateur().lireDossier(identifiantOuNumero);
    }

    @Override
    public void publierStatut(Statut statut) {
        adaptateur().publierStatut(statut);
    }

    @Override
    public EtatConnexion tester() {
        return adaptateur().tester();
    }
}
