package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CRLConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponseGenerator;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenGenerator;
import org.bouncycastle.cms.CMSSignedData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * PAdES-B-LTA (ETSI EN 319 142-1 ; addenda 4, Q4) vérifié par lecture indépendante du fichier : signature CAdES
 * horodatée, DSS portant la chaîne, le certificat de l'autorité d'horodatage et la liste de révocation publiée par
 * l'émetteur, puis horodatage de document couvrant le tout. Autorité de certification, liste et horodatage servis
 * localement.
 */
class PadesLtaTest {

    private static final Clock HORLOGE = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneOffset.UTC);

    private HttpServer serveur;
    private String base;
    private KeyPair cleCa;
    private X509Certificate ca;
    private final KeyPairGenerator generateur = cles();

    @BeforeEach
    void demarrer() throws Exception {
        cleCa = generateur.generateKeyPair();
        ca = certificat(new X500Name("CN=AC de test"), cleCa, new X500Name("CN=AC de test"), cleCa, null, false);
        serveur = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        base = "http://127.0.0.1:" + serveur.getAddress().getPort();
        serveur.createContext("/crl", ex -> repondre(ex, crl()));
        KeyPair cleTsa = generateur.generateKeyPair();
        X509Certificate tsa = certificat(new X500Name("CN=Autorité d'horodatage"), cleTsa, new X500Name("CN=AC de test"), cleCa, null, true);
        serveur.createContext("/tsa", ex -> repondre(ex, jeton(ex.getRequestBody().readAllBytes(), cleTsa, tsa)));
        serveur.start();
    }

    @AfterEach
    void arreter() {
        serveur.stop(0);
    }

    @Test
    void avec_autorite_le_pdf_scelle_atteint_pades_b_lta(@TempDir Path dossier) throws Exception {
        CachetX509 cachet = new CachetX509(magasin(dossier, base + "/crl").toString(), "secret", base + "/tsa", HORLOGE);
        assertEquals("PAdES-B-LTA", cachet.niveauPades());
        byte[] scelle = cachet.scellerPdf(pdf(), "Certificat de signature IQ-1");
        try (PDDocument doc = PDDocument.load(scelle)) {
            List<PDSignature> signatures = doc.getSignatureDictionaries();
            assertEquals(2, signatures.size(), "signature puis horodatage de document");
            assertEquals(PDSignature.SUBFILTER_ETSI_CADES_DETACHED.getName(), signatures.get(0).getSubFilter());
            PDSignature horodatage = signatures.get(1);
            assertEquals("ETSI.RFC3161", horodatage.getSubFilter());
            assertEquals("DocTimeStamp", horodatage.getCOSObject().getNameAsString(COSName.TYPE));
            TimeStampToken t = new TimeStampToken(new CMSSignedData(horodatage.getContents(scelle)));
            assertArrayEquals(MessageDigest.getInstance("SHA-256").digest(horodatage.getSignedContent(scelle)),
                    t.getTimeStampInfo().getMessageImprintDigest(), "l'horodatage couvre le document, DSS compris");
            int[] plage = horodatage.getByteRange();
            assertEquals(scelle.length, plage[2] + plage[3]);
            COSDictionary dss = doc.getDocumentCatalog().getCOSObject().getCOSDictionary(COSName.getPDFName("DSS"));
            List<String> sujets = sujets((COSArray) dss.getDictionaryObject(COSName.getPDFName("Certs")));
            assertTrue(sujets.containsAll(List.of("CN=Cachet IQ", "CN=AC de test", "CN=Autorité d'horodatage")), sujets.toString());
            assertEquals(1, ((COSArray) dss.getDictionaryObject(COSName.getPDFName("CRLs"))).size(), "liste de révocation de l'émetteur");
        }
    }

    @Test
    void un_point_de_distribution_injoignable_n_empeche_pas_le_scellement(@TempDir Path dossier) throws Exception {
        CachetX509 cachet = new CachetX509(magasin(dossier, "http://127.0.0.1:1/crl").toString(), "secret", "", HORLOGE);
        assertEquals("PAdES-B-B", cachet.niveauPades());
        try (PDDocument doc = PDDocument.load(cachet.scellerPdf(pdf(), "x"))) {
            assertEquals(1, doc.getSignatureDictionaries().size());
            COSDictionary dss = doc.getDocumentCatalog().getCOSObject().getCOSDictionary(COSName.getPDFName("DSS"));
            assertEquals(2, ((COSArray) dss.getDictionaryObject(COSName.getPDFName("Certs"))).size(), "cachet et autorité");
            assertNull(dss.getDictionaryObject(COSName.getPDFName("CRLs")));
        }
    }

    @Test
    void seuls_les_points_de_distribution_http_sont_retenus() throws Exception {
        KeyPair cle = generateur.generateKeyPair();
        X509Certificate c = certificat(new X500Name("CN=x"), cle, new X500Name("CN=AC de test"), cleCa,
                new CRLDistPoint(new DistributionPoint[]{
                        new DistributionPoint(new DistributionPointName(new GeneralNames(new GeneralName[]{
                                new GeneralName(GeneralName.uniformResourceIdentifier, "ldap://annuaire/crl"),
                                new GeneralName(GeneralName.uniformResourceIdentifier, base + "/crl")})), null, null),
                        new DistributionPoint(null, null, null)}), false);
        assertEquals(List.of(base + "/crl"), DonneesValidation.pointsDeDistribution(c));
        X509Certificate autres = certificat(new X500Name("CN=y"), cle, new X500Name("CN=AC de test"), cleCa,
                new CRLDistPoint(new DistributionPoint[]{
                        new DistributionPoint(new DistributionPointName(DistributionPointName.NAME_RELATIVE_TO_CRL_ISSUER,
                                new org.bouncycastle.asn1.x500.RDN(org.bouncycastle.asn1.x500.style.BCStyle.CN, new org.bouncycastle.asn1.DERUTF8String("crl"))), null, null),
                        new DistributionPoint(new DistributionPointName(new GeneralNames(
                                new GeneralName(GeneralName.dNSName, "crl.example"))), null, null)}), false);
        assertEquals(List.of(), DonneesValidation.pointsDeDistribution(autres), "nom relatif et nom DNS ignorés");
        KeyPair cle2 = generateur.generateKeyPair();
        JcaX509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(new X500Name("CN=AC de test"), BigInteger.TWO,
                new Date(0), new Date(4102444800000L), new X500Name("CN=z"), cle2.getPublic());
        b.addExtension(Extension.cRLDistributionPoints, false, new org.bouncycastle.asn1.DERUTF8String("pas une liste"));
        X509Certificate illisible = new JcaX509CertificateConverter().getCertificate(b.build(new JcaContentSignerBuilder("SHA256withRSA").build(cleCa.getPrivate())));
        assertEquals(List.of(), DonneesValidation.pointsDeDistribution(illisible), "extension illisible : journalisée, ignorée");
        assertEquals(List.of(), DonneesValidation.pointsDeDistribution(ca));
        assertThrows(IllegalStateException.class, () -> DonneesValidation.certificatsDe("pas un cms".getBytes()));
        assertThrows(IllegalStateException.class, () -> new DonneesValidation().completer("pas un pdf".getBytes(), List.of()));
    }

    @Test
    void une_collecte_interrompue_est_signalee(@TempDir Path dossier) throws Exception {
        serveur.createContext("/lente", ex -> {
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            repondre(ex, crl());
        });
        KeyPair cle = generateur.generateKeyPair();
        X509Certificate c = certificat(new X500Name("CN=lent"), cle, new X500Name("CN=AC de test"), cleCa,
                new CRLDistPoint(new DistributionPoint[]{new DistributionPoint(new DistributionPointName(
                        new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, base + "/lente"))), null, null)}), false);
        Thread.currentThread().interrupt();
        try {
            assertTrue(assertThrows(IllegalStateException.class, () -> new DonneesValidation().listesDeRevocation(c)).getMessage().contains("interrompue"));
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void sans_magasin_le_cachet_de_demonstration_verse_son_certificat_au_dss() throws Exception {
        CachetX509 cachet = new CachetX509("", "", "", HORLOGE);
        try (PDDocument doc = PDDocument.load(cachet.scellerPdf(pdf(), "x"))) {
            COSDictionary dss = doc.getDocumentCatalog().getCOSObject().getCOSDictionary(COSName.getPDFName("DSS"));
            assertEquals(1, ((COSArray) dss.getDictionaryObject(COSName.getPDFName("Certs"))).size());
        }
    }

    @Test
    void un_horodatage_de_document_refuse_fait_echouer_le_scellement(@TempDir Path dossier) throws Exception {
        serveur.createContext("/muet", ex -> {
            ex.sendResponseHeaders(500, -1);
            ex.close();
        });
        CachetX509 cachet = new CachetX509(magasin(dossier, base + "/crl").toString(), "secret", base + "/muet", HORLOGE);
        assertThrows(IllegalStateException.class, () -> cachet.scellerPdf(pdf(), "x"));
    }

    private Path magasin(Path dossier, String crl) throws Exception {
        KeyPair cle = generateur.generateKeyPair();
        CRLDistPoint cdp = new CRLDistPoint(new DistributionPoint[]{new DistributionPoint(new DistributionPointName(
                new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, crl))), null, null)});
        X509Certificate cert = certificat(new X500Name("CN=Cachet IQ"), cle, new X500Name("CN=AC de test"), cleCa, cdp, false);
        Path p = dossier.resolve("cachet.p12");
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(null, null);
        ks.setKeyEntry("cachet", cle.getPrivate(), "secret".toCharArray(), new X509Certificate[]{cert, ca});
        try (OutputStream out = Files.newOutputStream(p)) {
            ks.store(out, "secret".toCharArray());
        }
        return p;
    }

    private byte[] crl() {
        try {
            X509v2CRLBuilder b = new X509v2CRLBuilder(new X500Name("CN=AC de test"), Date.from(HORLOGE.instant()));
            b.setNextUpdate(Date.from(HORLOGE.instant().plusSeconds(86_400)));
            return new JcaX509CRLConverter().getCRL(b.build(new JcaContentSignerBuilder("SHA256withRSA").build(cleCa.getPrivate()))).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static byte[] jeton(byte[] requete, KeyPair cle, X509Certificate cert) {
        try {
            TimeStampTokenGenerator g = new TimeStampTokenGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build())
                    .build(new JcaContentSignerBuilder("SHA256withRSA").build(cle.getPrivate()), cert),
                    new JcaDigestCalculatorProviderBuilder().build().get(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256)),
                    new ASN1ObjectIdentifier("1.2.3.4"));
            g.addCertificates(new JcaCertStore(List.of(cert)));
            return new TimeStampResponseGenerator(g, TSPAlgorithms.ALLOWED)
                    .generate(new TimeStampRequest(requete), BigInteger.ONE, new Date()).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void repondre(com.sun.net.httpserver.HttpExchange ex, byte[] corps) throws java.io.IOException {
        ex.sendResponseHeaders(200, corps.length);
        ex.getResponseBody().write(corps);
        ex.close();
    }

    private static byte[] pdf() throws Exception {
        try (PDDocument doc = new PDDocument(); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            doc.addPage(new PDPage());
            doc.save(out);
            return out.toByteArray();
        }
    }

    private static List<String> sujets(COSArray certs) throws Exception {
        List<String> sujets = new ArrayList<>();
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        for (int i = 0; i < certs.size(); i++) {
            try (var in = ((COSStream) certs.getObject(i)).createInputStream()) {
                sujets.add(((X509Certificate) f.generateCertificate(new ByteArrayInputStream(in.readAllBytes()))).getSubjectX500Principal().getName());
            }
        }
        return sujets;
    }

    private static X509Certificate certificat(X500Name sujet, KeyPair cle, X500Name emetteur, KeyPair cleEmetteur,
                                              CRLDistPoint cdp, boolean horodatage) throws Exception {
        JcaX509v3CertificateBuilder b = new JcaX509v3CertificateBuilder(emetteur, BigInteger.valueOf(System.nanoTime()),
                new Date(0), new Date(4102444800000L), sujet, cle.getPublic());
        b.addExtension(Extension.basicConstraints, true, new BasicConstraints(sujet.equals(emetteur)));
        if (cdp != null) {
            b.addExtension(Extension.cRLDistributionPoints, false, cdp);
        }
        if (horodatage) {
            b.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_timeStamping));
        }
        return new JcaX509CertificateConverter().getCertificate(b.build(new JcaContentSignerBuilder("SHA256withRSA").build(cleEmetteur.getPrivate())));
    }

    private static KeyPairGenerator cles() {
        try {
            KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
            g.initialize(2048);
            return g;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
