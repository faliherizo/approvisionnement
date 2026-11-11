package qc.levis.contrat.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Le retour SAML est posté par l'IdP (« Origin: null » chez Keycloak, origine de l'IdP chez Entra ID) : il atteint la
 * vérification de l'assertion au lieu d'un refus CORS. Toute autre route garde la politique CORS de la plateforme.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsRetourSamlTest {

    @Autowired MockMvc mvc;

    @Test
    void le_retour_saml_poste_par_l_idp_n_est_pas_refuse_par_cors() throws Exception {
        for (String origine : new String[]{"null", "https://login.microsoftonline.com"}) {
            mvc.perform(post("/api/auth/saml/callback").header("Origin", origine)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("SAMLResponse", "invalide"))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Test
    void les_autres_routes_gardent_la_politique_cors() throws Exception {
        for (String origine : new String[]{"null", "https://login.microsoftonline.com"}) {
            mvc.perform(post("/api/auth/login").header("Origin", origine).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"x@iq.example\",\"motDePasse\":\"x\"}"))
                    .andExpect(status().isForbidden());
        }
    }
}
