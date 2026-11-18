package qc.levis.contrat.auth.infrastructure;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import qc.levis.contrat.integration.application.IntegrationSamlService.ConfigSaml;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;

/**
 * Adaptateur du protocole SAML 2.0 (Service Provider), sans dépendance externe (API XML et
 * XML-DSig du JDK). Génère l'{@code AuthnRequest} pour le binding HTTP-Redirect, puis
 * <b>valide</b> la {@code SAMLResponse} reçue à l'ACS : signature XML-DSig vérifiée contre le
 * certificat X.509 de l'IdP, statut, restriction d'audience et fenêtre temporelle. Extrait
 * l'identité (courriel via le NameID ou un attribut configuré). Analyse sécurisée (XXE désactivé).
 */
@Component
public class SamlClient {

    static final String NS_PROTOCOL = "urn:oasis:names:tc:SAML:2.0:protocol";
    static final String NS_ASSERTION = "urn:oasis:names:tc:SAML:2.0:assertion";
    static final String NS_SIGNATURE = "http://www.w3.org/2000/09/xmldsig#";
    private static final String STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
    private static final long CLOCK_SKEW_S = 120;

    private final SecureRandom random = new SecureRandom();

    /** Identité extraite d'une assertion SAML validée. */
    /** {@code faits} : groupes et attributs de l'assertion, pour les correspondances d'annuaire (addenda 3, Q43). */
    public record Identite(String courriel, String nom, java.util.List<qc.levis.contrat.auth.domain.FaitAnnuaire> faits) {
        public Identite(String courriel, String nom) {
            this(courriel, nom, java.util.List.of());
        }
    }

    /** Noms d'attribut qui portent des groupes : Microsoft Entra ID, ADFS, attributs usuels. */
    private static final java.util.Set<String> ATTRIBUTS_GROUPES = java.util.Set.of(
            "http://schemas.microsoft.com/ws/2008/06/identity/claims/groups",
            "http://schemas.microsoft.com/ws/2008/06/identity/claims/role", "groups", "memberOf", "Group", "Role");

    /**
     * Construit l'URL de redirection vers le SSO de l'IdP (binding HTTP-Redirect) : l'{@code AuthnRequest}
     * est compressé (DEFLATE brut), encodé Base64 puis placé dans le paramètre {@code SAMLRequest}.
     */
    public String urlRedirection(ConfigSaml cfg, String acsUrl, String relayState) {
        String authnRequest = construireAuthnRequest(cfg, acsUrl);
        String encode = base64(deflate(authnRequest.getBytes(StandardCharsets.UTF_8)));
        String sep = cfg.idpSsoUrl().contains("?") ? "&" : "?";
        StringBuilder url = new StringBuilder(cfg.idpSsoUrl()).append(sep)
                .append("SAMLRequest=").append(enc(encode));
        if (relayState != null && !relayState.isBlank()) {
            url.append("&RelayState=").append(enc(relayState));
        }
        return url.toString();
    }

    private String construireAuthnRequest(ConfigSaml cfg, String acsUrl) {
        String id = "_" + alea();
        String instant = OffsetDateTime.now().toInstant().toString();
        return "<samlp:AuthnRequest xmlns:samlp=\"" + NS_PROTOCOL + "\""
                + " xmlns:saml=\"" + NS_ASSERTION + "\""
                + " ID=\"" + id + "\" Version=\"2.0\" IssueInstant=\"" + instant + "\""
                + " Destination=\"" + echapXml(cfg.idpSsoUrl()) + "\""
                + " AssertionConsumerServiceURL=\"" + echapXml(acsUrl) + "\""
                + " ProtocolBinding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\">"
                + "<saml:Issuer>" + echapXml(spEntityId(cfg, acsUrl)) + "</saml:Issuer>"
                + "<samlp:NameIDPolicy Format=\"urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress\""
                + " AllowCreate=\"true\"/>"
                + "</samlp:AuthnRequest>";
    }

    private String spEntityId(ConfigSaml cfg, String acsUrl) {
        return (cfg.spEntityId() == null || cfg.spEntityId().isBlank()) ? acsUrl : cfg.spEntityId();
    }

    /**
     * Valide une {@code SAMLResponse} (Base64, binding HTTP-POST) et en extrait l'identité.
     *
     * @throws SamlException si le XML, la signature, le statut, l'audience ou la fenêtre temporelle
     *                       sont invalides, ou si le courriel est absent.
     */
    public Identite validerReponse(ConfigSaml cfg, String samlResponseBase64) {
        try {
            byte[] xml = Base64.getMimeDecoder().decode(samlResponseBase64);
            Document doc = analyser(xml);
            Element response = racine(doc, NS_PROTOCOL, "Response");
            verifierStatut(response);
            PublicKey cle = clePublique(cfg.idpCertificat());
            verifierSignature(doc, cle);
            Element assertion = premier(doc, NS_ASSERTION, "Assertion");
            if (assertion == null) throw new SamlException("Réponse SAML sans assertion.");
            verifierAudience(assertion, cfg.spEntityId());
            verifierFenetreTemporelle(assertion);
            String courriel = extraireCourriel(assertion, cfg.claimCourriel());
            if (courriel == null || courriel.isBlank()) {
                throw new SamlException("L'assertion ne contient pas le courriel (NameID ou attribut « "
                        + cfg.claimCourriel() + " »).");
            }
            return new Identite(courriel, extraireNom(assertion), faits(assertion));
        } catch (SamlException e) {
            throw e;
        } catch (Exception e) {
            throw new SamlException("Réponse SAML invalide : " + e.getMessage());
        }
    }

