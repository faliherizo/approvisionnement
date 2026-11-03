package qc.levis.contrat.commun.module.infrastructure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.module.application.repository.ModuleCatalogueRepository;
import qc.levis.contrat.commun.module.domain.ModuleCatalogue;

/**
 * Amorce le catalogue des modules de la plateforme de formulaires (CHU). Tous sont
 * {@code actifParDefaut = false} : ils doivent être activés explicitement par le superadmin par tenant.
 * Les dépendances pilotent la résolution (activer « saisie » tire « formulaire », etc.).
 */
@Component
@Order(5)
public class ModuleSeeder implements CommandLineRunner {

    private final ModuleCatalogueRepository repo;

    public ModuleSeeder(ModuleCatalogueRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        // code, nom, famille, ordre, requiert
        seed("formulaire", "Moteur de formulaires", "Plateforme", 10, null);
        seed("regle", "Règles & automatisation", "Plateforme", 20, "formulaire");
        seed("saisie", "Soumissions & workflow", "Plateforme", 30, "formulaire");
        seed("processus", "Processus & workflows (tâches, approbations)", "Plateforme", 35, "saisie");
        seed("notification", "Notifications (courriel/SMS/interne)", "Plateforme", 40, "saisie");
        seed("rapport", "Rapports, exports & tableaux de bord", "Plateforme", 50, "saisie");
        seed("interop", "API publique & interopérabilité", "Plateforme", 60, "saisie");
        seed("document", "Documents & pièces jointes de demande", "Plateforme", 65, "saisie");
        seed("application", "Applications & espaces fonctionnels", "Plateforme", 15, "formulaire");
        seed("securite_avancee", "Sécurité avancée (RBAC fin, classification)", "Sécurité", 70, null);
        seed("rh", "Dossiers employés (RH)", "RH", 75, null);
        seed("paie", "Paie & retenues Revenu Québec (RRQ, AE, RQAP, impôt)", "RH", 80, null);
        seed("clm", "Contrats & signature électronique (trousses, preuve, Salesforce)", "CLM", 90, null);
        seed("clm_ia", "Option CLM/IA (analyse de contrats, tarifée à part)", "CLM", 95, "clm");
    }

    private void seed(String code, String nom, String famille, int ordre, String requiert) {
        if (repo.existsByCode(code)) return;
        repo.save(new ModuleCatalogue(code, nom, famille, ordre, false, requiert));
    }
}
