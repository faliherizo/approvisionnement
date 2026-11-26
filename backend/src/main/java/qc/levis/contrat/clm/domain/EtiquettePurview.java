package qc.levis.contrat.clm.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Étiquette Microsoft Purview (Annexe 10, SEC-27 ; ADR-036) : étiquette de sensibilité (Microsoft Information Protection,
 * désignée par son identifiant) ou étiquette de rétention (désignée par son nom, avec sa durée de conservation en jours
 * quand Purview la fournit).
 *
 * @param retentionJours durée de rétention en jours ; nulle pour une étiquette de sensibilité ou une rétention sans durée
 */
public record EtiquettePurview(Type type, String id, String nom, Integer retentionJours) {

    public enum Type { SENSIBILITE, RETENTION }

    public static final int ID_MAX = 64;
    public static final int NOM_MAX = 100;

    public EtiquettePurview {
        if (type == null || nom == null || nom.isBlank() || nom.length() > NOM_MAX) {
            throw new IllegalArgumentException("Étiquette Purview invalide : type et nom (100 caractères au plus) requis.");
        }
        if (id == null || id.isBlank() || id.length() > ID_MAX) {
            throw new IllegalArgumentException("Étiquette Purview invalide : identifiant requis (64 caractères au plus).");
        }
        if (retentionJours != null && (type == Type.SENSIBILITE || retentionJours < 0)) {
            throw new IllegalArgumentException("Seule une étiquette de rétention porte une durée, positive.");
        }
    }

    public static EtiquettePurview sensibilite(String id, String nom) {
        return new EtiquettePurview(Type.SENSIBILITE, id, nom, null);
    }

    /** Une étiquette de rétention est désignée par son nom dans Microsoft Graph : il sert aussi d'identifiant. */
    public static EtiquettePurview retention(String nom, Integer jours) {
        return new EtiquettePurview(Type.RETENTION, nom, nom, jours);
    }

    /** Fin de la rétention imposée par l'étiquette à partir de la clôture ; nulle sans durée ou sans clôture. */
    public LocalDate finRetention(Instant cloture, ZoneId fuseau) {
        return retentionJours == null || cloture == null ? null : cloture.atZone(fuseau).toLocalDate().plusDays(retentionJours);
    }
}
