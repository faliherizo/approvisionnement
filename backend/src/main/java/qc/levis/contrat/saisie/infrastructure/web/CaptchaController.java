package qc.levis.contrat.saisie.infrastructure.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.saisie.application.port.CaptchaPublic;
import qc.levis.contrat.saisie.application.port.CaptchaVerifier;

/** Fournisseur et clé de site du défi anti-robot, pour le rendu du formulaire (CHU 3.07). */
@RestController
@RequestMapping("/api/saisie/captcha")
@PreAuthorize("@modules.actif('saisie')")
public class CaptchaController {

    private final CaptchaVerifier captcha;

    public CaptchaController(CaptchaVerifier captcha) {
        this.captcha = captcha;
    }

    @GetMapping
    public CaptchaPublic configuration() {
        return captcha.configurationPublique();
    }
}
