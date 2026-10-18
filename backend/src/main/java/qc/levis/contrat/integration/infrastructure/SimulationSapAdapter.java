package qc.levis.contrat.integration.infrastructure;

import java.nio.charset.StandardCharsets;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;
import qc.levis.contrat.integration.application.port.ReferenceFinancierePort;

/**
 * Adaptateur SAP par défaut (simulation) : enregistre la référence financière du dossier et
 * renvoie une URL simulée. Écrit aussi un enregistrement lisible dans le magasin du simulateur pour
 * pouvoir le visualiser. Un adaptateur SAP réel (RFC/OData) implémentant le même
 * {@link ReferenceFinancierePort} avec une priorité supérieure le remplace une fois configuré.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SimulationSapAdapter implements ReferenceFinancierePort {

    private final SimulateurDepotStore store;

    public SimulationSapAdapter(SimulateurDepotStore store) {
        this.store = store;
    }

    @Override
    public ResultatPublication deposer(ReferenceFinanciere reference) {
        String cle = reference.numeroAO() != null && !reference.numeroAO().isBlank()
                ? reference.numeroAO() : ("dossier-" + reference.dossierId());
        String contenu = "Référence financière SAP\nN° AO : " + cle
                + "\nObjet : " + reference.objet()
                + "\nDocuments : " + reference.nombreDocuments()
                + "\nMontant total : " + reference.montantTotal();
        store.enregistrer("SAP", TenantContext.get(), null, cle + ".txt",
                contenu.getBytes(StandardCharsets.UTF_8));
        return new ResultatPublication(true, "sap://dossier/" + cle,
                "Référence financière simulée (" + reference.nombreDocuments() + " doc., total "
                        + reference.montantTotal() + ")");
    }
}
