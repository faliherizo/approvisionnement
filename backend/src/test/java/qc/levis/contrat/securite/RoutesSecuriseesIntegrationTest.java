package qc.levis.contrat.securite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.util.SimpleMethodInvocation;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;
import qc.levis.contrat.auth.application.JwtService;
import qc.levis.contrat.commun.domain.Enums.Role;
import qc.levis.contrat.securite.CartographieDesRoutes.Motif;
import qc.levis.contrat.securite.CartographieDesRoutes.Route;

/**
 * Aucune route n'échappe au contrôle d'accès (refus par défaut). L'inventaire est celui de Spring MVC lui-même, pas la
 * documentation : une route oubliée dans l'OpenAPI, sans règle d'autorisation explicite ou ouverte par erreur fait
 * échouer la construction. La matrice rôle × opération est DÉRIVÉE des annotations {@code @PreAuthorize} par le moteur
 * d'autorisation de l'application, comparée à la matrice revue ({@code securite/donnees/matrice-roles.json}), puis
 * éprouvée en HTTP pour chaque rôle refusé.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RoutesSecuriseesIntegrationTest {

    /** Régénère la matrice revue : {@code mvn test -Dtest=RoutesSecuriseesIntegrationTest -Dsecurite.matrice.regenerer=true}. */
    private static final boolean REGENERER = Boolean.getBoolean("securite.matrice.regenerer");
    private static final Set<String> LECTURES = Set.of("GET");
    private static final List<String> METHODES = List.of("GET", "POST", "PUT", "PATCH", "DELETE");

    @Autowired @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping correspondances;
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired MethodSecurityExpressionHandler expressions;
    @Autowired ApplicationContext contexte;
    @Autowired JwtService jetons;

    List<Route> routes;
    List<Motif> publiques;
    List<Motif> authentifiees;

    @BeforeEach
    void inventaire() throws Exception {
        routes = CartographieDesRoutes.routes(correspondances);
        publiques = CartographieDesRoutes.motifs(CartographieDesRoutes.registre(json, "surface-publique.json"), "operations");
        authentifiees = CartographieDesRoutes.motifs(CartographieDesRoutes.registre(json, "authentification-seule.json"), "operations");
    }

    private boolean publique(Route r) { return CartographieDesRoutes.premier(publiques, r).isPresent(); }

    @Test
    void chaque_route_reelle_figure_dans_la_specification_openapi_et_reciproquement() throws Exception {
        JsonNode spec = json.readTree(mvc.perform(MockMvcRequestBuilders.get("/v3/api-docs")).andReturn().getResponse().getContentAsString());
        Set<String> documentees = new TreeSet<>();
        spec.get("paths").properties().forEach(p -> p.getValue().properties().forEach(m -> {
            if (METHODES.contains(m.getKey().toUpperCase())) documentees.add(m.getKey().toUpperCase() + " " + p.getKey().replaceAll("\\{[^}]+}", "{}"));
        }));
        Set<String> reelles = routes.stream().map(Route::forme).collect(Collectors.toCollection(TreeSet::new));
        Set<String> nonDocumentees = new TreeSet<>(reelles);
        nonDocumentees.removeAll(documentees);
        Set<String> fantomes = new TreeSet<>(documentees);
        fantomes.removeAll(reelles);
        assertEquals(Set.of(), nonDocumentees, "routes servies mais absentes de l'OpenAPI : la suite d'intrusion ne les verrait pas");
        assertEquals(Set.of(), fantomes, "opérations documentées qu'aucune route ne sert");
        assertTrue(routes.size() > 400, "inventaire anormalement court : " + routes.size());
    }

    @Test
    void chaque_route_declare_sa_methode_http_et_une_regle_d_acces_explicite() {
        Map<String, List<String>> matrice = matrice();
        List<String> ecarts = new ArrayList<>();
        for (Route r : routes) {
            if ("*".equals(r.methode())) ecarts.add(r.cle() + " : méthode HTTP non déclarée (toutes acceptées)");
            r.expression().filter(e -> e.contains("permitAll") || e.contains("isAnonymous")).ifPresent(e -> ecarts.add(r.cle() + " : règle ouverte " + e));
            if (publique(r)) continue;
            // Ouverte à tous les rôles (aucune annotation, ou seulement la souscription du module) : le contrôle fin doit être motivé.
            if (ouverteATous(matrice.get(r.cle())) && !declaree(r)) ecarts.add(r.cle() + " : ouverte à tout rôle sans contrôle fin motivé (authentification-seule.json)");
        }
        for (List<Motif> registre : List.of(publiques, authentifiees)) {
            for (Motif m : registre) if (routes.stream().noneMatch(m::couvre)) ecarts.add("motif sans route : " + m.motif());
            for (Motif m : registre) if (m.entree().path("raison").asText().isBlank()) ecarts.add("motif sans raison : " + m.motif());
        }
        for (Motif m : publiques) if (!Set.of("jeton", "ouvert").contains(m.entree().path("controle").asText())) ecarts.add("motif public sans contrôle (jeton|ouvert) : " + m.motif());
        for (Motif m : authentifiees) if (m.entree().path("controle").asText().isBlank() || m.entree().path("test").asText().isBlank()) ecarts.add("motif sans contrôle ni test : " + m.motif());
        assertEquals(List.of(), ecarts);
    }

    private boolean declaree(Route r) { return CartographieDesRoutes.premier(authentifiees, r).isPresent(); }

    private static boolean ouverteATous(List<String> permis) {
        return permis != null && (permis.contains("*") || permis.size() == Role.values().length);
    }

    @Test
    void sans_session_chaque_route_non_publique_repond_401_et_aucune_route_a_jeton_ne_s_ouvre_sans_jeton() throws Exception {
        List<String> ecarts = new ArrayList<>();
        int eprouvees = 0;
        for (Route r : routes) {
            int statut = appeler(r, r.url("1"), null);
            eprouvees++;
            var entree = CartographieDesRoutes.premier(publiques, r);
            if (entree.isEmpty() && statut != 401) ecarts.add(r.cle() + " → " + statut + " (401 attendu)");
            if (entree.isPresent() && statut >= 500) ecarts.add(r.cle() + " → " + statut + " (erreur interne sans session)");
            boolean aJeton = entree.map(e -> "jeton".equals(e.entree().path("controle").asText())).orElse(false);
            if (aJeton && (statut < 400)) ecarts.add(r.cle() + " → " + statut + " sans jeton (refus attendu)");
        }
        assertEquals(routes.size(), eprouvees);
        long nPubliques = routes.stream().filter(this::publique).count();
        System.out.printf("[securite] routes réelles : %d, publiques : %d, avec session : %d%n", routes.size(), nPubliques, routes.size() - nPubliques);
        assertEquals(List.of(), ecarts);
    }

    @Test
    void la_matrice_roles_operations_derivee_des_annotations_est_celle_qui_a_ete_revue() throws Exception {
        Map<String, List<String>> matrice = matrice();
        ObjectNode document = json.createObjectNode();
        document.put("description", "Matrice rôle × opération DÉRIVÉE des @PreAuthorize par le moteur d'autorisation de l'application "
                + "(RoutesSecuriseesIntegrationTest), modules supposés souscrits. « * » : toute personne authentifiée (contrôle fin dans le "
                + "service, voir authentification-seule.json). Toute modification d'autorisation change ce fichier et passe en revue.");
        document.put("operations", matrice.size());
        document.set("matrice", json.valueToTree(matrice));
        Path fichier = Path.of("..", "securite", "donnees", "matrice-roles.json");
        String attendu = json.writerWithDefaultPrettyPrinter().writeValueAsString(document) + "\n";
        if (REGENERER) Files.writeString(fichier, attendu);
        assertEquals(attendu, Files.readString(fichier), "matrice d'autorisation modifiée : la régénérer (-Dsecurite.matrice.regenerer=true) et la faire revoir");

        List<String> ecarts = new ArrayList<>();
        Set<String> permisLecteur = new TreeSet<>();
        CartographieDesRoutes.motifs(CartographieDesRoutes.registre(json, "roles.json"), "permisAuLecteur")
                .forEach(m -> routes.stream().filter(m::couvre).forEach(r -> permisLecteur.add(r.cle())));
        for (Route r : routes) {
            List<String> permis = matrice.get(r.cle());
            if (permis == null) continue;
            if (r.chemin().startsWith("/api/superadmin/") && !permis.equals(List.of("SUPERADMIN"))) ecarts.add(r.cle() + " ouverte à " + permis);
            boolean ecriture = !LECTURES.contains(r.methode());
            boolean lecteur = permis.contains("*") || permis.contains("LECTEUR");
            if (ecriture && lecteur && !permisLecteur.contains(r.cle()) && !declaree(r)) ecarts.add(r.cle() + " : modification permise au LECTEUR sans motif");
            boolean auditeur = permis.contains("*") || permis.contains("AUDITEUR");
            if (ecriture && auditeur && !declaree(r)) ecarts.add(r.cle() + " : modification permise à l'AUDITEUR (lecture seule) sans contrôle fin motivé");
        }
        assertEquals(List.of(), ecarts);
    }

    @Test
    void chaque_role_non_autorise_est_refuse_par_le_serveur() throws Exception {
        Map<String, List<String>> matrice = matrice();
        Map<Role, String> jetonParRole = new LinkedHashMap<>();
        // Organisation nulle : tous les modules sont réputés souscrits ; seul le rôle peut donc motiver le refus.
        for (Role role : Role.values()) jetonParRole.put(role, jetons.genererToken(role.name().toLowerCase() + "@matrice.example", List.of(role.name()), null));
        List<String> ecarts = new ArrayList<>();
        int refus403 = 0;
        int essais = 0;
        for (Route r : routes) {
            List<String> permis = matrice.get(r.cle());
            if (permis == null || permis.contains("*")) continue;
            for (Role role : Role.values()) {
                if (permis.contains(role.name())) continue;
                int statut = appeler(r, r.url("1"), jetonParRole.get(role));
                essais++;
                if (statut == 403) refus403++;
                else ecarts.add(r.cle() + " [" + role + "] → " + statut);
            }
        }
        assertTrue(essais > 1000, "trop peu d'essais : " + essais);
        System.out.printf("[securite] couples rôle refusé × opération : %d, refusés 403 : %d%n", essais, refus403);
        assertEquals(List.of(), ecarts, "rôles refusés par la matrice mais non refusés (403) par le serveur");
        assertEquals(essais, refus403);
    }

    @Test
    void une_methode_http_non_prevue_est_refusee() throws Exception {
        String jeton = jetons.genererToken("superadmin@matrice.example", List.of(Role.SUPERADMIN.name()), null);
        PathPatternParser analyseur = new PathPatternParser();
        Map<String, Set<String>> methodesParChemin = new TreeMap<>();
        for (Route r : routes) methodesParChemin.computeIfAbsent(r.chemin(), c -> new TreeSet<>()).add(r.methode());
        List<String> ecarts = new ArrayList<>();
        int essais = 0;
        for (var e : methodesParChemin.entrySet()) {
            Route modele = routes.stream().filter(r -> r.chemin().equals(e.getKey())).findFirst().orElseThrow();
            String url = modele.url("1");
            for (String m : METHODES) {
                if (e.getValue().contains(m)) continue;
                // Une autre route (ex. /x/recherche face à /x/{id}) peut légitimement servir cette méthode à cette adresse.
                boolean servie = routes.stream().anyMatch(r -> r.methode().equals(m) && analyseur.parse(r.chemin().replace("/**", "/{*reste}")).matches(
                        org.springframework.http.server.PathContainer.parsePath(url)));
                if (servie) continue;
                essais++;
                int statut = appeler(new Route(m, e.getKey(), modele.gestionnaire()), url, jeton);
                if (statut != 405) ecarts.add(m + " " + e.getKey() + " → " + statut);
            }
        }
        assertTrue(essais > 500, "trop peu d'essais : " + essais);
        System.out.printf("[securite] méthodes HTTP non prévues éprouvées : %d%n", essais);
        assertEquals(List.of(), ecarts, "méthodes HTTP non prévues qui ne répondent pas 405");
    }

    /** Décision du moteur d'autorisation de l'application pour chaque rôle, sans invoquer la méthode. */
    private Map<String, List<String>> matrice() {
        PreAuthorizeAuthorizationManager decideur = new PreAuthorizeAuthorizationManager();
        decideur.setExpressionHandler(expressions);
        Map<String, List<String>> matrice = new TreeMap<>();
        for (Route r : routes) {
            if (publique(r)) continue;
            if (r.expression().isEmpty()) { matrice.put(r.cle(), List.of("*")); continue; }
            Object cible = contexte.getBean(r.gestionnaire().getBeanType());
            List<String> permis = new ArrayList<>();
            for (Role role : Role.values()) {
                Authentication a = new UsernamePasswordAuthenticationToken(role.name(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
                MethodInvocation appel = new SimpleMethodInvocation(cible, r.gestionnaire().getMethod(), new Object[r.gestionnaire().getMethod().getParameterCount()]);
                AuthorizationResult decision = decideur.authorize(() -> a, appel);
                if (decision != null && decision.isGranted()) permis.add(role.name());
            }
            matrice.put(r.cle(), permis);
        }
        return matrice;
    }

    private int appeler(Route r, String url, String jeton) throws Exception {
        var mapping = org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation(r.gestionnaire().getMethod(),
                org.springframework.web.bind.annotation.RequestMapping.class);
        boolean fichiers = mapping != null && Arrays.asList(mapping.consumes()).contains(MediaType.MULTIPART_FORM_DATA_VALUE);
        if (fichiers) {
            var envoi = MockMvcRequestBuilders.multipart(HttpMethod.valueOf(r.methode()), url);
            if (jeton != null) envoi.header("Authorization", "Bearer " + jeton);
            return mvc.perform(envoi).andReturn().getResponse().getStatus();
        }
        MockHttpServletRequestBuilder req = MockMvcRequestBuilders.request(HttpMethod.valueOf(r.methode()), url);
        if (jeton != null) req = req.header("Authorization", "Bearer " + jeton);
        if (mapping != null && mapping.consumes().length > 0) req = req.contentType(mapping.consumes()[0]).content("x");
        else if (!LECTURES.contains(r.methode()) && !"DELETE".equals(r.methode())) req = req.contentType(MediaType.APPLICATION_JSON).content("{}");
        return mvc.perform(req).andReturn().getResponse().getStatus();
    }
}
