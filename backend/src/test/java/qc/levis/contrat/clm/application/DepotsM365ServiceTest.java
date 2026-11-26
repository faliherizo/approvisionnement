package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.DepotMicrosoft365;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.repository.DepotM365Repository;
import qc.levis.contrat.clm.application.repository.DiffusionM365Repository;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.DepotM365;
import qc.levis.contrat.clm.domain.DiffusionM365;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Trousse;

/** Diffusion Microsoft 365 des trousses terminées (Annexe 10, TI-01) : quand déposer, quoi, où, et comment reprendre. */
class DepotsM365ServiceTest {

    private static final Instant T0 = Instant.parse("2026-11-25T12:00:00Z");
    private static final Long TENANT = 3L;

    private final DiffusionM365Repository configurations = mock(DiffusionM365Repository.class);
    private final DepotM365Repository depots = mock(DepotM365Repository.class);
    private final DocumentTrousseRepository documents = mock(DocumentTrousseRepository.class);
    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final PreuveService preuves = mock(PreuveService.class);
    private final DepotMicrosoft365 port = mock(DepotMicrosoft365.class);
    private final JournalClm journal = mock(JournalClm.class);
    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(),
            Clock.fixed(T0, ZoneOffset.UTC));
    private final DepotsM365Service service = new DepotsM365Service(configurations, depots, documents, trousses, preuves, port, journal, ctx);

    private void configurer(boolean actif, boolean preuve, boolean avis) {
        DiffusionM365 c = new DiffusionM365(TENANT);
        c.appliquer(new DiffusionM365.Reglage(actif, DiffusionM365.Bibliotheque.SHAREPOINT, " Contrats ", preuve, avis), "ti", T0);
        when(configurations.findByTenantId(TENANT)).thenReturn(Optional.of(c));
    }

    private static Trousse trousse() {
        Trousse t = mock(Trousse.class);
        when(t.getId()).thenReturn(9L);
        when(t.getTenantId()).thenReturn(TENANT);
        when(t.getReference()).thenReturn("TR/2026-1");
        when(t.getTitre()).thenReturn("Entente");
        when(t.getClotureLe()).thenReturn(T0);
        return t;
    }

    private static DocumentTrousse document(long id, DocumentTrousse.Statut statut) {
        DocumentTrousse d = mock(DocumentTrousse.class);
        when(d.getId()).thenReturn(id);
        when(d.getStatut()).thenReturn(statut);
        when(d.getNom()).thenReturn("entente.pdf");
        when(d.getType()).thenReturn(FormatDocument.Type.PDF);
        when(d.getContenu()).thenReturn(new byte[]{1});
        return d;
    }

    @Test
    void seule_la_signature_terminee_d_une_organisation_qui_a_active_la_diffusion_est_deposee() {
        Trousse t = trousse();
        service.enfiler(t, "RETOURNEE", 1, Map.of());
        verifyNoInteractions(configurations, depots);

        when(configurations.findByTenantId(TENANT)).thenReturn(Optional.empty());
        service.enfiler(t, DepotsM365Service.ACHEVEMENT, 2, Map.of());
        verifyNoInteractions(depots);

        configurer(true, false, false);
        DocumentTrousse signe = document(41, DocumentTrousse.Statut.SIGNE);
        DocumentTrousse remplace = document(40, DocumentTrousse.Statut.REMPLACE);
        when(documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(TENANT, 9L)).thenReturn(List.of(signe, remplace));
        service.enfiler(t, DepotsM365Service.ACHEVEMENT, 3, Map.of());
        ArgumentCaptor<DepotM365> cree = ArgumentCaptor.forClass(DepotM365.class);
        verify(depots).save(cree.capture());
        assertEquals(DepotM365.Nature.DOCUMENT_SIGNE, cree.getValue().getNature(), "ni preuve ni avis sans les demander");
        assertEquals(41L, cree.getValue().getDocumentId());
    }

    @Test
    void preuve_et_avis_s_ajoutent_au_document_et_rien_n_est_inscrit_deux_fois() {
        configurer(true, true, true);
        when(documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(TENANT, 9L)).thenReturn(List.of());
        when(depots.existsByCleIdempotence("3:9:PREUVE")).thenReturn(true);
        service.enfiler(trousse(), DepotsM365Service.ACHEVEMENT, 3, Map.of());
        ArgumentCaptor<DepotM365> cree = ArgumentCaptor.forClass(DepotM365.class);
        verify(depots).save(cree.capture());
        assertEquals(DepotM365.Nature.AVIS_TEAMS, cree.getValue().getNature(), "la preuve déjà inscrite n'est pas redoublée");
    }

    @Test
    void le_relais_depose_document_preuve_puis_avis_dans_le_tenant_de_la_trousse() {
        configurer(true, true, true);
        Trousse t = trousse();
        when(trousses.findByIdAndTenantId(9L, TENANT)).thenReturn(Optional.of(t));
        DocumentTrousse doc = document(41, DocumentTrousse.Statut.SIGNE);
        when(documents.findByIdAndTenantId(41L, TENANT)).thenReturn(Optional.of(doc));
        when(preuves.exporter(eq(9L), any())).thenReturn(new byte[]{5});
        DepotM365 avis = new DepotM365(TENANT, 9L, DepotM365.Nature.AVIS_TEAMS, null, T0);
        DepotM365 document = new DepotM365(TENANT, 9L, DepotM365.Nature.DOCUMENT_SIGNE, 41L, T0);
        DepotM365 preuve = new DepotM365(TENANT, 9L, DepotM365.Nature.PREUVE, null, T0);
        when(depots.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(DepotM365.Statut.EN_ATTENTE, T0))
                .thenReturn(List.of(avis, document, preuve));
        List<DepotMicrosoft365.Destination> ordre = new ArrayList<>();
        List<DepotMicrosoft365.Fichier> fichiers = new ArrayList<>();
        when(port.deposer(any(), any())).thenAnswer(i -> {
            ordre.add(i.getArgument(0));
            fichiers.add(i.getArgument(1));
            assertEquals(TENANT, portee.courant(), "déposé dans le tenant de la trousse");
            return new DepotMicrosoft365.Resultat(true, "sim://" + ordre.size(), "ok");
        });
        when(depots.findByTenantIdAndTrousseIdOrderByIdAsc(TENANT, 9L)).thenReturn(List.of(document, preuve, avis));

        assertEquals(3, service.relayer());
        assertEquals(List.of(DepotMicrosoft365.Destination.SHAREPOINT, DepotMicrosoft365.Destination.SHAREPOINT,
                DepotMicrosoft365.Destination.TEAMS), ordre, "l'avis part en dernier");
        assertEquals("Contrats/TR_2026-1", fichiers.get(0).dossier(), "un sous-dossier par trousse");
        assertEquals("entente.pdf", fichiers.get(0).nom());
        assertEquals("preuve-TR_2026-1.zip", fichiers.get(1).nom());
        String texte = new String(fichiers.get(2).octets(), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(texte.contains("TR/2026-1 — Entente") && texte.contains("- document signé : sim://1")
                && texte.contains("- dossier de preuve : sim://2") && !texte.contains("avis Teams :"), texte);
        assertEquals(DepotM365.Statut.DEPOSE, avis.getStatut());
        verify(preuves).exporter(9L, DepotsM365Service.SYSTEME);
        verify(journal).consigner(t, "DEPOT_M365", "systeme", "avis Teams → sim://3");
    }

    @Test
    void un_echec_un_document_disparu_ou_une_trousse_detruite_reprennent_plus_tard_sans_rien_journaliser() {
        configurer(true, false, false);
        Trousse t = trousse();
        Trousse detruite = trousse();
        when(detruite.getDetruiteLe()).thenReturn(T0);
        DepotM365 echoue = new DepotM365(TENANT, 9L, DepotM365.Nature.DOCUMENT_SIGNE, 41L, T0);
        DepotM365 disparu = new DepotM365(TENANT, 9L, DepotM365.Nature.DOCUMENT_SIGNE, 42L, T0);
        DepotM365 orphelin = new DepotM365(TENANT, 8L, DepotM365.Nature.PREUVE, null, T0);
        DepotM365 surDetruite = new DepotM365(TENANT, 7L, DepotM365.Nature.PREUVE, null, T0);
        when(trousses.findByIdAndTenantId(9L, TENANT)).thenReturn(Optional.of(t));
        when(trousses.findByIdAndTenantId(8L, TENANT)).thenReturn(Optional.empty());
        when(trousses.findByIdAndTenantId(7L, TENANT)).thenReturn(Optional.of(detruite));
        DocumentTrousse doc = document(41, DocumentTrousse.Statut.SIGNE);
        when(documents.findByIdAndTenantId(41L, TENANT)).thenReturn(Optional.of(doc));
        when(documents.findByIdAndTenantId(42L, TENANT)).thenReturn(Optional.empty());
        when(port.deposer(any(), any())).thenReturn(new DepotMicrosoft365.Resultat(false, null, "Graph 503"));
        when(depots.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(DepotM365.Statut.EN_ATTENTE, T0))
                .thenReturn(List.of(echoue, disparu, orphelin, surDetruite));

        assertEquals(0, service.relayer());
        assertEquals("Graph 503", echoue.getDerniereErreur());
        assertEquals("Document signé introuvable.", disparu.getDerniereErreur());
        assertEquals("Trousse détruite ou introuvable : rien à déposer.", orphelin.getDerniereErreur());
        assertEquals(1, surDetruite.getTentatives());
        verify(journal, never()).consigner(any(), anyString(), anyString(), anyString());
    }

    @Test
    void l_administration_technique_regle_suit_et_relance_les_depots() {
        portee.executerDans(TENANT, () -> {
            when(configurations.findByTenantId(TENANT)).thenReturn(Optional.empty());
            ClmDto.DiffusionM365Vue defaut = service.configuration();
            assertFalse(defaut.actif());
            assertEquals("SHAREPOINT", defaut.bibliotheque());
            assertNull(defaut.modifiePar());

            assertTrue(assertThrows(IllegalArgumentException.class, () -> service.configurer(
                    new ClmDto.DiffusionM365Request(true, "AUTRE", null, true, true), "ti")).getMessage().contains("Bibliothèque"));
            when(configurations.save(any())).thenAnswer(i -> i.getArgument(0));
            ClmDto.DiffusionM365Vue vue = service.configurer(new ClmDto.DiffusionM365Request(true, "ONEDRIVE", " Signés ", false, true), "ti");
            assertTrue(vue.actif());
            assertEquals("Signés", vue.dossier());
            assertEquals("ti", vue.modifiePar());
            verify(journal).configuration("ti", "DIFFUSION_M365", "active · ONEDRIVE « Signés » · preuve non · avis Teams oui");
            service.configurer(new ClmDto.DiffusionM365Request(false, "SHAREPOINT", "X", true, false), "ti");
            verify(journal).configuration("ti", "DIFFUSION_M365", "inactive · SHAREPOINT « X » · preuve oui · avis Teams non");
            configurer(true, true, true);
            assertTrue(service.configuration().actif());

            DepotM365 d = new DepotM365(TENANT, 9L, DepotM365.Nature.PREUVE, null, T0);
            when(depots.findTop100ByTenantIdOrderByCreeLeDesc(TENANT)).thenReturn(List.of(d));
            when(trousses.findByIdAndTenantId(9L, TENANT)).thenReturn(Optional.empty());
            assertEquals("—", service.lister().get(0).reference());
            when(depots.findByIdAndTenantId(5L, TENANT)).thenReturn(Optional.of(d));
            when(depots.save(d)).thenReturn(d);
            Trousse t = trousse();
            when(trousses.findByIdAndTenantId(9L, TENANT)).thenReturn(Optional.of(t));
            assertEquals("TR/2026-1", service.relancer(5L, "ti").reference());
            verify(journal).configuration("ti", "DIFFUSION_M365", "relance du dépôt 5 (PREUVE)");
            assertThrows(NoSuchElementException.class, () -> service.relancer(6L, "ti"));
            return null;
        });
    }
}
