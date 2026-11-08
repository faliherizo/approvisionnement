package qc.levis.contrat.reference.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.application.ModuleAccess;
import qc.levis.contrat.commun.module.application.ModuleNonSouscrit;
import qc.levis.contrat.reference.application.dto.DeclarationCategorie;
import qc.levis.contrat.reference.application.dto.Option;
import qc.levis.contrat.reference.application.dto.ReferenceRequest;
import qc.levis.contrat.reference.application.dto.ValeurReference;
import qc.levis.contrat.reference.application.repository.CategorieReferenceRepository;
import qc.levis.contrat.reference.application.repository.ReferenceValueRepository;
import qc.levis.contrat.reference.domain.CategorieReference;
import qc.levis.contrat.reference.domain.ReferenceValue;

/**
 * Listes de référence : chaque liste relève du module qui la déclare ; l'organisation ajoute, personnalise ou retire les
 * valeurs de la plateforme, et supprimer sa personnalisation rétablit la valeur de la plateforme.
 */
class ReferenceServiceTest {

    private static final Long ORG = 7L;

    private final ReferenceValueRepository repo = mock(ReferenceValueRepository.class);
    private final CategorieReferenceRepository categories = mock(CategorieReferenceRepository.class);
    private final ModuleAccess modules = mock(ModuleAccess.class);
    private final ReferenceService service = new ReferenceService(repo, categories, modules);
    private final CategorieReference types = categorie("CLM_TYPE_CONTRAT", "clm", "Mots-clés");
    private final CategorieReference unites = categorie("CLM_UNITE", "clm", null);

    private static CategorieReference categorie(String code, String module, String libelleDetail) {
        CategorieReference c = new CategorieReference(code, module);
        c.decrire(module, code, null, libelleDetail, 0);
        return c;
    }

    private static ReferenceValue valeur(long id, String categorie, String code, String libelle, Long tenant, int ordre, boolean actif) {
        ReferenceValue v = new ReferenceValue(categorie, code, libelle, tenant, ordre);
        v.setActif(actif);
        ReflectionTestUtils.setField(v, "id", id);
        return v;
    }

    @BeforeEach
    void preparer() {
        when(categories.findByCode("CLM_TYPE_CONTRAT")).thenReturn(Optional.of(types));
        when(categories.findByCode("CLM_UNITE")).thenReturn(Optional.of(unites));
        when(modules.actif("clm")).thenReturn(true);
        when(repo.save(any())).thenAnswer(a -> a.getArgument(0));
        TenantContext.set(ORG);
    }

    @AfterEach
    void nettoyer() {
        TenantContext.clear();
    }

    @Test
    void la_declaration_amorce_les_valeurs_une_seule_fois_et_ne_garde_un_detail_que_si_la_liste_en_prevoit() {
        when(categories.findByCode("NOUVELLE")).thenReturn(Optional.empty());
        when(repo.existsByCategorieAndTenantIdIsNull("NOUVELLE")).thenReturn(false);
        service.declarer(new DeclarationCategorie("NOUVELLE", "clm", "Nouvelle", null, null, 5,
                List.of(new DeclarationCategorie.Valeur("A", "Alpha", "ignoré"), new DeclarationCategorie.Valeur("B", "Bêta", null))));
        ArgumentCaptor<ReferenceValue> amorcees = ArgumentCaptor.forClass(ReferenceValue.class);
        verify(repo, times(2)).save(amorcees.capture());
        assertNull(amorcees.getAllValues().get(0).getDetail(), "la liste n'a pas de détail");
        assertEquals(10, amorcees.getAllValues().get(1).getOrdre());
        verify(categories).save(any(CategorieReference.class));

        when(repo.existsByCategorieAndTenantIdIsNull("CLM_TYPE_CONTRAT")).thenReturn(false);
        service.declarer(new DeclarationCategorie("CLM_TYPE_CONTRAT", "clm", "Types", "Aide", "Mots-clés", 100,
                List.of(new DeclarationCategorie.Valeur("PRET", "Prêt", "prêt, taux"))));
        verify(repo, times(3)).save(amorcees.capture());
        assertEquals("prêt, taux", amorcees.getValue().getDetail(), "une liste à détail le conserve");
        assertEquals("Aide", types.getAide());
        assertEquals(100, types.getOrdre());

        when(repo.existsByCategorieAndTenantIdIsNull("CLM_UNITE")).thenReturn(true);
        service.declarer(new DeclarationCategorie("CLM_UNITE", "clm", "Unités", null, null, 110, null));
        verify(repo, times(3)).save(any(ReferenceValue.class));
    }

