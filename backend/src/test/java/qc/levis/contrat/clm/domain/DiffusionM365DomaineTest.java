package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Diffusion Microsoft 365 des trousses terminées (Annexe 10, TI-01) : réglage borné et dépôt avec reprises. */
class DiffusionM365DomaineTest {

    private static final Instant T0 = Instant.parse("2026-11-25T12:00:00Z");

    @Test
    void le_reglage_exige_une_bibliotheque_et_un_dossier_sans_traversee() {
        assertEquals(List.of(), reglage(DiffusionM365.Bibliotheque.SHAREPOINT, "Contrats signés/IQ").erreurs());
        assertEquals(List.of("Bibliothèque de dépôt requise : SHAREPOINT ou ONEDRIVE."), reglage(null, "Contrats").erreurs());
        String borne = "Dossier de dépôt : 1 à 300 caractères.";
        assertEquals(List.of(borne), reglage(DiffusionM365.Bibliotheque.ONEDRIVE, null).erreurs());
        assertEquals(List.of(borne), reglage(DiffusionM365.Bibliotheque.ONEDRIVE, "  ").erreurs());
        assertEquals(List.of(borne), reglage(DiffusionM365.Bibliotheque.ONEDRIVE, "x".repeat(301)).erreurs());
        assertEquals(List.of(), reglage(DiffusionM365.Bibliotheque.ONEDRIVE, "x".repeat(300)).erreurs());
        String traversee = "Dossier de dépôt : « .. » et « \\ » sont interdits.";
        assertEquals(List.of(traversee), reglage(DiffusionM365.Bibliotheque.ONEDRIVE, "Contrats/ .. /autre").erreurs());
        assertEquals(List.of(traversee), reglage(DiffusionM365.Bibliotheque.ONEDRIVE, "Contrats\\autre").erreurs());
        assertEquals(List.of(), reglage(DiffusionM365.Bibliotheque.ONEDRIVE, "Contrats/..v2").erreurs(), "« ..v2 » n'est pas une traversée");
    }

    @Test
    void la_configuration_garde_le_reglage_et_son_auteur() {
        DiffusionM365 c = new DiffusionM365(7L);
        DiffusionM365.Reglage r = new DiffusionM365.Reglage(true, DiffusionM365.Bibliotheque.ONEDRIVE, "Signés", false, true);
        c.appliquer(r, "admin.technique@iq.example", T0);
        assertEquals(r, c.reglage());
        assertEquals(7L, c.getTenantId());
        assertEquals("admin.technique@iq.example", c.getModifiePar());
        assertEquals(T0, c.getModifieLe());
        assertFalse(DiffusionM365.Reglage.INACTIF.actif(), "aucune diffusion tant que l'organisation ne l'active pas");
    }

    @Test
    void un_depot_reprend_avec_recul_puis_s_abandonne_et_se_relance() {
        DepotM365 d = new DepotM365(3L, 9L, DepotM365.Nature.DOCUMENT_SIGNE, 41L, T0);
        assertEquals("3:9:DOCUMENT_SIGNE:41", d.getCleIdempotence());
        assertEquals("3:9:PREUVE", new DepotM365(3L, 9L, DepotM365.Nature.PREUVE, null, T0).getCleIdempotence());
        assertEquals(DepotM365.Statut.EN_ATTENTE, d.getStatut());
        assertEquals(T0, d.getProchainEssai());
        d.echouer("x".repeat(1200), T0);
        assertEquals(1000, d.getDerniereErreur().length(), "erreur tronquée à la colonne");
        assertEquals(T0.plusSeconds(60), d.getProchainEssai());
        d.echouer(null, T0);
        assertNull(d.getDerniereErreur());
        for (int i = 2; i < DepotM365.REPRISES_MAX; i++) {
            d.echouer("Graph 503", T0);
        }
        assertEquals(DepotM365.Statut.ECHEC, d.getStatut());
        assertEquals(DepotM365.REPRISES_MAX, d.getTentatives());
        d.relancer(T0.plusSeconds(5));
        assertEquals(DepotM365.Statut.EN_ATTENTE, d.getStatut());
        assertEquals(0, d.getTentatives());
        assertEquals(T0.plusSeconds(5), d.getProchainEssai());
        d.reussir("sim://sharepoint/x.pdf", T0.plusSeconds(9));
        assertEquals(DepotM365.Statut.DEPOSE, d.getStatut());
        assertEquals("sim://sharepoint/x.pdf", d.getUrl());
        assertEquals(T0.plusSeconds(9), d.getDeposeLe());
        assertNull(d.getDerniereErreur());
        assertThrows(IllegalStateException.class, () -> d.relancer(T0));
        assertEquals(3L, d.getTenantId());
        assertEquals(9L, d.getTrousseId());
        assertEquals(41L, d.getDocumentId());
        assertEquals(DepotM365.Nature.DOCUMENT_SIGNE, d.getNature());
        assertEquals(T0, d.getCreeLe());
        assertNull(d.getId());
    }

    private static DiffusionM365.Reglage reglage(DiffusionM365.Bibliotheque b, String dossier) {
        return new DiffusionM365.Reglage(true, b, dossier, true, true);
    }
}
