package qc.levis.contrat.clm;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.Repository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/**
 * Règles d'architecture du module CLM (ST-02, ADR-006, ADR-008) vérifiées à chaque construction plutôt qu'en revue :
 * dépendances vers le domaine, domaine sans cadriciel, module isolé des autres, API protégées côté serveur et
 * requêtes de persistance bornées au tenant.
 */
class ArchitectureClmTest {

    private static final String CLM = "qc.levis.contrat.clm..";
    private static final String DOMAINE = "qc.levis.contrat.clm.domain..";
    private static final String APPLICATION = "qc.levis.contrat.clm.application..";
    private static final String INFRASTRUCTURE = "qc.levis.contrat.clm.infrastructure..";
    private static final String DEPOTS = "qc.levis.contrat.clm.application.repository";

    /** Seules requêtes non bornées au tenant, chacune motivée ; toute nouvelle exception doit être ajoutée ici et justifiée. */
    private static final Map<String, String> HORS_TENANT = Map.ofEntries(
            Map.entry("InvitationRepository.findByJetonHash",
                    "le jeton est la capacité du portail public ; le tenant se lit ensuite sur l'invitation trouvée"),
            Map.entry("NegociationRepository.findByJetonHash",
                    "le jeton est la capacité de la contrepartie au portail (RST-55) ; le tenant se lit ensuite sur la négociation trouvée"),
            Map.entry("MessageSortantRepository.existsByCleIdempotence", "la clé d'idempotence contient déjà le tenant"),
            Map.entry("MessageSortantRepository.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc",
                    "relais planifié tous tenants ; chaque message est ensuite publié dans son propre tenant"),
            Map.entry("EtapeTrousseRepository.findByStatut",
                    "rappels planifiés tous tenants ; chaque étape est ensuite traitée dans son propre tenant"),
            Map.entry("ClientIntegrationRepository.findByClientId",
                    "identifiant client unique sur la plateforme : le tenant se lit sur le client authentifié (API d'intégration)"),
            Map.entry("RappelSortantRepository.existsByCleIdempotence", "la clé d'idempotence contient déjà le tenant"),
            Map.entry("RappelSortantRepository.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc",
                    "relais planifié tous tenants ; chaque rappel est ensuite livré dans son propre tenant"),
            Map.entry("DepotM365Repository.existsByCleIdempotence", "la clé d'idempotence contient déjà le tenant"),
            Map.entry("DepotM365Repository.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc",
                    "relais planifié tous tenants ; chaque dépôt Microsoft 365 est ensuite livré dans son propre tenant"));

    private static final JavaClasses CODE = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("qc.levis.contrat");

