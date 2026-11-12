package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampTokenGenerator;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Cachet électronique normalisé (Cahier § 1.4.11) vérifié par des outils indépendants : vérification CMS de
 * BouncyCastle, signature PDF lue par PDFBox, jeton RFC 3161 d'une autorité locale de test.
 */
class CachetX509Test {

    private static final Clock HORLOGE = Clock.fixed(Instant.parse("2026-11-12T15:00:00Z"), ZoneOffset.UTC);
    private static final byte[] CONTENU = "Entente Agri-Nord v3".getBytes(StandardCharsets.UTF_8);

    private static boolean valide(byte[] p7s, byte[] contenu) throws Exception {
        CMSSignedData cms = new CMSSignedData(new CMSProcessableByteArray(contenu), p7s);
        SignerInformation si = cms.getSignerInfos().getSigners().iterator().next();
        X509CertificateHolder cert = (X509CertificateHolder) cms.getCertificates().getMatches(si.getSID()).iterator().next();
        try {
            return si.verify(new JcaSimpleSignerInfoVerifierBuilder().build(cert));
        } catch (org.bouncycastle.cms.CMSSignerDigestMismatchException e) {
            return false;
        }
    }

    @Test
    void sans_magasin_un_certificat_de_demonstration_signe_en_cades_verifiable() throws Exception {
        CachetX509 cachet = new CachetX509("", "", "", HORLOGE);
        assertTrue(cachet.demonstration());
        assertTrue(cachet.titulaire().contains("Cachet de démonstration"));
        byte[] p7s = cachet.signerDetache(CONTENU);
        assertTrue(valide(p7s, CONTENU));
        assertFalse(valide(p7s, "Entente Agri-Nord v4".getBytes(StandardCharsets.UTF_8)), "document altéré");
        SignerInformation si = new CMSSignedData(new CMSProcessableByteArray(CONTENU), p7s).getSignerInfos().getSigners().iterator().next();
        assertNotNull(si.getSignedAttributes().get(PKCSObjectIdentifiers.id_aa_signingCertificateV2), "CAdES-B : signing-certificate-v2");
        assertNotNull(si.getSignedAttributes().get(CMSAttributes.signingTime));
    }

