package qc.levis.contrat.formulaire;

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
import qc.levis.contrat.formulaire.application.FormulaireService;
import qc.levis.contrat.formulaire.application.dto.FormulaireRequest;
import qc.levis.contrat.formulaire.application.dto.StructureRequest;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.formulaire.domain.Formulaire;
import qc.levis.contrat.formulaire.domain.FormulaireVersion;
import qc.levis.contrat.formulaire.domain.StatutFormulaire;

@ExtendWith(MockitoExtension.class)
class FormulaireServiceTest {

    @Mock FormulaireRepository repo;
    @Mock FormulaireVersionRepository versions;
    FormulaireService service;

    @BeforeEach void avant() { service = new FormulaireService(repo, versions); TenantContext.set(1L); }
    @AfterEach void apres() { TenantContext.clear(); }

    private Formulaire form(long id, String domaine, boolean actif) {
        Formulaire f = new Formulaire(1L, "f" + id, "Form " + id, null);
        f.setDomaine(domaine); f.setActif(actif);
        ReflectionTestUtils.setField(f, "id", id);
        return f;
    }
    private FormulaireVersion version(int numero, StatutFormulaire statut) {
        FormulaireVersion v = new FormulaireVersion(1L, numero, "{\"pages\":[]}", "n");
        v.setStatut(statut);
        return v;
    }

    @Test void creer_cree_la_version_initiale() {
        when(repo.existsByTenantIdAndCode(1L, "demande")).thenReturn(false);
        when(repo.save(any())).thenAnswer(i -> { Formulaire f = i.getArgument(0); ReflectionTestUtils.setField(f, "id", 9L); return f; });
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(9L)).thenReturn(Optional.of(version(1, StatutFormulaire.BROUILLON)));
        var v = service.creer(new FormulaireRequest("demande", "Demande", "APPROVISIONNEMENT", null, 365));
        assertEquals("APPROVISIONNEMENT", v.domaine());
        verify(versions).save(any());
    }

    @Test void creer_refuse_code_duplique() {
        when(repo.existsByTenantIdAndCode(1L, "dup")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.creer(new FormulaireRequest("dup", "X", null, null, null)));
    }

    @Test void lister_filtre_par_domaine() {
        when(repo.findByTenantIdAndDomaineOrderByNomAsc(1L, "RH")).thenReturn(List.of(form(1, "RH", true)));
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(1L)).thenReturn(Optional.of(version(1, StatutFormulaire.PUBLIE)));
        assertEquals(1, service.lister("RH").size());
    }

    @Test void lister_ecarte_les_formulaires_supprimes() {
        when(repo.findByTenantIdOrderByNomAsc(1L)).thenReturn(List.of(form(1, null, true), form(2, null, false)));
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(1L)).thenReturn(Optional.of(version(1, StatutFormulaire.BROUILLON)));
        var liste = service.lister(null);
        assertEquals(1, liste.size());
        assertEquals(1L, liste.get(0).id());
    }

    @Test void catalogue_ne_garde_que_les_publies_actifs() {
        when(repo.findByTenantIdOrderByNomAsc(1L)).thenReturn(List.of(form(1, null, true), form(2, null, true)));
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(1L)).thenReturn(Optional.of(version(1, StatutFormulaire.PUBLIE)));
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(2L)).thenReturn(Optional.of(version(1, StatutFormulaire.BROUILLON)));
        assertEquals(1, service.catalogue(null).size());
    }

    @Test void maj_structure_sur_brouillon_modifie_la_version_courante() {
        when(repo.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(form(1, null, true)));
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(1L)).thenReturn(Optional.of(version(1, StatutFormulaire.BROUILLON)));
        when(versions.save(any())).thenAnswer(i -> i.getArgument(0));
        var v = service.majStructure(1L, new StructureRequest("{\"pages\":[1]}", "maj"));
        assertEquals("BROUILLON", v.statut());
        verify(versions, times(1)).save(any());
    }

    @Test void maj_structure_sur_publie_cree_un_nouveau_brouillon() {
        when(repo.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(form(1, null, true)));
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(1L)).thenReturn(Optional.of(version(1, StatutFormulaire.PUBLIE)));
        when(versions.save(any())).thenAnswer(i -> i.getArgument(0));
        service.majStructure(1L, new StructureRequest("{\"pages\":[]}", "correction"));
        verify(versions, times(2)).save(any()); // nouveau brouillon + maj
    }

    @Test void soumettre_puis_publier_suivent_les_transitions() {
        when(repo.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(form(1, null, true)));
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(1L)).thenReturn(Optional.of(version(1, StatutFormulaire.BROUILLON)));
        when(versions.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("EN_APPROBATION", service.soumettre(1L).statut());

        when(versions.findTopByFormulaireIdOrderByNumeroDesc(1L)).thenReturn(Optional.of(version(1, StatutFormulaire.EN_APPROBATION)));
        var pub = service.publier(1L);
        assertEquals("PUBLIE", pub.statut());
        assertNotNull(pub.publieLe());
    }

    @Test void dupliquer_copie_le_domaine() {
        when(repo.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(form(1, "RH", true)));
        when(repo.existsByTenantIdAndCode(1L, "copie")).thenReturn(false);
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(anyLong())).thenReturn(Optional.of(version(1, StatutFormulaire.BROUILLON)));
        when(repo.save(any())).thenAnswer(i -> { Formulaire f = i.getArgument(0); ReflectionTestUtils.setField(f, "id", 2L); return f; });
        assertEquals("RH", service.dupliquer(1L, "copie", "Copie").domaine());
    }

    @Test void supprimer_desactive_le_formulaire() {
        Formulaire f = form(1, null, true);
        when(repo.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(f));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        service.supprimer(1L);
        assertFalse(f.isActif());
    }

    @Test void exiger_introuvable_leve() {
        when(repo.findByIdAndTenantId(77L, 1L)).thenReturn(Optional.empty());
        assertThrows(java.util.NoSuchElementException.class, () -> service.versionCourante(77L));
    }
}