    /** Test de configuration : la config est-elle complète et le certificat X.509 parsable ? */
    public ResultatPublication testerConfiguration(qc.levis.contrat.integration.domain.IntegrationSaml c) {
        if (!c.estConfigure()) {
            return new ResultatPublication(false, null,
                    "Configuration SAML incomplète : EntityID, URL SSO et certificat de l'IdP requis.");
        }
        try {
            clePublique(c.getIdpCertificat());
            return new ResultatPublication(true, c.getIdpEntityId(),
                    "Configuration SAML valide (certificat X.509 de l'IdP reconnu).");
        } catch (Exception e) {
            return new ResultatPublication(false, null,
                    "Certificat X.509 de l'IdP illisible : " + e.getMessage());
        }
    }

    // --- Analyse et validations ---

    private Document analyser(byte[] xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        // Durcissement anti-XXE : aucune DTD ni entité externe.
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        DocumentBuilder b = f.newDocumentBuilder();
        Document doc = b.parse(new ByteArrayInputStream(xml));
        marquerIdentifiants(doc.getDocumentElement());
        return doc;
    }

    /** Déclare les attributs {@code ID} comme identifiants, requis pour résoudre les références XML-DSig. */
    private void marquerIdentifiants(Element e) {
        if (e.hasAttribute("ID")) e.setIdAttribute("ID", true);
        NodeList enfants = e.getChildNodes();
        for (int i = 0; i < enfants.getLength(); i++) {
            if (enfants.item(i) instanceof Element enfant) marquerIdentifiants(enfant);
        }
    }

    private void verifierSignature(Document doc, PublicKey cle) throws Exception {
        NodeList signatures = doc.getElementsByTagNameNS(NS_SIGNATURE, "Signature");
        if (signatures.getLength() == 0) throw new SamlException("Réponse SAML non signée.");
        boolean valide = false;
        for (int i = 0; i < signatures.getLength(); i++) {
            Node sig = signatures.item(i);
            Node parent = sig.getParentNode();
            String local = parent == null ? "" : parent.getLocalName();
            // On n'accepte que la signature de la réponse ou de l'assertion (pas d'un fragment arbitraire).
            if (!"Response".equals(local) && !"Assertion".equals(local)) continue;
            DOMValidateContext ctx = new DOMValidateContext(new CleFixe(cle), sig);
            XMLSignature signature = XMLSignatureFactory.getInstance("DOM").unmarshalXMLSignature(ctx);
            if (signature.validate(ctx)) { valide = true; break; }
        }
        if (!valide) throw new SamlException("Signature XML de la réponse SAML invalide.");
    }

    private void verifierStatut(Element response) {
        NodeList codes = response.getElementsByTagNameNS(NS_PROTOCOL, "StatusCode");
        if (codes.getLength() == 0 || !STATUS_SUCCESS.equals(
                ((Element) codes.item(0)).getAttribute("Value"))) {
            throw new SamlException("L'IdP a refusé l'authentification (statut SAML non « Success »).");
        }
    }

    private void verifierAudience(Element assertion, String spEntityId) {
        if (spEntityId == null || spEntityId.isBlank()) return;
        NodeList audiences = assertion.getElementsByTagNameNS(NS_ASSERTION, "Audience");
        if (audiences.getLength() == 0) return; // pas de restriction d'audience → non bloquant
        for (int i = 0; i < audiences.getLength(); i++) {
            if (spEntityId.equals(audiences.item(i).getTextContent().trim())) return;
        }
        throw new SamlException("Audience de l'assertion inattendue (SP EntityID non listé).");
    }

    private void verifierFenetreTemporelle(Element assertion) {
        Instant now = Instant.now();
        NodeList conditions = assertion.getElementsByTagNameNS(NS_ASSERTION, "Conditions");
        if (conditions.getLength() > 0) {
            Element c = (Element) conditions.item(0);
            Instant notBefore = instant(c.getAttribute("NotBefore"));
            Instant notOnOrAfter = instant(c.getAttribute("NotOnOrAfter"));
            if (notBefore != null && now.isBefore(notBefore.minusSeconds(CLOCK_SKEW_S))) {
                throw new SamlException("Assertion SAML pas encore valide (NotBefore).");
            }
            if (notOnOrAfter != null && !now.isBefore(notOnOrAfter.plusSeconds(CLOCK_SKEW_S))) {
                throw new SamlException("Assertion SAML expirée (NotOnOrAfter).");
            }
        }
    }

