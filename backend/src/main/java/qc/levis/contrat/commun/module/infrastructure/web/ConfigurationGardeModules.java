package qc.levis.contrat.commun.module.infrastructure.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Inscrit la garde de module sur toute l'API. */
@Configuration
public class ConfigurationGardeModules implements WebMvcConfigurer {

    private final GardeModules garde;

    public ConfigurationGardeModules(GardeModules garde) {
        this.garde = garde;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registre) {
        registre.addInterceptor(garde).addPathPatterns("/api/**");
    }
}
