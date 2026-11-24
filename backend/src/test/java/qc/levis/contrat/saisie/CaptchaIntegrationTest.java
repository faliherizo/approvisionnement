package qc.levis.contrat.saisie;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** CHU 3.07 : le formulaire connaît le défi à afficher ; sans le module « saisie », la configuration est refusée. */
@SpringBootTest
@AutoConfigureMockMvc
class CaptchaIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void la_configuration_publique_du_defi_est_servie_aux_organisations_qui_ont_la_saisie() throws Exception {
        Api api = new Api(mvc, json);
        Cookie avecSaisie = api.organisation("essai-captcha", "formulaire", "saisie");
        Api.Reponse r = api.get("/api/saisie/captcha", avecSaisie);
        assertEquals(200, r.statut());
        assertEquals("SIMULATION", r.corps().get("fournisseur").asText());
        assertEquals("", r.corps().get("cleSite").asText());

        Cookie sansSaisie = api.organisation("essai-captcha-sans", "formulaire");
        assertEquals(403, api.get("/api/saisie/captcha", sansSaisie).statut());
    }
}
