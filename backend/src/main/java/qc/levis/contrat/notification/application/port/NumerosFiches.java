package qc.levis.contrat.notification.application.port;

import java.util.Optional;

/** Numéro de suivi d'une fiche, que les gabarits de notification citent ({@code {{numero}}}). */
@FunctionalInterface
public interface NumerosFiches {
    Optional<String> numero(Long tenantId, Long ficheId);
}
