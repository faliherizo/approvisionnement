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
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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
        return org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl.fromHierarchy(
                "ROLE_ADMIN > ROLE_ADMIN_AFFAIRES\nROLE_ADMIN > ROLE_ADMIN_TECHNIQUE");
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
                // Administration de la plateforme : refusée au niveau de l'URL, avant même la lecture des paramètres.
                .requestMatchers("/api/superadmin/**").hasRole("SUPERADMIN")
                .anyRequest().authenticated()
            )
            // Sans session (ou session expirée) : 401, que l'interface reconnaît pour renvoyer à la connexion.
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .headers(this::entetesDeSecurite)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * En-têtes de sécurité (OWASP A05) : aucun encadrement (clic piégé), aucun reniflage de type, aucune fuite de
     * l'adresse d'origine, aucune mise en cache des réponses. L'API ne sert que des données : sa politique de contenu
     * n'autorise rien. La console H2 (poste de développement, fermée par défaut) garde l'encadrement par la même origine.
     */
    private void entetesDeSecurite(org.springframework.security.config.annotation.web.configurers.HeadersConfigurer<HttpSecurity> h) {
        // Console H2 et Swagger UI s'affichent dans un cadre de l'application même (même origine) ; le reste, jamais.
        var encadrables = new org.springframework.security.web.util.matcher.OrRequestMatcher(
                org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.withDefaults().matcher("/h2-console/**"),
                org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui/**"));
        h.frameOptions(f -> f.disable())
         .addHeaderWriter(new org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter(
                 new org.springframework.security.web.util.matcher.NegatedRequestMatcher(encadrables),
                 new org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter(
                         org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode.DENY)))
         .addHeaderWriter(new org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter(encadrables,
                 new org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter(
                         org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
         .addHeaderWriter(new org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter(
                 new org.springframework.security.web.util.matcher.OrRequestMatcher(
                         org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.withDefaults().matcher("/api/**"),
                         org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.withDefaults().matcher("/scim/**")),
                 new org.springframework.security.web.header.writers.StaticHeadersWriter("Content-Security-Policy",
                         "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'")))
         // Isolation inter-origines (attaques de type Spectre) : les réponses de l'API ne se chargent que depuis l'application.
         .addHeaderWriter(new org.springframework.security.web.header.writers.StaticHeadersWriter("Cross-Origin-Resource-Policy", "same-origin"))
         .addHeaderWriter(new org.springframework.security.web.header.writers.StaticHeadersWriter("Cross-Origin-Opener-Policy", "same-origin"))
         .referrerPolicy(r -> r.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
         .addHeaderWriter(new org.springframework.security.web.header.writers.StaticHeadersWriter("Permissions-Policy",
                 "camera=(), microphone=(), geolocation=(), payment=()"));
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
