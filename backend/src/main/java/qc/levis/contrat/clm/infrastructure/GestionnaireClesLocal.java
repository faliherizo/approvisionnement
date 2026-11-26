package qc.levis.contrat.clm.infrastructure;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.port.AleaCryptographique;
import qc.levis.contrat.clm.application.port.GestionnaireCles;
import qc.levis.contrat.clm.domain.CleEnveloppee;

/**
 * Service de clés local (ADR-037), par défaut : la clé maîtresse (AES-256) vient d'un secret de déploiement et enveloppe
 * les clés de données en AES-GCM, l'identifiant de la clé maîtresse servant de données authentifiées. Les anciennes clés
 * maîtresses restent déclarées le temps du réenveloppement. Sans clé fournie, une clé de développement publique est
 * employée et signalée ; elle est refusée dans l'environnement de production.
 */
@Component
@Profile("!azure-kms")
public class GestionnaireClesLocal implements GestionnaireCles {

    private static final Logger log = LoggerFactory.getLogger(GestionnaireClesLocal.class);

    static final String ID_DEVELOPPEMENT = "developpement";
    private static final String GRAINE_DEVELOPPEMENT = "vina-cle-maitresse-de-developpement-publique";
    private static final int IV = 12;

    private final Map<String, SecretKey> maitresses = new HashMap<>();
    private final String active;
    private final AleaCryptographique alea;

    public GestionnaireClesLocal(ChiffrementProprietes proprietes, ClmProprietes clm, AleaCryptographique alea) {
        this.alea = alea;
        boolean sansCle = proprietes.cleMaitresse() == null || proprietes.cleMaitresse().isBlank();
        if (sansCle && clm.production()) {
            throw new IllegalStateException("CLM_CLE_MAITRESSE requise en production (chiffrement au repos, ADR-037).");
        }
        this.active = sansCle ? ID_DEVELOPPEMENT : proprietes.cleMaitresseId();
        proprietes.anciennesCles().forEach((id, valeur) -> maitresses.put(id, cle(valeur)));
        maitresses.put(active, sansCle ? developpement() : cle(proprietes.cleMaitresse()));
        if (sansCle) {
            log.warn("Chiffrement au repos : clé maîtresse de DÉVELOPPEMENT (CLM_CLE_MAITRESSE absente) ; à ne jamais employer en production.");
        }
    }

    @Override
    public CleEnveloppee envelopper(byte[] cleDonnees) {
        byte[] iv = alea.octets(IV);
        byte[] chiffre = appliquer(Cipher.ENCRYPT_MODE, maitresses.get(active), iv, active, cleDonnees);
        return new CleEnveloppee(active, Base64.getEncoder().encodeToString(ByteBuffer.allocate(IV + chiffre.length).put(iv).put(chiffre).array()));
    }

    @Override
    public byte[] desenvelopper(CleEnveloppee cle) {
        SecretKey maitresse = maitresses.get(cle.cleMaitresse());
        if (maitresse == null) {
            throw new CleIllisible("Clé maîtresse inconnue : " + cle.cleMaitresse() + ".", null);
        }
        byte[] octets = decoder(cle.valeur());
        if (octets.length <= IV) {
            throw new CleIllisible("Clé enveloppée tronquée.", null);
        }
        ByteBuffer b = ByteBuffer.wrap(octets);
        byte[] iv = new byte[IV];
        b.get(iv);
        byte[] chiffre = new byte[b.remaining()];
        b.get(chiffre);
        return appliquer(Cipher.DECRYPT_MODE, maitresse, iv, cle.cleMaitresse(), chiffre);
    }

    @Override
    public String cleMaitresseActive() {
        return active;
    }

    private static byte[] appliquer(int mode, SecretKey cle, byte[] iv, String id, byte[] donnees) {
        try {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(mode, cle, new GCMParameterSpec(128, iv));
            c.updateAAD(id.getBytes(StandardCharsets.UTF_8));
            return c.doFinal(donnees);
        } catch (GeneralSecurityException e) {
            throw new CleIllisible("Enveloppe de clé refusée : " + e.getClass().getSimpleName() + ".", e);
        }
    }

    private static byte[] decoder(String valeur) {
        try {
            return Base64.getDecoder().decode(valeur);
        } catch (IllegalArgumentException e) {
            throw new CleIllisible("Clé enveloppée illisible (Base64).", e);
        }
    }

    static SecretKey cle(String base64) {
        byte[] octets = Base64.getDecoder().decode(base64.strip());
        if (octets.length != 32) {
            throw new IllegalArgumentException("Une clé maîtresse locale compte 32 octets (AES-256), en Base64.");
        }
        return new SecretKeySpec(octets, "AES");
    }

    /** Clé de développement publique : la graine, tronquée ou complétée à 32 octets ; aucun secret. */
    private static SecretKey developpement() {
        return new SecretKeySpec(java.util.Arrays.copyOf(GRAINE_DEVELOPPEMENT.getBytes(StandardCharsets.UTF_8), 32), "AES");
    }
}
