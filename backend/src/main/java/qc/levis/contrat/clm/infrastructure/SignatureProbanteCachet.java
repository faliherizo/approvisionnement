package qc.levis.contrat.clm.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.tsp.TimeStampToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.CachetElectronique;
import qc.levis.contrat.clm.application.port.FournisseurSignature;
import qc.levis.contrat.clm.application.port.Scelleur;

/**
 * Attestation de signature normalisée (ST-09) : l'empreinte des documents, l'identité du signataire, son niveau
 * d'authentification et l'instant du consentement sont signés en CAdES par le cachet X.509 de l'organisation et, avec
 * une autorité configurée, horodatés RFC 3161. L'attestation se vérifie hors de la plateforme (openssl cms -verify,
 * pyHanko) ; sa valeur probante tient au certificat et à l'autorité retenus par IQ. Retenu par
 * {@code app.clm.signature.fournisseur=cachet}. Les attestations antérieures de l'adaptateur local restent vérifiables.
 */
@Component
@ConditionalOnProperty(name = "app.clm.signature.fournisseur", havingValue = "cachet")
public class SignatureProbanteCachet implements FournisseurSignature {

    static final String FOURNISSEUR = "clm-cachet";
    static final String ALGORITHME = "CAdES-SHA256withRSA";

    private final CachetElectronique cachet;
    private final SignatureProbanteLocale anterieures;

    public SignatureProbanteCachet(CachetElectronique cachet, Scelleur scelleur) {
        this.cachet = cachet;
        this.anterieures = new SignatureProbanteLocale(scelleur);
    }

    @Override
    public Attestation signer(Demande d) {
        byte[] cms = cachet.signerDetache(canon(d));
        TimeStampToken jeton = jeton(cms);
        Instant horodatage = jeton == null ? d.consentementLe() : jeton.getTimeStampInfo().getGenTime().toInstant();
        String identifiant = "CAC-" + java.util.HexFormat.of().withUpperCase().formatHex(CachetX509.sha256(cms)).substring(0, 20);
        return new Attestation(FOURNISSEUR, identifiant, horodatage, Base64.getEncoder().encodeToString(cms),
                ALGORITHME + (jeton == null ? "" : "+RFC3161"));
    }

    /** Signature intacte, émise par le cachet de l'organisation, sur l'attestation de cette signature précise. */
    @Override
    public boolean verifier(Demande d, Attestation a) {
        if (a != null && SignatureProbanteLocale.FOURNISSEUR.equals(a.fournisseur())) {
            return anterieures.verifier(d, a);
        }
        if (a == null || !FOURNISSEUR.equals(a.fournisseur()) || a.jetonHorodatage() == null) {
            return false;
        }
        try {
            CMSSignedData signe = new CMSSignedData(new CMSProcessableByteArray(canon(d)), Base64.getDecoder().decode(a.jetonHorodatage()));
            SignerInformation si = signe.getSignerInfos().getSigners().iterator().next();
            X509CertificateHolder cert = (X509CertificateHolder) signe.getCertificates().getMatches(si.getSID()).iterator().next();
            X509Certificate c = new org.bouncycastle.cert.jcajce.JcaX509CertificateConverter().getCertificate(cert);
            return cachet.reconnait(c) && si.verify(new JcaSimpleSignerInfoVerifierBuilder().build(cert));
        } catch (Exception e) {
            return false;
        }
    }

    /** Jeton RFC 3161 joint à la signature (attribut non signé), ou null sans autorité configurée. */
    static TimeStampToken jeton(byte[] cms) {
        try {
            SignerInformation si = new CMSSignedData(cms).getSignerInfos().getSigners().iterator().next();
            var attribut = si.getUnsignedAttributes() == null ? null : si.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
            return attribut == null ? null : new TimeStampToken(new CMSSignedData(attribut.getAttrValues().getObjectAt(0).toASN1Primitive().getEncoded()));
        } catch (Exception e) {
            throw new IllegalStateException("Signature produite illisible : " + e.getMessage(), e);
        }
    }

    static byte[] canon(Demande d) {
        return String.join("|", d.empreinteDocuments(), d.signataire(), d.courriel(), String.valueOf(d.niveau()),
                String.valueOf(d.consentementLe())).getBytes(StandardCharsets.UTF_8);
    }
}