    @Test
    void seules_les_listes_des_modules_souscrits_sont_offertes() {
        when(categories.findAllByOrderByOrdreAscCodeAsc()).thenReturn(List.of(categorie("TYPE_CONTRAT", "redaction", null), types));
        assertEquals(List.of("CLM_TYPE_CONTRAT"), service.categories().stream().map(c -> c.code()).toList());
        when(categories.findByCode("TYPE_CONTRAT")).thenReturn(Optional.of(categorie("TYPE_CONTRAT", "redaction", null)));
        org.mockito.Mockito.doThrow(new ModuleNonSouscrit("redaction")).when(modules).exiger("redaction");
        assertThrows(ModuleNonSouscrit.class, () -> service.options("TYPE_CONTRAT"));
        assertThrows(NoSuchElementException.class, () -> service.options("INCONNUE"));
    }

    @Test
    void la_liste_effective_remplace_retire_et_complete_les_valeurs_de_la_plateforme() {
        when(repo.findByCategorieAndTenantIdIsNullOrderByOrdreAsc("CLM_TYPE_CONTRAT")).thenReturn(List.of(
                valeur(1, "CLM_TYPE_CONTRAT", "SUBVENTION", "Subvention", null, 10, true),
                valeur(2, "CLM_TYPE_CONTRAT", "PRET", "Prêt", null, 20, true),
                valeur(3, "CLM_TYPE_CONTRAT", "SERVICES", "Services", null, 30, true)));
        when(repo.findByCategorieAndTenantIdOrderByOrdreAsc("CLM_TYPE_CONTRAT", ORG)).thenReturn(List.of(
                valeur(11, "CLM_TYPE_CONTRAT", "PRET", "Prêt", ORG, 20, false),
                valeur(12, "CLM_TYPE_CONTRAT", "SERVICES", "Services pro", ORG, 5, true),
                valeur(13, "CLM_TYPE_CONTRAT", "GARANTIE", "Garantie", ORG, 15, true)));

        List<ValeurReference> effective = service.lister("CLM_TYPE_CONTRAT", false);
        assertEquals(List.of("SERVICES", "SUBVENTION", "GARANTIE", "PRET"), effective.stream().map(ValeurReference::code).toList());
        assertEquals(List.of("PERSONNALISEE", "PLATEFORME", "ORGANISATION", "PERSONNALISEE"),
                effective.stream().map(ValeurReference::origine).toList());
        List<Option> options = service.options("CLM_TYPE_CONTRAT");
        assertEquals(List.of("Services pro", "Subvention", "Garantie"), options.stream().map(Option::libelle).toList(),
                "une valeur retirée par l'organisation n'est plus proposée");
        assertEquals(3, service.lister("CLM_TYPE_CONTRAT", true).size(), "la plateforme ne voit que ses valeurs");

        TenantContext.clear();
        assertEquals(3, service.options("CLM_TYPE_CONTRAT").size(), "hors organisation, les valeurs de la plateforme");
    }

