package qc.levis.contrat.saisie.infrastructure;

import org.springframework.stereotype.Component;
import qc.levis.contrat.saisie.application.port.CaptchaVerifier;

/**
 * Adaptateur captcha par défaut (simulation, cf. patron ports/adaptateurs ADR-002) : accepte tout jeton
 * non vide. L'adaptateur réel (hCaptcha/reCAPTCHA) s'ajoute via un profil, sans toucher au domaine.
 */
@Component
public class CaptchaSimulation implements CaptchaVerifier {
    @Override
    public boolean verifier(String token) {
        return token != null && !token.isBlank();
    }
}
