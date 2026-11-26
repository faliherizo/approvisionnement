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
        EvenementTrousse e = sceller(t, type, qui(acteur), detail);
        audit.log(t.getTenantId(), qui(acteur), "CLM_" + type, "TROUSSE", t.getId(),
                t.getReference() + (detail == null || detail.isBlank() ? "" : " · " + detail));
        return e;
    }

    /**
     * Comme {@link #consigner}, pour un détail qui porte des valeurs d'affaires (avant/après d'un champ, AF-34) : il est
     * scellé et conservé au journal d'audit, mais n'est pas recopié dans le journal technique (Loi 25).
     */
    public EvenementTrousse consignerConfidentiel(Trousse t, String type, String acteur, String detail) {
        EvenementTrousse e = sceller(t, type, qui(acteur), detail);
        audit.logConfidentiel(t.getTenantId(), qui(acteur), "CLM_" + type, "TROUSSE", t.getId(), t.getReference() + " · " + detail);
        return e;
    }

    private EvenementTrousse sceller(Trousse t, String type, String qui, String detail) {
        var dernier = evenements.findTopByTenantIdAndTrousseIdOrderBySequenceDesc(t.getTenantId(), t.getId());
        long sequence = dernier.map(e -> e.getSequence() + 1).orElse(1L);
        String precedente = dernier.map(EvenementTrousse::getEmpreinte).orElse(ChaineScellee.ORIGINE);
        return evenements.save(new EvenementTrousse(t.getTenantId(), t.getId(), sequence, type, qui, ctx.maintenant(), detail, precedente));
    }

    private static String qui(String acteur) {
        return acteur == null || acteur.isBlank() ? "systeme" : acteur;
    }

    /** Refus d'accès (ST-02) : événement de sécurité du tenant, hors du journal scellé d'une trousse. */
    public void refus(String acteur, String ressource, String motif) {
        audit.log(acteur == null || acteur.isBlank() ? "anonyme" : acteur, "CLM_ACCES_REFUSE", "API", null, ressource + " · " + motif);
    }

    /** Accès de soutien de l'exploitant (SEC-17) : demande, décision, consultation ou refus, au journal de l'organisation. */
    public void soutien(String acteur, Long accesId, String detail) {
        audit.log(acteur, "CLM_ACCES_SOUTIEN", "SOUTIEN", accesId, detail);
    }

    /** Changement de configuration du module par l'administration de l'organisation (UI-21, UI-22). */
    public void configuration(String acteur, String objet, String detail) {
        audit.log(acteur, "CLM_CONFIGURATION", objet, null, detail);
    }
}
