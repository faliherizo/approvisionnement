package qc.levis.contrat.notification.infrastructure.vieprivee;

import java.util.List;
import org.springframework.stereotype.Component;
import qc.levis.contrat.vieprivee.application.port.SourceRenseignementsPersonnels;
import qc.levis.contrat.vieprivee.domain.BilanEffacement;
import qc.levis.contrat.vieprivee.domain.ExtraitRenseignements;
import qc.levis.contrat.vieprivee.domain.Pseudonyme;
import qc.levis.contrat.vieprivee.domain.Renseignement;
import qc.levis.contrat.notification.application.repository.NotificationEnvoiRepository;
import qc.levis.contrat.notification.domain.NotificationEnvoi;

/** Notifications envoyées à la personne : destinataire, objet et contenu. */
@Component
class NotificationsRenseignements implements SourceRenseignementsPersonnels {

    private final NotificationEnvoiRepository envois;

    NotificationsRenseignements(NotificationEnvoiRepository envois) { this.envois = envois; }

    @Override
    public ExtraitRenseignements extraire(Long tenantId, String courriel) {
        List<Renseignement> elements = envois.findByTenantIdAndDestinataireIgnoreCase(tenantId, courriel).stream()
                .map(n -> Renseignement.de("notification/" + n.getId(), n.getEnvoyeLe(), "canal", n.getCanal(), "objet", n.getSujet(), "contenu", n.getCorps(), "statut", n.getStatut()))
                .toList();
        return new ExtraitRenseignements("Notifications", "Avis transmis à la personne au fil du traitement de ses demandes.",
                "Le destinataire, l'objet et le contenu sont effacés ; seul l'envoi reste compté.", elements);
    }

    @Override
    public BilanEffacement effacer(Long tenantId, String courriel, Pseudonyme pseudonyme) {
        List<NotificationEnvoi> liste = envois.findByTenantIdAndDestinataireIgnoreCase(tenantId, courriel);
        liste.forEach(n -> n.pseudonymiser(pseudonyme.courriel()));
        envois.saveAll(liste);
        return BilanEffacement.complet("Notifications", liste.size());
    }
}
