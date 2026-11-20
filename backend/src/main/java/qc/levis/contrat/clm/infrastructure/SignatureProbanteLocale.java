package qc.levis.contrat.clm.infrastructure;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.FournisseurSignature;
import qc.levis.contrat.clm.application.port.Scelleur;

/**
 * Signature et horodatage locaux (HMAC serveur) : non qualifiés au sens d'un prestataire certifié. Adaptateur par
 * défaut jusqu'à la décision V1 sur le fournisseur de signature à valeur probante et l'autorité d'horodatage.
 */
@Component
@ConditionalOnProperty(name = "app.clm.signature.fournisseur", havingValue = "local", matchIfMissing = true)
public class SignatureProbanteLocale implements FournisseurSignature {

    static final String FOURNISSEUR = "clm-local";
    static final String ALGORITHME = "HMAC-SHA256";

    private final Scelleur scelleur;

    public SignatureProbanteLocale(Scelleur scelleur) {
        this.scelleur = scelleur;
    }

    @Override
    public Attestation signer(Demande d) {
        Instant horodatage = d.consentementLe();
        String jeton = scelleur.sceller(canon(d, horodatage));
        return new Attestation(FOURNISSEUR, "LOC-" + jeton.substring(0, 20).toUpperCase(), horodatage, jeton, ALGORITHME);
    }

    @Override
    public boolean verifier(Demande d, Attestation a) {
        return a != null && FOURNISSEUR.equals(a.fournisseur()) && a.horodatage() != null
                && scelleur.verifier(canon(d, a.horodatage()), a.jetonHorodatage());
    }

    private static byte[] canon(Demande d, Instant horodatage) {
        return String.join("|", d.empreinteDocuments(), d.signataire(), d.courriel(), String.valueOf(d.niveau()), horodatage.toString())
                .getBytes(StandardCharsets.UTF_8);
    }
}
