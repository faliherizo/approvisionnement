package qc.levis.contrat.saisie.infrastructure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.saisie.application.port.CaptchaPublic;
import qc.levis.contrat.saisie.application.port.CaptchaVerifier;

/**
 * Adaptateur captcha par défaut (simulation, cf. patron ports/adaptateurs ADR-002) : accepte tout jeton
 * non vide. L'adaptateur réel ({@link CaptchaSiteverify}) s'active par app.saisie.captcha.mode=siteverify.
 */
@Component
@ConditionalOnProperty(name = "app.saisie.captcha.mode", havingValue = "simulation", matchIfMissing = true)
public class CaptchaSimulation implements CaptchaVerifier {
    @Override
    public boolean verifier(String token) {
        return token != null && !token.isBlank();
    }

    @Override
    public CaptchaPublic configurationPublique() {
        return CaptchaPublic.simulation();
    }
}
