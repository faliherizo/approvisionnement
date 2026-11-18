package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.SliceImpl;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.MessageSortantRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Suivi centralisé à la volumétrie d'IQ (addenda 3, Q38 ; 65 000 trousses par an) : lecture par tranches, arrêt à la
 * limite, aucune ligne calculée pour une trousse invisible ou écartée par les filtres.
 */
class SuiviPaginationTest {

    private static final Instant T0 = Instant.parse("2026-11-17T12:00:00Z");
    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final ParcoursService parcours = mock(ParcoursService.class);
    private final LectureTrousse lecture = mock(LectureTrousse.class);
    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    private final SuiviService service = new SuiviService(trousses, mock(EtapeTrousseRepository.class), mock(EvenementTrousseRepository.class),
            mock(InvitationRepository.class), mock(MessageSortantRepository.class), parcours, lecture,
            new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(), Clock.fixed(T0, ZoneOffset.UTC)));
    private final Habilitations.Acteur admin = new Habilitations.Acteur("admin@iq.example", Set.of("ADMIN"), Set.of());
    private static final SuiviService.Filtre TOUT = new SuiviService.Filtre(null, null, null, null, null, null, null, false);

    private static List<Trousse> trousses(int debut, int n) {
        List<Trousse> l = new ArrayList<>();
        for (int i = debut; i < debut + n; i++) {
            Trousse t = new Trousse(3L, "TR-2026-" + i, "Trousse " + i, "i@iq.example", T0);
            org.springframework.test.util.ReflectionTestUtils.setField(t, "id", (long) i);
            l.add(t);
        }
        return l;
    }

    @Test
    void la_lecture_parcourt_les_tranches_et_s_arrete_a_la_limite() {
        when(trousses.findByTenantIdOrderByMajLeDescIdDesc(eq(3L), eq(PageRequest.of(0, 200))))
                .thenReturn(new SliceImpl<>(trousses(0, 200), PageRequest.of(0, 200), true));
        when(trousses.findByTenantIdOrderByMajLeDescIdDesc(eq(3L), eq(PageRequest.of(1, 200))))
                .thenReturn(new SliceImpl<>(trousses(200, 200), PageRequest.of(1, 200), true));
        when(lecture.visible(any(), eq(admin))).thenAnswer(i -> !((Trousse) i.getArgument(0)).getReference().endsWith("7"));
        List<?> lignes = portee.executerDans(3L, () -> service.suivi(admin, TOUT, 250));
        assertEquals(250, lignes.size(), "arrêt à la limite, au milieu de la deuxième tranche");
        verify(trousses, never()).findByTenantIdOrderByMajLeDescIdDesc(eq(3L), eq(PageRequest.of(2, 200)));
        verify(parcours, times(250)).etapesDe(any());
    }

    @Test
    void la_derniere_tranche_termine_la_lecture_et_les_filtres_precedent_le_calcul() {
        when(trousses.findByTenantIdOrderByMajLeDescIdDesc(eq(3L), any()))
                .thenReturn(new SliceImpl<>(trousses(0, 3), PageRequest.of(0, 200), false));
        when(lecture.visible(any(), any())).thenReturn(true);
        SuiviService.Filtre filtre = new SuiviService.Filtre("TR-2026-1", null, null, null, null, null, null, false);
        assertEquals(1, portee.executerDans(3L, () -> service.suivi(admin, filtre)).size());
        verify(parcours, times(1)).etapesDe(any());
        assertEquals(500, SuiviService.LIMITE_SUIVI);
    }
}
