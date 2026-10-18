package qc.levis.contrat.reference.infrastructure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.reference.application.repository.ReferenceValueRepository;
import qc.levis.contrat.reference.domain.ReferenceValue;

/** Amorce les valeurs de référence GLOBALES à partir des énumérations métier. */
@Component
@Order(4)
public class ReferenceSeeder implements CommandLineRunner {

    private final ReferenceValueRepository repo;

    public ReferenceSeeder(ReferenceValueRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        seed("TYPE_CONTRAT", new String[][]{
                {"DEMANDE_PRIX", "Demande de prix"},
                {"APPEL_OFFRES_PUBLIC", "Appel d'offres public"},
                {"GRE_A_GRE", "Gré à gré"},
                {"BAIL", "Bail"},
                {"PRET_A_USAGE", "Prêt à usage"},
                {"CONTRAT_SERVICES", "Contrat de services"},
                {"AVIS_INTENTION", "Avis d'intention"},
        });
        seed("MODE_SOLLICITATION", new String[][]{
                {"DEMANDE_PRIX", "Demande de prix"},
                {"APPEL_OFFRES_PUBLIC", "Appel d'offres public"},
                {"GRE_A_GRE", "Gré à gré"},
        });
        seed("CATEGORIE_CLAUSE", new String[][]{
                {"STANDARD", "Standard"},
                {"OPTIONNELLE", "Optionnelle"},
                {"PERSONNALISEE", "Personnalisée"},
        });
        seed("TYPE_OBLIGATION", new String[][]{
                {"LIVRABLE", "Livrable"},
                {"RENOUVELLEMENT", "Renouvellement"},
                {"PAIEMENT", "Paiement"},
                {"GARANTIE", "Garantie"},
                {"AUTRE", "Autre"},
        });
    }

    private void seed(String categorie, String[][] valeurs) {
        if (repo.existsByCategorie(categorie)) return;
        int ordre = 0;
        for (String[] v : valeurs) {
            repo.save(new ReferenceValue(categorie, v[0], v[1], null, ordre++));
        }
    }
}
