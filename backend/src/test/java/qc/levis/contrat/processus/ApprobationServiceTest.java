package qc.levis.contrat.processus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.processus.application.ApprobationService;
import qc.levis.contrat.processus.application.dto.DecisionApproRequest;
import qc.levis.contrat.processus.application.dto.DemandeApproRequest;
import qc.levis.contrat.processus.application.dto.RegleApproRequest;
import qc.levis.contrat.processus.application.repository.DemandeApprobationRepository;
import qc.levis.contrat.processus.application.repository.EtapeApprobationRepository;
import qc.levis.contrat.processus.application.repository.RegleApprobationRepository;
import qc.levis.contrat.processus.domain.DemandeApprobation;
import qc.levis.contrat.processus.domain.EtapeApprobation;
import qc.levis.contrat.processus.domain.RegleApprobation;

@ExtendWith(MockitoExtension.class)
class ApprobationServiceTest {

    @Mock RegleApprobationRepository regles;
    @Mock DemandeApprobationRepository demandes;
    @Mock EtapeApprobationRepository etapes;
    ApprobationService service;

    @BeforeEach void avant() { service = new ApprobationService(regles, demandes, etapes); TenantContext.set(1L); }
    @AfterEach void apres() { TenantContext.clear(); }

    @Test void enregistrer_regle_invalide_rejetee() {
        assertThrows(IllegalArgumentException.class,
                () -> service.enregistrerRegle(new RegleApproRequest("achat", "montant", "between", "1000", 1, "DIRECTION", null)));
    }

    @Test void creer_demande_genere_les_etapes_selon_les_regles() {
        RegleApprobation r = new RegleApprobation(1L, "achat", "montant", "gte", "10000", 1, "DIRECTION", null);
        when(regles.findByTenantIdAndDocumentType(1L, "achat")).thenReturn(List.of(r));
        when(demandes.save(any())).thenAnswer(i -> { DemandeApprobation d = i.getArgument(0); ReflectionTestUtils.setField(d, "id", 100L); return d; });
        when(etapes.save(any())).thenAnswer(i -> i.getArgument(0));
        EtapeApprobation etape = new EtapeApprobation(1L, 100L, 1, "DIRECTION", null, "1");
        when(etapes.findByTenantIdAndDemandeIdOrderByNiveauAsc(1L, 100L)).thenReturn(List.of(etape));

        var vue = service.creerDemande(new DemandeApproRequest("achat", "AP-1", Map.of("montant", 25000)), "u");
        assertEquals(1, vue.etapes().size());
        verify(etapes).save(any());
    }

    @Test void creer_demande_sans_regle_est_approuvee_directement() {
        when(regles.findByTenantIdAndDocumentType(1L, "achat")).thenReturn(List.of());
        when(demandes.save(any())).thenAnswer(i -> { DemandeApprobation d = i.getArgument(0); ReflectionTestUtils.setField(d, "id", 101L); return d; });
        when(etapes.findByTenantIdAndDemandeIdOrderByNiveauAsc(1L, 101L)).thenReturn(List.of());
        var vue = service.creerDemande(new DemandeApproRequest("achat", null, Map.of("montant", 10)), "u");
        assertEquals("APPROVED", vue.statut());
    }

    @Test void decider_approuve_la_derniere_etape_et_valide_la_demande() {
        EtapeApprobation e = new EtapeApprobation(1L, 100L, 1, "DIRECTION", null, "1");
        ReflectionTestUtils.setField(e, "id", 10L);
        when(etapes.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(e));
        when(etapes.save(any())).thenAnswer(i -> i.getArgument(0));
        when(etapes.findByTenantIdAndDemandeIdOrderByNiveauAsc(1L, 100L)).thenReturn(List.of(e));
        when(demandes.findByIdAndTenantId(100L, 1L)).thenReturn(Optional.of(mkDemande(100L, "PENDING")));
        when(demandes.save(any())).thenAnswer(i -> i.getArgument(0));
        var vue = service.decider(10L, List.of("DIRECTION"), "dir@x.ca", new DecisionApproRequest("APPROUVE", "ok"));
        assertEquals("APPROVED", vue.statut());
    }

    @Test void decider_sans_role_assigne_refuse() {
        EtapeApprobation e = new EtapeApprobation(1L, 100L, 1, "DIRECTION", null, "1");
        ReflectionTestUtils.setField(e, "id", 10L);
        when(etapes.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(e));
        assertThrows(IllegalStateException.class,
                () -> service.decider(10L, List.of("REDACTEUR"), "u", new DecisionApproRequest("APPROUVE", null)));
    }

    private DemandeApprobation mkDemande(Long id, String statut) {
        DemandeApprobation d = new DemandeApprobation(1L, "achat", "AP-1", "{}", "u");
        ReflectionTestUtils.setField(d, "id", id);
        d.setStatut(statut);
        return d;
    }
}