    @Test
    void les_dependances_pointent_vers_le_domaine() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Domaine").definedBy(DOMAINE)
                .layer("Application").definedBy(APPLICATION)
                .layer("Infrastructure").definedBy(INFRASTRUCTURE)
                .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
                .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure")
                .whereLayer("Domaine").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
                .check(CODE);
    }

    @Test
    void le_domaine_ne_depend_que_du_jdk_et_de_jpa() {
        classes().that().resideInAPackage(DOMAINE)
                .should().onlyDependOnClassesThat().resideInAnyPackage(DOMAINE, "java..", "jakarta.persistence..")
                .because("le domaine ne dépend d'aucun cadriciel ni d'aucun autre module (JPA : concession n° 1 d'ADR-006)")
                .check(CODE);
    }

    @Test
    void l_application_ignore_le_transport_et_toute_infrastructure() {
        noClasses().that().resideInAPackage(APPLICATION)
                .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "org.springframework.web..",
                        "org.springframework.http..", "jakarta.servlet..", "java.net.http..")
                .because("les cas d'utilisation passent par des ports ; le transport et les adaptateurs restent à l'extérieur")
                .check(CODE);
    }

    @Test
    void le_module_ne_lit_ni_les_entites_ni_les_depots_d_un_autre_module() {
        noClasses().that().resideInAPackage(CLM)
                .should().dependOnClassesThat(entiteOuDepotDUnAutreModule())
                .because("un module en lit un autre par un service ou un port, jamais par ses tables (ADR-006)")
                .check(CODE);
    }

    @Test
    void aucun_autre_module_ne_depend_du_clm() {
        noClasses().that().resideInAPackage("qc.levis.contrat..").and().resideOutsideOfPackage(CLM)
                .should().dependOnClassesThat().resideInAPackage(CLM)
                .because("le module s'active et se retire par tenant sans effet sur le reste de la plateforme")
                .check(CODE);
    }

    @Test
    void chaque_api_hors_portail_public_exige_une_autorisation_serveur() {
        classes().that().resideInAPackage(INFRASTRUCTURE).and().areAnnotatedWith(RestController.class)
                .and().doNotHaveSimpleName("PortailController")
                // Portail de négociation (RST-55) : même capacité par jeton ; l'option est vérifiée pour le tenant de la négociation.
                .and().doNotHaveSimpleName("PortailNegociationController")
                // API d'intégration : jeton client credentials vérifié par le contrôleur, module contrôlé pour le tenant du client.
                .and().doNotHaveSimpleName("IntegrationEntranteController")
                // Connecteur MCP (ADR-023) : même jeton client credentials, vérifié par le contrôleur.
                .and().doNotHaveSimpleName("McpController")
                .should().beAnnotatedWith(PreAuthorize.class)
                .because("refus par défaut : masquer un bouton dans l'interface n'est pas un contrôle d'accès (ST-02)")
                .check(CODE);
    }

    @Test
    void les_ports_sont_des_interfaces_realisees_en_infrastructure() {
        classes().that().resideInAPackage("qc.levis.contrat.clm.application.port..").and().areTopLevelClasses()
                .should().beInterfaces()
                .check(CODE);
        classes().that().implement(JavaClass.Predicates.resideInAPackage("qc.levis.contrat.clm.application.port.."))
                .and().areNotInterfaces()
                .should().resideInAPackage(INFRASTRUCTURE)
                .check(CODE);
    }

    @Test
    void entites_depots_et_controleurs_sont_a_leur_place() {
        classes().that().resideInAPackage(CLM).and().areAnnotatedWith(Entity.class)
                .should().resideInAPackage(DOMAINE).check(CODE);
        classes().that().resideInAPackage(CLM).and().areAssignableTo(Repository.class)
                .should().resideInAPackage(DEPOTS).check(CODE);
        classes().that().resideInAPackage(CLM).and().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("qc.levis.contrat.clm.infrastructure.web").check(CODE);
        noClasses().that().resideInAPackage("qc.levis.contrat.clm.infrastructure.web")
                .should().dependOnClassesThat().resideInAPackage(DEPOTS)
                .because("un contrôleur passe par un cas d'utilisation, jamais directement par la persistance")
                .check(CODE);
    }

    @Test
    void chaque_requete_d_un_depot_est_bornee_au_tenant() {
        methods().that().areDeclaredInClassesThat().resideInAPackage(DEPOTS)
                .and().haveNameMatching("(find|count|exists|delete|remove).*")
                .should(etreBorneeAuTenant())
                .check(CODE);
    }

    @Test
    void chaque_exception_hors_tenant_designe_une_requete_existante() {
        Set<String> declarees = CODE.stream().filter(c -> c.getPackageName().equals(DEPOTS))
                .flatMap(c -> c.getMethods().stream().map(m -> c.getSimpleName() + "." + m.getName()))
                .collect(Collectors.toSet());
        assertTrue(declarees.containsAll(HORS_TENANT.keySet()), "exception périmée : " + HORS_TENANT.keySet());
    }

    private static DescribedPredicate<JavaClass> entiteOuDepotDUnAutreModule() {
        return new DescribedPredicate<>("une entité ou un dépôt d'un autre module") {
            @Override
            public boolean test(JavaClass c) {
                String paquet = c.getPackageName();
                return paquet.startsWith("qc.levis.contrat.") && !paquet.startsWith("qc.levis.contrat.clm")
                        && (c.isAnnotatedWith(Entity.class) || c.isAssignableTo(Repository.class));
            }
        };
    }

    private static ArchCondition<JavaMethod> etreBorneeAuTenant() {
        return new ArchCondition<>("porter le tenant ou figurer parmi les exceptions motivées") {
            @Override
            public void check(JavaMethod m, ConditionEvents events) {
                String nom = m.getOwner().getSimpleName() + "." + m.getName();
                boolean bornee = m.getName().contains("TenantId") || HORS_TENANT.containsKey(nom);
                events.add(new SimpleConditionEvent(m, bornee, nom + (bornee ? " est bornée" : " n'est pas bornée au tenant")));
            }
        };
    }
}
