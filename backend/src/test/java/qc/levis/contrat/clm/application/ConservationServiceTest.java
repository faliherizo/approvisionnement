package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.repository.AnalyseIaRepository;
import qc.levis.contrat.clm.application.repository.DecisionEtapeRepository;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.ParticipantRepository;
import qc.levis.contrat.clm.application.repository.RegleConservationRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.DecisionEtape;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Identite;
import qc.levis.contrat.clm.domain.Invitation;
import qc.levis.contrat.clm.domain.Participant;
import qc.levis.contrat.clm.domain.RegleConservation;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/** Destruction et versement à l'échéance (Cahier § 1.4.11, § 1.4.12) : l'horloge est placée après l'échéance. */
class ConservationServiceTest {

    private static final Long IQ = 7L;
    private static final Instant CLOTURE = Instant.parse("2018-03-01T15:00:00Z");
    private static final Instant MAINTENANT = Instant.parse("2026-11-11T15:00:00Z");

    private final RegleConservationRepository regles = mock(RegleConservationRepository.class);
    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final DocumentTrousseRepository documents = mock(DocumentTrousseRepository.class);
    private final ParticipantRepository participants = mock(ParticipantRepository.class);
    private final InvitationRepository invitations = mock(InvitationRepository.class);
    private final DecisionEtapeRepository decisions = mock(DecisionEtapeRepository.class);
    private final AnalyseIaRepository analyses = mock(AnalyseIaRepository.class);
    private final JournalClm journal = mock(JournalClm.class);
    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    private ConservationService service;
    private Trousse trousse;

