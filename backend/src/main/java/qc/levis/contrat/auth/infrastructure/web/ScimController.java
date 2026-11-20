package qc.levis.contrat.auth.infrastructure.web;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.auth.application.ScimService;
import qc.levis.contrat.auth.application.dto.ScimGroupe;
import qc.levis.contrat.auth.application.dto.ScimGroupeVue;
import qc.levis.contrat.auth.application.dto.ScimUtilisateur;
import qc.levis.contrat.auth.application.dto.ScimUtilisateurVue;

/**
 * Point SCIM 2.0 (RFC 7643, 7644) pour le provisionnement par Saviynt ou Microsoft Entra ID (addenda 3, Q51). Le jeton
 * porteur désigne l'organisation : aucune session, aucun rôle d'utilisateur. Le contrôleur traduit le protocole ; le
 * service applique les règles.
 */
@RestController
@RequestMapping(value = "/scim/v2", produces = {ScimController.SCIM_JSON, MediaType.APPLICATION_JSON_VALUE})
public class ScimController {

    static final String SCIM_JSON = "application/scim+json";
    private static final String SCHEMA_USER = "urn:ietf:params:scim:schemas:core:2.0:User";
    private static final String SCHEMA_GROUP = "urn:ietf:params:scim:schemas:core:2.0:Group";
    private static final String SCHEMA_LISTE = "urn:ietf:params:scim:api:messages:2.0:ListResponse";
    private static final String SCHEMA_ERREUR = "urn:ietf:params:scim:api:messages:2.0:Error";
    private static final Pattern FILTRE = Pattern.compile("^\\s*(userName|displayName)\\s+eq\\s+\"([^\"]*)\"\\s*$", Pattern.CASE_INSENSITIVE);
    private static final String EXTENSION_ENTREPRISE = "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";
    /** Attributs de compte retenus comme faits d'annuaire (addenda 4, Q31) : du schéma de base et de l'extension entreprise. */
    private static final List<String> ATTRIBUTS_BASE = List.of("title", "userType", "preferredLanguage");
    private static final List<String> ATTRIBUTS_ENTREPRISE = List.of("department", "division", "organization", "costCenter", "employeeNumber");
    private static final Pattern MEMBRE_CIBLE = Pattern.compile("members\\[\\s*value\\s+eq\\s+\"([^\"]+)\"\\s*]", Pattern.CASE_INSENSITIVE);

    /** Jeton absent, inconnu ou révoqué. */
    static final class NonAutorise extends RuntimeException {
        NonAutorise() {
            super("Jeton SCIM invalide.");
        }
    }

    private final ScimService scim;

    public ScimController(ScimService scim) {
        this.scim = scim;
    }

    // --- Découverte ---