    @Test
    void creer_et_modifier_bornent_les_saisies_et_refusent_un_code_deja_pris() {
        when(repo.existsByCategorieAndCodeAndTenantId("CLM_TYPE_CONTRAT", "PRET", ORG)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.creer(new ReferenceRequest("CLM_TYPE_CONTRAT", "PRET", "Prêt", null, null, null), false));
        when(repo.existsByCategorieAndCodeAndTenantIdIsNull("CLM_TYPE_CONTRAT", "PRET")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.creer(new ReferenceRequest("CLM_TYPE_CONTRAT", "PRET", "Prêt", null, null, null), true));
        assertThrows(IllegalArgumentException.class, () -> service.creer(new ReferenceRequest("CLM_UNITE", "X", "y".repeat(256), null, 1, true), false));
        assertThrows(IllegalArgumentException.class, () -> service.creer(new ReferenceRequest("CLM_UNITE", " ", "Y", null, 1, true), false));
        assertThrows(IllegalArgumentException.class, () -> service.creer(new ReferenceRequest("CLM_UNITE", null, "Y", null, 1, true), false));

        ValeurReference propre = service.creer(new ReferenceRequest("CLM_TYPE_CONTRAT", " BAIL ", " Bail ", " bail, loyer ", null, null), false);
        assertEquals("BAIL", propre.code());
        assertEquals("bail, loyer", propre.detail());
        assertEquals(0, propre.ordre());
        assertTrue(propre.actif());
        assertEquals("ORGANISATION", propre.origine());
        when(repo.existsByCategorieAndCodeAndTenantIdIsNull("CLM_TYPE_CONTRAT", "SUBVENTION")).thenReturn(true);
        assertEquals("PERSONNALISEE", service.creer(new ReferenceRequest("CLM_TYPE_CONTRAT", "SUBVENTION", "Aide", " ", 3, false), false).origine());
        assertNull(service.creer(new ReferenceRequest("CLM_UNITE", "AUDIT", "Audit", "ignoré", 1, true), false).detail());
        assertEquals("PLATEFORME", service.creer(new ReferenceRequest("CLM_UNITE", "QC", "Québec", null, 1, true), true).origine());

        ReferenceValue existante = valeur(21, "CLM_TYPE_CONTRAT", "BAIL_COM", "Bail", ORG, 4, true);
        when(repo.findById(21L)).thenReturn(Optional.of(existante));
        ValeurReference meme = service.modifier(21L, new ReferenceRequest("CLM_TYPE_CONTRAT", "BAIL_COM", "Bail commercial", null, null, null), false);
        assertEquals(4, meme.ordre(), "un ordre absent est conservé");
        verify(repo, never()).existsByCategorieAndCodeAndTenantId("CLM_TYPE_CONTRAT", "BAIL_COM", ORG);
        when(repo.existsByCategorieAndCodeAndTenantId("CLM_TYPE_CONTRAT", "LOCATION", ORG)).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.modifier(21L,
                new ReferenceRequest("CLM_TYPE_CONTRAT", "LOCATION", "Location", null, 2, false), false));
        ValeurReference renommee = service.modifier(21L, new ReferenceRequest("CLM_TYPE_CONTRAT", "BAIL_2", "Bail", "x".repeat(10), 2, false), false);
        assertEquals(2, renommee.ordre());
        assertFalse(renommee.actif());
        assertThrows(IllegalArgumentException.class, () -> service.modifier(21L,
                new ReferenceRequest("CLM_TYPE_CONTRAT", "BAIL_2", "Bail", "x".repeat(2001), 2, false), false));
    }

    @Test
    void une_valeur_ne_se_gere_que_dans_sa_portee() {
        ReferenceValue plateforme = valeur(1, "CLM_UNITE", "QC", "Québec", null, 0, true);
        ReferenceValue autre = valeur(2, "CLM_UNITE", "MTL", "Montréal", 99L, 0, true);
        ReferenceValue propre = valeur(3, "CLM_UNITE", "LEV", "Lévis", ORG, 0, true);
        when(repo.findById(1L)).thenReturn(Optional.of(plateforme));
        when(repo.findById(2L)).thenReturn(Optional.of(autre));
        when(repo.findById(3L)).thenReturn(Optional.of(propre));
        when(repo.findById(4L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.supprimer(1L, false), "l'organisation ne supprime pas la plateforme");
        assertThrows(NoSuchElementException.class, () -> service.supprimer(2L, false), "ni une autre organisation");
        assertThrows(NoSuchElementException.class, () -> service.supprimer(3L, true), "la plateforme ne supprime pas une valeur d'organisation");
        assertThrows(NoSuchElementException.class, () -> service.supprimer(4L, false));
        service.supprimer(3L, false);
        verify(repo).delete(propre);
        service.supprimer(1L, true);
        verify(repo).delete(plateforme);
    }
}
