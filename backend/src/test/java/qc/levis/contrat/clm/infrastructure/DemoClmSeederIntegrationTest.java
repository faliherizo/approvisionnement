package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import qc.levis.contrat.clm.application.repository.GroupeClmRepository;
import qc.levis.contrat.clm.application.repository.ModeleContratRepository;
import qc.levis.contrat.clm.application.repository.RegleConservationRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.ModeleContrat;
import qc.levis.contrat.clm.domain.Trousse;
import qc.levis.contrat.tenant.application.repository.TenantRepository;

/**
 * Démonstration CLM amorcée au démarrage (data/clm-demo.json) : chez IQ, les modèles sont publiés — la signature en lot
 * et la création de trousse les proposent d'emblée —, les groupes, règles de conservation et trousses existent ; un second
 * passage ne duplique rien.
 */
@SpringBootTest
@TestPropertySource(properties = {"app.clm.planificateur.actif=false", "app.amorcage.tenants-demo=true"})
class DemoClmSeederIntegrationTest {

    @Autowired DemoClmSeeder seeder;
    @Autowired TenantRepository tenants;
    @Autowired ModeleContratRepository modeles;
    @Autowired GroupeClmRepository groupes;
    @Autowired RegleConservationRepository regles;
    @Autowired TrousseRepository trousses;

    @Test
    void iq_dispose_de_modeles_publies_de_groupes_de_regles_et_de_trousses_sans_doublon_au_second_passage() {
        Long iq = tenants.findBySlug("iq").orElseThrow().getId();
        Map<String, String> statuts = demo(modeles.findByTenantIdOrderByNomAscNumeroDesc(iq));
        assertEquals(Map.of("Contrat de services professionnels", "PUBLIE", "Convention d'aide financière", "PUBLIE",
                "Entente de confidentialité", "PUBLIE", "Lettre d'offre de prêt", "PUBLIE"), statuts);
        assertTrue(groupes.findByTenantIdOrderByCodeAsc(iq).stream().map(g -> g.getCode()).toList()
                .containsAll(List.of("DIRECTION_FIN", "FINANCEMENT", "JURIDIQUE")));
        List<String> types = regles.findByTenantIdOrderByTypeContratAsc(iq).stream().map(r -> String.valueOf(r.getTypeContrat())).toList();
        assertTrue(types.containsAll(List.of("null", "SUBVENTION", "PRET", "CONFIDENTIALITE")), types.toString());
        int nbRegles = types.size();
        Map<String, String> etats = trousses.findByTenantIdOrderByMajLeDesc(iq).stream()
                .filter(t -> t.getReference().startsWith("DEMO-"))
                .collect(Collectors.toMap(Trousse::getReference, t -> t.getStatut().name()));
        assertEquals(Map.of("DEMO-AGRINORD", "EN_TRAITEMENT", "DEMO-MAURICIE", "EN_TRAITEMENT", "DEMO-LAURENTIDES", "BROUILLON"), etats,
                "deux trousses lancées (tâches en attente), une en brouillon");

        seeder.run();

        assertEquals(statuts.size(), demo(modeles.findByTenantIdOrderByNomAscNumeroDesc(iq)).size());
        assertEquals(nbRegles, regles.findByTenantIdOrderByTypeContratAsc(iq).size());
        assertEquals(3, trousses.findByTenantIdOrderByMajLeDesc(iq).stream().filter(t -> t.getReference().startsWith("DEMO-")).count());
    }

    private static Map<String, String> demo(List<ModeleContrat> liste) {
        List<String> noms = List.of("Contrat de services professionnels", "Convention d'aide financière", "Entente de confidentialité",
                "Lettre d'offre de prêt");
        return liste.stream().filter(m -> noms.contains(m.getNom()))
                .collect(Collectors.toMap(ModeleContrat::getNom, m -> m.getStatut().name()));
    }
}
