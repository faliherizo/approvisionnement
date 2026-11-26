package qc.levis.contrat.clm.infrastructure;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Réglage du chiffrement au repos ({@code app.clm.chiffrement.*}, ADR-037). Aucune valeur secrète ne figure dans le dépôt :
 * la clé maîtresse locale vient d'un secret de déploiement ({@code CLM_CLE_MAITRESSE}, 32 octets en Base64) ; sous le
 * profil {@code azure-kms}, elle reste dans Azure Key Vault et seuls son adresse et l'identité de l'application sont
 * fournies.
 *
 * @param cleMaitresse   clé maîtresse locale active (Base64) ; vide : clé de développement, refusée en production
 * @param cleMaitresseId identifiant de la clé maîtresse active, inscrit avec chaque clé de données enveloppée
 * @param anciennesCles  anciennes clés maîtresses (identifiant → Base64), gardées pour ouvrir les clés à réenvelopper
 */
@ConfigurationProperties("app.clm.chiffrement")
public record ChiffrementProprietes(String cleMaitresse, String cleMaitresseId, Map<String, String> anciennesCles, Azure azure) {

    /** Azure Key Vault : coffre, nom de la clé (RSA) et identité d'application Entra ID autorisée à envelopper et ouvrir. */
    public record Azure(String vaultUrl, String cle, String tenantId, String clientId, String clientSecret, String loginUrl) {
        public Azure {
            loginUrl = loginUrl == null || loginUrl.isBlank() ? "https://login.microsoftonline.com" : loginUrl;
        }
    }

    public ChiffrementProprietes {
        cleMaitresseId = cleMaitresseId == null || cleMaitresseId.isBlank() ? "locale-1" : cleMaitresseId;
        anciennesCles = anciennesCles == null ? Map.of() : Map.copyOf(anciennesCles);
        azure = azure == null ? new Azure(null, null, null, null, null, null) : azure;
    }
}