    @BeforeEach
    void preparer() {
        ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(),
                Clock.fixed(MAINTENANT, ZoneId.of("America/Toronto")));
        service = new ConservationService(regles, trousses, documents, participants, invitations, decisions, analyses, journal, ctx);
        trousse = new Trousse(IQ, "TR-2018-00042", "Entente Agri-Nord", "init@iq.example", CLOTURE);
        ReflectionTestUtils.setField(trousse, "id", 42L);
        trousse.setTypeContrat("GARANTIE");
        trousse.changerStatut(StatutTrousse.ANNULEE, CLOTURE);
        when(trousses.findByIdAndTenantId(42L, IQ)).thenReturn(Optional.of(trousse));
        when(trousses.findByTenantIdAndStatutInAndDetruiteLeIsNullOrderByClotureLeAsc(eq(IQ), any())).thenReturn(List.of(trousse));
        when(regles.save(any(RegleConservation.class))).thenAnswer(i -> i.getArgument(0));
    }

    private RegleConservation regle(String type, int ans, RegleConservation.SortFinal sort) {
        RegleConservation r = new RegleConservation(IQ, type);
        r.definir(ans, sort, "admin", CLOTURE);
        return r;
    }

    @Test
    void echue_selon_la_regle_du_type_puis_detruite_avec_un_bordereau_sans_contenu() {
        when(regles.findByTenantIdOrderByTypeContratAsc(IQ)).thenReturn(List.of(regle(null, 20, RegleConservation.SortFinal.CONSERVER),
                regle("GARANTIE", 7, RegleConservation.SortFinal.DETRUIRE)));
        DocumentTrousse doc = new DocumentTrousse(IQ, 42L, "c", 1, "entente.pdf", FormatDocument.Type.PDF, "%PDF-1.4".getBytes(), "i", "m", CLOTURE);
        when(documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(IQ, 42L)).thenReturn(List.of(doc));
        Participant p = new Participant(IQ, 42L, "Alice", "alice@exemple.ca", "+15145550101", "SIGNATAIRE_1", true, Identite.Niveau.CODE_SMS);
        when(participants.findByTenantIdAndTrousseIdOrderByIdAsc(IQ, 42L)).thenReturn(List.of(p));
        Invitation inv = new Invitation(IQ, 42L, 1L, 1L, "h", CLOTURE, Identite.Niveau.COURRIEL);
        when(invitations.findByTenantIdAndTrousseIdOrderByIdAsc(IQ, 42L)).thenReturn(List.of(inv));
        DecisionEtape d = new DecisionEtape(IQ, 42L, 1L, "v@iq.example", DecisionEtape.Nature.APPROUVE, "ok", CLOTURE);
        when(decisions.findByTenantIdAndTrousseIdOrderByDecideLeAsc(IQ, 42L)).thenReturn(List.of(d));

        ClmDto.ConservationVue vue = portee.executerDans(IQ, () -> service.suivi(true)).get(0);
        assertTrue(vue.echue());
        assertEquals("DETRUIRE", vue.sortFinal(), "la règle du type prime sur celle par défaut");
        assertEquals(java.time.LocalDate.of(2025, 3, 1), vue.echeance());

        portee.executerDans(IQ, () -> {
            service.detruire(42L, "admin.ti@iq.example");
            return null;
        });
        assertEquals(0, doc.getContenu().length);
        assertEquals("Participant anonymisé", p.getNom());
        assertNull(d.getCommentaire());
        assertNotNull(trousse.getDetruiteLe());
        verify(analyses).deleteAll(any());
        verify(journal).consigner(eq(trousse), eq("TROUSSE_DETRUITE"), eq("admin.ti@iq.example"),
                argThat(s -> s.contains("GARANTIE 7 an(s) DETRUIRE") && s.contains("entente.pdf v1") && !s.contains("Alice")));
        String bordereau = trousse.getBordereauDestruction();
        assertTrue(bordereau.contains("\"resultat\":\"DETRUIT\"") && bordereau.contains("Entente Agri-Nord")
                && bordereau.contains("admin.ti@iq.example") && bordereau.contains("entente.pdf") && !bordereau.contains("Alice"), bordereau);
        assertThrows(IllegalStateException.class, () -> trousse.consignerBordereau("{}"), "un seul bordereau");
        when(trousses.findByTenantIdAndDetruiteLeIsNotNullOrderByDetruiteLeDesc(IQ)).thenReturn(List.of(trousse));
        ClmDto.DestructionVue detruite = portee.executerDans(IQ, () -> service.detruites()).get(0);
        assertEquals("DETRUIT", detruite.resultat());
        assertEquals("TR-2018-00042", detruite.reference());
    }

    @Test
    void un_versement_precede_la_destruction_et_la_conservation_permanente_ne_s_echoit_jamais() {
        when(regles.findByTenantIdOrderByTypeContratAsc(IQ)).thenReturn(List.of(regle("GARANTIE", 5, RegleConservation.SortFinal.VERSER)));
        assertThrows(IllegalStateException.class, () -> portee.executerDans(IQ, () -> {
            service.detruire(42L, "admin");
            return null;
        }), "versement à confirmer d'abord");
        assertNotNull(portee.executerDans(IQ, () -> service.marquerVersee(42L, "admin")).verseeLe());
        verify(journal).consigner(trousse, "TROUSSE_VERSEE", "admin", "règle GARANTIE 5 an(s) VERSER");
        portee.executerDans(IQ, () -> {
            service.detruire(42L, "admin");
            return null;
        });
        assertNotNull(trousse.getDetruiteLe());

        Trousse permanente = new Trousse(IQ, "TR-2", "T", "i", CLOTURE);
        ReflectionTestUtils.setField(permanente, "id", 43L);
        permanente.changerStatut(StatutTrousse.ANNULEE, CLOTURE);
        when(trousses.findByIdAndTenantId(43L, IQ)).thenReturn(Optional.of(permanente));
        when(regles.findByTenantIdOrderByTypeContratAsc(IQ)).thenReturn(List.of(regle(null, 5, RegleConservation.SortFinal.CONSERVER)));
        assertTrue(assertThrows(IllegalStateException.class, () -> portee.executerDans(IQ, () -> service.marquerVersee(43L, "a")))
                .getMessage().contains("permanente"));
    }

    @Test
    void sans_regle_rien_ne_se_detruit_et_un_versement_exige_le_sort_verser() {
        when(regles.findByTenantIdOrderByTypeContratAsc(IQ)).thenReturn(List.of());
        assertNull(portee.executerDans(IQ, () -> service.suivi(false)).get(0).echeance());
        assertTrue(portee.executerDans(IQ, () -> service.suivi(true)).isEmpty());
        assertThrows(IllegalStateException.class, () -> portee.executerDans(IQ, () -> service.marquerVersee(42L, "a")));
        when(regles.findByTenantIdOrderByTypeContratAsc(IQ)).thenReturn(List.of(regle(null, 1, RegleConservation.SortFinal.DETRUIRE)));
        assertThrows(IllegalStateException.class, () -> portee.executerDans(IQ, () -> service.marquerVersee(42L, "a")), "sort DETRUIRE");
        assertThrows(NoSuchElementException.class, () -> portee.executerDans(IQ, () -> service.geler(99L, "x", "a")));
        assertThrows(NoSuchElementException.class, () -> portee.executerDans(IQ, () -> {
            service.supprimer(5L, "a");
            return null;
        }));
    }
}
