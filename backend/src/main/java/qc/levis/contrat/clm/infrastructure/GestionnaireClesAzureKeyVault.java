package qc.levis.contrat.clm.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.Base64;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.GestionnaireCles;
import qc.levis.contrat.clm.domain.CleEnveloppee;

/**
 * Service de clés Azure Key Vault (profil {@code azure-kms}, ADR-037) : la clé maîtresse, une clé RSA du coffre (logicielle
 * ou HSM), ne quitte jamais Key Vault ; les clés de données s'y enveloppent et s'y ouvrent par {@code wrapkey} et
 * {@code unwrapkey} (RSA-OAEP-256). La clé enveloppée retient l'identifiant versionné de la clé du coffre : une rotation
 * dans Key Vault crée une nouvelle version, les anciennes ouvrent encore les clés de données jusqu'à leur réenveloppement.
 * Seul le coffre configuré est appelé : un identifiant de clé qui désigne un autre hôte est refusé sans appel.
 */
@Component
@Profile("azure-kms")
public class GestionnaireClesAzureKeyVault implements GestionnaireCles {

    static final String API = "?api-version=7.4";
    static final String ALGORITHME = "RSA-OAEP-256";

    private final ClientEntra client;
    private final ClientEntra.Identite identite;
    private final String coffre;
    private final String cle;

    public GestionnaireClesAzureKeyVault(ChiffrementProprietes proprietes, ObjectMapper json, Clock horloge) {
        ChiffrementProprietes.Azure a = proprietes.azure();
        if (a.vaultUrl() == null || a.vaultUrl().isBlank() || a.cle() == null || a.cle().isBlank()) {
            throw new IllegalStateException("Profil azure-kms : app.clm.chiffrement.azure.vault-url et .cle requis.");
        }
        this.client = new ClientEntra(json, horloge);
        this.coffre = a.vaultUrl().replaceAll("/+$", "");
        this.cle = a.cle();
        this.identite = new ClientEntra.Identite(a.loginUrl(), a.tenantId(), a.clientId(), a.clientSecret(), "https://vault.azure.net/.default");
    }

    @Override
    public CleEnveloppee envelopper(byte[] cleDonnees) {
        JsonNode r = client.post(identite, coffre + "/keys/" + cle + "/wrapkey" + API,
                Map.of("alg", ALGORITHME, "value", Base64.getUrlEncoder().withoutPadding().encodeToString(cleDonnees)));
        return new CleEnveloppee(r.path("kid").asText(), r.path("value").asText());
    }

    @Override
    public byte[] desenvelopper(CleEnveloppee enveloppee) {
        if (!enveloppee.cleMaitresse().startsWith(coffre + "/keys/")) {
            throw new CleIllisible("Clé maîtresse hors du coffre configuré.", null);
        }
        try {
            JsonNode r = client.post(identite, enveloppee.cleMaitresse() + "/unwrapkey" + API,
                    Map.of("alg", ALGORITHME, "value", enveloppee.valeur()));
            return Base64.getUrlDecoder().decode(r.path("value").asText());
        } catch (ClientEntra.Refus | IllegalArgumentException | IllegalStateException e) {
            throw new CleIllisible("Key Vault refuse d'ouvrir la clé : " + e.getMessage(), e);
        }
    }

    /** Version courante de la clé du coffre (son identifiant versionné). */
    @Override
    public String cleMaitresseActive() {
        return client.get(identite, coffre + "/keys/" + cle + API).path("key").path("kid").asText();
    }
}
