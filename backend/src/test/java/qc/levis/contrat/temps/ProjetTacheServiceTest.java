package qc.levis.contrat.temps;

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
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.rh.application.repository.EmployeRepository;
import qc.levis.contrat.rh.domain.Employe;
import qc.levis.contrat.temps.application.ProjetTacheService;
import qc.levis.contrat.temps.application.dto.ProjetRequest;
import qc.levis.contrat.temps.application.dto.TacheProjetRequest;
import qc.levis.contrat.temps.application.repository.ProjetRepository;
import qc.levis.contrat.temps.application.repository.TacheProjetRepository;
import qc.levis.contrat.temps.domain.Projet;
import qc.levis.contrat.temps.domain.TacheProjet;

@ExtendWith(MockitoExtension.class)
class ProjetTacheServiceTest {

    @Mock ProjetRepository projets;
    @Mock TacheProjetRepository taches;
    @Mock EmployeRepository employes;
    ProjetTacheService service;

    @BeforeEach void avant() { service = new ProjetTacheService(projets, taches, employes); TenantContext.set(1L); }
    @AfterEach void apres() { TenantContext.clear(); }

    private Projet projet() { Projet p = new Projet(1L, "P1", "Projet 1", null, null); ReflectionTestUtils.setField(p, "id", 3L); return p; }
    private Employe employe(long id, String courriel) {
        Employe e = new Employe(); ReflectionTestUtils.setField(e, "id", id); e.setTenantId(1L); e.setPrenom("Marie"); e.setNom("Tremblay"); e.setCourriel(courriel); return e;
    }

    @Test void creer_projet_refuse_code_duplique() {
        when(projets.existsByTenantIdAndCode(1L, "P1")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.creerProjet(new ProjetRequest("P1", "x", null, null)));
    }

    @Test void creer_tache_assignee_a_une_personne() {
        when(projets.findByIdAndTenantId(3L, 1L)).thenReturn(Optional.of(projet()));
        when(employes.findByIdAndTenantId(7L, 1L)).thenReturn(Optional.of(employe(7, "marie@x.ca")));
        when(taches.save(any())).thenAnswer(i -> { TacheProjet t = i.getArgument(0); ReflectionTestUtils.setField(t, "id", 50L); return t; });
        var v = service.creerTache(new TacheProjetRequest(3L, "Analyser", "desc", "feature", "A_FAIRE", 7L, 4.0, "2026-07-01"));
        assertEquals(7L, v.assigneId());
        assertEquals("Marie Tremblay", v.assigneNom());
        assertEquals("Projet 1", v.projetNom());
    }

    @Test void creer_tache_projet_invalide_refuse() {
        when(projets.findByIdAndTenantId(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> service.creerTache(new TacheProjetRequest(99L, "x", null, null, null, null, null, null)));
    }

    @Test void reassigner_une_tache() {
        TacheProjet t = new TacheProjet(1L, 3L, "T", null, "feature", "A_FAIRE", null, null, null);
        ReflectionTestUtils.setField(t, "id", 50L);
        when(taches.findByIdAndTenantId(50L, 1L)).thenReturn(Optional.of(t));
        when(projets.findByIdAndTenantId(3L, 1L)).thenReturn(Optional.of(projet()));
        when(employes.findByIdAndTenantId(7L, 1L)).thenReturn(Optional.of(employe(7, "marie@x.ca")));
        when(taches.save(any())).thenAnswer(i -> i.getArgument(0));
        var v = service.modifierTache(50L, new TacheProjetRequest(3L, "T", null, "feature", "EN_COURS", 7L, null, null));
        assertEquals(7L, v.assigneId());
        assertEquals("EN_COURS", v.statut());
    }

    @Test void mes_taches_par_courriel() {
        when(employes.findByTenantIdOrderByNomAscPrenomAsc(1L)).thenReturn(List.of(employe(7, "marie@x.ca")));
        when(taches.findByTenantIdAndAssigneIdOrderByCreeLeDesc(1L, 7L)).thenReturn(List.of());
        assertNotNull(service.mesTaches("marie@x.ca"));
        verify(taches).findByTenantIdAndAssigneIdOrderByCreeLeDesc(1L, 7L);
    }
}
