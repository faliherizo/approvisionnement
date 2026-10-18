package qc.levis.contrat.api;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.infrastructure.SimulateurDepotStore;
import qc.levis.contrat.integration.infrastructure.web.SimulateurController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Tests unitaires du contrôleur de visualisation des dépôts simulés (listing, type média, aperçu). */
class SimulateurControllerTest {

    private SimulateurDepotStore store;
    private SimulateurController controller;

    @BeforeEach
    void setUp() {
        store = mock(SimulateurDepotStore.class);
        controller = new SimulateurController(store);
        TenantContext.set(1L);
        when(store.lire(eq(1L), any())).thenReturn(new byte[]{1, 2, 3});
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    @Test
    void lister_delegueAuMagasin() {
        when(store.lister(1L)).thenReturn(List.of(
                new SimulateurDepotStore.Depot("SHAREPOINT", "SHAREPOINT/c.pdf", "c.pdf", 3, Instant.now())));
        assertEquals(1, controller.lister().size());
    }

    @Test
    void contenu_choisitLeTypeMediaSelonExtension() {
        assertEquals(MediaType.APPLICATION_PDF, type("SHAREPOINT/Contrats/c.pdf"));
        assertEquals(MediaType.TEXT_PLAIN, type("SAP/7000017905.txt"));
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                type("ONEDRIVE/c.docx").toString());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, type("TEAMS/archive.bin"));
    }

    @Test
    void contenu_renvoieLesOctets_enApercuInline() {
        ResponseEntity<byte[]> r = controller.contenu("SHAREPOINT/c.pdf");
        assertArrayEquals(new byte[]{1, 2, 3}, r.getBody());
        assertTrue(r.getHeaders().getFirst("Content-Disposition").contains("inline"));
        assertTrue(r.getHeaders().getFirst("Content-Disposition").contains("c.pdf"));
    }

    private MediaType type(String chemin) {
        return controller.contenu(chemin).getHeaders().getContentType();
    }
}
