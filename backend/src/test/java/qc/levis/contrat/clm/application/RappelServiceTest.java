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
import qc.levis.contrat.clm.domain.Relances;
import qc.levis.contrat.clm.domain.ParametresClm;

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
    @Mock ParametresClmService parametres;
    final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    RappelService service;
    Trousse trousse;

    @BeforeEach
    void preparer() {
        ContexteClm ctx = new ContexteClm(perimetre, portee, new ObjectMapper(), Clock.fixed(ACTIVATION.plusSeconds(3 * 86_400), ZoneOffset.UTC));
        lenient().doCallRealMethod().when(decisions).duCycleEnCours(any());
        lenient().when(parametres.reglages(1L)).thenReturn(reglages(Relances.PREAVIS_PAR_DEFAUT_JOURS));
        service = new RappelService(etapes, decisions, trousses, invitations, parcours, journal, ctx, parametres);
        trousse = new Trousse(1L, "TR-1", "T", "init@x.ca", ACTIVATION);
        ReflectionTestUtils.setField(trousse, "id", 10L);
        when(trousses.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(trousse));
    }

    private static ParametresClm.Reglages reglages(int preavisJours) {
        return new ParametresClm.Reglages(ParametresClm.UN_MO, 14, "https://iq.example/portail", "IQ", 2, 3, preavisJours,
                Identite.Politique.DEFAUT, "montant");
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

    @Test
    void echeance_proche_alerte_une_fois_ceux_qui_n_ont_pas_repondu() {
        EtapeTrousse e = etape(Parcours.TypeEtape.VALIDATION,
                "[{\"type\":\"UTILISATEUR\",\"valeur\":\"a@x.ca\"},{\"type\":\"UTILISATEUR\",\"valeur\":\"b@x.ca\"}]", 4);
        when(etapes.findByStatut(Parcours.StatutEtape.ACTIVE)).thenReturn(List.of(e));
        when(decisions.findByTenantIdAndEtapeIdOrderByDecideLeAsc(1L, 20L)).thenReturn(List.of(
                new DecisionEtape(1L, 10L, 20L, "b@x.ca", DecisionEtape.Nature.APPROUVE, null, ACTIVATION.plusSeconds(60))));

        service.executer();
        service.executer();

        verify(parcours, times(1)).notifierTache(trousse, e, new Parcours.Affectation(Parcours.TypeAffecte.UTILISATEUR, "a@x.ca"),
                ModeleMessage.Evenement.ECHEANCE);
        verify(parcours, never()).notifierTache(any(), any(), eq(new Parcours.Affectation(Parcours.TypeAffecte.UTILISATEUR, "b@x.ca")),
                eq(ModeleMessage.Evenement.ECHEANCE));
        verify(journal, times(1)).consigner(trousse, "ECHEANCE_PROCHE", "systeme", "Étape · 1 destinataire(s)");
        assertTrue(e.isEcheanceProcheSignalee());
        verify(parcours, never()).notifierProprietaire(any(), eq(ModeleMessage.Evenement.RETARD), any());
    }

    @Test
    void sans_preavis_ou_hors_preavis_aucune_alerte_d_echeance() {
        EtapeTrousse proche = etape(Parcours.TypeEtape.VALIDATION, "[{\"type\":\"GROUPE\",\"valeur\":\"G\"}]", 4);
        when(etapes.findByStatut(Parcours.StatutEtape.ACTIVE)).thenReturn(List.of(proche));
        when(parametres.reglages(1L)).thenReturn(reglages(0));
        service.executer();
        assertFalse(proche.isEcheanceProcheSignalee(), "préavis nul : l'organisation a désactivé l'alerte");

        EtapeTrousse lointaine = etape(Parcours.TypeEtape.VALIDATION, "[{\"type\":\"GROUPE\",\"valeur\":\"G\"}]", 10);
        when(etapes.findByStatut(Parcours.StatutEtape.ACTIVE)).thenReturn(List.of(lointaine));
        when(parametres.reglages(1L)).thenReturn(reglages(2));
        service.executer();
        assertFalse(lointaine.isEcheanceProcheSignalee(), "échéance dans 7 jours, préavis de 2 jours");
        verify(parcours, never()).notifierTache(any(), any(), any(), eq(ModeleMessage.Evenement.ECHEANCE));
    }

    @Test
    void une_trousse_critique_est_relancee_plus_tot_qu_une_trousse_normale() {
        EtapeTrousse e = etape(Parcours.TypeEtape.VALIDATION, "[{\"type\":\"GROUPE\",\"valeur\":\"G\"}]", null);
        when(etapes.findByStatut(Parcours.StatutEtape.ACTIVE)).thenReturn(List.of(e));
        trousse.setRappelFrequenceJours(5);
        assertEquals(0, service.executer(), "normale : 5 jours entre deux rappels, 3 jours écoulés");

        trousse.prioriser(qc.levis.contrat.clm.domain.PrioriteTrousse.CRITIQUE);
        assertEquals(1, service.executer(), "critique : 5 jours / 4 = 30 h, 3 jours écoulés");
        verify(journal).consigner(trousse, "RAPPEL_ENVOYE", "systeme", "Étape · 1 destinataire(s)");
    }
}
