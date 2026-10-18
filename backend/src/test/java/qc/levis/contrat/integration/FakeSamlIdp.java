package qc.levis.contrat.integration;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import qc.levis.contrat.auth.infrastructure.SamlClient;

/**
 * Fournisseur d'identité SAML 2.0 simulé, sans dépendance externe (API XML et XML-DSig du JDK).
 * Produit une {@code SAMLResponse} réellement <b>signée</b> (RSA-SHA256, enveloppée) avec une paire
 * clé/certificat de test fixe : de quoi valider de bout en bout la vérification de signature du SP
 * ({@code SamlClient}) sans IdP réel — pendant SAML du {@code FakeOidcServer}. Sert aux tests.
 */
final class FakeSamlIdp {

    static final String NS_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
    static final String NS_ASSERTION = "urn:oasis:names:tc:SAML:2.0:assertion";

    /** Certificat X.509 de test (public) — à configurer côté SP comme certificat de l'IdP. */
    static final String CERT_PEM = """
            -----BEGIN CERTIFICATE-----
            MIIDHTCCAgWgAwIBAgIUR1VRIlcifpRlXdh+gSxNuS0ehDMwDQYJKoZIhvcNAQEL
            BQAwHTEbMBkGA1UEAwwSRmFrZSBTQU1MIElkUCBUZXN0MCAXDTI2MDkyMzExNTAz
            NFoYDzIxMjYwODMwMTE1MDM0WjAdMRswGQYDVQQDDBJGYWtlIFNBTUwgSWRQIFRl
            c3QwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDKRBbu5RTrKQZPMHPy
            1AxrE/X4P8FKnVPvTwRteioyXL5ZUFwzru5V7VG7F4QpogwbLBPkerL2jLi9p4CV
            71Y9Uuf+U+J5k4yIrNwOQKeCHdzZyZ9srOhpyg/nI9O+avodOq4idb4LgeoWfgf/
            Tq6JQJfyPEnZvjAezkb+MvoIWHdToqXYCi15cytWNNT7X1MsnVt7ioqIIW4FYPFW
            xdzU073/CwkTwys1k0JXXHnFjGAFvXceuruOMAmdsLHl/pxfajkb6UH+7D4qR2nF
            l5nn8GJEv0XcGkx8finHo00k+CpROAkpEhFpdIU0UCnwdAvBTOCjFQaHJ/VwGXmX
            iM2TAgMBAAGjUzBRMB0GA1UdDgQWBBQfdxkW9TjE+2mxugjbSkCeeMyapDAfBgNV
            HSMEGDAWgBQfdxkW9TjE+2mxugjbSkCeeMyapDAPBgNVHRMBAf8EBTADAQH/MA0G
            CSqGSIb3DQEBCwUAA4IBAQATmsSlVgNCyiaOIvVH1yvoT6bZaYCg1kSdVWX1lDAj
            V4OTt5oD8HsHcqkGvJJbJkDwDrhyRDnoZuUxzu7/cI3RGeFwL2AFO/cKH9fXZenA
            C1dLhAESUyjezoF3+M82/81plgWSNnuRhlWqTzfcmvwgMYw/RolTxwKpafc38GXF
            H4W+hFXgOzWPWgM2iiLVfsXxXIClcPS7xge5S+JbZ38PXJ/TW/DmTcnmicvBw248
            JdPNYMAi1zDHsLABIkGxx/183PGD2GXzVrc5QlC/5ZtYNxru1UWo+edqm9LRHIaI
            h0lSXuYcG+A1Rz9lVv7EwmjUPyPKpSvoRzEDbBHdoLn6
            -----END CERTIFICATE-----""";

