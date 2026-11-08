package qc.levis.contrat.clm.infrastructure;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.port.AlertesExploitation;
import qc.levis.contrat.clm.application.port.AnalyseAntivirus;
import qc.levis.contrat.clm.application.port.AnnuaireOrganisation;
import qc.levis.contrat.clm.application.port.IdentiteOrganisation;
import qc.levis.contrat.clm.application.port.NotificateurClm;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.port.PorteeTenant;
import qc.levis.contrat.clm.application.port.ReferencesClm;
import qc.levis.contrat.clm.application.port.Scelleur;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.notification.application.port.CanalNotification;
import qc.levis.contrat.reference.application.ReferenceService;
import qc.levis.contrat.securite.application.SecuriteService;
import qc.levis.contrat.tenant.application.AdminUserService;
import qc.levis.contrat.tenant.application.TenantAdminService;

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

    /**
     * Alertes d'exploitation au journal applicatif, sur une ligne au format stable « CLM_ALERTE code=… » que Log Analytics
     * relève (règles de infra/terraform/azure/monitoring.tf, procédures du runbook d'exploitation CLM).
     */
    @Component
    public static class AlertesJournal implements AlertesExploitation {
        private static final Logger ALERTES = LoggerFactory.getLogger("qc.levis.contrat.clm.ALERTES");

        @Override
        public void signaler(Code code, Long tenantId, String ressource, String detail) {
            ALERTES.error("CLM_ALERTE code={} tenant={} ressource={} detail={}", code, tenantId == null ? "-" : tenantId,
                    neutre(ressource), neutre(detail));
        }

        /** Une valeur tient sur une ligne : un retour chariot injecté ne forge pas une fausse entrée de journal. */
        static String neutre(String valeur) {
            return valeur == null || valeur.isBlank() ? "-" : valeur.replaceAll("[\\r\\n\\t]+", " ").trim();
        }
    }

    /** Identité publique de l'organisation, lue par le service des tenants (nom et logo de son thème). */
    @Component
    public static class IdentiteTenant implements IdentiteOrganisation {
        private final TenantAdminService tenants;

        public IdentiteTenant(TenantAdminService tenants) {
            this.tenants = tenants;
        }

        @Override
        public Organisation de(Long tenantId) {
            return tenants.publicParId(tenantId).map(t -> new Organisation(t.titre(), t.theme().logoImage()))
                    .orElse(new Organisation(null, null));
        }
    }

    /** Tenant porté par le fil d'exécution, alimenté par le filtre d'authentification de la plateforme. */
    @Component
    public static class PorteeTenantFil implements PorteeTenant {
        @Override
        public Long courant() {
            return TenantContext.get();
        }

        @Override
        public <T> T executerDans(Long tenantId, Supplier<T> action) {
            Long precedent = TenantContext.get();
            TenantContext.set(tenantId);
            try {
                return action.get();
            } finally {
                // Retirer plutôt que poser null : un fil réutilisé par le serveur ne garde aucune trace du traitement.
                if (precedent == null) {
                    TenantContext.clear();
                } else {
                    TenantContext.set(precedent);
                }
            }
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

    /** Annuaire de l'organisation, lu par le service des comptes : courriel et nom, jamais les rôles ni les secrets. */
    @Component
    public static class AnnuaireComptes implements AnnuaireOrganisation {
        private final AdminUserService comptes;

        public AnnuaireComptes(AdminUserService comptes) {
            this.comptes = comptes;
        }

        @Override
        public List<Membre> membres(Long tenantId) {
            return comptes.membresActifs(tenantId).stream().map(m -> new Membre(m.courriel(), m.nom())).toList();
        }
    }

    /** Listes effectives de l'organisation, servies par le service des références de la plateforme. */
    @Component
    public static class ReferencesPlateforme implements ReferencesClm {
        private final ReferenceService references;

        public ReferencesPlateforme(ReferenceService references) {
            this.references = references;
        }

        @Override
        public List<Valeur> valeurs(String liste) {
            return references.options(liste).stream().map(o -> new Valeur(o.code(), o.libelle(), o.detail())).toList();
        }
    }
}
