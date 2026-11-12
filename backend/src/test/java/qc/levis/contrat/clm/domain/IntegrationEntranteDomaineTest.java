package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Clients d'intégration, lots et rappels sortants (addenda 1) : règles du domaine, sans infrastructure. */
class IntegrationEntranteDomaineTest {

    private static final Instant T0 = Instant.parse("2026-11-11T12:00:00Z");

    private static ClientIntegration client(String url) {
        return ClientIntegration.creer(7L, " Workday RH ", ClientIntegration.Systeme.WORKDAY, url, "clm-1", "s3cret", "chiffre", "admin", T0);
    }

    @Test
    void un_client_s_authentifie_par_son_secret_tant_qu_il_n_est_pas_revoque() {
        ClientIntegration c = client("https://wd.iq.example/rappels");
        assertEquals("Workday RH", c.getNom());
        assertTrue(c.authentifie("s3cret"));
        assertFalse(c.authentifie("autre"));
        assertFalse(c.authentifie(null));
        assertTrue(c.rappelConfigure());
        c.revoquer(T0);
        assertFalse(c.isActif());
        assertEquals(T0, c.getRevoqueLe());
        assertFalse(c.authentifie("s3cret"), "révoqué : jamais authentifié");
        assertThrows(IllegalStateException.class, () -> c.revoquer(T0));
    }

    @Test
    void l_adresse_de_rappel_est_https_ou_locale_et_facultative() {
        assertFalse(client(null).rappelConfigure());
        assertFalse(client("  ").rappelConfigure());
        assertEquals("http://localhost:8080/r", client("http://localhost:8080/r").getUrlRappel());
        assertEquals("http://127.0.0.1:9/r", client("http://127.0.0.1:9/r").getUrlRappel());
    }

    @ParameterizedTest
    @ValueSource(strings = {"http://wd.iq.example/r", "ftp://wd.iq.example/r", "ftp://localhost/r", "https:/sans-hote", "pas une url", "https://a b/c"})
    void une_adresse_de_rappel_non_sure_est_refusee(String url) {
        assertThrows(IllegalArgumentException.class, () -> client(url));
    }

    @Test
    void nom_et_systeme_sont_exiges() {
        assertThrows(IllegalArgumentException.class, () -> ClientIntegration.creer(7L, " ", ClientIntegration.Systeme.AUTRE, null, "c", "s", "k", "a", T0));
        assertThrows(IllegalArgumentException.class, () -> ClientIntegration.creer(7L, null, ClientIntegration.Systeme.AUTRE, null, "c", "s", "k", "a", T0));
        assertThrows(IllegalArgumentException.class, () -> ClientIntegration.creer(7L, "x".repeat(121), ClientIntegration.Systeme.AUTRE, null, "c", "s", "k", "a", T0));
        assertThrows(IllegalArgumentException.class, () -> ClientIntegration.creer(7L, "SF", null, null, "c", "s", "k", "a", T0));
    }

    @Test
    void un_lot_se_clot_selon_ses_echecs_et_ne_depasse_jamais_son_total() {
        LotSignature ok = LotSignature.ouvrir(7L, " Conventions ", 3L, null, 2, true, "init", T0);
        assertEquals("Conventions", ok.getLibelle());
        assertEquals(LotSignature.Statut.EN_COURS, ok.getStatut());
        ok.noterReussite();
        ok.noterReussite();
        assertEquals(LotSignature.Statut.TERMINE, ok.getStatut());
        assertThrows(IllegalStateException.class, ok::noterEchec);

        LotSignature mixte = LotSignature.ouvrir(7L, null, null, 4L, 2, false, "sf", T0);
        assertEquals("Lot du 2026-11-11", mixte.getLibelle());
        mixte.noterReussite();
        mixte.noterEchec();
        assertEquals(LotSignature.Statut.TERMINE_AVEC_ECHECS, mixte.getStatut());
        assertEquals(1, mixte.getEchecs());
        assertEquals(LotSignature.TAILLE_MAX, LotSignature.ouvrir(7L, "x".repeat(300), null, null, LotSignature.TAILLE_MAX, false, "a", T0)
                .getTotal());
        assertEquals(200, LotSignature.ouvrir(7L, "x".repeat(300), null, null, 1, false, "a", T0).getLibelle().length());
        assertThrows(IllegalArgumentException.class, () -> LotSignature.ouvrir(7L, "x", null, null, 0, false, "a", T0));
        assertThrows(IllegalArgumentException.class, () -> LotSignature.ouvrir(7L, "x", null, null, LotSignature.TAILLE_MAX + 1, false, "a", T0));
    }

