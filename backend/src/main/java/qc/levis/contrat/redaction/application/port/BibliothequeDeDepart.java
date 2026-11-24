package qc.levis.contrat.redaction.application.port;

/**
 * Bibliothèque de départ d'une organisation : les clauses et templates de la plateforme, recopiés chez elle pour qu'elle
 * rédige dès sa création. Idempotent : une organisation qui a déjà des clauses (ou des templates) n'en reçoit pas d'autres.
 */
public interface BibliothequeDeDepart {

    /** Amorce la bibliothèque de l'organisation ({@code tenantId}), sauf si elle en a déjà une. */
    void amorcer(Long tenantId);
}
