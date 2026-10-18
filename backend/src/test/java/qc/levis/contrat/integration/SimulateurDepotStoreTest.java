package qc.levis.contrat.integration;

import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import qc.levis.contrat.integration.infrastructure.SimulateurDepotStore;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires du magasin de dépôts simulés (écriture, listing, lecture, isolation, sécurité). */
class SimulateurDepotStoreTest {

    @TempDir Path repertoire;
    private SimulateurDepotStore store;

    @BeforeEach
    void setUp() { store = new SimulateurDepotStore(repertoire.toString()); }

    @Test
    void enregistrer_puis_lister_et_lire() {
        String chemin = store.enregistrer("SHAREPOINT", 1L, "Contrats/2027", "c.pdf",
                "abc".getBytes(StandardCharsets.UTF_8));
        assertEquals("SHAREPOINT/Contrats/2027/c.pdf", chemin);

        List<SimulateurDepotStore.Depot> depots = store.lister(1L);
        assertEquals(1, depots.size());
        assertEquals("SHAREPOINT", depots.get(0).cible());
        assertEquals(3, depots.get(0).taille());
        assertEquals("abc", new String(store.lire(1L, chemin), StandardCharsets.UTF_8));
    }

    @Test
    void enregistrer_nettoieLaTraverseeDansLaDestination() {
        String chemin = store.enregistrer("ONEDRIVE", 1L, "a/../../b", "f.txt", new byte[]{1});
        assertEquals("ONEDRIVE/a/b/f.txt", chemin);   // « .. » retiré, reste sous le tenant
    }

    @Test
    void tenants_sontIsoles() {
        store.enregistrer("TEAMS", 1L, "", "t1.txt", new byte[]{1});
        store.enregistrer("TEAMS", 2L, "", "t2.txt", new byte[]{2});
        assertEquals(1, store.lister(1L).size());
        assertEquals(1, store.lister(2L).size());
    }

    @Test
    void lister_tenantSansDepot_renvoieVide() {
        assertTrue(store.lister(99L).isEmpty());
    }

    @Test
    void enregistrer_nomVide_donneSansNom() {
        String chemin = store.enregistrer("OUTLOOK", 1L, "", "", new byte[]{1});
        assertEquals("OUTLOOK/sans-nom", chemin);
    }

    @Test
    void enregistrer_destinationEntierementTraversee_placeSousLaCible() {
        String chemin = store.enregistrer("TEAMS", 1L, "../..", "f.txt", new byte[]{1});
        assertEquals("TEAMS/f.txt", chemin);   // « .. » retirés → directement sous l'environnement
    }

    @Test
    void enregistrer_nomAvecChemin_neGardeQueLeDernierSegment() {
        String chemin = store.enregistrer("ONEDRIVE", 1L, "d", "a/b/evil.txt", new byte[]{1});
        assertEquals("ONEDRIVE/d/evil.txt", chemin);
    }

    @Test
    void lire_horsDuTenant_refuse() {
        store.enregistrer("SHAREPOINT", 1L, "", "ok.txt", new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> store.lire(1L, "../../etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> store.lire(1L, "SHAREPOINT/inexistant.txt"));
    }

    @Test
    void enregistrer_erreurEcriture_estEnveloppee() throws Exception {
        // Place un fichier là où un répertoire d'environnement est attendu → createDirectories échoue.
        Path tenant = repertoire.resolve("1");
        Files.createDirectories(tenant);
        Files.writeString(tenant.resolve("SHAREPOINT"), "fichier, pas dossier");
        assertThrows(UncheckedIOException.class,
                () -> store.enregistrer("SHAREPOINT", 1L, "sous-dossier", "f.txt", new byte[]{1}));
    }
}
