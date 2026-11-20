package qc.levis.contrat.clm.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.CRLException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.CertificateID;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPReqBuilder;
import org.bouncycastle.cert.ocsp.OCSPResp;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.util.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Données de validation à long terme (PAdES-B-LT, ETSI EN 319 142-1 § 5.4 ; addenda 4, Q4) : dictionnaire DSS ajouté
 * en mise à jour incrémentale, avec les certificats de toutes les signatures et horodatages du document et les listes de
 * révocation publiées par leurs émetteurs. La signature reste vérifiable après l'expiration des certificats.
 */
final class DonneesValidation {

    private static final Logger log = LoggerFactory.getLogger(DonneesValidation.class);

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    byte[] completer(byte[] pdf, List<X509Certificate> chaine) {
        try (PDDocument doc = PDDocument.load(pdf); ByteArrayOutputStream sortie = new ByteArrayOutputStream()) {
            Map<String, X509Certificate> certificats = new LinkedHashMap<>();
            chaine.forEach(c -> certificats.put(c.getSerialNumber() + "|" + c.getIssuerX500Principal(), c));
            for (PDSignature s : doc.getSignatureDictionaries()) {
                certificatsDe(s.getContents(pdf)).forEach(c -> certificats.put(c.getSerialNumber() + "|" + c.getIssuerX500Principal(), c));
            }
            List<byte[]> listes = new ArrayList<>();
            List<byte[]> ocsp = new ArrayList<>();
            for (X509Certificate c : new ArrayList<>(certificats.values())) {
                listes.addAll(listesDeRevocation(c));
                for (OCSPResp r : reponsesOcsp(c, certificats.values())) {
                    ocsp.add(r.getEncoded());
                    convertir(((BasicOCSPResp) r.getResponseObject()).getCerts())
                            .forEach(rc -> certificats.put(rc.getSerialNumber() + "|" + rc.getIssuerX500Principal(), rc));
                }
            }
            ecrireDss(doc, new ArrayList<>(certificats.values()), listes, ocsp);
            doc.saveIncremental(sortie);
            return sortie.toByteArray();
        } catch (IOException | CertificateException | OCSPException e) {
            throw new IllegalStateException("Données de validation à long terme impossibles : " + e.getMessage(), e);
        }
    }

