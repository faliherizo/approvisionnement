package qc.levis.contrat.clm.application;

import org.springframework.stereotype.Component;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.domain.ChaineScellee;
import qc.levis.contrat.clm.domain.EvenementTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Consigne chaque action métier sensible deux fois : dans le journal scellé de la trousse (preuve, ST-10) et dans
 * le journal d'audit du tenant (exploitation, ST-14), avec la référence de trousse comme corrélation.
 */
@Component
public class JournalClm {

    private final EvenementTrousseRepository evenements;
    private final AuditService audit;
    private final ContexteClm ctx;

    public JournalClm(EvenementTrousseRepository evenements, AuditService audit, ContexteClm ctx) {
        this.evenements = evenements;
        this.audit = audit;
        this.ctx = ctx;
    }

    public EvenementTrousse consigner(Trousse t, String type, String acteur, String detail) {
        var dernier = evenements.findTopByTenantIdAndTrousseIdOrderBySequenceDesc(t.getTenantId(), t.getId());
        long sequence = dernier.map(e -> e.getSequence() + 1).orElse(1L);
        String precedente = dernier.map(EvenementTrousse::getEmpreinte).orElse(ChaineScellee.ORIGINE);
        String qui = acteur == null || acteur.isBlank() ? "systeme" : acteur;
        EvenementTrousse e = evenements.save(new EvenementTrousse(t.getTenantId(), t.getId(), sequence, type, qui,
                ctx.maintenant(), detail, precedente));
        audit.log(t.getTenantId(), qui, "CLM_" + type, "TROUSSE", t.getId(),
                t.getReference() + (detail == null || detail.isBlank() ? "" : " · " + detail));
        return e;
    }

    /** Refus d'accès (ST-02) : événement de sécurité du tenant, hors du journal scellé d'une trousse. */
    public void refus(String acteur, String ressource, String motif) {
        audit.log(acteur == null || acteur.isBlank() ? "anonyme" : acteur, "CLM_ACCES_REFUSE", "API", null, ressource + " · " + motif);
    }

    /** Changement de configuration du module par l'administration de l'organisation (UI-21, UI-22). */
    public void configuration(String acteur, String objet, String detail) {
        audit.log(acteur, "CLM_CONFIGURATION", objet, null, detail);
    }
}
