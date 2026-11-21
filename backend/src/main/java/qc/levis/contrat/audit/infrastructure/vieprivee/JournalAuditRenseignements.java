package qc.levis.contrat.audit.infrastructure.vieprivee;

import java.util.List;
import org.springframework.stereotype.Component;
import qc.levis.contrat.vieprivee.application.port.SourceRenseignementsPersonnels;
import qc.levis.contrat.vieprivee.domain.BilanEffacement;
import qc.levis.contrat.vieprivee.domain.ExtraitRenseignements;
import qc.levis.contrat.vieprivee.domain.Pseudonyme;
import qc.levis.contrat.vieprivee.domain.Renseignement;
import qc.levis.contrat.audit.application.repository.AuditLogRepository;
import qc.levis.contrat.audit.domain.AuditLog;

/** Journal d'audit : les actions de la personne et celles qui la mentionnent. */
@Component
class JournalAuditRenseignements implements SourceRenseignementsPersonnels {

    private final AuditLogRepository journal;

    JournalAuditRenseignements(AuditLogRepository journal) { this.journal = journal; }

    @Override
    public ExtraitRenseignements extraire(Long tenantId, String courriel) {
        List<Renseignement> elements = journal.deLaPersonne(tenantId, courriel).stream()
                .map(a -> Renseignement.de("audit/" + a.getId(), a.getHorodatage(), "action", a.getAction(), "auteur", a.getUtilisateur(), "detail", a.getDetails()))
                .toList();
        return new ExtraitRenseignements("Journal d'audit", "Traçabilité des actions faites dans la plateforme (sécurité, preuve).",
                "Les événements restent au journal ; le courriel est remplacé par un pseudonyme.", elements);
    }

    @Override
    public BilanEffacement effacer(Long tenantId, String courriel, Pseudonyme pseudonyme) {
        List<AuditLog> entrees = journal.deLaPersonne(tenantId, courriel);
        entrees.forEach(a -> a.pseudonymiser(courriel, pseudonyme.valeur()));
        journal.saveAll(entrees);
        return BilanEffacement.complet("Journal d'audit", entrees.size());
    }
}