    @Test
    void un_element_garde_sa_trousse_ou_son_motif_d_echec() {
        LotSignature lot = LotSignature.ouvrir(7L, "L", null, null, 3, true, "a", T0);
        assertEquals(ElementLot.Statut.LANCEE, ElementLot.reussi(lot, 1, "R1", 10L, true).getStatut());
        ElementLot cree = ElementLot.reussi(lot, 2, null, 11L, false);
        assertEquals(ElementLot.Statut.CREEE, cree.getStatut());
        assertEquals(11L, cree.getTrousseId());
        assertNull(cree.getId(), "attribué à l'enregistrement");
        assertEquals(lot.getId(), cree.getLotId());
        assertEquals("Erreur inconnue.", ElementLot.echoue(lot, 3, "R3", null).getErreur());
        assertEquals("Erreur inconnue.", ElementLot.echoue(lot, 3, "R3", " ").getErreur());
        assertEquals(500, ElementLot.echoue(lot, 3, "R3", "x".repeat(800)).getErreur().length());
    }

    @Test
    void un_rappel_est_repris_puis_abandonne_et_peut_etre_relance() {
        RappelSortant r = new RappelSortant(7L, 1L, 10L, "LANCEE", "7:10:LANCEE:1", "{}", T0);
        for (int i = 0; i < RappelSortant.REPRISES_MAX - 1; i++) {
            r.echouer("HTTP 503", T0);
        }
        assertEquals(RappelSortant.Statut.EN_ATTENTE, r.getStatut());
        assertTrue(r.getProchainEssai().isAfter(T0));
        r.echouer(null, T0);
        assertEquals(RappelSortant.Statut.ECHEC, r.getStatut());
        assertNull(r.getDerniereErreur());
        r.relancer(T0);
        assertEquals(RappelSortant.Statut.EN_ATTENTE, r.getStatut());
        assertEquals(0, r.getTentatives());
        r.echouer("x".repeat(1200), T0);
        assertEquals(1000, r.getDerniereErreur().length());
        r.reussir(T0);
        assertEquals(T0, r.getEnvoyeLe());
        assertThrows(IllegalStateException.class, () -> r.relancer(T0), "un rappel livré ne se relance pas");
    }

    @Test
    void une_trousse_rattachee_garde_son_origine_reelle() {
        Trousse t = new Trousse(7L, "TR-1", "T", "init@iq.example", T0);
        t.rattacherExterne(Trousse.Source.WORKDAY, 3L, "  WD-1 ", 9L);
        assertEquals(Trousse.Source.WORKDAY, t.getSource());
        assertEquals("WD-1", t.getReferenceExterne());
        assertEquals(3L, t.getClientIntegrationId());
        assertEquals(9L, t.getLotId());

        Trousse sf = new Trousse(7L, "TR-2", "T", "init@iq.example", T0);
        sf.setSource(Trousse.Source.SALESFORCE);
        sf.rattacherExterne(Trousse.Source.INTEGRATION, null, " ", null);
        assertEquals(Trousse.Source.SALESFORCE, sf.getSource(), "une trousse préremplie depuis Salesforce le reste");
        assertNull(sf.getReferenceExterne());
        assertThrows(IllegalArgumentException.class, () -> sf.rattacherExterne(Trousse.Source.INTEGRATION, null, "x".repeat(121), null));
        sf.rattacherExterne(Trousse.Source.INTEGRATION, null, null, null);
        assertNull(sf.getReferenceExterne(), "sans référence externe");
    }
}
