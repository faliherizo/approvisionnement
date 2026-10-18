package qc.levis.contrat.service;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.dossier.application.DossierService;
import qc.levis.contrat.dossier.domain.Dossier;
import qc.levis.contrat.integration.application.SapReferenceService;
import qc.levis.contrat.integration.application.port.PublicationCiblePort.ResultatPublication;
import qc.levis.contrat.integration.application.port.ReferenceFinancierePort;
import qc.levis.contrat.redaction.domain.Document;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Tests unitaires de {@link SapReferenceService} (agrégation du montant, délégation au port). */
class SapReferenceServiceTest {

    private DossierService dossiers;
    private ReferenceFinancierePort sap;
    private AuditService audit;
    private SapReferenceService service;

    @BeforeEach
    void setUp() {
        dossiers = mock(DossierService.class);
        sap = mock(ReferenceFinancierePort.class);
        audit = mock(AuditService.class);
        service = new SapReferenceService(dossiers, sap, audit);
    }

    private Dossier dossier() {
        Dossier d = new Dossier();
        d.setTitre("Acquisition");
        d.setNumeroAO("7000017905");
        d.setObjet("Objet");
        return d;
    }

    private Document doc(String valeur) {
        Document d = new Document();
        if (valeur != null) d.setValeurEstimee(new BigDecimal(valeur));
        return d;
    }

    @Test
    void transmettre_agregeMontant_etDelegueAuPort() {
        when(dossiers.get(1L)).thenReturn(dossier());
        when(dossiers.documentsDu(1L)).thenReturn(List.of(doc("100000"), doc("50000"), doc(null)));
        when(sap.deposer(any())).thenReturn(new ResultatPublication(true, "sap://dossier/7000017905", "ok"));

        ResultatPublication r = service.transmettre(1L, "appro");

        assertTrue(r.succes());
        ArgumentCaptor<ReferenceFinancierePort.ReferenceFinanciere> ref =
                ArgumentCaptor.forClass(ReferenceFinancierePort.ReferenceFinanciere.class);
        verify(sap).deposer(ref.capture());
        assertEquals(new BigDecimal("150000"), ref.getValue().montantTotal(), "valeurs nulles ignorées");
        assertEquals(3, ref.getValue().nombreDocuments());
        assertEquals("7000017905", ref.getValue().numeroAO());
        verify(audit).log(eq("appro"), eq("REFERENCE_SAP"), eq("Dossier"), eq(1L), any());
    }

    @Test
    void transmettre_dossierSansDocument_montantZero() {
        when(dossiers.get(2L)).thenReturn(dossier());
        when(dossiers.documentsDu(2L)).thenReturn(List.of());
        when(sap.deposer(any())).thenReturn(new ResultatPublication(true, "sap://dossier/x", "ok"));

        service.transmettre(2L, "appro");

        ArgumentCaptor<ReferenceFinancierePort.ReferenceFinanciere> ref =
                ArgumentCaptor.forClass(ReferenceFinancierePort.ReferenceFinanciere.class);
        verify(sap).deposer(ref.capture());
        assertEquals(BigDecimal.ZERO, ref.getValue().montantTotal());
        assertEquals(0, ref.getValue().nombreDocuments());
    }
}