    private static final String KEY_PEM = """
            MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDKRBbu5RTrKQZP
            MHPy1AxrE/X4P8FKnVPvTwRteioyXL5ZUFwzru5V7VG7F4QpogwbLBPkerL2jLi9
            p4CV71Y9Uuf+U+J5k4yIrNwOQKeCHdzZyZ9srOhpyg/nI9O+avodOq4idb4LgeoW
            fgf/Tq6JQJfyPEnZvjAezkb+MvoIWHdToqXYCi15cytWNNT7X1MsnVt7ioqIIW4F
            YPFWxdzU073/CwkTwys1k0JXXHnFjGAFvXceuruOMAmdsLHl/pxfajkb6UH+7D4q
            R2nFl5nn8GJEv0XcGkx8finHo00k+CpROAkpEhFpdIU0UCnwdAvBTOCjFQaHJ/Vw
            GXmXiM2TAgMBAAECggEAHl0puC6UUlfJRlq2vU257i/ZQq2Hfdpaaa10s7Py4Qvt
            Lgk984BKRS9S5VblaelAD1WG8vYR0aipvxwwgHxPzPWaNXgwTSH/pzDpTrZurxN+
            k683NfWgZOkxiU275sphG8GIYfdHqACWZ06qMq3ZeRSaUCy0GsejVQHlxG6wpvnP
            IgvH3KyAF6x+LienYGRJj+Tr04Wz9XEwGqRqlvOyXKtrjXkEVkTKxx8NSZ4+DgJ9
            yzG0zCku7hdx0GrhA1nWzGeEoZ1a0B3WwCqpRSc5QlGw5vVNYSnoLjgjCLclfY8w
            65BLvlNqfq47DBPji6gz4WvYqKifirCQTUdBHOEReQKBgQDuqH2gdyyOIjZc9J7v
            VXotJs7wypyhcFw2dwHQFfZ04Rep84yoxysLv+2owQXiBmWVPdaUPeV60bBB9dQn
            jQxFQ5ZFYiATFC5kloHRcOXtmkFbBlwilOUX6X7m6+txE1RQXJl8GE/xrOuX44mZ
            t6eOQMDb7z4abSmkeRy5OlAS5wKBgQDY9qIAK8zWJPn0RsVugcLav7Pmei0/6U+d
            i1KDM+V/hGyG/5Huaql+q+MVtuTHOBt+3pH7JEMBdKEE+QB9YheooAfcw2wsnWiX
            oc26Z8fFbxh1Ufvyyh54wPVtbFqNh2IX5liBC4qVS5qIYct5RlhCYCiH1WGq/Vv5
            Oiq4QnNGdQKBgQCKVhgbv3iQGrywPQszZ7SeH3SsloUb/uk0mN7wIwrszNzOomHS
            I0CMWwADZX9neJXBYErVOtudyZYFZd1H37ZRH9Am/63f4T7TB3rQUES26Ucg4Brf
            4La6mZn5eJ7MuIuS2VoOWHleeQFG1ZmZBJ+XEWm5EoBJCSWuNJW1uFtZ2QKBgAD0
            7Xf0uX2EQjnqifpXxiUyzTdJ+KMcg0Zdk+qSMwPh8JyTye/0NnUhiunQmaerPqvL
            jXc/DLCX9BHSi0cCRLpwojg+AF8d/pUUmJSm2oGrYL87slG6xHGh4DMwVCHlo/AZ
            CvDevSi1YaQSUlRhlAgO5n7a2Qho/5vUvtV7LYj1AoGAL00tsfvXR04GjC/s+dv6
            PfsBU16u8pbBuAYSQf6rV775nBSeivpqPQ5VjXlVZrwhBrc78uvq7NkWGZKPvOm7
            R5Z4DdVXpnYkTf/w1giLwP5pIN6XY+vN7vrsyLnR3N7++79yf2Nt66NxwJafOMDg
            Vfmw2NDoFmyLSW4bpI0cmkQ=""";

    // Paramètres de l'assertion émise (modifiables par test).
    String idpEntityId = "https://idp.test/entity";
    String spEntityId = "https://contrat.levis.qc.ca/sp";
    String courriel = "agent.saml@ville.ca";
    String nom = "Agent SAML";

    private final PrivateKey privateKey;

    FakeSamlIdp() {
        try {
            byte[] der = Base64.getMimeDecoder().decode(KEY_PEM);
            this.privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de charger la clé de test SAML", e);
        }
    }

