package qc.levis.contrat.securite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Inventaire RÉEL des routes de l'application, lu dans les correspondances de Spring MVC (et non dans la documentation) :
 * c'est la référence contre laquelle se vérifient la spécification OpenAPI, la surface publique déclarée et la règle
 * d'autorisation de chaque opération. Les registres partagés avec la suite d'intrusion vivent dans {@code securite/donnees/}.
 */
final class CartographieDesRoutes {

    static final String PAQUET_APPLICATION = "qc.levis.contrat.";
    private static final Pattern VARIABLE = Pattern.compile("\\{\\*?([^}:]+)(?::[^}]*)?}");

    /** Une opération : méthode HTTP, gabarit de chemin (variables sans contrainte) et méthode Java qui la sert. */
    record Route(String methode, String chemin, HandlerMethod gestionnaire) {
        String cle() { return methode + " " + chemin; }

        /** Gabarit sans nom de variable, pour comparer à la spécification OpenAPI. */
        String forme() { return methode + " " + chemin.replaceAll("\\{[^}]+}", "{}"); }

        /** Adresse concrète : chaque variable remplacée par une valeur neutre. */
        String url(String valeur) { return chemin.replaceAll("\\{[^}]+}", valeur).replace("/**", "/x"); }

        /** Règle d'autorisation effective : celle de la méthode, sinon celle de la classe (sémantique de Spring Security). */
        Optional<String> expression() {
            PreAuthorize m = AnnotatedElementUtils.findMergedAnnotation(gestionnaire.getMethod(), PreAuthorize.class);
            if (m != null) return Optional.of(m.value());
            PreAuthorize c = AnnotatedElementUtils.findMergedAnnotation(gestionnaire.getBeanType(), PreAuthorize.class);
            return Optional.ofNullable(c).map(PreAuthorize::value);
        }
    }

    /** Motif d'un registre partagé (surface publique, authentification seule) avec ses attributs. */
    record Motif(Pattern motif, JsonNode entree) {
        boolean couvre(Route r) { return motif.matcher(r.cle()).find(); }
    }

    private CartographieDesRoutes() {}

    static List<Route> routes(RequestMappingHandlerMapping correspondances) {
        List<Route> routes = new ArrayList<>();
        correspondances.getHandlerMethods().forEach((info, gestionnaire) -> {
            if (!gestionnaire.getBeanType().getName().startsWith(PAQUET_APPLICATION)) return;
            for (String gabarit : gabarits(info)) {
                String chemin = VARIABLE.matcher(gabarit).replaceAll("{$1}");
                if (info.getMethodsCondition().getMethods().isEmpty()) routes.add(new Route("*", chemin, gestionnaire));
                for (RequestMethod m : info.getMethodsCondition().getMethods()) routes.add(new Route(m.name(), chemin, gestionnaire));
            }
        });
        routes.sort(Comparator.comparing(Route::chemin).thenComparing(Route::methode));
        return routes;
    }

    private static java.util.Set<String> gabarits(RequestMappingInfo info) {
        return info.getPathPatternsCondition() != null ? info.getPathPatternsCondition().getPatternValues() : info.getPatternValues();
    }

    /** Fichier partagé avec la suite d'intrusion : la construction Maven s'exécute dans {@code backend/}. */
    static JsonNode registre(ObjectMapper json, String nom) throws IOException {
        Path fichier = Path.of("..", "securite", "donnees", nom);
        if (!Files.exists(fichier)) throw new IllegalStateException("Registre de sécurité introuvable : " + fichier.toAbsolutePath());
        return json.readTree(fichier.toFile());
    }

    static List<Motif> motifs(JsonNode registre, String champ) {
        List<Motif> motifs = new ArrayList<>();
        for (JsonNode e : registre.get(champ)) motifs.add(new Motif(Pattern.compile(e.get("motif").asText()), e));
        return motifs;
    }

    static Optional<Motif> premier(List<Motif> motifs, Route r) {
        return motifs.stream().filter(m -> m.couvre(r)).findFirst();
    }
}
