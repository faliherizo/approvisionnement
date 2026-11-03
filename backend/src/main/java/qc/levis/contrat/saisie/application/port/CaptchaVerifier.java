package qc.levis.contrat.saisie.application.port;

/** Port de vérification anti-robot (captcha). Implémentation réelle = adaptateur d'infrastructure. */
public interface CaptchaVerifier {
    boolean verifier(String token);
}