    /** SAMLResponse signée, encodée Base64 (binding HTTP-POST). {@code alterer}=corrompt après signature. */
    String reponseBase64(boolean alterer) {
        try {
            // On fige la disposition des espaces de noms par un cycle sérialisation→relecture AVANT de
            // signer : la forme canonique exclusive vue par le SP correspond alors exactement à celle
            // sur laquelle l'empreinte a été calculée (sinon les déclarations xmlns migrent).
            Document doc = reparser(serialiser(construireReponse()));
            Element response = doc.getDocumentElement();
            response.setIdAttribute("ID", true);
            signer(response, "_resp1");
            if (alterer) {
                // Modifie le courriel APRÈS signature → l'empreinte ne correspond plus.
                doc.getElementsByTagNameNS(NS_ASSERTION, "NameID").item(0)
                        .setTextContent("intrus@pirate.ca");
            }
            return Base64.getEncoder().encodeToString(
                    serialiser(doc).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Impossible de produire la réponse SAML de test", e);
        }
    }

    /** SAMLResponse NON signée, encodée Base64 (doit être rejetée par le SP). */
    String reponseNonSigneeBase64() {
        try {
            return Base64.getEncoder().encodeToString(
                    serialiser(construireReponse()).getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Document construireReponse() throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        Document doc = f.newDocumentBuilder().newDocument();
        String now = OffsetDateTime.now().toInstant().toString();
        String futur = OffsetDateTime.now().plusMinutes(10).toInstant().toString();
        String passe = OffsetDateTime.now().minusMinutes(5).toInstant().toString();

        Element response = doc.createElementNS(NS_PROTOCOL, "samlp:Response");
        response.setAttribute("ID", "_resp1");
        response.setAttribute("Version", "2.0");
        response.setAttribute("IssueInstant", now);
        doc.appendChild(response);

        response.appendChild(elem(doc, NS_ASSERTION, "saml:Issuer", idpEntityId));

        Element status = doc.createElementNS(NS_PROTOCOL, "samlp:Status");
        Element statusCode = doc.createElementNS(NS_PROTOCOL, "samlp:StatusCode");
        statusCode.setAttribute("Value", "urn:oasis:names:tc:SAML:2.0:status:Success");
        status.appendChild(statusCode);
        response.appendChild(status);

        Element assertion = doc.createElementNS(NS_ASSERTION, "saml:Assertion");
        assertion.setAttribute("ID", "_asrt1");
        assertion.setAttribute("Version", "2.0");
        assertion.setAttribute("IssueInstant", now);
        response.appendChild(assertion);

        assertion.appendChild(elem(doc, NS_ASSERTION, "saml:Issuer", idpEntityId));

        Element subject = doc.createElementNS(NS_ASSERTION, "saml:Subject");
        subject.appendChild(elem(doc, NS_ASSERTION, "saml:NameID", courriel));
        assertion.appendChild(subject);

        Element conditions = doc.createElementNS(NS_ASSERTION, "saml:Conditions");
        conditions.setAttribute("NotBefore", passe);
        conditions.setAttribute("NotOnOrAfter", futur);
        Element audienceRestriction = doc.createElementNS(NS_ASSERTION, "saml:AudienceRestriction");
        audienceRestriction.appendChild(elem(doc, NS_ASSERTION, "saml:Audience", spEntityId));
        conditions.appendChild(audienceRestriction);
        assertion.appendChild(conditions);

        Element attrStatement = doc.createElementNS(NS_ASSERTION, "saml:AttributeStatement");
        attrStatement.appendChild(attribut(doc, "email", courriel));
        attrStatement.appendChild(attribut(doc, "name", nom));
        assertion.appendChild(attrStatement);
        return doc;
    }

    private Element attribut(Document doc, String nomAttr, String valeur) {
        Element attr = doc.createElementNS(NS_ASSERTION, "saml:Attribute");
        attr.setAttribute("Name", nomAttr);
        attr.appendChild(elem(doc, NS_ASSERTION, "saml:AttributeValue", valeur));
        return attr;
    }

    private Element elem(Document doc, String ns, String nomQualifie, String texte) {
        Element e = doc.createElementNS(ns, nomQualifie);
        e.setTextContent(texte);
        return e;
    }

    private Document reparser(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        return f.newDocumentBuilder().parse(
                new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private void signer(Element aSigner, String id) throws Exception {
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        Reference ref = fac.newReference("#" + id,
                fac.newDigestMethod(DigestMethod.SHA256, null),
                List.of(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null),
                        fac.newTransform(CanonicalizationMethod.INCLUSIVE, (TransformParameterSpec) null)),
                null, null);
        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA256, null), List.of(ref));
        // La signature enveloppée s'insère juste après l'Issuer (élément #1) de l'élément signé.
        DOMSignContext dsc = new DOMSignContext(privateKey, aSigner,
                aSigner.getFirstChild().getNextSibling());
        XMLSignature signature = fac.newXMLSignature(si, null);
        signature.sign(dsc);
    }

    private String serialiser(Document doc) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(sw));
        return sw.toString();
    }
}
