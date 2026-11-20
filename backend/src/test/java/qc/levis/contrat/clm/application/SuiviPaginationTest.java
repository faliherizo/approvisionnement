package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.MessageSortantRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Invitation;
import qc.levis.contrat.clm.domain.MessageSortant;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Suivi paginé à la volumétrie d'IQ (addenda 3, Q38 ; 65 000 trousses par an) : page tirée de la base pour qui voit tout,
 * parcours par tranches pour les autres, lignes calculées en requêtes groupées et jamais trousse par trousse.
 */
class SuiviPaginationTest {

    private static final Instant T0 = Instant.parse("2026-11-17T12:00:00Z");
    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final EtapeTrousseRepository etapes = mock(EtapeTrousseRepository.class);
    private final MessageSortantRepository sorties = mock(MessageSortantRepository.class);
    private final InvitationRepository invitations = mock(InvitationRepository.class);
    private final EvenementTrousseRepository evenements = mock(EvenementTrousseRepository.class);
    private final LectureTrousse lecture = mock(LectureTrousse.class);
    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    private final SuiviService service = new SuiviService(trousses, etapes, evenements, invitations, sorties, mock(ParcoursService.class), lecture,
            new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(), Clock.fixed(T0, ZoneOffset.UTC)),
            new LecturePaginee(trousses, lecture));
    private final Habilitations.Acteur admin = new Habilitations.Acteur("admin@iq.example", Set.of("ADMIN"), Set.of());
    private final Habilitations.Acteur initiateur = new Habilitations.Acteur("i@iq.example", Set.of("REDACTEUR"), Set.of());
    private static final SuiviService.Filtre TOUT = new SuiviService.Filtre(null, null, null, null, null, null, null, false);
    private static final SuiviService.Filtre EXCEPTIONS = new SuiviService.Filtre(null, null, null, null, null, null, null, true);

    private static List<Trousse> trousses(int debut, int n) {
        List<Trousse> l = new ArrayList<>();
        for (int i = debut; i < debut + n; i++) {
            Trousse t = new Trousse(3L, "TR-2026-" + i, "Trousse " + i, "i@iq.example", T0);
            org.springframework.test.util.ReflectionTestUtils.setField(t, "id", (long) i);
            l.add(t);
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    private void tranches(int total) {
        when(trousses.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(i -> {
            Pageable p = i.getArgument(1);
            int debut = (int) p.getOffset();
            int n = Math.max(0, Math.min(p.getPageSize(), total - debut));
            return new PageImpl<>(trousses(debut, n), p, total);
        });
    }

    @Test
    void l_administration_recoit_une_page_de_la_base_avec_le_total_en_quatre_requetes_groupees() {
        tranches(1_000);
        ClmDto.SuiviPage page = portee.executerDans(3L, () -> service.page(admin, TOUT, 2, 50));
        assertEquals(50, page.lignes().size());
        assertEquals("TR-2026-100", page.lignes().get(0).reference(), "page 2 de 50 : à partir de la 100e");
        assertEquals(1_000L, page.total());
        assertTrue(page.suivant());
        verify(etapes, times(1)).findByTenantIdAndTrousseIdInAndStatut(eq(3L), anyCollection(), any());
        verify(invitations, times(1)).findByTenantIdAndTrousseIdIn(eq(3L), anyCollection());
        verify(evenements, times(1)).derniersParTenantIdEtTrousses(eq(3L), anyCollection());
        verify(lecture, never()).visible(any(), any());
        assertEquals(SuiviService.TAILLE_MAX, portee.executerDans(3L, () -> service.page(admin, TOUT, -1, 5_000)).taille(), "taille bornée");
    }

    @Test
    void un_initiateur_parcourt_les_tranches_selon_sa_visibilite_sans_total() {
        tranches(450);
        when(lecture.visible(any(), eq(initiateur))).thenAnswer(i -> ((Trousse) i.getArgument(0)).getId() % 2 == 0);
        ClmDto.SuiviPage page = portee.executerDans(3L, () -> service.page(initiateur, TOUT, 2, 100));
        assertEquals(25, page.lignes().size(), "225 trousses visibles : la page 2 de 100 en garde 25");
        assertEquals("TR-2026-400", page.lignes().get(0).reference());
        assertNull(page.total());
        assertFalse(page.suivant());
        assertTrue(portee.executerDans(3L, () -> service.page(initiateur, TOUT, 0, 100)).suivant());
    }

    @Test
    void le_filtre_des_exceptions_ne_retient_que_les_trousses_en_anomalie() {
        tranches(10);
        when(lecture.visible(any(), any())).thenReturn(true);
        MessageSortant echec = mock(MessageSortant.class);
        when(echec.getTrousseId()).thenReturn(3L);
        when(sorties.findByTenantIdAndTrousseIdInAndStatut(eq(3L), anyCollection(), eq(MessageSortant.Statut.ECHEC))).thenReturn(List.of(echec));
        Invitation expiree = mock(Invitation.class);
        when(expiree.getTrousseId()).thenReturn(5L);
        when(expiree.getStatut()).thenReturn(Invitation.Statut.ENVOYEE);
        when(expiree.estExpiree(T0)).thenReturn(true);
        when(invitations.findByTenantIdAndTrousseIdIn(eq(3L), anyCollection())).thenReturn(List.of(expiree));
        ClmDto.SuiviPage page = portee.executerDans(3L, () -> service.page(admin, EXCEPTIONS, 0, 1));
        assertEquals(List.of("TR-2026-3"), page.lignes().stream().map(ClmDto.SuiviLigne::reference).toList());
        assertEquals(List.of(SuiviService.ERREUR_SALESFORCE), page.lignes().get(0).exceptions());
        assertTrue(page.suivant(), "la trousse 5 suit");
        ClmDto.SuiviPage deuxieme = portee.executerDans(3L, () -> service.page(admin, EXCEPTIONS, 1, 1));
        assertEquals(List.of(SuiviService.SIGNATURE_EXPIREE), deuxieme.lignes().get(0).exceptions());
        assertFalse(deuxieme.suivant());
    }

    @Test
    void l_export_parcourt_toutes_les_tranches() {
        tranches(450);
        when(lecture.visible(any(), any())).thenReturn(true);
        String csv = portee.executerDans(3L, () -> service.exporterCsv(admin, TOUT));
        assertEquals(451, csv.split("\n").length, "en-tête et 450 lignes");
    }

    @Test
    void le_tableau_de_bord_compte_en_base_pour_qui_voit_tout_et_par_tranches_sinon() {
        tranches(3);
        when(trousses.count(any(Specification.class))).thenReturn(4L, 9L);
        qc.levis.contrat.clm.domain.EtapeTrousse validation = mock(qc.levis.contrat.clm.domain.EtapeTrousse.class);
        when(validation.getTrousseId()).thenReturn(1L);
        when(validation.getType()).thenReturn(qc.levis.contrat.clm.domain.Parcours.TypeEtape.VALIDATION);
        when(validation.getEcheance()).thenReturn(T0.minusSeconds(60));
        qc.levis.contrat.clm.domain.EtapeTrousse signature = mock(qc.levis.contrat.clm.domain.EtapeTrousse.class);
        when(signature.getTrousseId()).thenReturn(2L);
        when(signature.getType()).thenReturn(qc.levis.contrat.clm.domain.Parcours.TypeEtape.SIGNATURE);
        when(etapes.findByTenantIdAndTrousseIdInAndStatut(eq(3L), anyCollection(), any())).thenReturn(List.of(validation, signature));
        qc.levis.contrat.clm.domain.EvenementTrousse evt = mock(qc.levis.contrat.clm.domain.EvenementTrousse.class);
        when(evt.getTrousseId()).thenReturn(1L);
        when(evt.getType()).thenReturn("TROUSSE_LANCEE");
        when(evenements.findTop30ByTenantIdOrderByHorodatageDesc(3L)).thenReturn(List.of(evt));
        MessageSortant echec = mock(MessageSortant.class);
        when(echec.getTrousseId()).thenReturn(2L);
        when(echec.getStatut()).thenReturn(MessageSortant.Statut.ECHEC);
        when(sorties.findTop50ByTenantIdAndStatutOrderByCreeLeDesc(3L, MessageSortant.Statut.ECHEC)).thenReturn(List.of(echec));
        when(trousses.findByTenantIdAndIdIn(eq(3L), anyCollection())).thenReturn(trousses(1, 2));
        when(lecture.visible(any(), any())).thenReturn(true);

        ClmDto.TableauBord admin = portee.executerDans(3L, () -> service.tableauBord(this.admin, TOUT));
        assertEquals(java.util.Map.of("brouillons", 4L, "enValidation", 1L, "enSignature", 1L, "enRetard", 1L, "terminees", 9L), admin.indicateurs());
        assertEquals(1, admin.activite().size());
        assertEquals(1, admin.alertesSalesforce().size());

        ClmDto.TableauBord perso = portee.executerDans(3L, () -> service.tableauBord(initiateur, TOUT));
        assertEquals(0L, perso.indicateurs().get("terminees"), "trousses neuves : brouillons seulement");
        assertEquals(3L, perso.indicateurs().get("brouillons"));
        assertEquals(0L, perso.indicateurs().get("enValidation"), "aucune trousse en traitement : aucune étape lue");
    }

    @Test
    void sans_activite_ni_alerte_aucune_trousse_n_est_relue() {
        tranches(0);
        ClmDto.TableauBord vide = portee.executerDans(3L, () -> service.tableauBord(initiateur, TOUT));
        assertTrue(vide.activite().isEmpty() && vide.alertesSalesforce().isEmpty());
        verify(trousses, never()).findByTenantIdAndIdIn(any(), anyCollection());
    }
}

