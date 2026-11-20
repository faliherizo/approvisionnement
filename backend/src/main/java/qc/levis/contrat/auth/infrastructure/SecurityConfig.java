package qc.levis.contrat.auth.infrastructure;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import qc.levis.contrat.auth.infrastructure.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Séparation des administrations (addenda 4, Q10) : {@code ADMIN} cumule l'administration d'affaires et l'administration
     * technique ; chacune des deux, attribuée seule, n'ouvre que son périmètre (moindre privilège).
     */
    @Bean
    static org.springframework.security.access.hierarchicalroles.RoleHierarchy hierarchieDesRoles() {
        org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl h =
                new org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl();
        h.setHierarchy("ROLE_ADMIN > ROLE_ADMIN_AFFAIRES\nROLE_ADMIN > ROLE_ADMIN_TECHNIQUE");
        return h;
    }

    @Bean
    static org.springframework.security.access.expression.method.MethodSecurityExpressionHandler gestionnaireExpressions(
            org.springframework.security.access.hierarchicalroles.RoleHierarchy hierarchie,
            org.springframework.context.ApplicationContext contexte) {
        org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler g =
                new org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler();
        g.setRoleHierarchy(hierarchie);
        g.setApplicationContext(contexte);
        return g;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Le renvoi interne vers /error ne doit pas exiger d'authentification : sinon une erreur serveur
                // levée après le contrôleur (ex. à la validation de la transaction) apparaît comme un 403 vide.
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/logout", "/api/auth/config").permitAll()
                .requestMatchers("/api/auth/oidc/**").permitAll() // flux OIDC (authorization code + PKCE)
                .requestMatchers("/api/auth/saml/**").permitAll() // flux SAML 2.0 SP-initiated
                .requestMatchers("/api/signature/**").permitAll() // signature externe par jeton
                .requestMatchers("/api/clm/portail/**").permitAll() // portail CLM du signataire (jeton + vérification d'identité)
                .requestMatchers("/api/clm/integration/**").permitAll() // API d'intégration : jeton client credentials vérifié par le contrôleur
                .requestMatchers("/scim/v2/**").permitAll() // SCIM 2.0 (addenda 3, Q51) : jeton porteur de l'organisation vérifié par le contrôleur
                .requestMatchers(HttpMethod.GET, "/api/tenants", "/api/tenants/*/public").permitAll() // sélecteur & titre de connexion
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/public/**").permitAll() // API publique tokenisée (auth par jeton d'API/OAuth)
                .anyRequest().authenticated()
            )
            .headers(h -> h.frameOptions(f -> f.disable())) // H2 console (dév)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // allowedOriginPatterns accepte des origines exactes ET des motifs
        // (ex. http://localhost:*), compatibles avec allowCredentials=true.
        cfg.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        // Le retour SAML (ACS) est une navigation de page postée par l'IdP, souvent avec « Origin: null » ou l'origine de
        // l'IdP : CORS ne la protège pas et la refuserait (403) avant la vérification de l'assertion signée, qui fait foi.
        return request -> SAML_ACS.equals(request.getRequestURI()) ? null : src.getCorsConfiguration(request);
    }

    static final String SAML_ACS = "/api/auth/saml/callback";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
