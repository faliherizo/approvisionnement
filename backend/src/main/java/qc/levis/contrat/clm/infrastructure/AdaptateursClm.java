package qc.levis.contrat.clm.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.port.AnalyseAntivirus;
import qc.levis.contrat.clm.application.port.NotificateurClm;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.port.Scelleur;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.notification.application.port.CanalNotification;
import qc.levis.contrat.securite.application.SecuriteService;

/** Adaptateurs techniques simples du module CLM. */
public final class AdaptateursClm {

    private AdaptateursClm() {
    }

    @Component
    public static class ScelleurHmac implements Scelleur {
        private final byte[] cle;

        public ScelleurHmac(ClmProprietes proprietes) {
            this.cle = proprietes.preuve().cle().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String sceller(byte[] contenu) {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(cle, "HmacSHA256"));
                return HexFormat.of().formatHex(mac.doFinal(contenu));
            } catch (Exception e) {
                throw new IllegalStateException("Scellement impossible", e);
            }
        }

        @Override
        public boolean verifier(byte[] contenu, String sceau) {
            return sceau != null && MessageDigest.isEqual(sceller(contenu).getBytes(StandardCharsets.US_ASCII),
                    sceau.getBytes(StandardCharsets.US_ASCII));
        }
    }

    @Component
    public static class SourceAleatoireSecurisee implements SourceAleatoire {
        private final SecureRandom alea = new SecureRandom();

        @Override
        public String jeton() {
            byte[] octets = new byte[32];
            alea.nextBytes(octets);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
        }

        @Override
        public String code() {
            return String.format("%06d", alea.nextInt(1_000_000));
        }

        @Override
        public String uuid() {
            return UUID.randomUUID().toString();
        }
    }

    /** Détecte la signature de test EICAR ; à remplacer par l'antivirus d'IQ (ClamAV/ICAP) en production. */
    @Component
    public static class AntivirusSignatures implements AnalyseAntivirus {
        private static final byte[] EICAR = "EICAR-STANDARD-ANTIVIRUS-TEST-FILE".getBytes(StandardCharsets.US_ASCII);

        @Override
        public boolean estSain(byte[] contenu) {
            outer:
            for (int i = 0; i <= contenu.length - EICAR.length; i++) {
                for (int j = 0; j < EICAR.length; j++) {
                    if (contenu[i + j] != EICAR[j]) {
                        continue outer;
                    }
                }
                return false;
            }
            return true;
        }
    }

    @Component
    public static class NotificateurCanaux implements NotificateurClm {
        private final List<CanalNotification> canaux;

        public NotificateurCanaux(List<CanalNotification> canaux) {
            this.canaux = canaux;
        }

        @Override
        public String envoyer(String canal, String destinataire, String sujet, String corps) {
            if (destinataire == null || destinataire.isBlank()) {
                return "ECHEC";
            }
            return canaux.stream().filter(c -> c.canal().equals(canal)).findFirst()
                    .map(c -> c.envoyer(destinataire, sujet, corps)).orElse("ECHEC");
        }
    }

    @Component
    public static class PerimetreSecurite implements PerimetreUtilisateur {
        private final SecuriteService securite;

        public PerimetreSecurite(SecuriteService securite) {
            this.securite = securite;
        }

        @Override
        public Set<String> groupes(Long tenantId, String utilisateur) {
            return tenantId == null || utilisateur == null ? Set.of() : securite.groupesDe(tenantId, utilisateur);
        }
    }
}
