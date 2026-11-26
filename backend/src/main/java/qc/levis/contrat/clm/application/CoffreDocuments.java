package qc.levis.contrat.clm.application;

import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.application.port.AleaCryptographique;
import qc.levis.contrat.clm.application.port.GestionnaireCles;
import qc.levis.contrat.clm.domain.CleOrganisation;
import qc.levis.contrat.clm.domain.EnveloppeDocument;

/**
 * Chiffrement d'enveloppe du contenu des documents par organisation (Annexe 10, SEC-28 ; ADR-037) : AES-256-GCM avec la clé
 * de données de l'organisation, elle-même ouverte par le service de gestion des clés. Les clés ouvertes restent en mémoire
 * (jamais en base ni au journal) ; la version active se relit périodiquement, pour qu'une rotation faite sur une autre
 * instance s'applique sans redémarrage. Un ancien contenu en clair se lit tel quel jusqu'à son réenveloppement.
 */
@Service
public class CoffreDocuments {

    static final String ALGORITHME = "AES/GCM/NoPadding";
    static final int BITS_ETIQUETTE = 128;
    static final Duration FRAICHEUR_ACTIVE = Duration.ofMinutes(5);

    /** Document dont l'enveloppe ne s'ouvre pas : autre organisation, clé inconnue ou contenu altéré. */
    public static final class DocumentIllisible extends RuntimeException {
        public DocumentIllisible(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** Forme stockée et version de clé qui l'a produite. */
    public record Chiffre(byte[] octets, int version) {
    }

    private record Active(int version, Instant lue) {
    }

    private final RegistreClesOrganisation registre;
    private final GestionnaireCles kms;
    private final AleaCryptographique alea;
    private final Clock horloge;
    private final Map<String, SecretKey> clesOuvertes = new ConcurrentHashMap<>();
    private final Map<Long, Active> actives = new ConcurrentHashMap<>();

    public CoffreDocuments(RegistreClesOrganisation registre, GestionnaireCles kms, AleaCryptographique alea, Clock horloge) {
        this.registre = registre;
        this.kms = kms;
        this.alea = alea;
        this.horloge = horloge;
    }

    /** Chiffre un contenu de l'organisation par sa clé active ; un contenu vide (effacé) reste vide. */
    public Chiffre chiffrer(Long tenantId, byte[] clair) {
        int version = versionActive(tenantId);
        if (clair.length == 0) {
            return new Chiffre(clair, version);
        }
        byte[] iv = alea.octets(EnveloppeDocument.LONGUEUR_IV);
        byte[] entete = EnveloppeDocument.entete(tenantId, version);
        byte[] chiffre = appliquer(Cipher.ENCRYPT_MODE, cle(tenantId, version), iv, entete, clair);
        return new Chiffre(new EnveloppeDocument(tenantId, version, iv, chiffre).octets(), version);
    }

    /**
     * Déchiffre la forme stockée d'un document de l'organisation. Une enveloppe d'une autre organisation est refusée avant
     * tout calcul : la clé d'une organisation n'ouvre jamais le document d'une autre.
     */
    public byte[] dechiffrer(Long tenantId, byte[] stockage) {
        if (!EnveloppeDocument.estEnveloppe(stockage)) {
            return stockage;
        }
        EnveloppeDocument e = EnveloppeDocument.lire(stockage);
        if (e.tenantId() != tenantId) {
            throw new DocumentIllisible("Document chiffré pour une autre organisation.", null);
        }
        return appliquer(Cipher.DECRYPT_MODE, cle(tenantId, e.version()), e.iv(), e.entete(), e.chiffre());
    }

    /** Après une rotation : la version active se relit, les clés ouvertes de l'organisation sont oubliées. */
    public void oublier(Long tenantId) {
        actives.remove(tenantId);
        clesOuvertes.keySet().removeIf(k -> k.startsWith(tenantId + ":"));
    }

    int versionActive(Long tenantId) {
        Instant maintenant = Instant.now(horloge);
        Active a = actives.get(tenantId);
        if (a != null && a.lue().plus(FRAICHEUR_ACTIVE).isAfter(maintenant)) {
            return a.version();
        }
        int version = lireActive(tenantId).getVersion();
        actives.put(tenantId, new Active(version, maintenant));
        return version;
    }

    /** Deux premières écritures simultanées : la seconde création échoue sur l'unicité, la clé créée par l'autre se relit. */
    private CleOrganisation lireActive(Long tenantId) {
        try {
            return registre.active(tenantId);
        } catch (DataIntegrityViolationException concurrente) {
            return registre.active(tenantId);
        }
    }

    private SecretKey cle(Long tenantId, int version) {
        return clesOuvertes.computeIfAbsent(tenantId + ":" + version, k -> {
            CleOrganisation c = registre.version(tenantId, version)
                    .orElseThrow(() -> new DocumentIllisible("Clé de l'organisation introuvable (version " + version + ").", null));
            return new SecretKeySpec(kms.desenvelopper(c.enveloppe()), "AES");
        });
    }

    private static byte[] appliquer(int mode, SecretKey cle, byte[] iv, byte[] entete, byte[] donnees) {
        try {
            Cipher c = Cipher.getInstance(ALGORITHME);
            c.init(mode, cle, new GCMParameterSpec(BITS_ETIQUETTE, iv));
            c.updateAAD(entete);
            return c.doFinal(donnees);
        } catch (GeneralSecurityException e) {
            throw new DocumentIllisible("Chiffrement du document impossible : " + e.getClass().getSimpleName() + ".", e);
        }
    }
}
