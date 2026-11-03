package qc.levis.contrat.processus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.processus.application.ProcessusService;
import qc.levis.contrat.processus.application.WorkflowService;
import qc.levis.contrat.processus.application.dto.DecisionRequest;
import qc.levis.contrat.processus.application.repository.DecisionRepository;
import qc.levis.contrat.processus.application.repository.TacheRepository;
import qc.levis.contrat.processus.application.repository.WorkflowRepository;
import qc.levis.contrat.processus.domain.EtapeDef;
import qc.levis.contrat.processus.domain.StatutTache;
import qc.levis.contrat.processus.domain.Tache;
import qc.levis.contrat.processus.domain.WorkflowDef;
import qc.levis.contrat.saisie.application.SaisieService;
import qc.levis.contrat.saisie.application.dto.SaisieVue;

@ExtendWith(MockitoExtension.class)
class ProcessusServiceTest {

    @Mock WorkflowService workflows;
    @Mock WorkflowRepository workflowRepo;
    @Mock TacheRepository taches;
    @Mock DecisionRepository decisions;
    @Mock ModuleService modules;
    @Mock SaisieService saisies;
    @Mock FormulaireRepository formulaires;
    ProcessusService service;

    @BeforeEach void avant() {
        service = new ProcessusService(workflows, workflowRepo, taches, decisions, modules, saisies, formulaires);
        TenantContext.set(1L);
    }
    @AfterEach void apres() { TenantContext.clear(); }

    private Tache tache(boolean commentaireObligatoire) {
        return new Tache(1L, 10L, 2L, "e1", "Approbation", "APPRO", null, commentaireObligatoire, 0, null);
    }

    @Test void mes_taches_vides_sans_roles() {
        assertTrue(service.mesTaches(List.of()).isEmpty());
        verifyNoInteractions(taches);
    }

    @Test void decider_refuse_sans_role_assigne() {
        when(taches.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(tache(false)));
        assertThrows(IllegalStateException.class,
                () -> service.decider(1L, List.of("REDACTEUR"), "u", new DecisionRequest("APPROUVE", null)));
    }

    @Test void decider_exige_un_commentaire_au_refus_si_configure() {
        when(taches.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(tache(true)));
        assertThrows(IllegalArgumentException.class,
                () -> service.decider(1L, List.of("APPRO"), "u", new DecisionRequest("REFUSE", "  ")));
    }

    @Test void refuser_rejette_la_demande_et_annule_les_taches() {
        when(taches.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(tache(false)));
        when(taches.findByTenantIdAndSaisieIdOrderByOrdreAsc(1L, 10L)).thenReturn(List.of());
        service.decider(1L, List.of("APPRO"), "u", new DecisionRequest("REFUSE", "non conforme"));
        verify(saisies).rejeter(10L);
        verify(decisions).save(any());
    }

    @Test void approuver_derniere_etape_valide_la_demande() {
        when(taches.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(tache(false)));
        when(workflowRepo.findByTenantIdAndFormulaireId(1L, 2L)).thenReturn(Optional.empty());
        when(workflows.extraire(any())).thenReturn(new WorkflowDef(List.of(
                new EtapeDef("e1", "Approbation", "APPROBATION", "APPRO", null, 3, false, List.of()))));
        when(saisies.obtenir(10L)).thenReturn(new SaisieVue(10L, 2L, "AP-1", 1, "EN_TRAITEMENT", "{}", null, null));
        service.decider(1L, List.of("APPRO"), "u", new DecisionRequest("APPROUVE", "ok"));
        verify(saisies).valider(10L);   // dernière (et seule) étape approuvée → demande validée
    }

    @Test void sur_soumission_cree_la_premiere_tache_et_prend_en_charge() {
        when(modules.estActif(1L, "processus")).thenReturn(true);
        when(workflowRepo.findByTenantIdAndFormulaireId(1L, 2L)).thenReturn(Optional.empty());
        when(workflows.extraire(any())).thenReturn(new WorkflowDef(List.of(
                new EtapeDef("e1", "Approbation", "APPROBATION", "APPRO", null, 3, false, List.of()))));
        when(taches.save(any())).thenAnswer(i -> i.getArgument(0));
        service.surFiche(new qc.levis.contrat.saisie.application.FicheEvent(1L, 2L, 10L, "SOUMISSION", java.util.Map.of()));
        verify(taches).save(any());
        verify(saisies).prendreEnCharge(10L);
    }

    @Test void sur_soumission_sans_workflow_ne_fait_rien() {
        when(modules.estActif(1L, "processus")).thenReturn(true);
        when(workflowRepo.findByTenantIdAndFormulaireId(1L, 2L)).thenReturn(Optional.empty());
        when(workflows.extraire(any())).thenReturn(new WorkflowDef(List.of()));
        service.surFiche(new qc.levis.contrat.saisie.application.FicheEvent(1L, 2L, 10L, "SOUMISSION", java.util.Map.of()));
        verify(taches, never()).save(any());
    }

    @Test void mes_taches_enrichit_du_numero_et_du_formulaire() {
        when(taches.findByTenantIdAndAssigneRoleInAndStatutInOrderByEcheanceLeAsc(eq(1L), eq(List.of("APPRO")), any()))
                .thenReturn(List.of(tache(false)));
        when(saisies.obtenir(10L)).thenReturn(new qc.levis.contrat.saisie.application.dto.SaisieVue(10L, 2L, "AP-1", 1, "EN_TRAITEMENT", "{}", null, null));
        qc.levis.contrat.formulaire.domain.Formulaire f = new qc.levis.contrat.formulaire.domain.Formulaire(1L, "da", "Demande achat", null);
        f.setDomaine("APPROVISIONNEMENT");
        when(formulaires.findByIdAndTenantId(2L, 1L)).thenReturn(Optional.of(f));
        var vues = service.mesTaches(List.of("APPRO"));
        assertEquals(1, vues.size());
        assertEquals("AP-1", vues.get(0).numeroDemande());
        assertEquals("Demande achat", vues.get(0).formulaireNom());
    }

    @Test void decider_sur_tache_terminee_est_refuse() {
        Tache t = tache(false);
        t.setStatut(StatutTache.TERMINEE);
        when(taches.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(t));
        assertThrows(IllegalStateException.class,
                () -> service.decider(1L, List.of("APPRO"), "u", new DecisionRequest("APPROUVE", null)));
    }
}
