package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.repository.DecisionEtapeRepository;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.InvitationRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.DecisionEtape;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.Identite;
import qc.levis.contrat.clm.domain.Invitation;
import qc.levis.contrat.clm.domain.ModeleMessage;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.Trousse;

/** Rappels de non-réponse et retards (ST-11), avec une horloge fixe trois jours après l'activation. */
@ExtendWith(MockitoExtension.class)
class RappelServiceTest {

    private static final Instant ACTIVATION = Instant.parse("2026-11-01T08:00:00Z");

    @Mock EtapeTrousseRepository etapes;
    @Mock DecisionEtapeRepository decisions;
    @Mock TrousseRepository trousses;
    @Mock InvitationRepository invitations;
    @Mock ParcoursService parcours;
    @Mock JournalClm journal;
    @Mock PerimetreUtilisateur perimetre;
    final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    RappelService service;
    Trousse trousse;

    @BeforeEach
    void preparer() {
        ContexteClm ctx = new ContexteClm(perimetre, portee, new ObjectMapper(), Clock.fixed(ACTIVATION.plusSeconds(3 * 86_400), ZoneOffset.UTC));
        lenient().doCallRealMethod().when(decisions).duCycleEnCours(any());
        service = new RappelService(etapes, decisions, trousses, invitations, parcours, journal, ctx);
        trousse = new Trousse(1L, "TR-1", "T", "init@x.ca", ACTIVATION);
        ReflectionTestUtils.setField(trousse, "id", 10L);
        when(trousses.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(trousse));
    }

    private EtapeTrousse etape(Parcours.TypeEtape type, String affectationsJson, Integer delai) {
        EtapeTrousse e = new EtapeTrousse(1L, 10L, new Parcours.DefinitionEtape("k", "Étape", 1, type, List.of(), 1, null, delai), affectationsJson);
        ReflectionTestUtils.setField(e, "id", 20L);
        e.activer(ACTIVATION);
        return e;
    }

    @Test
    void relance_seulement_ceux_qui_n_ont_pas_decide_dans_ce_cycle_et_signale_le_retard_une_fois() {
        EtapeTrousse e = etape(Parcours.TypeEtape.APPROBATION,
                "[{\"type\":\"UTILISATEUR\",\"valeur\":\"a@x.ca\"},{\"type\":\"UTILISATEUR\",\"valeur\":\"b@x.ca\"},{\"type\":\"ROLE\",\"valeur\":\"DIRECTION\"}]", 2);
        when(etapes.findByStatut(Parcours.StatutEtape.ACTIVE)).thenReturn(List.of(e));
        when(decisions.findByTenantIdAndEtapeIdOrderByDecideLeAsc(1L, 20L)).thenReturn(List.of(
                new DecisionEtape(1L, 10L, 20L, "b@x.ca", DecisionEtape.Nature.APPROUVE, null, ACTIVATION.minusSeconds(86_400)),
                new DecisionEtape(1L, 10L, 20L, "A@x.ca", DecisionEtape.Nature.APPROUVE, null, ACTIVATION)));

        assertEquals(2, service.executer(), "b a décidé lors d'un cycle antérieur : il est relancé avec le rôle DIRECTION");
        verify(parcours, times(2)).notifierTache(eq(trousse), eq(e), any(), eq(ModeleMessage.Evenement.RAPPEL));
        verify(parcours).notifierProprietaire(trousse, ModeleMessage.Evenement.RETARD, "Étape");
        assertTrue(e.isRetardSignale());
        assertEquals(1, e.getRappelsEnvoyes());

        assertEquals(0, service.executer(), "le rappel suivant attend une nouvelle période");
        verify(parcours, times(1)).notifierProprietaire(any(), eq(ModeleMessage.Evenement.RETARD), any());
        assertEquals(List.of(1L, 1L), portee.traverses(), "chaque passage traite l'étape dans son tenant");
        assertNull(portee.courant(), "le contexte tenant est restauré");
    }

    @Test
    void signature_relance_les_invitations_ouvertes_seulement() {
        EtapeTrousse e = etape(Parcours.TypeEtape.SIGNATURE,
                "[{\"type\":\"PARTICIPANT\",\"valeur\":\"7\"},{\"type\":\"PARTICIPANT\",\"valeur\":\"8\"}]", null);
        when(etapes.findByStatut(Parcours.StatutEtape.ACTIVE)).thenReturn(List.of(e));
        Invitation ouverte = new Invitation(1L, 10L, 20L, 7L, "h1", ACTIVATION.plusSeconds(864_000), Identite.Niveau.COURRIEL);
        Invitation signee = new Invitation(1L, 10L, 20L, 8L, "h2", ACTIVATION.plusSeconds(864_000), Identite.Niveau.COURRIEL);
        signee.verifier(ACTIVATION);
        signee.consentir(ACTIVATION);
        signee.signer("B", null, null, "{}", "{}", ACTIVATION);
        when(invitations.findByTenantIdAndEtapeId(1L, 20L)).thenReturn(List.of(ouverte, signee));

        assertEquals(1, service.executer());
        verify(parcours).notifierTache(eq(trousse), eq(e), eq(new Parcours.Affectation(Parcours.TypeAffecte.PARTICIPANT, "7")), eq(ModeleMessage.Evenement.RAPPEL));
        verify(parcours, never()).notifierProprietaire(any(), any(), any());
    }

    @Test
    void une_erreur_sur_une_etape_n_interrompt_pas_les_autres() {
        EtapeTrousse e = etape(Parcours.TypeEtape.VALIDATION, "[{\"type\":\"GROUPE\",\"valeur\":\"G\"}]", null);
        when(etapes.findByStatut(Parcours.StatutEtape.ACTIVE)).thenReturn(List.of(e, e));
        when(decisions.findByTenantIdAndEtapeIdOrderByDecideLeAsc(1L, 20L)).thenThrow(new IllegalStateException("panne")).thenReturn(List.of());
        assertEquals(1, service.executer());
        assertNull(portee.courant(), "l'erreur n'a pas laissé le tenant de l'étape en place");
    }

    @Test
    void une_etape_dont_la_trousse_a_disparu_est_ignoree() {
        EtapeTrousse e = etape(Parcours.TypeEtape.VALIDATION, "[{\"type\":\"GROUPE\",\"valeur\":\"G\"}]", null);
        when(etapes.findByStatut(Parcours.StatutEtape.ACTIVE)).thenReturn(List.of(e));
        when(trousses.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.empty());
        assertEquals(0, service.executer());
        verifyNoInteractions(parcours, journal);
    }
}
