package qc.levis.contrat.vieprivee;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.audit.domain.AuditLog;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.notification.domain.NotificationEnvoi;
import qc.levis.contrat.redaction.domain.Comment;
import qc.levis.contrat.vieprivee.domain.*;

class VieePriveeDomaineTest {

    private static final Pseudonyme P = Pseudonyme.nouveau(UUID.fromString("12345678-9abc-def0-1234-56789abcdef0"));

    @Test
    void le_pseudonyme_est_tire_au_hasard_et_donne_une_adresse_non_routable() {
        assertEquals("personne-effacee-12345678-9ab", P.valeur());
        assertEquals("personne-effacee-12345678-9ab@efface.invalid", P.courriel());
        assertThrows(IllegalArgumentException.class, () -> new Pseudonyme("marie@levis.qc.ca"));
        assertThrows(NullPointerException.class, () -> new Pseudonyme(null));
    }

    @Test
    void un_renseignement_communique_vide_une_valeur_absente() {
        Renseignement r = Renseignement.de("compte/1", null, "nom", "Marie", "direction", null);
        assertEquals("", r.valeurs().get("direction"));
        assertEquals("Marie", r.valeurs().get("nom"));
        assertThrows(IllegalArgumentException.class, () -> Renseignement.de("x", null, "seul"));
    }

    @Test
    void le_dossier_compte_les_renseignements_de_toutes_les_sources() {
        Renseignement r = Renseignement.de("a", Instant.EPOCH, "k", "v");
        DossierRenseignements d = new DossierRenseignements("m@x.ca", Instant.EPOCH, List.of(
                new ExtraitRenseignements("A", "d", "e", List.of(r, r)), new ExtraitRenseignements("B", "d", "e", List.of(r))));
        assertEquals(3, d.total());
        assertEquals(new BilanEffacement("S", 2, 0, null), BilanEffacement.complet("S", 2));
        DemandeVieePrivee dem = new DemandeVieePrivee(1L, DemandeVieePrivee.Type.ACCES, "e", "admin", "m", "r", Instant.EPOCH);
        assertEquals(List.of(1L, "e", "admin", "m", "r", Instant.EPOCH, DemandeVieePrivee.Type.ACCES),
                List.of(dem.getTenantId(), dem.getEmpreinteCourriel(), dem.getTraitePar(), dem.getMotif(), dem.getResultat(),
                        dem.getTraiteeLe(), dem.getType()));
        assertNull(dem.getId());
        assertEquals("refus", new EffacementRefuse("refus").getMessage());
    }

    @Test
    void le_journal_garde_l_evenement_sans_nommer_la_personne() {
        AuditLog a = new AuditLog("Marie@Levis.qc.ca", "LOGIN", "User", 1L, "connexion de marie@levis.qc.ca depuis le portail");
        a.pseudonymiser("marie@levis.qc.ca", P.valeur());
        assertEquals(P.valeur(), a.getUtilisateur());
        assertEquals("connexion de " + P.valeur() + " depuis le portail", a.getDetails());
        AuditLog autre = new AuditLog("admin@levis.qc.ca", "CREATE", "User", 2L, null);
        autre.pseudonymiser("marie@levis.qc.ca", P.valeur());
        assertEquals("admin@levis.qc.ca", autre.getUtilisateur());
        assertNull(autre.getDetails());
    }

    @Test
    void la_notification_perd_destinataire_objet_et_contenu() {
        NotificationEnvoi n = new NotificationEnvoi(1L, "COURRIEL", "marie@levis.qc.ca", "Votre demande", "Bonjour Marie", "ENVOYE");
        n.pseudonymiser(P.courriel());
        assertEquals(P.courriel(), n.getDestinataire());
        assertEquals(NotificationEnvoi.CONTENU_EFFACE, n.getSujet());
        assertEquals(NotificationEnvoi.CONTENU_EFFACE, n.getCorps());
    }

    @Test
    void le_commentaire_garde_son_texte_mais_plus_ses_auteurs() {
        Comment c = new Comment();
        c.setAuteur("marie@levis.qc.ca");
        c.setResoluPar("MARIE@levis.qc.ca");
        c.setCorps("À revoir");
        c.pseudonymiser("marie@levis.qc.ca", P.valeur());
        assertEquals(P.valeur(), c.getAuteur());
        assertEquals(P.valeur(), c.getResoluPar());
        Comment d = new Comment();
        d.setAuteur("jean@levis.qc.ca");
        d.pseudonymiser("marie@levis.qc.ca", P.valeur());
        assertEquals("jean@levis.qc.ca", d.getAuteur());
        assertNull(d.getResoluPar());
    }

    @Test
    void le_compte_efface_ne_designe_plus_personne_et_ne_s_ouvre_plus() {
        User u = new User();
        u.setEmail("marie@levis.qc.ca");
        u.setNom("Marie");
        u.setDirection("Approvisionnement");
        u.setRoles(Set.of(Enums.Role.REDACTEUR));
        u.setMfaActive(true);
        u.setMfaMethode(Enums.MfaMethode.TOTP);
        u.setTotpSecret("SECRET");
        u.setCompteSecours(true);
        u.effacer(P.courriel(), "!compte-efface");
        assertEquals(P.courriel(), u.getEmail());
        assertEquals("Personne effacée", u.getNom());
        assertNull(u.getDirection());
        assertFalse(u.isActif());
        assertTrue(u.getRoles().isEmpty());
        assertFalse(u.isMfaActive());
        assertFalse(u.isCompteSecours());
        assertEquals(Enums.MfaMethode.NONE, u.getMfaMethode());
        assertNull(u.getTotpSecret());
        assertTrue(u.getQuestionsSecurite().isEmpty());
    }
}
