package qc.levis.contrat.commun.module.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.application.ModuleAccess;

/**
 * Applique {@link ModuleRequis} à toute l'API : refus par défaut d'une fonctionnalité que l'organisation n'a pas souscrite,
 * quel que soit le rôle de l'appelant. Le superadmin et les appels sans session (jetons publics) passent : leur tenant est nul
 * et ils relèvent d'autres contrôles.
 */
@Component
public class GardeModules implements HandlerInterceptor, WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(GardeModules.class);

    private final ModuleAccess modules;
    private final ObjectMapper json;

    public GardeModules(ModuleAccess modules, ObjectMapper json) {
        this.modules = modules;
        this.json = json;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registre) {
        registre.addInterceptor(this).addPathPatterns("/api/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest requete, HttpServletResponse reponse, Object cible) throws IOException {
        if (!(cible instanceof HandlerMethod methode)) {
            return true;
        }
        ModuleRequis requis = AnnotatedElementUtils.findMergedAnnotation(methode.getBeanType(), ModuleRequis.class);
        if (requis == null || Arrays.stream(requis.value()).anyMatch(modules::actif)) {
            return true;
        }
        String codes = String.join(" » ou « ", requis.value());
        log.warn("Accès refusé : module « {} » inactif pour le tenant {} ({}.{})", codes, TenantContext.get(),
                methode.getBeanType().getSimpleName(), methode.getMethod().getName());
        reponse.setStatus(HttpStatus.FORBIDDEN.value());
        reponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        reponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        json.writeValue(reponse.getWriter(), Map.of("message",
                "Fonctionnalité non souscrite par votre organisation (module « " + codes + " »)."));
        return false;
    }
}
