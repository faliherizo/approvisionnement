package qc.levis.contrat.clm.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Duration;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.ess.ESSCertIDv2;
import org.bouncycastle.asn1.ess.SigningCertificateV2;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSAttributeTableGenerator;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.CachetElectronique;

/**
 * Cachet électronique X.509 de l'organisation. Certificat et clé viennent d'un magasin PKCS#12 désigné par
 * configuration (variables d'environnement, jamais versionné) ; sans magasin, un certificat de démonstration est
 * généré au démarrage et déclaré comme tel. Signature CMS (CAdES-B : attribut ESS signing-certificate-v2), PDF signé en
 * PAdES (sous-filtre ETSI.CAdES.detached, heure de signature dans le dictionnaire), horodatage RFC 3161 facultatif.
 */
@Component
public class CachetX509 implements CachetElectronique {

    private static final Logger log = LoggerFactory.getLogger(CachetX509.class);
    private static final String ALGORITHME = "SHA256withRSA";

    private final PrivateKey cle;
    private final X509Certificate certificat;
    private final boolean demonstration;
    private final String tsaUrl;
    private final Clock horloge;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public CachetX509(@Value("${app.clm.cachet.magasin:}") String magasin,
                      @Value("${app.clm.cachet.mot-de-passe:}") String motDePasse,
                      @Value("${app.clm.cachet.tsa-url:}") String tsaUrl, Clock horloge) {
        this.tsaUrl = tsaUrl == null || tsaUrl.isBlank() ? null : tsaUrl.strip();
        this.horloge = horloge;
        try {
            if (magasin == null || magasin.isBlank()) {
                KeyPair paire = paire();
                this.cle = paire.getPrivate();
                this.certificat = autosigne(paire, horloge);
                this.demonstration = true;
                log.warn("Cachet électronique de démonstration (certificat autosigné) : configurer CLM_CACHET_MAGASIN en production.");
            } else {
                KeyStore ks = KeyStore.getInstance("PKCS12");
                char[] mdp = motDePasse == null ? new char[0] : motDePasse.toCharArray();
                try (InputStream in = Files.newInputStream(Path.of(magasin.strip()))) {
                    ks.load(in, mdp);
                }
                String alias = null;
                for (String a : Collections.list(ks.aliases())) {
                    if (alias == null && ks.isKeyEntry(a)) {
                        alias = a;
                    }
                }
                if (alias == null) {
                    throw new IllegalStateException("Magasin de cachet sans clé privée.");
                }
                this.cle = (PrivateKey) ks.getKey(alias, mdp);
                this.certificat = (X509Certificate) ks.getCertificate(alias);
                this.demonstration = false;
            }
        } catch (IOException | GeneralSecurityException | OperatorCreationException e) {
            throw new IllegalStateException("Cachet électronique indisponible : " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] signerDetache(byte[] contenu) {
        return signer(contenu, false);
    }

    @Override
    public byte[] scellerPdf(byte[] pdf, String motif) {
        try (PDDocument doc = PDDocument.load(pdf); ByteArrayOutputStream sortie = new ByteArrayOutputStream()) {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ETSI_CADES_DETACHED);
            signature.setName(titulaire());
            signature.setReason(motif);
            Calendar quand = Calendar.getInstance();
            quand.setTimeInMillis(horloge.millis());
            signature.setSignDate(quand);
            doc.addSignature(signature, contenu -> signer(IOUtils.toByteArray(contenu), true));
            doc.saveIncremental(sortie);
            return sortie.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Scellement PAdES impossible : " + e.getMessage(), e);
        }
    }

    @Override
    public String titulaire() {
        return certificat.getSubjectX500Principal().getName();
    }

    @Override
    public boolean demonstration() {
        return demonstration;
    }

    X509Certificate certificat() {
        return certificat;
    }

    /** CMS détaché ; en PAdES, l'attribut signing-time est retiré : l'heure figure dans le dictionnaire de signature. */
    private byte[] signer(byte[] contenu, boolean pades) {
        try {
            ContentSigner signataire = new JcaContentSignerBuilder(ALGORITHME).build(cle);
            SignerInfoGenerator info = new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
                    .setSignedAttributeGenerator(attributs(pades)).build(signataire, certificat);
            CMSSignedDataGenerator generateur = new CMSSignedDataGenerator();
            generateur.addSignerInfoGenerator(info);
            generateur.addCertificates(new JcaCertStore(List.of(certificat)));
            CMSSignedData signe = generateur.generate(new CMSProcessableByteArray(contenu), false);
            return (tsaUrl == null ? signe : horodater(signe)).getEncoded();
        } catch (OperatorCreationException | CMSException | GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Signature CMS impossible : " + e.getMessage(), e);
        }
    }

    private CMSAttributeTableGenerator attributs(boolean pades) throws GeneralSecurityException, IOException {
        byte[] empreinteCertificat = MessageDigest.getInstance("SHA-256").digest(certificat.getEncoded());
        SigningCertificateV2 essV2 = new SigningCertificateV2(new ESSCertIDv2(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256),
                empreinteCertificat));
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new Attribute(PKCSObjectIdentifiers.id_aa_signingCertificateV2, new DERSet(essV2)));
        DefaultSignedAttributeTableGenerator base = new DefaultSignedAttributeTableGenerator(new AttributeTable(v));
        return parametres -> {
            AttributeTable table = base.getAttributes(parametres);
            return pades ? table.remove(CMSAttributes.signingTime) : table;
        };
    }

    /** Jeton RFC 3161 sur la valeur de signature, ajouté comme attribut non signé (signature-time-stamp). */
    private CMSSignedData horodater(CMSSignedData signe) throws IOException, CMSException, GeneralSecurityException {
        SignerInformation si = signe.getSignerInfos().getSigners().iterator().next();
        byte[] empreinte = MessageDigest.getInstance("SHA-256").digest(si.getSignature());
        TimeStampRequestGenerator g = new TimeStampRequestGenerator();
        g.setCertReq(true);
        TimeStampRequest requete = g.generate(NISTObjectIdentifiers.id_sha256, empreinte, BigInteger.valueOf(horloge.millis()));
        try {
            HttpResponse<byte[]> r = http.send(HttpRequest.newBuilder(URI.create(tsaUrl)).timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/timestamp-query")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requete.getEncoded())).build(), HttpResponse.BodyHandlers.ofByteArray());
            TimeStampResponse reponse = new TimeStampResponse(r.body());
            reponse.validate(requete);
            if (reponse.getTimeStampToken() == null) {
                throw new IllegalStateException("Horodatage refusé par l'autorité : " + reponse.getStatusString());
            }
            Hashtable<ASN1ObjectIdentifier, Attribute> nonSignes = new Hashtable<>();
            nonSignes.put(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken, new Attribute(
                    PKCSObjectIdentifiers.id_aa_signatureTimeStampToken, new DERSet(reponse.getTimeStampToken().toCMSSignedData().toASN1Structure())));
            SignerInformation horodate = SignerInformation.replaceUnsignedAttributes(si, new AttributeTable(nonSignes));
            return CMSSignedData.replaceSigners(signe, new SignerInformationStore(horodate));
        } catch (TSPException e) {
            throw new IllegalStateException("Réponse d'horodatage invalide : " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Horodatage interrompu.", e);
        }
    }

    private static KeyPair paire() throws GeneralSecurityException {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(3072);
        return g.generateKeyPair();
    }

    private static X509Certificate autosigne(KeyPair paire, Clock horloge) throws OperatorCreationException, GeneralSecurityException {
        X500Name nom = new X500Name("CN=Cachet de démonstration — module Contrats et signature, O=Plateforme CLM");
        Date debut = new Date(horloge.millis() - Duration.ofDays(1).toMillis());
        Date fin = new Date(horloge.millis() + Duration.ofDays(730).toMillis());
        JcaX509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(nom, BigInteger.valueOf(horloge.millis()), debut, fin, nom, paire.getPublic());
        return new JcaX509CertificateConverter().getCertificate(b.build(new JcaContentSignerBuilder(ALGORITHME).build(paire.getPrivate())));
    }
}
