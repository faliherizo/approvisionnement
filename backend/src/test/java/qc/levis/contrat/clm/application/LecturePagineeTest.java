package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Trousse;

/** Lecture paginée des trousses (RST-40) : page de la base pour qui voit tout ; fenêtre filtrée par visibilité sinon. */
class LecturePagineeTest {

    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final LectureTrousse lecture = mock(LectureTrousse.class);
    private final LecturePaginee pagination = new LecturePaginee(trousses, lecture);
    private final Specification<Trousse> tout = (r, q, cb) -> null;

    @SuppressWarnings("unchecked")
    private void base(int total) {
        when(trousses.findAll(any(Specification.class), any(Pageable.class))).thenAnswer(i -> {
            Pageable p = i.getArgument(1);
            List<Trousse> l = new ArrayList<>();
            for (long k = p.getOffset(); k < Math.min(total, p.getOffset() + p.getPageSize()); k++) {
                Trousse t = new Trousse(3L, "TR-" + k, "T" + k, "i@iq.example", Instant.EPOCH);
                org.springframework.test.util.ReflectionTestUtils.setField(t, "id", k);
                l.add(t);
            }
            return new PageImpl<>(l, p, total);
        });
    }

    @Test
    void l_audit_recoit_la_page_de_la_base_avec_son_total() {
        base(120);
        Habilitations.Acteur auditeur = new Habilitations.Acteur("a@iq.example", Set.of("AUDITEUR"), Set.of());
        LecturePaginee.Fenetre f = pagination.page(auditeur, tout, 1, 50);
        assertEquals(50, f.trousses().size());
        assertEquals(120L, f.total());
        assertTrue(f.suivant());
        verify(lecture, never()).visible(any(), any());
    }

    @Test
    void un_initiateur_recoit_une_fenetre_de_ses_trousses_visibles() {
        base(500);
        Habilitations.Acteur initiateur = new Habilitations.Acteur("i@iq.example", Set.of("REDACTEUR"), Set.of());
        when(lecture.visible(any(), eq(initiateur))).thenAnswer(i -> ((Trousse) i.getArgument(0)).getId() % 5 == 0);
        LecturePaginee.Fenetre f = pagination.page(initiateur, tout, 1, 30);
        assertEquals(30, f.trousses().size());
        assertEquals("TR-150", f.trousses().get(0).getReference(), "100 visibles parmi 500 : la page 1 de 30 commence à la 31e");
        assertNull(f.total());
        assertTrue(f.suivant());
        assertFalse(pagination.page(initiateur, tout, 3, 30).suivant(), "la 4e page en garde 10, la dernière");
    }
}
