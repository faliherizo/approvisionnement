package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Système externe autorisé à déclencher des signatures (Salesforce Grantmaking, Workday — addenda 1, TI-22 et AF-47).
 * Le secret n'est conservé que sous forme d'empreinte ; la clé qui signe les rappels est chiffrée par l'application.
 */
@Entity
@Table(name = "clm_client_integration", uniqueConstraints = @UniqueConstraint(name = "uk_clm_client_id", columnNames = "client_id"))
public class ClientIntegration {

    /** Système d'origine ; il donne aussi l'origine des trousses qu'il crée. */
    public enum Systeme { SALESFORCE, WORKDAY, AUTRE }

    public static final int NOM_MAX = 120;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = NOM_MAX)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Systeme systeme;

    @Column(name = "client_id", nullable = false, length = 60)
    private String clientId;

    @Column(name = "secret_empreinte", nullable = false, length = 64)
    private String secretEmpreinte;

    @Column(name = "url_rappel", length = 500)
    private String urlRappel;

    @Column(name = "cle_rappel_chiffree", nullable = false, length = 500)
    private String cleRappelChiffree;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "cree_par", nullable = false, length = 200)
    private String creePar;

    @Column(name = "cree_le", nullable = false)
    private Instant creeLe;

    @Column(name = "revoque_le")
    private Instant revoqueLe;

    protected ClientIntegration() {
    }

    /** {@code secret} et {@code cleRappelChiffree} viennent de l'application : aléa et chiffrement lui sont injectés. */
    public static ClientIntegration creer(Long tenantId, String nom, Systeme systeme, String urlRappel, String clientId,
                                          String secret, String cleRappelChiffree, String par, Instant le) {
        String n = nom == null ? "" : nom.strip();
        if (n.isEmpty() || n.length() > NOM_MAX) {
            throw new IllegalArgumentException("Nom du client d'intégration : 1 à " + NOM_MAX + " caractères.");
        }
        if (systeme == null) {
            throw new IllegalArgumentException("Système d'origine requis : SALESFORCE, WORKDAY ou AUTRE.");
        }
        ClientIntegration c = new ClientIntegration();
        c.tenantId = tenantId;
        c.nom = n;
        c.systeme = systeme;
        c.urlRappel = urlRappelValide(urlRappel);
        c.clientId = clientId;
        c.secretEmpreinte = empreinte(secret);
        c.cleRappelChiffree = cleRappelChiffree;
        c.creePar = par;
        c.creeLe = le;
        return c;
    }

    /** Comparaison à temps constant de l'empreinte : un client révoqué n'est jamais authentifié. */
    public boolean authentifie(String secret) {
        return actif && secret != null && MessageDigest.isEqual(
                secretEmpreinte.getBytes(StandardCharsets.US_ASCII), empreinte(secret).getBytes(StandardCharsets.US_ASCII));
    }

    public void revoquer(Instant le) {
        if (!actif) {
            throw new IllegalStateException("Client d'intégration déjà révoqué.");
        }
        actif = false;
        revoqueLe = le;
    }

    public boolean rappelConfigure() {
        return urlRappel != null;
    }

    /** Le rappel transporte des données de contrat : https obligatoire, sauf l'hôte local réservé aux essais. */
    static String urlRappelValide(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        try {
            URI u = new URI(url.strip());
            boolean local = "localhost".equalsIgnoreCase(u.getHost()) || "127.0.0.1".equals(u.getHost());
            if (u.getHost() == null || !("https".equalsIgnoreCase(u.getScheme()) || local && "http".equalsIgnoreCase(u.getScheme()))) {
                throw new IllegalArgumentException("Adresse de rappel : URL https complète requise.");
            }
            return u.toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Adresse de rappel : URL https complète requise.", e);
        }
    }

    static String empreinte(String secret) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(String.valueOf(secret).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible.", e);
        }
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getNom() { return nom; }
    public Systeme getSysteme() { return systeme; }
    public String getClientId() { return clientId; }
    public String getUrlRappel() { return urlRappel; }
    public String getCleRappelChiffree() { return cleRappelChiffree; }
    public boolean isActif() { return actif; }
    public String getCreePar() { return creePar; }
    public Instant getCreeLe() { return creeLe; }
    public Instant getRevoqueLe() { return revoqueLe; }
}
