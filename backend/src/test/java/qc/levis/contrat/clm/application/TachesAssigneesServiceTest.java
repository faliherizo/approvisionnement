package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AnnuaireOrganisation;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.port.ReferencesClm;
import qc.levis.contrat.clm.application.repository.TacheAssigneeRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.ModeleMessage.Evenement;
import qc.levis.contrat.clm.domain.TacheAssignee;
import qc.levis.contrat.clm.domain.Trousse;

/** Tâches assignées : annuaire et liste de l'organisation, intervenants, journal de la trousse et message à l'assigné. */
class TachesAssigneesServiceTest {

    private static final Long TENANT = 7L;
    private static final Instant MAINTENANT = Instant.parse("2026-11-03T01:30:00Z");
    private static final Habilitations.Acteur CREATEUR = new Habilitations.Acteur("createur@iq.ca", java.util.Set.of("REDACTEUR"), java.util.Set.of());
    private static final Habilitations.Acteur JULIE = new Habilitations.Acteur("julie@iq.ca", java.util.Set.of("JURIDIQUE"), java.util.Set.of());
    private static final Habilitations.Acteur TIERS = new Habilitations.Acteur("tiers@iq.ca", java.util.Set.of("JURIDIQUE"), java.util.Set.of());

    private final TacheAssigneeRepository taches = mock(TacheAssigneeRepository.class);
    private final TrousseRepository trousses = mock(TrousseRepository.class);
    private final LectureTrousse lecture = mock(LectureTrousse.class);
    private final ReferencesClm references = mock(ReferencesClm.class);
    private final MessagerieClm messagerie = mock(MessagerieClm.class);
    private final JournalClm journal = mock(JournalClm.class);
    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    // 01 h 30 UTC le 3 novembre = 20 h 30 le 2 novembre à Québec : « aujourd'hui » suit le fuseau de l'horloge.
    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(),
            Clock.fixed(MAINTENANT, ZoneId.of("America/Toronto")));
    private final AnnuaireOrganisation annuaire = tenant -> List.of(
            new AnnuaireOrganisation.Membre("julie@iq.ca", "Julie Juriste"),
            new AnnuaireOrganisation.Membre("marc@iq.ca", "Marc Analyste"),
            new AnnuaireOrganisation.Membre("createur@iq.ca", "Créateur"));
    private TachesAssigneesService service;
    private Trousse trousse;

    @BeforeEach
    void preparer() {
        service = new TachesAssigneesService(taches, trousses, lecture, new ListesClm(references), annuaire, messagerie, journal, ctx);
        trousse = new Trousse(TENANT, "TR-0042", "Aide Agri-Nord", "createur@iq.ca", MAINTENANT);
        when(references.valeurs("CLM_PRIORITE_TACHE")).thenReturn(List.of(
                new ReferencesClm.Valeur("NORMALE", "Normale", null), new ReferencesClm.Valeur("URGENTE", "Urgente", null)));
        when(taches.save(any())).thenAnswer(i -> i.getArgument(0));
        when(trousses.findByIdAndTenantId(42L, TENANT)).thenReturn(Optional.of(trousse));
    }

    private <T> T dansTenant(Supplier<T> action) {
        return portee.executerDans(TENANT, action);
    }

    private ClmDto.TacheAssigneeRequest demande(Long trousseId, String assigne, String priorite, LocalDate echeance) {
        return new ClmDto.TacheAssigneeRequest(trousseId, "Préparer l'annexe financière", null, assigne, priorite, echeance);
    }

    private TacheAssignee existante(String assigne, Long trousseId) {
        TacheAssignee t = TacheAssignee.creer(TENANT, new TacheAssignee.Saisie(trousseId, "Relire la clause 7", null, assigne, null,
                LocalDate.of(2026, 11, 2)), "createur@iq.ca", MAINTENANT);
        when(taches.findByIdAndTenantId(5L, TENANT)).thenReturn(Optional.of(t));
        return t;
    }

    @Test
    void l_annuaire_expose_courriel_et_nom() {
        assertEquals(List.of(new ClmDto.MembreVue("julie@iq.ca", "Julie Juriste"), new ClmDto.MembreVue("marc@iq.ca", "Marc Analyste"),
                new ClmDto.MembreVue("createur@iq.ca", "Créateur")), dansTenant(service::membres));
    }

    @Test
    void creer_sur_une_trousse_trace_au_journal_et_previent_l_assigne() {
        when(lecture.exigerContribution(42L, CREATEUR)).thenReturn(trousse);
        ClmDto.TacheAssigneeVue v = dansTenant(() -> service.creer(CREATEUR, demande(42L, "Julie@IQ.ca", "urgente", LocalDate.of(2026, 11, 2))));

        assertEquals("TR-0042", v.reference());
        assertEquals("julie@iq.ca", v.assigneA());
        assertEquals("URGENTE", v.priorite(), "code retenu tel que la liste de l'organisation l'écrit");
        assertEquals("A_FAIRE", v.statut());
        assertFalse(v.enRetard(), "due le 2 novembre, il est 20 h 30 ce jour-là à Québec");
        assertFalse(v.peutDemarrer(), "le créateur ne démarre pas la tâche d'autrui");
        assertTrue(v.peutAnnuler());
        verify(journal).consigner(trousse, "TACHE_CREEE", "createur@iq.ca", "Préparer l'annexe financière → julie@iq.ca");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> vars = ArgumentCaptor.forClass(Map.class);
        verify(messagerie).envoyer(eq(TENANT), eq(Evenement.TACHE), eq("COURRIEL"), eq("julie@iq.ca"), vars.capture(), eq(List.of()));
        assertEquals(Map.of("reference", "TR-0042", "titre", "Aide Agri-Nord", "etape", "Préparer l'annexe financière",
                "echeance", "2026-11-02"), vars.getValue());
    }

    @Test
    void se_confier_une_tache_hors_trousse_ne_journalise_ni_ne_previent() {
        ClmDto.TacheAssigneeVue v = dansTenant(() -> service.creer(JULIE, demande(null, "julie@iq.ca", " ", null)));
        assertNull(v.reference());
        assertNull(v.priorite(), "priorité facultative");
        assertTrue(v.peutDemarrer());
        verifyNoInteractions(journal, messagerie, lecture);
    }

    @Test
    void une_tache_hors_trousse_confiee_a_autrui_previent_sans_reference() {
        dansTenant(() -> service.creer(CREATEUR, demande(null, "marc@iq.ca", null, null)));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> vars = ArgumentCaptor.forClass(Map.class);
        verify(messagerie).envoyer(eq(TENANT), eq(Evenement.TACHE), eq("COURRIEL"), eq("marc@iq.ca"), vars.capture(), eq(List.of()));
        assertEquals("—", vars.getValue().get("reference"));
        assertEquals("Préparer l'annexe financière", vars.getValue().get("titre"));
        assertEquals("—", vars.getValue().get("echeance"));
        verifyNoInteractions(journal);
    }

    @Test
    void creer_refuse_une_personne_hors_organisation_une_priorite_hors_liste_et_une_demande_vide() {
        assertEquals("« inconnu@ailleurs.ca » n'est pas une personne active de l'organisation.", assertThrows(IllegalArgumentException.class,
                () -> dansTenant(() -> service.creer(CREATEUR, demande(null, " inconnu@ailleurs.ca ", null, null)))).getMessage());
        assertTrue(assertThrows(IllegalArgumentException.class,
                () -> dansTenant(() -> service.creer(CREATEUR, demande(null, "julie@iq.ca", "CRITIQUE", null)))).getMessage()
                .contains("Priorité de tâche"));
        assertThrows(IllegalArgumentException.class, () -> dansTenant(() -> service.creer(CREATEUR, null)));
        assertThrows(IllegalArgumentException.class, () -> dansTenant(() -> service.creer(CREATEUR, demande(null, "  ", null, null))),
                "courriel vide : l'entité refuse la saisie");
        verify(taches, never()).save(any());
    }

    @Test
    void creer_hors_espace_tenant_est_refuse() {
        assertThrows(IllegalStateException.class, () -> service.creer(CREATEUR, demande(null, "julie@iq.ca", null, null)));
    }

    @Test
    void lister_selon_le_filtre() {
        TacheAssignee ouverte = TacheAssignee.creer(TENANT, new TacheAssignee.Saisie(42L, "A", null, "julie@iq.ca", null,
                LocalDate.of(2026, 11, 1)), "createur@iq.ca", MAINTENANT);
        TacheAssignee libre = TacheAssignee.creer(TENANT, new TacheAssignee.Saisie(null, "B", null, "julie@iq.ca", null, null),
                "createur@iq.ca", MAINTENANT);
        when(taches.findByTenantIdAndAssigneAIgnoreCaseAndStatutInOrderByEcheanceAscIdAsc(TENANT, "julie@iq.ca",
                EnumSet.of(TacheAssignee.Statut.A_FAIRE, TacheAssignee.Statut.EN_COURS))).thenReturn(List.of(ouverte, libre));
        when(taches.findByTenantIdAndAssigneAIgnoreCaseAndStatutInOrderByEcheanceAscIdAsc(TENANT, "julie@iq.ca",
                EnumSet.of(TacheAssignee.Statut.TERMINEE))).thenReturn(List.of());
        when(taches.findByTenantIdAndCreeParIgnoreCaseOrderByIdDesc(TENANT, "createur@iq.ca")).thenReturn(List.of(libre));

        List<ClmDto.TacheAssigneeVue> miennes = dansTenant(() -> service.lister(JULIE, null));
        assertEquals(List.of("A", "B"), miennes.stream().map(ClmDto.TacheAssigneeVue::titre).toList());
        assertTrue(miennes.get(0).enRetard(), "échue le 1er novembre");
        assertEquals("TR-0042", miennes.get(0).reference());
        assertEquals(miennes, dansTenant(() -> service.lister(JULIE, " assignees ")));
        assertEquals(miennes, dansTenant(() -> service.lister(JULIE, "   ")), "un filtre vide vaut « assignées »");
        assertEquals(List.of(), dansTenant(() -> service.lister(JULIE, "terminees")));
        assertEquals(1, dansTenant(() -> service.lister(CREATEUR, "CREEES")).size());
        assertEquals("Filtre inconnu : toutes", assertThrows(IllegalArgumentException.class,
                () -> dansTenant(() -> service.lister(JULIE, "toutes"))).getMessage());
    }

    @Test
    void les_taches_d_une_trousse_suivent_sa_visibilite() {
        when(taches.findByTenantIdAndTrousseIdOrderByIdAsc(TENANT, 42L)).thenReturn(List.of());
        assertEquals(List.of(), dansTenant(() -> service.deLaTrousse(42L, TIERS)));
        verify(lecture).exigerVisible(42L, TIERS);

        when(lecture.exigerVisible(43L, TIERS)).thenThrow(new AccessDeniedException("Accès refusé."));
        assertThrows(AccessDeniedException.class, () -> dansTenant(() -> service.deLaTrousse(43L, TIERS)));
    }

    @Test
    void l_assigne_demarre_et_termine_le_createur_annule() {
        existante("julie@iq.ca", 42L);
        assertEquals("EN_COURS", dansTenant(() -> service.agir(5L, JULIE, new ClmDto.ActionTacheRequest(" demarrer "))).statut());
        verify(journal).consigner(trousse, "TACHE_EN_COURS", "julie@iq.ca", "Relire la clause 7");
        ClmDto.TacheAssigneeVue fin = dansTenant(() -> service.agir(5L, JULIE, new ClmDto.ActionTacheRequest("TERMINER")));
        assertEquals("TERMINEE", fin.statut());
        assertEquals(MAINTENANT, fin.termineeLe());
        assertFalse(fin.peutAnnuler(), "une tâche close ne s'annule plus");
        assertEquals("A_FAIRE", dansTenant(() -> service.agir(5L, CREATEUR, new ClmDto.ActionTacheRequest("ROUVRIR"))).statut());
        assertEquals("ANNULEE", dansTenant(() -> service.agir(5L, CREATEUR, new ClmDto.ActionTacheRequest("ANNULER"))).statut());
        verify(taches, times(4)).save(any());
    }

    @Test
    void les_actions_reservees_sont_refusees_en_403() {
        existante("julie@iq.ca", null);
        assertEquals("Seule la personne assignée démarre la tâche.", assertThrows(AccessDeniedException.class,
                () -> dansTenant(() -> service.agir(5L, CREATEUR, new ClmDto.ActionTacheRequest("DEMARRER")))).getMessage());
        assertEquals("Seul le créateur annule la tâche.", assertThrows(AccessDeniedException.class,
                () -> dansTenant(() -> service.agir(5L, JULIE, new ClmDto.ActionTacheRequest("ANNULER")))).getMessage());
        assertEquals("Seuls le créateur et la personne assignée agissent sur cette tâche.", assertThrows(AccessDeniedException.class,
                () -> dansTenant(() -> service.agir(5L, TIERS, new ClmDto.ActionTacheRequest("TERMINER")))).getMessage());
        verify(taches, never()).save(any());
    }

    @Test
    void action_inconnue_absente_ou_tache_introuvable() {
        existante("julie@iq.ca", null);
        assertEquals("Action inconnue : SUPPRIMER", assertThrows(IllegalArgumentException.class,
                () -> dansTenant(() -> service.agir(5L, JULIE, new ClmDto.ActionTacheRequest("supprimer")))).getMessage());
        assertEquals("Action inconnue : ", assertThrows(IllegalArgumentException.class,
                () -> dansTenant(() -> service.agir(5L, JULIE, null))).getMessage());
        assertThrows(IllegalArgumentException.class, () -> dansTenant(() -> service.agir(5L, JULIE, new ClmDto.ActionTacheRequest(null))));
        when(taches.findByIdAndTenantId(9L, TENANT)).thenReturn(Optional.empty());
        assertEquals("Tâche introuvable : 9", assertThrows(NoSuchElementException.class,
                () -> dansTenant(() -> service.agir(9L, JULIE, new ClmDto.ActionTacheRequest("TERMINER")))).getMessage());
    }

    @Test
    void reassigner_dans_l_organisation_trace_le_motif_et_previent_le_nouvel_assigne() {
        existante("julie@iq.ca", 42L);
        ClmDto.TacheAssigneeVue v = dansTenant(() -> service.reassigner(5L, JULIE, new ClmDto.DelegationRequest("marc@iq.ca", " Congé ")));
        assertEquals("marc@iq.ca", v.assigneA());
        verify(journal).consigner(trousse, "TACHE_REASSIGNEE", "julie@iq.ca", "Relire la clause 7 : julie@iq.ca → marc@iq.ca · Congé");
        verify(messagerie).envoyer(eq(TENANT), eq(Evenement.TACHE), eq("COURRIEL"), eq("marc@iq.ca"), anyMap(), eq(List.of()));

        dansTenant(() -> service.reassigner(5L, CREATEUR, new ClmDto.DelegationRequest("julie@iq.ca", "  ")));
        verify(journal).consigner(trousse, "TACHE_REASSIGNEE", "createur@iq.ca", "Relire la clause 7 : marc@iq.ca → julie@iq.ca");
        dansTenant(() -> service.reassigner(5L, JULIE, new ClmDto.DelegationRequest("marc@iq.ca", null)));
        verify(journal).consigner(trousse, "TACHE_REASSIGNEE", "julie@iq.ca", "Relire la clause 7 : julie@iq.ca → marc@iq.ca");
    }

    @Test
    void reassigner_hors_organisation_ou_sans_destinataire_est_refuse() {
        existante("julie@iq.ca", null);
        assertThrows(IllegalArgumentException.class,
                () -> dansTenant(() -> service.reassigner(5L, JULIE, new ClmDto.DelegationRequest("inconnu@ailleurs.ca", null))));
        assertThrows(IllegalArgumentException.class, () -> dansTenant(() -> service.reassigner(5L, JULIE, null)));
        verify(taches, never()).save(any());
    }

    @Test
    void une_trousse_disparue_n_empeche_pas_d_agir() {
        when(trousses.findByIdAndTenantId(99L, TENANT)).thenReturn(Optional.empty());
        existante("julie@iq.ca", 99L);
        ClmDto.TacheAssigneeVue v = dansTenant(() -> service.agir(5L, JULIE, new ClmDto.ActionTacheRequest("TERMINER")));
        assertNull(v.reference());
        verifyNoInteractions(journal);
    }
}
