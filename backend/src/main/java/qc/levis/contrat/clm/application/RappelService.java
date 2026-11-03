package qc.levis.contrat.clm.application;

import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.repository.DecisionEtapeRepository;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.Invitation;
import qc.levis.contrat.clm.domain.ModeleMessage.Evenement;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.Relances;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Rappels de non-réponse et signalement des échéances dépassées (ST-11). Exécuté par le planificateur, tous tenants
 * confondus ; chaque étape est traitée dans le contexte de son tenant.
 */
@Service
public class RappelService {

    private static final Logger log = LoggerFactory.getLogger(RappelService.class);

    private final EtapeTrousseRepository etapes;
    private final DecisionEtapeRepository decisions;
    private final TrousseRepository trousses;
    private final InvitationRepository invitations;
    private final ParcoursService parcours;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public RappelService(EtapeTrousseRepository etapes, DecisionEtapeRepository decisions, TrousseRepository trousses,
                         InvitationRepository invitations, ParcoursService parcours, JournalClm journal, ContexteClm ctx) {
        this.etapes = etapes;
        this.decisions = decisions;
        this.trousses = trousses;
        this.invitations = invitations;
        this.parcours = parcours;
        this.journal = journal;
        this.ctx = ctx;
    }

    /** Retourne le nombre de rappels envoyés. */
    @Transactional
    public int executer() {
        Instant maintenant = ctx.maintenant();
        int envoyes = 0;
        for (EtapeTrousse e : etapes.findByStatut(Parcours.StatutEtape.ACTIVE)) {
            try {
                envoyes += ctx.dansTenant(e.getTenantId(), () -> trousses.findByIdAndTenantId(e.getTrousseId(), e.getTenantId())
                        .map(t -> traiter(t, e, maintenant)).orElse(0));
            } catch (RuntimeException ex) {
                log.warn("Rappel CLM impossible pour l'étape {} : {}", e.getId(), ex.getMessage());
            }
        }
        return envoyes;
    }

    private int traiter(Trousse t, EtapeTrousse e, Instant maintenant) {
        if (Relances.enRetard(e.getEcheance(), maintenant) && !e.isRetardSignale()) {
            e.signalerRetard();
            etapes.save(e);
            journal.consigner(t, "ECHEANCE_DEPASSEE", "systeme", e.getLibelle());
            parcours.notifierProprietaire(t, Evenement.RETARD, e.getLibelle());
        }
        if (!Relances.rappelDu(e.getActiveeLe(), e.getDernierRappel(), e.getRappelsEnvoyes(), maintenant,
                t.getRappelFrequenceJours(), t.getRappelsMax())) {
            return 0;
        }
        List<Parcours.Affectation> enAttente = enAttente(t, e);
        enAttente.forEach(a -> parcours.notifierTache(t, e, a, Evenement.RAPPEL));
        e.noterRappel(maintenant);
        etapes.save(e);
        journal.consigner(t, "RAPPEL_ENVOYE", "systeme", e.getLibelle() + " · " + enAttente.size() + " destinataire(s)");
        return enAttente.size();
    }

    /** Ne relance que ceux qui n'ont pas encore répondu ; pour une signature, les invitations encore ouvertes. */
    private List<Parcours.Affectation> enAttente(Trousse t, EtapeTrousse e) {
        List<Parcours.Affectation> affs = ctx.affectations(e.getAffectationsJson());
        if (e.getType() != Parcours.TypeEtape.SIGNATURE) {
            List<String> ontDecide = decisions.duCycleEnCours(e).stream()
                    .map(d -> d.getActeur().toLowerCase(java.util.Locale.ROOT)).toList();
            return affs.stream().filter(a -> a.type() != Parcours.TypeAffecte.UTILISATEUR
                    || !ontDecide.contains(a.valeur().toLowerCase(java.util.Locale.ROOT))).toList();
        }
        List<String> ouverts = invitations.findByTenantIdAndEtapeId(t.getTenantId(), e.getId()).stream()
                .filter(i -> i.getStatut() == Invitation.Statut.ENVOYEE || i.getStatut() == Invitation.Statut.VERIFIEE)
                .map(i -> String.valueOf(i.getParticipantId())).toList();
        return affs.stream().filter(a -> ouverts.contains(a.valeur())).toList();
    }
}
