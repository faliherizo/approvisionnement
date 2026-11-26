package qc.levis.contrat.securite.infrastructure.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Inscrit l'autorisation anticipée en tête des intercepteurs, sur toute route servie par un contrôleur (SEC-32). */
@Configuration
public class ConfigurationAutorisationAnticipee implements WebMvcConfigurer {

    private final AutorisationAnticipee autorisation;

    public ConfigurationAutorisationAnticipee(AutorisationAnticipee autorisation) {
        this.autorisation = autorisation;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registre) {
        registre.addInterceptor(autorisation).order(Ordered.HIGHEST_PRECEDENCE);
    }
}
