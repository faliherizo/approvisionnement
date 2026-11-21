package qc.levis.contrat.messagerie.application.port;

/** Seuls des membres actifs de l'organisation peuvent participer à une conversation. */
public interface AnnuaireMembres {
    boolean estMembreActif(Long tenantId, String courriel);
}
