package qc.levis.contrat.securite.infrastructure;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import qc.levis.contrat.securite.infrastructure.web.AutorisationAnticipee;

/** SEC-32 : l'autorisation est décidée avant la lecture de la requête, avec la règle @PreAuthorize de la méthode. */
class AutorisationAnticipeeTest {

    static class Controleur {
        @PreAuthorize("hasRole('ADMIN')")
        public String reserve(String corps) { return corps; }

        public String libre() { return "ok"; }
    }

    private final AutorisationAnticipee garde = new AutorisationAnticipee(new DefaultMethodSecurityExpressionHandler());

    @AfterEach
    void nettoyer() { SecurityContextHolder.clearContext(); }

    private static void connecter(String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x", null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private boolean appeler(Object gestionnaire) {
        return garde.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), gestionnaire);
    }

    @Test
    void un_role_non_autorise_est_refuse_avant_toute_lecture_du_corps() throws Exception {
        connecter("LECTEUR");
        HandlerMethod m = new HandlerMethod(new Controleur(), Controleur.class.getMethod("reserve", String.class));
        assertThrows(AuthorizationDeniedException.class, () -> appeler(m));
    }

    @Test
    void un_role_autorise_et_une_methode_sans_regle_passent() throws Exception {
        connecter("ADMIN");
        assertTrue(appeler(new HandlerMethod(new Controleur(), Controleur.class.getMethod("reserve", String.class))));
        connecter("LECTEUR");
        assertTrue(appeler(new HandlerMethod(new Controleur(), Controleur.class.getMethod("libre"))));
    }

    @Test
    void un_gestionnaire_qui_n_est_pas_un_controleur_ou_non_resolu_est_laisse_a_la_securite_de_methode() throws Exception {
        assertTrue(appeler(new Object()));
        var fabrique = new org.springframework.beans.factory.support.DefaultListableBeanFactory();
        fabrique.registerSingleton("controleur", new Controleur());
        var nonResolu = new HandlerMethod("controleur", fabrique, Controleur.class.getMethod("reserve", String.class));
        assertTrue(appeler(nonResolu));
    }
}