    /** Certificats du CMS et de ses jetons d'horodatage (attribut non signé signature-time-stamp). */
    static List<X509Certificate> certificatsDe(byte[] cms) {
        try {
            CMSSignedData signe = new CMSSignedData(cms);
            List<X509Certificate> tous = new ArrayList<>(convertir(signe.getCertificates()));
            for (SignerInformation si : signe.getSignerInfos().getSigners()) {
                Attribute jeton = si.getUnsignedAttributes() == null ? null
                        : si.getUnsignedAttributes().get(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
                if (jeton != null) {
                    tous.addAll(convertir(new CMSSignedData(ContentInfo.getInstance(jeton.getAttrValues().getObjectAt(0))).getCertificates()));
                }
            }
            return tous;
        } catch (CMSException | CertificateException e) {
            throw new IllegalStateException("Signature illisible : " + e.getMessage(), e);
        }
    }

    private static List<X509Certificate> convertir(Store<X509CertificateHolder> magasin) throws CertificateException {
        return convertir(magasin.getMatches(null).toArray(new X509CertificateHolder[0]));
    }

    private static List<X509Certificate> convertir(X509CertificateHolder[] titulaires) throws CertificateException {
        JcaX509CertificateConverter conv = new JcaX509CertificateConverter();
        List<X509Certificate> sortie = new ArrayList<>();
        for (X509CertificateHolder h : titulaires) {
            sortie.add(conv.getCertificate(h));
        }
        return sortie;
    }

    /**
     * Statut OCSP (RFC 6960) auprès du répondeur annoncé par le certificat (accès aux informations de l'autorité),
     * lorsque son émetteur figure parmi les certificats connus. Un répondeur injoignable ou une réponse non concluante
     * est journalisé sans bloquer : la liste de révocation reste disponible.
     */
    List<OCSPResp> reponsesOcsp(X509Certificate certificat, Collection<X509Certificate> connus) {
        X509Certificate emetteur = connus.stream().filter(c -> !c.equals(certificat)
                && c.getSubjectX500Principal().equals(certificat.getIssuerX500Principal())).findFirst().orElse(null);
        List<OCSPResp> reponses = new ArrayList<>();
        for (String adresse : emetteur == null ? List.<String>of() : repondeurs(certificat)) {
            try {
                CertificateID id = new CertificateID(new JcaDigestCalculatorProviderBuilder().build().get(CertificateID.HASH_SHA1),
                        new JcaX509CertificateHolder(emetteur), certificat.getSerialNumber());
                byte[] requete = new OCSPReqBuilder().addRequest(id).build().getEncoded();
                HttpResponse<byte[]> r = http.send(HttpRequest.newBuilder(URI.create(adresse)).timeout(Duration.ofSeconds(20))
                        .header("Content-Type", "application/ocsp-request").POST(HttpRequest.BodyPublishers.ofByteArray(requete)).build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                OCSPResp reponse = new OCSPResp(r.body());
                if (reponse.getStatus() != OCSPResp.SUCCESSFUL) {
                    throw new OCSPException("statut " + reponse.getStatus());
                }
                reponses.add(reponse);
            } catch (IOException | OCSPException | OperatorCreationException | CertificateEncodingException | RuntimeException e) {
                log.warn("Réponse OCSP indisponible ({}) : {}", adresse, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Collecte OCSP interrompue.", e);
            }
        }
        return reponses;
    }

    static List<String> repondeurs(X509Certificate certificat) {
        byte[] extension = certificat.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (extension == null) {
            return List.of();
        }
        try {
            return java.util.Arrays.stream(AuthorityInformationAccess.getInstance(JcaX509ExtensionUtils.parseExtensionValue(extension))
                            .getAccessDescriptions())
                    .filter(d -> d.getAccessMethod().equals(AccessDescription.id_ad_ocsp))
                    .map(AccessDescription::getAccessLocation)
                    .filter(g -> g.getTagNo() == GeneralName.uniformResourceIdentifier)
                    .map(g -> g.getName().toString())
                    .filter(uri -> uri.startsWith("http"))
                    .toList();
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Accès aux informations de l'autorité illisible : {}", e.getMessage());
            return List.of();
        }
    }

    /** Listes publiées aux points de distribution HTTP du certificat ; un point injoignable est journalisé, pas bloquant. */
    List<byte[]> listesDeRevocation(X509Certificate certificat) {
        List<byte[]> listes = new ArrayList<>();
        for (String adresse : pointsDeDistribution(certificat)) {
            try {
                HttpResponse<byte[]> r = http.send(HttpRequest.newBuilder(URI.create(adresse)).timeout(Duration.ofSeconds(20)).GET().build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                X509CRL crl = (X509CRL) CertificateFactory.getInstance("X.509").generateCRL(new ByteArrayInputStream(r.body()));
                listes.add(crl.getEncoded());
            } catch (IOException | CertificateException | CRLException | RuntimeException e) {
                log.warn("Liste de révocation indisponible ({}) : {}", adresse, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Collecte des listes de révocation interrompue.", e);
            }
        }
        return listes;
    }

    static List<String> pointsDeDistribution(X509Certificate certificat) {
        byte[] extension = certificat.getExtensionValue(Extension.cRLDistributionPoints.getId());
        List<String> adresses = new ArrayList<>();
        if (extension == null) {
            return adresses;
        }
        try {
            for (DistributionPoint dp : CRLDistPoint.getInstance(JcaX509ExtensionUtils.parseExtensionValue(extension)).getDistributionPoints()) {
                adresses.addAll(adressesHttp(dp.getDistributionPoint()));
            }
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Points de distribution illisibles : {}", e.getMessage());
        }
        return adresses;
    }

    private static List<String> adressesHttp(DistributionPointName nom) {
        if (nom == null || nom.getType() != DistributionPointName.FULL_NAME) {
            return List.of();
        }
        return java.util.Arrays.stream(GeneralNames.getInstance(nom.getName()).getNames())
                .filter(g -> g.getTagNo() == GeneralName.uniformResourceIdentifier)
                .map(g -> g.getName().toString())
                .filter(uri -> uri.startsWith("http"))
                .toList();
    }

    /** Dictionnaire DSS (ETSI EN 319 142-1 § 5.4.2) : certificats, listes de révocation et réponses OCSP en flux. */
    private static void ecrireDss(PDDocument doc, List<X509Certificate> certificats, List<byte[]> listes, List<byte[]> ocsp)
            throws IOException, CertificateEncodingException {
        List<byte[]> encodes = new ArrayList<>();
        for (X509Certificate c : certificats) {
            encodes.add(c.getEncoded());
        }
        COSDictionary dss = new COSDictionary();
        dss.setItem(COSName.getPDFName("Certs"), tableau(doc, encodes));
        if (!listes.isEmpty()) {
            dss.setItem(COSName.getPDFName("CRLs"), tableau(doc, listes));
        }
        if (!ocsp.isEmpty()) {
            dss.setItem(COSName.getPDFName("OCSPs"), tableau(doc, ocsp));
        }
        dss.setNeedToBeUpdated(true);
        COSDictionary catalogue = doc.getDocumentCatalog().getCOSObject();
        catalogue.setItem(COSName.getPDFName("DSS"), dss);
        catalogue.setNeedToBeUpdated(true);
    }

    private static COSArray tableau(PDDocument doc, List<byte[]> contenus) throws IOException {
        COSArray t = new COSArray();
        for (byte[] c : contenus) {
            t.add(flux(doc, c));
        }
        t.setNeedToBeUpdated(true);
        return t;
    }

    private static COSStream flux(PDDocument doc, byte[] contenu) throws IOException {
        COSStream s = doc.getDocument().createCOSStream();
        try (OutputStream out = s.createOutputStream()) {
            out.write(contenu);
        }
        s.setNeedToBeUpdated(true);
        return s;
    }
}
