package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.ConservationService;
import qc.levis.contrat.clm.application.ContexteClm;
import qc.levis.contrat.clm.application.DocumentClmService;
import qc.levis.contrat.clm.application.GroupesClmService;
import qc.levis.contrat.clm.application.ModeleService;
import qc.levis.contrat.clm.application.TrousseService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.GroupeClmRepository;
import qc.levis.contrat.clm.application.repository.ModeleContratRepository;
import qc.levis.contrat.clm.application.repository.RegleConservationRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Habilitations;

/**
 * Chargement d'un jeu de démonstration : ce qui existe déjà n'est pas recréé, un élément en échec est écarté avec son
 * motif sans empêcher les autres, et une organisation sans jeu est refusée.
 */
class DemonstrationClmTest {

    private final ModeleService modeles = mock(ModeleService.class);
    private final GroupesClmService groupes = mock(GroupesClmService.class);
    private final ConservationService conservation = mock(ConservationService.class);
    private final GroupeClmRepository groupesLus = mock(GroupeClmRepository.class);
    private final RegleConservationRepository reglesLues = mock(RegleConservationRepository.class);
    private final ContexteClm ctx = mock(ContexteClm.class);
    private final TrousseService trousses = mock(TrousseService.class);
    private final DocumentClmService documents = mock(DocumentClmService.class);
    private final TrousseRepository troussesLues = mock(TrousseRepository.class);
    private final qc.levis.contrat.clm.application.repository.DocumentTrousseRepository documentsLus =
            mock(qc.levis.contrat.clm.application.repository.DocumentTrousseRepository.class);

    private DemonstrationClm avec(DemonstrationClm.DemoClm demo) {
        DemonstrationClm.Moyens moyens = new DemonstrationClm.Moyens(modeles, trousses, documents,
                groupes, conservation, mock(ModeleContratRepository.class), troussesLues, groupesLus, reglesLues, documentsLus);
        when(ctx.tenant()).thenReturn(5L);
        when(ctx.acteur(anyString(), anyList())).thenAnswer(i -> new Habilitations.Acteur(i.getArgument(0), Set.copyOf(i.<List<String>>getArgument(1)), Set.of()));
        return new DemonstrationClm(moyens, ctx) {
            @Override
            Map<String, DemoClm> lire() {
                return Map.of("org", demo);
            }
        };
    }

    private static DemonstrationClm.DemoClm demo(Map<String, List<String>> acteurs) {
        ClmDto.ModeleRequest modele = new ClmDto.ModeleRequest("Entente", "SUBVENTION", null, "Texte", List.of(), List.of(), List.of(), null);
        return new DemonstrationClm.DemoClm("affaires@org", "principal@org", acteurs,
                List.of(new DemonstrationClm.GroupeDemo("JUR", "Juridique", List.of("a@org", "b@org")),
                        new DemonstrationClm.GroupeDemo("EXISTANT", "Déjà là", List.of())),
                List.of(new ClmDto.RegleConservationRequest("PRET", 7, "DETRUIRE")), List.of(modele), List.of());
    }

    @Test
    void une_organisation_sans_jeu_de_demonstration_est_refusee() {
        DemonstrationClm d = avec(demo(Map.of()));
        assertEquals(Set.of("org"), d.organisations());
        assertThrows(NoSuchElementException.class, () -> d.charger("autre"));
    }

    @Test
    void ce_qui_existe_reste_et_un_element_sans_acteur_declare_est_ecarte_avec_son_motif() {
        when(groupesLus.existsByTenantIdAndCode(5L, "EXISTANT")).thenReturn(true);
        when(groupes.creer(any(), eq("affaires@org"))).thenReturn(new ClmDto.GroupeVue(11L, "JUR", "Juridique", List.of(), false));

        ClmDto.BilanDemonstration bilan = avec(demo(Map.of("affaires@org", List.of("ADMIN_AFFAIRES")))).charger("org");

        assertEquals(1, bilan.groupes(), "le groupe existant n'est pas recréé");
        verify(groupes).ajouterMembre(eq(11L), eq(new ClmDto.MembreRequest("b@org")), eq("affaires@org"));
        assertEquals(1, bilan.regles());
        assertEquals(0, bilan.modeles(), "le publieur n'est pas déclaré parmi les acteurs");
        assertEquals(1, bilan.ecarts().size());
        assertTrue(bilan.ecarts().get(0).startsWith("modèle « Entente » : Compte absent des acteurs"), bilan.ecarts().toString());
        verify(modeles, never()).creer(any(), any());
    }

    @Test
    void une_regle_deja_definie_pour_le_type_n_est_pas_remplacee() {
        qc.levis.contrat.clm.domain.RegleConservation existante = mock(qc.levis.contrat.clm.domain.RegleConservation.class);
        when(existante.getTypeContrat()).thenReturn("PRET");
        when(reglesLues.findByTenantIdOrderByTypeContratAsc(5L)).thenReturn(List.of(existante));
        when(groupes.creer(any(), anyString())).thenReturn(new ClmDto.GroupeVue(12L, "JUR", "Juridique", List.of(), false));

        ClmDto.BilanDemonstration bilan = avec(demo(null)).charger("org");

        assertEquals(0, bilan.regles());
        verify(conservation, never()).definir(any(), anyString());
        assertEquals(1, bilan.ecarts().size(), "sans acteurs déclarés, le modèle est écarté");
    }

    private static DemonstrationClm.DemoClm demoTrousses(String... references) {
        List<DemonstrationClm.TrousseDemo> liste = java.util.Arrays.stream(references).map(r -> new DemonstrationClm.TrousseDemo("init@org", null,
                new ClmDto.TrousseRequest(r, "Titre " + r, null, null, "FAIBLE", null, null, null, null, null, null), Map.of(), List.of(), true)).toList();
        return new DemonstrationClm.DemoClm("affaires@org", "principal@org", Map.of("init@org", List.of("REDACTEUR")),
                List.of(), List.of(), List.of(), liste);
    }

    @Test
    void une_trousse_laissee_en_brouillon_sans_document_est_reprise_les_autres_sont_laissees_telles_quelles() {
        qc.levis.contrat.clm.domain.Trousse inachevee = mock(qc.levis.contrat.clm.domain.Trousse.class);
        when(inachevee.getId()).thenReturn(21L);
        when(inachevee.getStatut()).thenReturn(qc.levis.contrat.clm.domain.StatutTrousse.BROUILLON);
        qc.levis.contrat.clm.domain.Trousse complete = mock(qc.levis.contrat.clm.domain.Trousse.class);
        when(complete.getId()).thenReturn(22L);
        when(complete.getStatut()).thenReturn(qc.levis.contrat.clm.domain.StatutTrousse.BROUILLON);
        when(troussesLues.findByTenantIdAndReference(5L, "DEMO-A")).thenReturn(java.util.Optional.of(inachevee));
        when(troussesLues.findByTenantIdAndReference(5L, "DEMO-B")).thenReturn(java.util.Optional.of(complete));
        when(documentsLus.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(5L, 22L))
                .thenReturn(List.of(mock(qc.levis.contrat.clm.domain.DocumentTrousse.class)));

        ClmDto.BilanDemonstration bilan = avec(demoTrousses("demo-a", "demo-b")).charger("org");

        assertEquals(1, bilan.trousses(), "seule la trousse inachevée est reprise");
        verify(documents).generer(eq(21L), any(), eq(qc.levis.contrat.clm.domain.FormatDocument.Type.PDF));
        verify(trousses).lancer(eq(21L), any());
        verify(documents, never()).generer(eq(22L), any(), any());
        verify(trousses, never()).creer(any(), any());
    }
}
