package qc.levis.contrat.clm.application.port;

import java.util.function.Supplier;

/**
 * Tenant (organisme) des cas d'utilisation : celui de la requête authentifiée, ou celui fixé le temps d'un traitement
 * hors requête (portail public du signataire, planificateur). L'isolation multi-tenant en dépend.
 */
public interface PorteeTenant {

    /** Tenant courant ; {@code null} hors espace tenant (superadministration). */
    Long courant();

    /** Exécute {@code action} dans le tenant indiqué, puis rétablit le tenant précédent, même en cas d'erreur. */
    <T> T executerDans(Long tenantId, Supplier<T> action);
}
