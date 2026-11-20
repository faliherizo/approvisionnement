package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampTokenGenerator;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.FournisseurSignature;
import qc.levis.contrat.clm.application.port.Scelleur;
import qc.levis.contrat.clm.domain.Identite;

/**
 * Attestation de signature normalisée (ST-09) : CAdES par le cachet X.509, horodatée RFC 3161 par une autorité locale ;
 * toute modification de l'attestation, un autre cachet ou un contenu illisible la font refuser.
 */
class SignatureProbanteCachetTest {

    private static final Instant T0 = Instant.parse("2026-11-20T12:00:00Z");
    private static final Clock HORLOGE = Clock.fixed(T0, ZoneOffset.UTC);
    private static final Scelleur SCELLEUR = new Scelleur() {
        @Override
        public String sceller(byte[] contenu) {
            return HexFormat.of().formatHex(CachetX509.sha256(contenu));
        }

        @Override
        public boolean verifier(byte[] contenu, String sceau) {
            return sceau != null && sceau.equals(sceller(contenu));
        }
    };
    private static final FournisseurSignature.Demande DEMANDE = new FournisseurSignature.Demande("e3b0c442", "Marie Tremblay",
            "marie@agrinord.example", Identite.Niveau.CODE_SMS, T0, "10.0.0.1");

    @Test
    void sans_autorite_l_attestation_cades_se_verifie_et_toute_modification_est_refusee() {
        CachetX509 cachet = new CachetX509("", "", "", HORLOGE);
        SignatureProbanteCachet s = new SignatureProbanteCachet(cachet, SCELLEUR);
        FournisseurSignature.Attestation a = s.signer(DEMANDE);
        assertEquals("clm-cachet", a.fournisseur());
        assertEquals("CAdES-SHA256withRSA", a.algorithme());
        assertEquals(T0, a.horodatage(), "sans autorité : l'instant du consentement");
        assertTrue(a.identifiant().matches("CAC-[0-9A-F]{20}"));
        assertTrue(s.verifier(DEMANDE, a));
        assertFalse(s.verifier(new FournisseurSignature.Demande("autre", "Marie Tremblay", "marie@agrinord.example",
                Identite.Niveau.CODE_SMS, T0, "10.0.0.1"), a), "documents différents");
        assertFalse(s.verifier(new FournisseurSignature.Demande("e3b0c442", "Marie Tremblay", "marie@agrinord.example",
                Identite.Niveau.COURRIEL, T0, "10.0.0.1"), a), "niveau d'identité différent");
        assertFalse(new SignatureProbanteCachet(new CachetX509("", "", "", HORLOGE), SCELLEUR).verifier(DEMANDE, a),
                "émise par un autre cachet");
        assertFalse(s.verifier(DEMANDE, null));
        assertFalse(s.verifier(DEMANDE, new FournisseurSignature.Attestation("autre", "x", T0, a.jetonHorodatage(), "x")));
        assertFalse(s.verifier(DEMANDE, new FournisseurSignature.Attestation("clm-cachet", "x", T0, null, "x")));
        assertFalse(s.verifier(DEMANDE, new FournisseurSignature.Attestation("clm-cachet", "x", T0,
                Base64.getEncoder().encodeToString("pas un cms".getBytes(StandardCharsets.UTF_8)), "x")), "illisible");
    }

    @Test
    void une_signature_produite_illisible_est_signalee_et_sans_autorite_aucun_jeton() {
        assertTrue(assertThrows(IllegalStateException.class, () -> SignatureProbanteCachet.jeton("pas un cms".getBytes(StandardCharsets.UTF_8)))
                .getMessage().contains("illisible"));
        assertNull(SignatureProbanteCachet.jeton(new CachetX509("", "", "", HORLOGE).signerDetache("x".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void les_attestations_de_l_adaptateur_local_restent_verifiables() {
        FournisseurSignature.Attestation locale = new SignatureProbanteLocale(SCELLEUR).signer(DEMANDE);
        SignatureProbanteCachet s = new SignatureProbanteCachet(new CachetX509("", "", "", HORLOGE), SCELLEUR);
        assertTrue(s.verifier(DEMANDE, locale));
        assertFalse(s.verifier(new FournisseurSignature.Demande("autre", "Marie Tremblay", "marie@agrinord.example",
                Identite.Niveau.CODE_SMS, T0, "10.0.0.1"), locale));
    }

    @Test
    void avec_autorite_l_attestation_porte_l_heure_du_jeton_rfc_3161() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair cle = g.generateKeyPair();
        X500Name nom = new X500Name("CN=TSA de test");
        JcaX509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(nom, BigInteger.ONE, new Date(0), new Date(4102444800000L), nom, cle.getPublic());
        b.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping));
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(b.build(new JcaContentSignerBuilder("SHA256withRSA").build(cle.getPrivate())));
        Date heure = Date.from(T0.plusSeconds(3));
        HttpServer tsa = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        tsa.createContext("/tsa", ex -> {
            try {
                TimeStampTokenGenerator jetons = new TimeStampTokenGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
                        .build(new JcaContentSignerBuilder("SHA256withRSA").build(cle.getPrivate()), cert),
                        new JcaDigestCalculatorProviderBuilder().build().get(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256)),
                        new ASN1ObjectIdentifier("1.2.3.4"));
                jetons.addCertificates(new JcaCertStore(List.of(cert)));
                byte[] r = new TimeStampResponseGenerator(jetons, TSPAlgorithms.ALLOWED)
                        .generate(new TimeStampRequest(ex.getRequestBody().readAllBytes()), BigInteger.ONE, heure).getEncoded();
                ex.sendResponseHeaders(200, r.length);
                ex.getResponseBody().write(r);
            } catch (Exception e) {
                ex.sendResponseHeaders(500, -1);
            } finally {
                ex.close();
            }
        });
        tsa.start();
        try {
            SignatureProbanteCachet s = new SignatureProbanteCachet(
                    new CachetX509("", "", "http://127.0.0.1:" + tsa.getAddress().getPort() + "/tsa", HORLOGE), SCELLEUR);
            FournisseurSignature.Attestation a = s.signer(DEMANDE);
            assertEquals("CAdES-SHA256withRSA+RFC3161", a.algorithme());
            assertEquals(heure.toInstant(), a.horodatage(), "heure attestée par l'autorité");
            assertTrue(s.verifier(DEMANDE, a));
        } finally {
            tsa.stop(0);
        }
    }
}