    @GetMapping("/ServiceProviderConfig")
    public Map<String, Object> configuration(@RequestHeader(value = "Authorization", required = false) String auth) {
        organisation(auth);
        return Map.of("schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"),
                "patch", Map.of("supported", true), "bulk", Map.of("supported", false, "maxOperations", 0, "maxPayloadSize", 0),
                "filter", Map.of("supported", true, "maxResults", 200), "changePassword", Map.of("supported", false),
                "sort", Map.of("supported", false), "etag", Map.of("supported", false),
                "authenticationSchemes", List.of(Map.of("type", "oauthbearertoken", "name", "Jeton porteur",
                        "description", "Jeton SCIM de l'organisation, créé dans l'administration")));
    }

    @GetMapping("/ResourceTypes")
    public Map<String, Object> typesDeRessources(@RequestHeader(value = "Authorization", required = false) String auth) {
        organisation(auth);
        return liste(List.of(
                Map.of("id", "User", "name", "User", "endpoint", "/Users", "schema", SCHEMA_USER),
                Map.of("id", "Group", "name", "Group", "endpoint", "/Groups", "schema", SCHEMA_GROUP)), 1, 2);
    }

    // --- Utilisateurs ---

    @GetMapping("/Users")
    public Map<String, Object> utilisateurs(@RequestHeader(value = "Authorization", required = false) String auth,
                                            @RequestParam(required = false) String filter,
                                            @RequestParam(defaultValue = "1") int startIndex, @RequestParam(defaultValue = "100") int count) {
        Long tenant = organisation(auth);
        List<Map<String, Object>> tous = scim.utilisateurs(tenant, valeurFiltre(filter, "userName")).stream().map(ScimController::utilisateur).toList();
        return page(tous, startIndex, count);
    }

    @PostMapping("/Users")
    public ResponseEntity<Map<String, Object>> creerUtilisateur(@RequestHeader(value = "Authorization", required = false) String auth,
                                                                @RequestBody JsonNode corps) {
        Long tenant = organisation(auth);
        ScimUtilisateurVue u = scim.creerUtilisateur(tenant, new ScimUtilisateur(texte(corps, "userName"), nom(corps), booleen(corps.get("active")),
                attributs(corps)));
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateur(u));
    }

    @GetMapping("/Users/{id}")
    public Map<String, Object> utilisateur(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String id) {
        return utilisateur(scim.utilisateur(organisation(auth), id));
    }

    @PutMapping("/Users/{id}")
    public Map<String, Object> remplacerUtilisateur(@RequestHeader(value = "Authorization", required = false) String auth,
                                                    @PathVariable String id, @RequestBody JsonNode corps) {
        Long tenant = organisation(auth);
        return utilisateur(scim.modifierUtilisateur(tenant, id, new ScimUtilisateur(null, nom(corps), booleen(corps.get("active")),
                attributs(corps))));
    }

    @PatchMapping("/Users/{id}")
    public Map<String, Object> modifierUtilisateur(@RequestHeader(value = "Authorization", required = false) String auth,
                                                   @PathVariable String id, @RequestBody JsonNode corps) {
        Long tenant = organisation(auth);
        String nom = null;
        Boolean actif = null;
        Map<String, String> attributs = new LinkedHashMap<>();
        for (JsonNode op : operations(corps)) {
            String chemin = texte(op, "path");
            JsonNode valeur = op.get("value");
            String attribut = chemin == null ? null : attributCible(chemin);
            if (chemin == null && valeur != null && valeur.isObject()) {
                actif = valeur.has("active") ? booleen(valeur.get("active")) : actif;
                nom = nom(valeur) == null ? nom : nom(valeur);
                java.util.Optional.ofNullable(attributs(valeur)).ifPresent(attributs::putAll);
            } else if (attribut != null) {
                attributs.put(attribut, valeur == null || valeur.isNull() || "remove".equalsIgnoreCase(texte(op, "op")) ? "" : valeur.asText());
            } else if ("active".equalsIgnoreCase(chemin)) {
                actif = booleen(valeur);
            } else if (chemin != null && (chemin.equalsIgnoreCase("displayName") || chemin.equalsIgnoreCase("name.formatted"))) {
                nom = valeur == null ? null : valeur.asText();
            }
        }
        return utilisateur(scim.modifierUtilisateur(tenant, id, new ScimUtilisateur(null, nom, actif,
                attributs.isEmpty() ? null : fusion(scim.attributs(tenant, id), attributs))));
    }

    @DeleteMapping("/Users/{id}")
    public ResponseEntity<Void> supprimerUtilisateur(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String id) {
        scim.desactiverUtilisateur(organisation(auth), id);
        return ResponseEntity.noContent().build();
    }

    // --- Groupes ---

    @GetMapping("/Groups")
    public Map<String, Object> groupes(@RequestHeader(value = "Authorization", required = false) String auth,
                                       @RequestParam(required = false) String filter,
                                       @RequestParam(defaultValue = "1") int startIndex, @RequestParam(defaultValue = "100") int count) {
        Long tenant = organisation(auth);
        List<Map<String, Object>> tous = scim.groupes(tenant, valeurFiltre(filter, "displayName")).stream().map(ScimController::groupe).toList();
        return page(tous, startIndex, count);
    }

    @PostMapping("/Groups")
    public ResponseEntity<Map<String, Object>> creerGroupe(@RequestHeader(value = "Authorization", required = false) String auth,
                                                           @RequestBody JsonNode corps) {
        Long tenant = organisation(auth);
        ScimGroupeVue g = scim.creerGroupe(tenant, new ScimGroupe(texte(corps, "displayName"), texte(corps, "externalId"), membres(corps.get("members"))));
        return ResponseEntity.status(HttpStatus.CREATED).body(groupe(g));
    }

    @GetMapping("/Groups/{id}")
    public Map<String, Object> groupe(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String id) {
        return groupe(scim.groupe(organisation(auth), id));
    }

    @PutMapping("/Groups/{id}")
    public Map<String, Object> remplacerGroupe(@RequestHeader(value = "Authorization", required = false) String auth,
                                               @PathVariable String id, @RequestBody JsonNode corps) {
        Long tenant = organisation(auth);
        return groupe(scim.remplacerGroupe(tenant, id, new ScimGroupe(texte(corps, "displayName"), texte(corps, "externalId"),
                membres(corps.get("members")))));
    }

    @PatchMapping("/Groups/{id}")
    public Map<String, Object> modifierGroupe(@RequestHeader(value = "Authorization", required = false) String auth,
                                              @PathVariable String id, @RequestBody JsonNode corps) {
        Long tenant = organisation(auth);
        List<String> ajouts = new ArrayList<>();
        List<String> retraits = new ArrayList<>();
        String nom = null;
        for (JsonNode op : operations(corps)) {
            String type = texte(op, "op") == null ? "" : texte(op, "op").toLowerCase(Locale.ROOT);
            String chemin = texte(op, "path");
            JsonNode valeur = op.get("value");
            Matcher cible = chemin == null ? null : MEMBRE_CIBLE.matcher(chemin);
            if (cible != null && cible.matches() && "remove".equals(type)) {
                retraits.add(cible.group(1));
            } else if ("members".equalsIgnoreCase(chemin) && "add".equals(type)) {
                ajouts.addAll(membres(valeur));
            } else if ("members".equalsIgnoreCase(chemin) && "remove".equals(type)) {
                retraits.addAll(membres(valeur));
            } else if ("displayName".equalsIgnoreCase(chemin)) {
                nom = valeur == null ? null : valeur.asText();
            } else if (chemin == null && valeur != null && valeur.has("displayName")) {
                nom = valeur.get("displayName").asText();
            }
        }
        return groupe(scim.modifierMembres(tenant, id, ajouts, retraits, nom));
    }

    @DeleteMapping("/Groups/{id}")
    public ResponseEntity<Void> supprimerGroupe(@RequestHeader(value = "Authorization", required = false) String auth, @PathVariable String id) {
        scim.supprimerGroupe(organisation(auth), id);
        return ResponseEntity.noContent().build();
    }

    // --- Erreurs au format SCIM ---

    @ExceptionHandler(NonAutorise.class)
    public ResponseEntity<Map<String, Object>> nonAutorise(NonAutorise e) {
        return erreur(HttpStatus.UNAUTHORIZED, e.getMessage(), null);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> introuvable(NoSuchElementException e) {
        return erreur(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(ScimService.Conflit.class)
    public ResponseEntity<Map<String, Object>> conflit(ScimService.Conflit e) {
        return erreur(HttpStatus.CONFLICT, e.getMessage(), "uniqueness");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> invalide(IllegalArgumentException e) {
        return erreur(HttpStatus.BAD_REQUEST, e.getMessage(), "invalidValue");
    }

    private static ResponseEntity<Map<String, Object>> erreur(HttpStatus statut, String detail, String type) {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("schemas", List.of(SCHEMA_ERREUR));
        corps.put("status", String.valueOf(statut.value()));
        corps.put("detail", detail);
        if (type != null) {
            corps.put("scimType", type);
        }
        return ResponseEntity.status(statut).contentType(MediaType.parseMediaType(SCIM_JSON)).body(corps);
    }

    // --- Traduction du protocole ---

    private Long organisation(String auth) {
        String jeton = auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7) ? auth.substring(7).strip() : null;
        return scim.organisation(jeton).orElseThrow(NonAutorise::new);
    }

    static String valeurFiltre(String filtre, String attribut) {
        if (filtre == null || filtre.isBlank()) {
            return null;
        }
        Matcher m = FILTRE.matcher(filtre);
        if (!m.matches() || !m.group(1).equalsIgnoreCase(attribut)) {
            throw new IllegalArgumentException("Filtre non pris en charge : seul « " + attribut + " eq \"…\" » l'est.");
        }
        return m.group(2);
    }

    private static Map<String, Object> page(List<Map<String, Object>> tous, int startIndex, int count) {
        int debut = Math.max(startIndex, 1) - 1;
        int taille = Math.max(0, Math.min(count, 200));
        List<Map<String, Object>> page = tous.subList(Math.min(debut, tous.size()), Math.min(debut + taille, tous.size()));
        Map<String, Object> r = liste(page, debut + 1, tous.size());
        return r;
    }

    private static Map<String, Object> liste(List<? extends Map<String, Object>> ressources, int startIndex, int total) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("schemas", List.of(SCHEMA_LISTE));
        r.put("totalResults", total);
        r.put("startIndex", startIndex);
        r.put("itemsPerPage", ressources.size());
        r.put("Resources", ressources);
        return r;
    }

    private static Map<String, Object> utilisateur(ScimUtilisateurVue u) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("schemas", List.of(SCHEMA_USER));
        r.put("id", u.id());
        r.put("userName", u.userName());
        r.put("name", Map.of("formatted", u.nom()));
        r.put("displayName", u.nom());
        r.put("active", u.actif());
        r.put("emails", List.of(Map.of("value", u.userName(), "primary", true, "type", "work")));
        r.put("groups", u.groupes().stream().map(g -> Map.of("value", g)).toList());
        r.put("meta", Map.of("resourceType", "User", "location", "/scim/v2/Users/" + u.id()));
        return r;
    }

    private static Map<String, Object> groupe(ScimGroupeVue g) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("schemas", List.of(SCHEMA_GROUP));
        r.put("id", g.id());
        r.put("displayName", g.nom());
        if (g.idExterne() != null) {
            r.put("externalId", g.idExterne());
        }
        r.put("members", g.membres().stream().map(m -> Map.of("value", m)).toList());
        r.put("meta", Map.of("resourceType", "Group", "location", "/scim/v2/Groups/" + g.id()));
        return r;
    }

    private static List<JsonNode> operations(JsonNode corps) {
        List<JsonNode> ops = new ArrayList<>();
        JsonNode liste = corps == null ? null : corps.get("Operations");
        if (liste != null && liste.isArray()) {
            liste.forEach(ops::add);
        }
        return ops;
    }

    private static List<String> membres(JsonNode valeur) {
        if (valeur == null || valeur.isNull()) {
            return null;
        }
        List<String> ids = new ArrayList<>();
        if (valeur.isArray()) {
            valeur.forEach(m -> ids.add(m.isObject() ? m.path("value").asText() : m.asText()));
        }
        return ids;
    }

    /** Attributs retenus d'une ressource User ; nul si elle n'en porte aucun (rien à changer). */
    static Map<String, String> attributs(JsonNode corps) {
        Map<String, String> m = new LinkedHashMap<>();
        ATTRIBUTS_BASE.stream().filter(a -> corps.hasNonNull(a)).forEach(a -> m.put(a, corps.get(a).asText()));
        JsonNode ext = corps.get(EXTENSION_ENTREPRISE);
        if (ext != null && ext.isObject()) {
            ATTRIBUTS_ENTREPRISE.stream().filter(ext::hasNonNull).forEach(a -> m.put(a, ext.get(a).asText()));
        }
        return m.isEmpty() ? null : m;
    }

    /** Attribut visé par un chemin PATCH : « title », « department » ou « urn:…:enterprise:2.0:User:department ». */
    static String attributCible(String chemin) {
        String nom = chemin.startsWith(EXTENSION_ENTREPRISE + ":") ? chemin.substring(EXTENSION_ENTREPRISE.length() + 1) : chemin;
        return ATTRIBUTS_BASE.contains(nom) || ATTRIBUTS_ENTREPRISE.contains(nom) ? nom : null;
    }

    /** Un PATCH ne pousse que les attributs modifiés : ils complètent ceux déjà connus ; une valeur vide en retire un. */
    private static Map<String, String> fusion(Map<String, String> connus, Map<String, String> modifies) {
        Map<String, String> m = new LinkedHashMap<>(connus);
        m.putAll(modifies);
        m.values().removeIf(String::isBlank);
        return m;
    }

    /** Nom complet : name.formatted, sinon prénom et nom, sinon displayName. */
    private static String nom(JsonNode corps) {
        JsonNode name = corps.get("name");
        if (name != null && name.hasNonNull("formatted")) {
            return name.get("formatted").asText();
        }
        if (name != null && (name.hasNonNull("givenName") || name.hasNonNull("familyName"))) {
            return (name.path("givenName").asText("") + " " + name.path("familyName").asText("")).strip();
        }
        return texte(corps, "displayName");
    }

    /** Booléen SCIM ; Entra ID envoie parfois « True » ou « False » en texte. */
    static Boolean booleen(JsonNode v) {
        if (v == null || v.isNull()) {
            return null;
        }
        return v.isBoolean() ? v.asBoolean() : Boolean.valueOf(v.asText().strip().equalsIgnoreCase("true"));
    }

    private static String texte(JsonNode n, String champ) {
        JsonNode v = n == null ? null : n.get(champ);
        return v == null || v.isNull() ? null : v.asText();
    }
}
