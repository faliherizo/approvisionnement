package qc.levis.contrat.saisie.application.port;

/**
 * Ce que le navigateur doit connaître pour afficher le défi anti-robot : le fournisseur et sa clé de site, publique par
 * nature. En simulation, le fournisseur vaut « SIMULATION » et aucune clé n'est exposée.
 *
 * @param fournisseur SIMULATION, TURNSTILE ou HCAPTCHA
 * @param cleSite     clé publique du widget ; vide en simulation
 */
public record CaptchaPublic(String fournisseur, String cleSite) {

    public static final String SIMULATION = "SIMULATION";

    public static CaptchaPublic simulation() {
        return new CaptchaPublic(SIMULATION, "");
    }
}
