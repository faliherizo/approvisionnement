package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/** Exportation vers un référentiel (addenda 2, Q13) : décisions d'autorisation, de période, de taille de lot et d'index. */
class ExportationReferentielServiceTest {

    private static final LocalDate JOUR = LocalDate.of(2026, 11, 16);
    private static final Habilitations.Acteur ADMIN = new Habilitations.Acteur("admin.ti@iq.example", Set.of("ADMIN"), Set.of());

    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final PreuveService preuves = mock(PreuveService.class);
    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(),
            Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC));
    private final ExportationReferentielService service = new ExportationReferentielService(trousses, preuves, mock(JournalClm.class), ctx);

    private static Trousse close(String reference, Instant cloture) {
        Trousse t = mock(Trousse.class);
        when(t.getId()).thenReturn(1L);
        when(t.getReference()).thenReturn(reference);
        when(t.getTitre()).thenReturn("=HYPERLINK(\"x\")");
        when(t.getStatut()).thenReturn(StatutTrousse.TERMINEE);
        when(t.getClotureLe()).thenReturn(cloture);
        return t;
    }

    @Test
    void seule_l_administration_exporte_et_la_periode_doit_etre_ordonnee() {
        Habilitations.Acteur initiateur = new Habilitations.Acteur("initiateur@iq.example", Set.of("USER"), Set.of());
        assertThrows(AccessDeniedException.class, () -> service.exporter(initiateur, JOUR, JOUR));
        assertThrows(IllegalArgumentException.class, () -> service.exporter(ADMIN, null, JOUR));
        assertThrows(IllegalArgumentException.class, () -> service.exporter(ADMIN, JOUR, null));
        assertThrows(IllegalArgumentException.class, () -> service.exporter(ADMIN, JOUR, JOUR.minusDays(1)));
        verifyNoInteractions(trousses, preuves);
    }

    @Test
    void un_lot_trop_grand_est_refuse_et_une_trousse_sans_cloture_ou_hors_periode_est_ecartee() {
        List<Trousse> beaucoup = new ArrayList<>();
        for (int i = 0; i <= ExportationReferentielService.LOT_MAX; i++) {
            beaucoup.add(close("TR-" + i, Instant.parse("2026-11-16T10:00:00Z")));
        }
        when(trousses.findByTenantIdAndStatutInAndDetruiteLeIsNullOrderByClotureLeAsc(any(), any())).thenReturn(beaucoup);
        assertTrue(assertThrows(IllegalArgumentException.class, () -> portee.executerDans(3L, () -> service.exporter(ADMIN, JOUR, JOUR))).getMessage().contains("réduire la période"));

        List<Trousse> lot = List.of(close("TR-SANS", null), close("TR-AVANT", Instant.parse("2026-11-14T10:00:00Z")),
                close("TR-APRES", Instant.parse("2026-11-18T10:00:00Z")), close("TR/1 OK", Instant.parse("2026-11-16T10:00:00Z")));
        when(trousses.findByTenantIdAndStatutInAndDetruiteLeIsNullOrderByClotureLeAsc(any(), any())).thenReturn(lot);
        when(preuves.exporter(anyLong(), any())).thenReturn(new byte[]{1, 2, 3});
        byte[] zip = portee.executerDans(3L, () -> service.exporter(ADMIN, JOUR, JOUR));
        String contenu = new String(zip, java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(contenu.contains("trousses/TR_1_OK.zip"), "nom de fichier assaini");
        assertFalse(contenu.contains("TR-SANS") || contenu.contains("TR-AVANT") || contenu.contains("TR-APRES"));
    }

    @Test
    void l_index_csv_neutralise_les_formules_et_echappe_les_separateurs() {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("reference", "TR-1");
        e.put("titre", "=1+1");
        e.put("typeContrat", null);
        e.put("statut", "a;b");
        e.put("clotureLe", "dit \"x\"");
        e.put("sfDossierId", "");
        e.put("fichier", "ligne\nsuite");
        e.put("sha256", "@x");
        String csv = ExportationReferentielService.csv(List.of(e));
        assertEquals("reference;titre;typeContrat;statut;clotureLe;sfDossierId;fichier;sha256\n"
                + "TR-1;'=1+1;;\"a;b\";\"dit \"\"x\"\"\";;\"ligne\nsuite\";'@x\n", csv);
    }
}
