package qc.levis.contrat.clm.infrastructure;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import qc.levis.contrat.clm.application.ConnexionsSalesforce;
import qc.levis.contrat.clm.application.port.PortSalesforce;

/**
 * Assertion du flux OAuth 2.0 JWT Bearer de Salesforce (RFC 7523), solution de remplacement au flux client credentials
 * exigée par l'addenda 2 (Q11). Le jeton est signé RS256 par la clé privée de l'application connectée ; seul le
 * certificat public est déposé chez Salesforce, et aucun secret partagé ne circule.
 */
final class AssertionJwt {

    /** Salesforce refuse une assertion dont l'expiration dépasse trois minutes. */
    private static final Duration VALIDITE = Duration.ofMinutes(3);
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    private AssertionJwt() {
    }

    /**
     * @param c connexion en mode JWT Bearer : {@code clientId} (émetteur), {@code utilisateur} (compte d'exécution) et
     *          {@code clientSecret} (clé privée PKCS#8 en PEM)
     */
    static String signer(ConnexionsSalesforce.Connexion c, Instant maintenant) {
        String entete = "{\"alg\":\"RS256\",\"typ\":\"JWT\"}";
        String charge = "{\"iss\":" + chaine(c.clientId()) + ",\"sub\":" + chaine(c.utilisateur()) + ",\"aud\":"
                + chaine(audience(c.tokenUrl())) + ",\"exp\":" + maintenant.plus(VALIDITE).getEpochSecond() + "}";
        String signable = b64(entete) + "." + b64(charge);
        try {
            Signature rs256 = Signature.getInstance("SHA256withRSA");
            rs256.initSign(cle(c.clientSecret()));
            rs256.update(signable.getBytes(StandardCharsets.US_ASCII));
            return signable + "." + B64.encodeToString(rs256.sign());
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            // Le motif ne reprend jamais la clé : seule sa nature invalide est signalée.
            throw new PortSalesforce.Indisponible("Clé privée JWT Bearer invalide (PKCS#8 RSA attendu).", e);
        }
    }

    /** L'audience est l'origine du serveur de jetons (ex. https://login.salesforce.com), sans chemin. */
    static String audience(String tokenUrl) {
        URI u = URI.create(tokenUrl);
        return u.getScheme() + "://" + u.getAuthority();
    }

    private static PrivateKey cle(String pem) throws GeneralSecurityException {
        String corps = pem == null ? "" : pem.replaceAll("-----(BEGIN|END) PRIVATE KEY-----", "").replaceAll("\\s", "");
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(corps)));
    }

    private static String b64(String json) {
        return B64.encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String chaine(String v) {
        String s = v == null ? "" : v;
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
