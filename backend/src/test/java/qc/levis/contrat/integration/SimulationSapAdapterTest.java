package qc.levis.contrat.integration;

import java.math.BigDecimal;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;
import qc.levis.contrat.integration.application.port.ReferenceFinancierePort.ReferenceFinanciere;
import qc.levis.contrat.integration.infrastructure.SimulateurDepotStore;
import qc.levis.contrat.integration.infrastructure.SimulationSapAdapter;
import static org.junit.jupiter.api.Assertions.*;

/** Tests unitaires de l'adaptateur SAP par défaut (simulation), avec enregistrement visualisable. */
class SimulationSapAdapterTest {

    @TempDir Path repertoire;
    private SimulateurDepotStore store;
    private SimulationSapAdapter adaptateur;

    @BeforeEach
    void setUp() {
        store = new SimulateurDepotStore(repertoire.toString());
        adaptateur = new SimulationSapAdapter(store);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    @Test
    void deposer_avecNumeroAO_utiliseLeNumero() {
        ResultatPublication r = adaptateur.deposer(
                new ReferenceFinanciere(1L, "7000017905", "Objet", 3, new BigDecimal("150000")));
        assertTrue(r.succes());
        assertEquals("sap://dossier/7000017905", r.url());
        assertTrue(r.message().contains("150000"));

        // Un enregistrement SAP lisible est écrit dans le magasin du simulateur.
        var depots = store.lister(1L);
        assertEquals(1, depots.size());
        assertEquals("SAP", depots.get(0).cible());
        assertTrue(new String(store.lire(1L, depots.get(0).chemin())).contains("7000017905"));
    }

    @Test
    void deposer_sansNumeroAO_repliSurIdentifiant() {
        ResultatPublication r = adaptateur.deposer(
                new ReferenceFinanciere(42L, null, "Objet", 0, BigDecimal.ZERO));
        assertEquals("sap://dossier/dossier-42", r.url());
    }

    @Test
    void deposer_numeroAOVide_repliSurIdentifiant() {
        ResultatPublication r = adaptateur.deposer(
                new ReferenceFinanciere(42L, "   ", "Objet", 0, BigDecimal.ZERO));
        assertEquals("sap://dossier/dossier-42", r.url());
    }
}
