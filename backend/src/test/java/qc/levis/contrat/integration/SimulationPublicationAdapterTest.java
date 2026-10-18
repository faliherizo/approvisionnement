package qc.levis.contrat.integration;

import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;
import qc.levis.contrat.integration.infrastructure.SimulateurDepotStore;
import qc.levis.contrat.integration.infrastructure.SimulationPublicationAdapter;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires de l'adaptateur de publication par défaut (simulation), avec écriture réelle. */
class SimulationPublicationAdapterTest {

    @TempDir Path repertoire;
    private SimulateurDepotStore store;
    private SimulationPublicationAdapter adaptateur;

    @BeforeEach
    void setUp() {
        store = new SimulateurDepotStore(repertoire.toString());
        adaptateur = new SimulationPublicationAdapter(store);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    @Test
    void supporte_touteCible() {
        for (Enums.CiblePublication cible : Enums.CiblePublication.values()) {
            assertTrue(adaptateur.supporte(cible));
        }
    }

    @Test
    void deposer_avecRepertoire_urlInclutLeDossier() {
        var contenu = new PublicationCiblePort.ContenuPublie(1L, "Contrat", "contrat-1.pdf",
                "application/pdf", new byte[]{1, 2, 3, 4}, "Contrats/2027");
        PublicationCiblePort.ResultatPublication r =
                adaptateur.deposer(Enums.CiblePublication.SHAREPOINT, contenu);

        assertTrue(r.succes());
        assertEquals("sim://sharepoint/Contrats/2027/contrat-1.pdf", r.url());
        assertTrue(r.message().contains("4 octets"));

        // Le fichier est réellement écrit et relisable dans le magasin du simulateur.
        var depots = store.lister(1L);
        assertEquals(1, depots.size());
        assertEquals("SHAREPOINT", depots.get(0).cible());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, store.lire(1L, depots.get(0).chemin()));
    }

    @Test
    void deposer_sansRepertoire_urlAlaRacine() {
        var contenu = new PublicationCiblePort.ContenuPublie(1L, "Contrat", "contrat-1.pdf",
                "application/pdf", new byte[]{1}, "");
        PublicationCiblePort.ResultatPublication r =
                adaptateur.deposer(Enums.CiblePublication.ONEDRIVE, contenu);

        assertEquals("sim://onedrive/contrat-1.pdf", r.url());
    }
}
