package qc.levis.contrat.securite.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.util.SimpleMethodInvocation;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Décide l'autorisation ({@code @PreAuthorize}) AVANT la lecture de la requête (constat SEC-32). La sécurité de méthode de
 * Spring ne s'applique qu'à l'appel du contrôleur, donc après la lecture du corps, sa validation et la réception des
 * fichiers : un rôle non autorisé recevait un 400 qui décrivait le format attendu, et le serveur recevait, pour lui, un
 * téléversement complet. La décision est prise ici, par le même moteur et avec les mêmes expressions ; aucune règle de
 * l'application ne lit les arguments de la méthode, la décision ne dépend donc que de la personne et de son organisation.
 * La sécurité de méthode reste en place, à l'appel, comme seconde barrière.
 */
@Component
public class AutorisationAnticipee implements HandlerInterceptor {

    private final PreAuthorizeAuthorizationManager decideur = new PreAuthorizeAuthorizationManager();

    public AutorisationAnticipee(MethodSecurityExpressionHandler expressions) {
        decideur.setExpressionHandler(expressions);
    }

    @Override
    public boolean preHandle(HttpServletRequest requete, HttpServletResponse reponse, Object gestionnaire) {
        if (!(gestionnaire instanceof HandlerMethod methode) || methode.getBean() instanceof String) return true;
        var appel = new SimpleMethodInvocation(methode.getBean(), methode.getMethod(), new Object[methode.getMethod().getParameterCount()]);
        AuthorizationResult decision = decideur.authorize(SecurityContextHolder.getContext()::getAuthentication, appel);
        if (decision != null && !decision.isGranted()) throw new AuthorizationDeniedException("Accès refusé.", decision);
        return true;
    }
}
