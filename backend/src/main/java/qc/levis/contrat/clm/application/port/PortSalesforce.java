package qc.levis.contrat.clm.application.port;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/** Port vers Salesforce Grantmaking (ST-06 lecture, ST-12 retour de statuts). */
public interface PortSalesforce {

    record Dossier(String id, String numero, Map<String, Object> champs, Instant recupereLe) {
    }

    record Statut(String sfDossierId, String evenement, String correlationId, String cleIdempotence, Map<String, Object> donnees) {
    }

    record EtatConnexion(boolean disponible, String mode, String detail) {
    }

    /** Salesforce injoignable ou authentification refusée : l'appelant peut réessayer plus tard. */
    class Indisponible extends RuntimeException {
        public Indisponible(String message) {
            super(message);
        }

        public Indisponible(String message, Throwable cause) {
            super(message, cause);
        }
    }

    Optional<Dossier> lireDossier(String identifiantOuNumero);

    void publierStatut(Statut statut);

    EtatConnexion tester();
}