    private Instant instant(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return OffsetDateTime.parse(v).toInstant();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String extraireCourriel(Element assertion, String claim) {
        String parAttribut = valeurAttribut(assertion, claim);
        if (parAttribut != null && !parAttribut.isBlank()) return parAttribut.trim();
        NodeList nameIds = assertion.getElementsByTagNameNS(NS_ASSERTION, "NameID");
        return nameIds.getLength() == 0 ? null : nameIds.item(0).getTextContent().trim();
    }

    private String extraireNom(Element assertion) {
        for (String nom : new String[]{"name", "displayName",
                "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name"}) {
            String v = valeurAttribut(assertion, nom);
            if (v != null && !v.isBlank()) return v.trim();
        }
        return null;
    }

    /** Chaque valeur de chaque attribut de l'assertion, comme fait de groupe ou d'attribut. */
    private java.util.List<qc.levis.contrat.auth.domain.FaitAnnuaire> faits(Element assertion) {
        java.util.List<qc.levis.contrat.auth.domain.FaitAnnuaire> faits = new java.util.ArrayList<>();
        NodeList attributs = assertion.getElementsByTagNameNS(NS_ASSERTION, "Attribute");
        for (int i = 0; i < attributs.getLength(); i++) {
            Element a = (Element) attributs.item(i);
            String nom = a.getAttribute("Name");
            boolean groupe = ATTRIBUTS_GROUPES.contains(nom) || ATTRIBUTS_GROUPES.contains(a.getAttribute("FriendlyName"));
            NodeList valeurs = a.getElementsByTagNameNS(NS_ASSERTION, "AttributeValue");
            for (int j = 0; j < valeurs.getLength(); j++) {
                String v = valeurs.item(j).getTextContent();
                if (v != null && !v.isBlank()) {
                    faits.add(groupe ? qc.levis.contrat.auth.domain.FaitAnnuaire.groupe(v)
                            : qc.levis.contrat.auth.domain.FaitAnnuaire.attribut(nom, v));
                }
            }
        }
        return faits;
    }

    /** Première valeur d'un attribut SAML dont le {@code Name} ou {@code FriendlyName} correspond. */
    private String valeurAttribut(Element assertion, String nom) {
        if (nom == null || nom.isBlank()) return null;
        NodeList attributs = assertion.getElementsByTagNameNS(NS_ASSERTION, "Attribute");
        for (int i = 0; i < attributs.getLength(); i++) {
            Element a = (Element) attributs.item(i);
            if (nom.equals(a.getAttribute("Name")) || nom.equals(a.getAttribute("FriendlyName"))) {
                NodeList valeurs = a.getElementsByTagNameNS(NS_ASSERTION, "AttributeValue");
                if (valeurs.getLength() > 0) return valeurs.item(0).getTextContent();
            }
        }
        return null;
    }

    private Element racine(Document doc, String ns, String local) {
        Element e = doc.getDocumentElement();
        if (e == null || !ns.equals(e.getNamespaceURI()) || !local.equals(e.getLocalName())) {
            throw new SamlException("Message SAML inattendu (élément racine " + local + " requis).");
        }
        return e;
    }

    private Element premier(Document doc, String ns, String local) {
        NodeList l = doc.getElementsByTagNameNS(ns, local);
        return l.getLength() == 0 ? null : (Element) l.item(0);
    }

    private PublicKey clePublique(String pem) throws Exception {
        String base64 = pem.replaceAll("-----BEGIN CERTIFICATE-----", "")
                .replaceAll("-----END CERTIFICATE-----", "")
                .replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(base64);
        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(der));
        return cert.getPublicKey();
    }

    private byte[] deflate(byte[] data) {
        try {
            var out = new java.io.ByteArrayOutputStream();
            try (DeflaterOutputStream dos = new DeflaterOutputStream(out,
                    new Deflater(Deflater.DEFAULT_COMPRESSION, true))) {
                dos.write(data);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new SamlException("Compression de l'AuthnRequest impossible : " + e.getMessage());
        }
    }

    private String base64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private String enc(String v) {
        return URLEncoder.encode(v, StandardCharsets.UTF_8);
    }

    private String echapXml(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String alea() {
        byte[] buf = new byte[20];
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    /** Sélecteur qui impose la clé publique du certificat configuré (jamais celle du KeyInfo reçu). */
    private static final class CleFixe extends KeySelector {
        private final Key cle;
        CleFixe(Key cle) { this.cle = cle; }

        @Override
        public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method,
                                        XMLCryptoContext context) throws KeySelectorException {
            return () -> cle;
        }
    }

    /** Erreur métier du flux SAML (analyse ou validation). */
    public static class SamlException extends RuntimeException {
        public SamlException(String message) { super(message); }
    }
}
