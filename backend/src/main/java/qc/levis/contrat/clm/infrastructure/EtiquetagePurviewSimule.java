package qc.levis.contrat.clm.infrastructure;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.EtiquetagePurview;
import qc.levis.contrat.clm.domain.EtiquettePurview;

/**
 * Purview simulé (défaut, ADR-036) : un catalogue représentatif d'une organisation publique québécoise, et une apposition
 * consignée sans appel externe. Il permet de régler et de démontrer l'étiquetage tant que le locataire Microsoft 365 de
 * l'organisation n'est pas raccordé (profil {@code purview}).
 */
@Component
@Profile("!purview")
public class EtiquetagePurviewSimule implements EtiquetagePurview {

    static final List<EtiquettePurview> CATALOGUE = List.of(
            EtiquettePurview.sensibilite("b1a5d0c2-0000-4000-8000-000000000001", "Public"),
            EtiquettePurview.sensibilite("b1a5d0c2-0000-4000-8000-000000000002", "Interne"),
            EtiquettePurview.sensibilite("b1a5d0c2-0000-4000-8000-000000000003", "Confidentiel"),
            EtiquettePurview.sensibilite("b1a5d0c2-0000-4000-8000-000000000004", "Hautement confidentiel"),
            EtiquettePurview.retention("Contrats - 7 ans", 2557),
            EtiquettePurview.retention("Dossiers RH - 10 ans", 3653),
            EtiquettePurview.retention("Conservation permanente", null));

    @Override
    public List<EtiquettePurview> catalogue(Long tenantId) {
        return CATALOGUE;
    }

    @Override
    public Resultat apposer(Long tenantId, String urlDepot, EtiquettePurview sensibilite, EtiquettePurview retention) {
        if (urlDepot == null || urlDepot.isBlank()) {
            return new Resultat(false, "Aucun fichier déposé à étiqueter.");
        }
        return new Resultat(true, "Étiquettes apposées (Purview simulé).");
    }
}