    @Test
    void un_pdf_scelle_porte_une_signature_pades_couvrant_tout_le_document() throws Exception {
        CachetX509 cachet = new CachetX509(null, null, null, HORLOGE);
        byte[] pdf;
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.addPage(new PDPage());
            doc.save(out);
            pdf = out.toByteArray();
        }
        byte[] scelle = cachet.scellerPdf(pdf, "Certificat de signature TR-1");
        try (PDDocument doc = PDDocument.load(scelle)) {
            List<PDSignature> signatures = doc.getSignatureDictionaries();
            assertEquals(1, signatures.size());
            PDSignature sig = signatures.get(0);
            assertEquals(PDSignature.SUBFILTER_ETSI_CADES_DETACHED.getName(), sig.getSubFilter());
            assertEquals("Certificat de signature TR-1", sig.getReason());
            byte[] signe = sig.getSignedContent(scelle);
            byte[] cms = sig.getContents(scelle);
            assertTrue(valide(cms, signe), "signature PAdES valide sur la plage signée");
            SignerInformation si = new CMSSignedData(new CMSProcessableByteArray(signe), cms).getSignerInfos().getSigners().iterator().next();
            assertNull(si.getSignedAttributes().get(CMSAttributes.signingTime), "PAdES : l'heure est dans le dictionnaire");
            int[] plage = sig.getByteRange();
            assertEquals(scelle.length, plage[2] + plage[3], "la plage signée couvre tout le fichier");
        }
        assertThrows(IllegalStateException.class, () -> cachet.scellerPdf("pas un pdf".getBytes(), "x"));
    }

    @Test
    void un_magasin_pkcs12_fournit_le_cachet_et_une_autorite_rfc3161_horodate_la_signature(@TempDir Path dossier) throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair cle = g.generateKeyPair();
        X509Certificate cert = certificat(cle, "CN=Investissement Québec — cachet", false);
        Path magasin = dossier.resolve("cachet.p12");
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setCertificateEntry("ca", cert);
        ks.setKeyEntry("cachet", cle.getPrivate(), "secret".toCharArray(), new X509Certificate[]{cert});
        try (OutputStream out = Files.newOutputStream(magasin)) {
            ks.store(out, "secret".toCharArray());
        }

        KeyPair cleTsa = g.generateKeyPair();
        X509Certificate certTsa = certificat(cleTsa, "CN=Autorité d'horodatage de test", true);
        HttpServer tsa = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        tsa.createContext("/tsa", ex -> {
            try {
                TimeStampRequest requete = new TimeStampRequest(ex.getRequestBody().readAllBytes());
                TimeStampTokenGenerator jetons = new TimeStampTokenGenerator(new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().build()).build(new JcaContentSignerBuilder("SHA256withRSA").build(cleTsa.getPrivate()), certTsa),
                        new JcaDigestCalculatorProviderBuilder().build().get(new org.bouncycastle.asn1.x509.AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256)),
                        new org.bouncycastle.asn1.ASN1ObjectIdentifier("1.2.3.4"));
                jetons.addCertificates(new JcaCertStore(List.of(certTsa)));
                byte[] reponse = new TimeStampResponseGenerator(jetons, TSPAlgorithms.ALLOWED)
                        .generate(requete, BigInteger.ONE, new Date()).getEncoded();
                ex.sendResponseHeaders(200, reponse.length);
                ex.getResponseBody().write(reponse);
            } catch (Exception e) {
                ex.sendResponseHeaders(500, -1);
            } finally {
                ex.close();
            }
        });
        tsa.start();
        try {
            CachetX509 cachet = new CachetX509(magasin.toString(), "secret", "http://127.0.0.1:" + tsa.getAddress().getPort() + "/tsa", HORLOGE);
            assertFalse(cachet.demonstration());
            assertEquals(cert, cachet.certificat());
            assertTrue(cachet.titulaire().contains("Investissement Québec"));
            byte[] p7s = cachet.signerDetache(CONTENU);
            assertTrue(valide(p7s, CONTENU));
            SignerInformation si = new CMSSignedData(new CMSProcessableByteArray(CONTENU), p7s).getSignerInfos().getSigners().iterator().next();
            assertNotNull(si.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken), "jeton RFC 3161");
        } finally {
            tsa.stop(0);
        }
        assertThrows(IllegalStateException.class, () -> new CachetX509(magasin.toString(), "mauvais", "", HORLOGE));
        assertThrows(IllegalStateException.class, () -> new CachetX509("http://127.0.0.1:1/tsa", "", "http://127.0.0.1:1/tsa", HORLOGE)
                .signerDetache(CONTENU));
        CachetX509 sansAutorite = new CachetX509(magasin.toString(), "secret", "http://127.0.0.1:1/tsa", HORLOGE);
        assertThrows(IllegalStateException.class, () -> sansAutorite.signerDetache(CONTENU), "autorité injoignable");
    }

    /** Autorité de test qui refuse, ou qui répond pour une autre empreinte que celle demandée. */
    @Test
    void un_refus_ou_une_reponse_d_horodatage_inadequate_fait_echouer_la_signature() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair cleTsa = g.generateKeyPair();
        X509Certificate certTsa = certificat(cleTsa, "CN=TSA", true);
        HttpServer tsa = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        tsa.createContext("/refus", ex -> {
            try {
                TimeStampRequest requete = new TimeStampRequest(ex.getRequestBody().readAllBytes());
                byte[] reponse = new TimeStampResponseGenerator(generateur(cleTsa, certTsa), TSPAlgorithms.ALLOWED)
                        .generateRejectedResponse(new org.bouncycastle.tsp.TSPException("refus")).getEncoded();
                ex.sendResponseHeaders(200, reponse.length);
                ex.getResponseBody().write(reponse);
            } catch (Exception e) {
                ex.sendResponseHeaders(500, -1);
            } finally {
                ex.close();
            }
        });
        tsa.createContext("/autre", ex -> {
            try {
                ex.getRequestBody().readAllBytes();
                TimeStampRequest autre = new TimeStampRequestGenerator().generate(NISTObjectIdentifiers.id_sha256, new byte[32], BigInteger.TEN);
                byte[] reponse = new TimeStampResponseGenerator(generateur(cleTsa, certTsa), TSPAlgorithms.ALLOWED)
                        .generate(autre, BigInteger.ONE, new Date()).getEncoded();
                ex.sendResponseHeaders(200, reponse.length);
                ex.getResponseBody().write(reponse);
            } catch (Exception e) {
                ex.sendResponseHeaders(500, -1);
            } finally {
                ex.close();
            }
        });
        tsa.start();
        String base = "http://127.0.0.1:" + tsa.getAddress().getPort();
        try {
            assertTrue(assertThrows(IllegalStateException.class, () -> new CachetX509("", "", base + "/refus", HORLOGE).signerDetache(CONTENU))
                    .getMessage().contains("refusé"));
            assertTrue(assertThrows(IllegalStateException.class, () -> new CachetX509("", "", base + "/autre", HORLOGE).signerDetache(CONTENU))
                    .getMessage().contains("invalide"));
            CachetX509 cachet = new CachetX509("", "", base + "/refus", HORLOGE);
            Thread.currentThread().interrupt();
            try {
                assertTrue(assertThrows(IllegalStateException.class, () -> cachet.signerDetache(CONTENU)).getMessage().contains("interrompu"));
            } finally {
                Thread.interrupted();
            }
        } finally {
            tsa.stop(0);
        }
    }

    @Test
    void un_magasin_sans_cle_ou_sans_mot_de_passe_est_refuse(@TempDir Path dossier) throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        X509Certificate cert = certificat(g.generateKeyPair(), "CN=Seul certificat", false);
        Path sansCle = dossier.resolve("sans-cle.p12");
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setCertificateEntry("ca", cert);
        try (OutputStream out = Files.newOutputStream(sansCle)) {
            ks.store(out, "secret".toCharArray());
        }
        assertTrue(assertThrows(IllegalStateException.class, () -> new CachetX509(sansCle.toString(), "secret", "", HORLOGE))
                .getMessage().contains("sans clé privée"));
        assertThrows(IllegalStateException.class, () -> new CachetX509(sansCle.toString(), null, "", HORLOGE), "mot de passe absent");
    }

    private static TimeStampTokenGenerator generateur(KeyPair cle, X509Certificate cert) throws Exception {
        TimeStampTokenGenerator jetons = new TimeStampTokenGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
                .build(new JcaContentSignerBuilder("SHA256withRSA").build(cle.getPrivate()), cert),
                new JcaDigestCalculatorProviderBuilder().build().get(new org.bouncycastle.asn1.x509.AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256)),
                new org.bouncycastle.asn1.ASN1ObjectIdentifier("1.2.3.4"));
        jetons.addCertificates(new JcaCertStore(List.of(cert)));
        return jetons;
    }

    private static X509Certificate certificat(KeyPair cle, String nom, boolean horodatage) throws Exception {
        X500Name x500 = new X500Name(nom);
        JcaX509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(x500, BigInteger.TEN, new Date(0), new Date(4102444800000L), x500, cle.getPublic());
        if (horodatage) {
            b.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping));
        }
        return new JcaX509CertificateConverter().getCertificate(b.build(new JcaContentSignerBuilder("SHA256withRSA").build(cle.getPrivate())));
    }
}
