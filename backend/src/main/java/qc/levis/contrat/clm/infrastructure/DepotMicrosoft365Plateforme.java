package qc.levis.contrat.clm.infrastructure;

import java.util.List;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.DepotMicrosoft365;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;

/**
 * Dépôt Microsoft 365 du module par le connecteur de la plateforme (ADR-002, ADR-029) : les mêmes adaptateurs que la
 * publication du module de rédaction, essayés dans leur ordre de priorité. Microsoft Graph prend la main lorsque
 * l'organisation a configuré et activé sa connexion ; sinon la simulation dépose dans le magasin visualisable. Aucun
 * protocole n'est réécrit ici.
 */
@Component
public class DepotMicrosoft365Plateforme implements DepotMicrosoft365 {

    private final List<PublicationCiblePort> adaptateurs;

    public DepotMicrosoft365Plateforme(List<PublicationCiblePort> adaptateurs) {
        this.adaptateurs = List.copyOf(adaptateurs);
    }

    @Override
    public Resultat deposer(Destination destination, Fichier fichier) {
        Enums.CiblePublication cible = Enums.CiblePublication.valueOf(destination.name());
        PublicationCiblePort adaptateur = adaptateurs.stream().filter(a -> a.supporte(cible)).findFirst().orElse(null);
        if (adaptateur == null) {
            return new Resultat(false, null, "Aucun connecteur Microsoft 365 pour " + cible + ".");
        }
        try {
            PublicationCiblePort.ResultatPublication r = adaptateur.deposer(cible, new PublicationCiblePort.ContenuPublie(
                    fichier.trousseId(), fichier.titre(), fichier.nom(), fichier.mediaType(), fichier.octets(), fichier.dossier()));
            return new Resultat(r.succes(), r.url(), r.message());
        } catch (RuntimeException e) {
            return new Resultat(false, null, "Échec Microsoft 365 : " + e.getMessage());
        }
    }
}
