package qc.levis.contrat.integration.infrastructure;

import java.util.Locale;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;

/**
 * Adaptateur de publication par défaut (simulation) : enregistre le dépôt et renvoie une URL
 * simulée, sans appel externe. Il permet d'exploiter la fonctionnalité hors connexion M365/SAP
 * et de tester le parcours de bout en bout. Un adaptateur réel (MS Graph, SAP) implémentant le
 * même {@link PublicationCiblePort} avec une priorité supérieure le remplace dès qu'il est
 * configuré (ADR-002, OCP) — aucun changement du code appelant.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SimulationPublicationAdapter implements PublicationCiblePort {

    private final SimulateurDepotStore store;

    public SimulationPublicationAdapter(SimulateurDepotStore store) {
        this.store = store;
    }

    @Override
    public boolean supporte(Enums.CiblePublication cible) {
        return true; // solution de repli universelle
    }

    @Override
    public ResultatPublication deposer(Enums.CiblePublication cible, ContenuPublie contenu) {
        // Écrit réellement le fichier dans le magasin du simulateur (visualisable/téléchargeable).
        store.enregistrer(cible.name(), TenantContext.get(), contenu.destination(),
                contenu.nomFichier(), contenu.octets());
        String dossier = contenu.destination() == null || contenu.destination().isBlank()
                ? "" : contenu.destination() + "/";
        String url = "sim://" + cible.name().toLowerCase(Locale.ROOT) + "/" + dossier + contenu.nomFichier();
        return new ResultatPublication(true, url,
                "Dépôt simulé vers " + cible.name() + " dans « " + contenu.destination()
                        + " » (" + contenu.octets().length + " octets)");
    }
}
