package qc.levis.contrat.clm.application.mcp;

import java.util.Map;
import qc.levis.contrat.clm.domain.ClientIntegration;

/**
 * Outil exposé par le connecteur MCP (ADR-023). Un outil s'ajoute par un nouveau bean, sans toucher au protocole ; il
 * lit seulement, dans l'organisation du client d'intégration qui l'appelle.
 */
public interface OutilMcp {

    /** Nom stable de l'outil, tel que l'assistant l'appelle. */
    String nom();

    String titre();

    /** Description lue par l'assistant pour choisir l'outil. */
    String description();

    /** Schéma JSON des arguments. */
    Map<String, Object> schemaEntree();

    /** Résultat structuré (un objet JSON) ; {@link ArgumentInvalide} ou {@link java.util.NoSuchElementException} si refusé. */
    Map<String, Object> executer(ClientIntegration client, Map<String, Object> arguments);

    /** Argument absent ou mal formé : l'assistant reçoit un résultat d'erreur qu'il peut corriger. */
    class ArgumentInvalide extends IllegalArgumentException {
        public ArgumentInvalide(String message) {
            super(message);
        }
    }

    /** Identifiant entier positif requis sous ce nom. */
    static Long identifiant(Map<String, Object> arguments, String nom) {
        Object v = arguments.get(nom);
        if (v instanceof Number n && n.longValue() > 0 && n.doubleValue() == n.longValue()) {
            return n.longValue();
        }
        throw new ArgumentInvalide("Argument « " + nom + " » : entier positif requis.");
    }

    static Map<String, Object> schemaIdentifiant(String nom, String description) {
        return Map.of("type", "object", "additionalProperties", false, "required", java.util.List.of(nom),
                "properties", Map.of(nom, Map.of("type", "integer", "minimum", 1, "description", description)));
    }
}
