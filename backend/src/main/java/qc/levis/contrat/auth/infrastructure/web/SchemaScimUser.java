package qc.levis.contrat.auth.infrastructure.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Définition du schéma SCIM « User » (RFC 7643, § 7) telle que la plateforme la gère. Les attributs « roles » et
 * « entitlements » sont déclarés en lecture seule (RST-52) : ils se lisent pour découvrir les droits effectifs, mais
 * se modifient par les groupes d'annuaire et l'administration, jamais par SCIM.
 */
final class SchemaScimUser {

    static final String ID = "urn:ietf:params:scim:schemas:core:2.0:User";
    private static final String SCHEMA = "urn:ietf:params:scim:schemas:core:2.0:Schema";

    private SchemaScimUser() {
    }

    static Map<String, Object> schema() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("schemas", List.of(SCHEMA));
        s.put("id", ID);
        s.put("name", "User");
        s.put("description", "Compte de la plateforme");
        s.put("attributes", List.of(
                simple("userName", "readWrite", "server"),
                simple("displayName", "readWrite", "none"),
                booleen("active"),
                multiple("groups", "readOnly", "Groupes d'annuaire du compte"),
                multiple("roles", "readOnly", "Rôles effectifs de la plateforme"),
                multiple("entitlements", "readOnly", "Droits accordés par les modules, dont les groupes du module CLM (module:code)")));
        s.put("meta", Map.of("resourceType", "Schema", "location", "/scim/v2/Schemas/" + ID));
        return s;
    }

    private static Map<String, Object> simple(String nom, String mutabilite, String unicite) {
        return Map.of("name", nom, "type", "string", "multiValued", false, "required", "userName".equals(nom),
                "caseExact", false, "mutability", mutabilite, "returned", "default", "uniqueness", unicite);
    }

    private static Map<String, Object> booleen(String nom) {
        return Map.of("name", nom, "type", "boolean", "multiValued", false, "required", false,
                "mutability", "readWrite", "returned", "default");
    }

    private static Map<String, Object> multiple(String nom, String mutabilite, String description) {
        return Map.of("name", nom, "type", "complex", "multiValued", true, "required", false, "description", description,
                "mutability", mutabilite, "returned", "default", "subAttributes", List.of(
                        sous("value"), sous("display"), sous("type")));
    }

    private static Map<String, Object> sous(String nom) {
        return Map.of("name", nom, "type", "string", "multiValued", false, "required", false,
                "mutability", "readOnly", "returned", "default");
    }
}
