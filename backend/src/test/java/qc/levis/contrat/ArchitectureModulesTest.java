package qc.levis.contrat;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static java.util.Map.entry;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;

/**
 * Isolation fonctionnelle entre modules, vérifiée à chaque construction : toute API relève d'un module activable par tenant,
 * ou figure parmi les fonctions transverses motivées ci-dessous. Une organisation n'atteint ainsi aucune fonctionnalité d'un
 * module qu'elle n'a pas souscrit, même en appelant l'API directement.
 */
class ArchitectureModulesTest {

    private static final String GARDE = "@modules.actif(";

    /** Contrôleurs transverses, chacun motivé ; toute nouvelle exception doit être ajoutée ici et justifiée. */
    private static final Map<String, String> TRANSVERSES = Map.ofEntries(
            entry("AuthController", "session, connexion et second facteur de tout compte"),
            entry("OidcController", "connexion fédérée OIDC du tenant"),
            entry("SamlController", "connexion fédérée SAML du tenant"),
            entry("ProfileController", "profil personnel de tout compte"),
            entry("AdminUserController", "comptes et rôles du tenant"),
            entry("AuditController", "journal d'audit de sécurité du tenant (connexions, actions)"),
            entry("IntegrationOidcController", "authentification unique OIDC du tenant"),
            entry("IntegrationSamlController", "authentification unique SAML du tenant"),
            entry("ModuleController", "modules souscrits par le tenant, lus par la navigation"),
            entry("PublicTenantController", "identité publique du tenant, page de connexion"),
            entry("HealthController", "sonde d'exploitation"),
            entry("VersionController", "version déployée"),
            entry("TenantController", "console superadmin, hors tenant"),
            entry("SuperadminModuleController", "console superadmin, hors tenant"),
            entry("SuperadminReferenceController", "console superadmin, hors tenant"),
            entry("ConsoleSoutienClm", "console superadmin, hors tenant : demande d'accès de soutien ; le diagnostic exige l'accord de l'organisation (ADR-031)"),
            entry("ReferenceController", "listes de référence de tous les modules : le service refuse celle d'un module non souscrit"),
            entry("AdminReferenceController", "listes de référence de tous les modules : le service refuse celle d'un module non souscrit"),
            entry("EchangesHeritesPublicController", "échanges hérités sur l'API publique : le tenant n'est connu qu'après le jeton ; le contrôleur vérifie alors le module"),
            entry("McpController", "connecteur MCP : jeton d'un client d'intégration ; le tenant n'est connu qu'après le jeton, puis le module est vérifié"),
            entry("InteropPublicController", "jeton d'API : le tenant n'est connu qu'après le jeton ; le contrôleur vérifie alors le module"),
            entry("PolitiqueSessionController", "politique de session (inactivité, durée maximale) de toute organisation"),
            entry("ConnexionLocaleController", "politique de connexion locale et comptes de secours du tenant"),
            entry("AnnuaireController", "correspondances d'annuaire et jeton SCIM : comptes et rôles du tenant"),
            entry("ScimController", "provisionnement SCIM 2.0 : le tenant n'est connu qu'après le jeton porteur, propre au tenant"),
            entry("PortailController", "portail public du signataire : le tenant n'est connu qu'à la lecture du jeton d'invitation"),
            entry("DroitsVieePriveeController", "droits Loi 25 de toute personne : obligation légale de toute organisation, quels que soient ses modules"),
            entry("BanqueHeuresController", "banque d'heures de services professionnels : contrat de service avec le fournisseur, hors modules"),
            entry("SuperadminBanqueHeuresController", "console superadmin, hors tenant"),
            entry("VitrineController", "site vitrine public de l'organisation (avant connexion) et son édition par la console superadmin, hors tenant"),
            entry("MediaController", "médiathèque publique : vidéos d'aide et de présentation, communes à toutes les organisations"),
            entry("IntegrationEntranteController", "jeton d'un client d'intégration (Workday, Salesforce) : le tenant n'est connu qu'après le jeton ; le contrôleur vérifie alors le module"));

    private static final JavaClasses CODE = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("qc.levis.contrat");

    @Test
    void chaque_api_releve_d_un_module_activable_ou_d_une_fonction_transverse_motivee() {
        classes().that().areAnnotatedWith(RestController.class)
                .should(releverDUnModuleOuEtreTransverse())
                .because("refus par défaut : un module non souscrit ne doit être atteignable ni par l'interface ni par l'API")
                .check(CODE);
    }

    @Test
    void une_annotation_de_methode_repete_la_garde_de_module_qu_elle_remplace() {
        methods().that().areDeclaredInClassesThat(gardeesParAnnotationDeClasse()).and().areAnnotatedWith(PreAuthorize.class)
                .should(repeterLaGarde())
                .because("une annotation @PreAuthorize de méthode remplace celle de la classe")
                .check(CODE);
    }

    @Test
    void chaque_exception_transverse_designe_un_controleur_existant() {
        Set<String> controleurs = CODE.stream().filter(c -> c.isAnnotatedWith(RestController.class))
                .map(JavaClass::getSimpleName).collect(Collectors.toSet());
        assertTrue(controleurs.containsAll(TRANSVERSES.keySet()), "exception périmée parmi " + TRANSVERSES.keySet());
    }

    private static String preAutorisation(JavaClass c) {
        return c.tryGetAnnotationOfType(PreAuthorize.class).map(PreAuthorize::value).orElse("");
    }

    private static ArchCondition<JavaClass> releverDUnModuleOuEtreTransverse() {
        return new ArchCondition<>("relever d'un module (@ModuleRequis ou @modules.actif) ou figurer parmi les transverses motivées") {
            @Override
            public void check(JavaClass c, ConditionEvents events) {
                boolean ok = c.isAnnotatedWith(ModuleRequis.class) || preAutorisation(c).contains(GARDE)
                        || TRANSVERSES.containsKey(c.getSimpleName());
                events.add(new SimpleConditionEvent(c, ok, c.getName() + (ok ? " relève d'un module" : " ne relève d'aucun module")));
            }
        };
    }

    private static DescribedPredicate<JavaClass> gardeesParAnnotationDeClasse() {
        return new DescribedPredicate<>("gardées par @modules.actif au niveau de la classe") {
            @Override
            public boolean test(JavaClass c) {
                return preAutorisation(c).contains(GARDE);
            }
        };
    }

    private static ArchCondition<JavaMethod> repeterLaGarde() {
        return new ArchCondition<>("répéter @modules.actif") {
            @Override
            public void check(JavaMethod m, ConditionEvents events) {
                boolean ok = m.tryGetAnnotationOfType(PreAuthorize.class).map(PreAuthorize::value).orElse("").contains(GARDE);
                events.add(new SimpleConditionEvent(m, ok, m.getFullName() + (ok ? " répète" : " ne répète pas") + " la garde de module"));
            }
        };
    }
}
