package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.ClasseurTableur;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.repository.DepotM365Repository;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.application.repository.MessageSortantRepository;
import qc.levis.contrat.clm.application.repository.RappelSortantRepository;
import qc.levis.contrat.clm.application.repository.RapportClmRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.DepotM365;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.EvenementTrousse;
import qc.levis.contrat.clm.domain.MessageSortant;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.RappelSortant;
import qc.levis.contrat.clm.domain.RapportClm;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/** Rapports (Annexe 10, AF-37) : incidents de toutes sources, signatures en attente, filtres et formats d'export. */
class RapportsClmServiceTest {

    private static final Instant MAINTENANT = Instant.parse("2026-11-25T15:00:00Z");
    private static final Long TENANT = 3L;

    private final RapportClmRepository rapports = mock(RapportClmRepository.class);
    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final EvenementTrousseRepository evenements = mock(EvenementTrousseRepository.class);
    private final EtapeTrousseRepository etapes = mock(EtapeTrousseRepository.class);
    private final DepotM365Repository depots = mock(DepotM365Repository.class);
    private final MessageSortantRepository sorties = mock(MessageSortantRepository.class);
    private final RappelSortantRepository rappels = mock(RappelSortantRepository.class);
    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(), Clock.fixed(MAINTENANT, ZoneOffset.UTC));
    private final RapportsClmService service = new RapportsClmService(rapports, trousses, evenements, etapes, depots, sorties, rappels,
            mock(UtilisationService.class), mock(ClasseurTableur.class), mock(JournalClm.class), ctx);

    private void rapport(RapportClm.Type type, String unite) {
        RapportClm r = new RapportClm(TENANT, "admin");
        r.definir(new RapportClm.Definition("R", type, 30, unite, null, null, null), MAINTENANT);
        when(rapports.findByIdAndTenantId(1L, TENANT)).thenReturn(Optional.of(r));
    }

    private static Trousse trousse(long id, String unite, StatutTrousse statut) {
        Trousse t = mock(Trousse.class);
        when(t.getId()).thenReturn(id);
        when(t.getTenantId()).thenReturn(TENANT);
        when(t.getReference()).thenReturn("TR-" + id);
        when(t.getTitre()).thenReturn("=Titre " + id);
        when(t.getUnite()).thenReturn(unite);
        when(t.getStatut()).thenReturn(statut);
        return t;
    }

    private static EvenementTrousse evt(long trousse, String type, String detail, Instant quand) {
        EvenementTrousse e = mock(EvenementTrousse.class);
        when(e.getTrousseId()).thenReturn(trousse);
        when(e.getType()).thenReturn(type);
        when(e.getDetail()).thenReturn(detail);
        when(e.getHorodatage()).thenReturn(quand);
        return e;
    }

    @Test
    void les_incidents_reunissent_antivirus_integrite_et_files_d_integration_de_la_periode() {
        rapport(RapportClm.Type.INCIDENTS, null);
        Instant hier = MAINTENANT.minusSeconds(86_400);
        List<EvenementTrousse> evts = List.of(evt(7, "DOCUMENT_INFECTE", "virus.pdf", hier), evt(7, "INTEGRITE_VERIFIEE", "intègre", hier),
                evt(7, "INTEGRITE_VERIFIEE", "ALTÉRATION DÉTECTÉE", hier.plusSeconds(60)), evt(7, "TROUSSE_CREEE", null, hier));
        when(evenements.findByTenantIdAndHorodatageBetweenOrderByHorodatageDesc(eq(TENANT), any(), any())).thenReturn(evts);
        Trousse t = trousse(7, null, StatutTrousse.EN_TRAITEMENT);
        when(trousses.findByTenantIdAndIdIn(eq(TENANT), anyCollection())).thenReturn(List.of(t));
        when(trousses.findByIdAndTenantId(7L, TENANT)).thenReturn(Optional.of(t));
        DepotM365 depot = new DepotM365(TENANT, 7L, DepotM365.Nature.PREUVE, null, hier.plusSeconds(120));
        depot.echouer("Graph 503", MAINTENANT);
        DepotM365 ancien = new DepotM365(TENANT, 7L, DepotM365.Nature.PREUVE, null, MAINTENANT.minusSeconds(86_400 * 90L));
        ancien.echouer("hors période", MAINTENANT);
        when(depots.findTop100ByTenantIdOrderByCreeLeDesc(TENANT)).thenReturn(List.of(depot, ancien, new DepotM365(TENANT, 7L,
                DepotM365.Nature.AVIS_TEAMS, null, hier)));
        MessageSortant sf = mock(MessageSortant.class);
        when(sf.getDerniereErreur()).thenReturn("Salesforce 401");
        when(sf.getCreeLe()).thenReturn(hier.plusSeconds(180));
        when(sf.getTrousseId()).thenReturn(8L);
        when(sf.getEvenement()).thenReturn("SIGNATURE_TERMINEE");
        when(sorties.findTop100ByTenantIdOrderByCreeLeDesc(TENANT)).thenReturn(List.of(sf, mock(MessageSortant.class)));
        when(trousses.findByIdAndTenantId(8L, TENANT)).thenReturn(Optional.empty());
        RappelSortant rappel = new RappelSortant(TENANT, 1L, 7L, "SIGNATURE_TERMINEE", "cle", "{}", hier.plusSeconds(240));
        rappel.echouer("hôte injoignable", MAINTENANT);
        when(rappels.findTop100ByTenantIdOrderByCreeLeDesc(TENANT)).thenReturn(List.of(rappel,
                new RappelSortant(TENANT, 1L, 7L, "X", "cle2", "{}", hier)));

        TableauExport t1 = portee.executerDans(TENANT, () -> service.executer(1L));
        assertEquals(List.of("Date", "Catégorie", "Référence", "Détail"), t1.colonnes());
        assertEquals(List.of("RAPPEL_INTEGRATION", "SALESFORCE", "MICROSOFT_365", "INTEGRITE", "ANTIVIRUS"),
                t1.lignes().stream().map(l -> l.get(1)).toList(), "du plus récent au plus ancien, hors période exclue");
        assertEquals("—", t1.lignes().get(1).get(2), "trousse détruite depuis : référence absente");
        assertEquals("PREUVE · Graph 503", t1.lignes().get(2).get(3));
    }

    @Test
    void les_signatures_en_attente_comptent_les_jours_depuis_l_activation_de_l_etape() {
        rapport(RapportClm.Type.SIGNATURES_EN_ATTENTE, "VP");
        Trousse retenue = trousse(7, "VP", StatutTrousse.EN_TRAITEMENT);
        Trousse autreUnite = trousse(8, "AUTRE", StatutTrousse.EN_TRAITEMENT);
        Trousse terminee = trousse(9, "VP", StatutTrousse.TERMINEE);
        when(trousses.findByTenantIdOrderByMajLeDesc(TENANT)).thenReturn(List.of(retenue, autreUnite, terminee));
        EtapeTrousse signature = mock(EtapeTrousse.class);
        when(signature.getTrousseId()).thenReturn(7L);
        when(signature.getType()).thenReturn(Parcours.TypeEtape.SIGNATURE);
        when(signature.getLibelle()).thenReturn("Signature");
        when(signature.getActiveeLe()).thenReturn(MAINTENANT.minusSeconds(3 * 86_400L));
        EtapeTrousse sansDate = mock(EtapeTrousse.class);
        when(sansDate.getTrousseId()).thenReturn(7L);
        when(sansDate.getType()).thenReturn(Parcours.TypeEtape.SIGNATURE);
        EtapeTrousse validation = mock(EtapeTrousse.class);
        when(validation.getType()).thenReturn(Parcours.TypeEtape.VALIDATION);
        when(etapes.findByTenantIdAndTrousseIdInAndStatut(TENANT, Set.of(7L), Parcours.StatutEtape.ACTIVE))
                .thenReturn(List.of(signature, validation, sansDate));

        TableauExport t = portee.executerDans(TENANT, () -> service.executer(1L));
        assertEquals(2, t.lignes().size(), "une ligne par étape de signature active des trousses retenues");
        assertEquals(List.of("TR-7", "=Titre 7", "VP", "", "Signature", "2026-11-22 15:00", "", "3"), t.lignes().get(0));
        assertEquals("", t.lignes().get(1).get(7));
        assertTrue(t.csv().contains("'=Titre 7"), "formule neutralisée dans le CSV");
    }

    @Test
    void sans_evenement_les_rapports_sont_vides_et_un_type_inconnu_est_refuse() {
        when(evenements.findByTenantIdAndHorodatageBetweenOrderByHorodatageDesc(eq(TENANT), any(), any())).thenReturn(List.of());
        for (RapportClm.Type type : List.of(RapportClm.Type.DELAIS, RapportClm.Type.REJETS, RapportClm.Type.INCIDENTS)) {
            rapport(type, null);
            assertTrue(portee.executerDans(TENANT, () -> service.executer(1L)).lignes().isEmpty(), type.name());
        }
        assertEquals("Type de rapport requis.", assertThrows(IllegalArgumentException.class, () -> portee.executerDans(TENANT,
                () -> service.creer(new qc.levis.contrat.clm.application.dto.ClmDto.RapportRequest("R", "AUTRE", null, null, null, null, null), "a")))
                .getMessage());
        assertEquals("\"a;b\"", TableauExport.cellule("a;b"), "séparateur protégé");
        assertEquals("\"x\"\"y\"", TableauExport.cellule("x\"y"), "guillemet doublé");
        assertEquals("", TableauExport.cellule(null));
    }
}
