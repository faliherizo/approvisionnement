package qc.levis.contrat.clm.application.port;

/**
 * Livraison d'un rappel de statut au système externe (addenda 1, TI-22). Le corps est signé par l'application ;
 * l'adaptateur ne fait que le transport. Une réponse hors 2xx ou un hôte injoignable lève {@link Refuse}.
 */
public interface PortRappel {

    void envoyer(String url, String corpsJson, String signature, String cleIdempotence);

    class Refuse extends RuntimeException {
        public Refuse(String message) {
            super(message);
        }

        public Refuse(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
