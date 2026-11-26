package qc.levis.contrat.clm.domain;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Événement du journal d'audit d'une organisation, normalisé pour un SIEM (Annexe 10, TI-04 ; ADR-033) : champs du schéma
 * ASIM « AuditEvent » de Microsoft Sentinel, et ligne CEF (ArcSight Common Event Format) pour les collecteurs qui la
 * préfèrent. Le détail d'une action qui porte des valeurs d'affaires reste au journal de l'organisation.
 */
public record EvenementSiem(long id, Instant horodatage, String acteur, String action, String objet, Long objetId, String detail) {

    public static final String FOURNISSEUR = "Vina";
    public static final String PRODUIT = "CLM";
    public static final String VERSION = "1.0";

    /** Actions dont le détail contient des valeurs d'affaires (avant/après d'un champ) : jamais transmis au SIEM. */
    static final Set<String> CONFIDENTIELLES = Set.of("CLM_VALEUR_MODIFIEE");

    private static final List<String> ECHECS = List.of("REFUSE", "ECHEC", "ECHOUE", "INFECTE", "ALTER", "BLOQUE");
    private static final List<String> SENSIBLES = List.of("SOUTIEN", "CONFIGURATION", "ROLE", "CLIENT", "MFA", "SECRET", "JETON");

    public EvenementSiem {
        action = action == null ? "" : action;
    }

    /** Échec, refus ou altération : ce qu'un analyste de sécurité doit voir en premier. */
    public boolean echec() {
        return ECHECS.stream().anyMatch(action::contains);
    }

    /** Sévérité ASIM : Medium pour un échec, Low pour une action d'administration ou d'accès sensible, sinon Informational. */
    public String severite() {
        if (echec()) {
            return "Medium";
        }
        return SENSIBLES.stream().anyMatch(action::contains) ? "Low" : "Informational";
    }

    public Map<String, Object> asim() {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("EventUid", id);
        e.put("TimeGenerated", horodatage.toString());
        e.put("EventVendor", FOURNISSEUR);
        e.put("EventProduct", PRODUIT);
        e.put("EventProductVersion", VERSION);
        e.put("EventSchema", "AuditEvent");
        e.put("EventSchemaVersion", "0.1");
        e.put("EventType", "Other");
        e.put("EventOriginalType", action);
        e.put("EventResult", echec() ? "Failure" : "Success");
        e.put("EventSeverity", severite());
        e.put("ActorUsername", acteur);
        e.put("Object", objet);
        e.put("TargetResourceId", objetId);
        e.put("EventMessage", message());
        return e;
    }

    /** Ligne CEF ; sévérité CEF de 0 à 10 (3 information, 5 faible, 7 moyen). */
    public String cef() {
        int sev = echec() ? 7 : "Low".equals(severite()) ? 5 : 3;
        return "CEF:0|" + FOURNISSEUR + "|" + PRODUIT + "|" + VERSION + "|" + entete(action) + "|" + entete(action) + "|" + sev
                + "|rt=" + horodatage.toEpochMilli() + " externalId=" + id + " suser=" + extension(acteur) + " act=" + extension(action)
                + " cs1Label=objet cs1=" + extension(java.util.Objects.toString(objet, "") + (objetId == null ? "" : "#" + objetId))
                + " outcome=" + (echec() ? "failure" : "success") + " msg=" + extension(message());
    }

    private String message() {
        return CONFIDENTIELLES.contains(action) ? "Détail au journal d'audit de l'organisation." : detail;
    }

    static String entete(String v) {
        return v == null ? "" : v.replace("\\", "\\\\").replace("|", "\\|");
    }

    static String extension(String v) {
        return v == null ? "" : v.replace("\\", "\\\\").replace("=", "\\=").replace("\r", "").replace("\n", "\\n");
    }
}
