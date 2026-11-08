package qc.levis.contrat.commun.module.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import qc.levis.contrat.commun.module.application.ModuleAccess;

/** Garde de module : refus par défaut d'une fonctionnalité non souscrite, quel que soit le rôle de l'appelant. */
class GardeModulesTest {

    @ModuleRequis({"redaction", "formulaire"})
    static class ControleurPartage {
        public void lire() {
        }
    }

    static class ControleurTransverse {
        public void lire() {
        }
    }

    private final ModuleAccess modules = mock(ModuleAccess.class);
    private final GardeModules garde = new GardeModules(modules, new ObjectMapper());

    @Test
    void une_ressource_statique_ou_un_controleur_transverse_passe_sans_consulter_les_modules() throws Exception {
        assertTrue(garde.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));
        assertTrue(garde.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), methode(new ControleurTransverse())));
        verifyNoInteractions(modules);
    }

    @Test
    void un_seul_des_modules_requis_suffit() throws Exception {
        when(modules.actif("formulaire")).thenReturn(true);
        assertTrue(garde.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), methode(new ControleurPartage())));
    }

    @Test
    void sans_aucun_module_souscrit_la_requete_est_refusee_avant_execution() throws Exception {
        MockHttpServletResponse reponse = new MockHttpServletResponse();
        assertFalse(garde.preHandle(new MockHttpServletRequest(), reponse, methode(new ControleurPartage())));
        assertEquals(403, reponse.getStatus());
        assertTrue(reponse.getContentType().startsWith("application/json"), reponse.getContentType());
        assertEquals("{\"message\":\"Fonctionnalité non souscrite par votre organisation (module « redaction » ou « formulaire »).\"}",
                reponse.getContentAsString(StandardCharsets.UTF_8));
    }

    @Test
    void la_garde_couvre_toute_l_api() {
        InterceptorRegistry registre = mock(InterceptorRegistry.class);
        InterceptorRegistration inscription = mock(InterceptorRegistration.class);
        when(registre.addInterceptor(garde)).thenReturn(inscription);
        new ConfigurationGardeModules(garde).addInterceptors(registre);
        verify(inscription).addPathPatterns("/api/**");
    }

    private static HandlerMethod methode(Object controleur) throws NoSuchMethodException {
        return new HandlerMethod(controleur, controleur.getClass().getMethod("lire"));
    }
}
