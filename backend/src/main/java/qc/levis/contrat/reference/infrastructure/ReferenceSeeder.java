package qc.levis.contrat.reference.infrastructure;

import java.util.Arrays;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.reference.application.ReferenceService;
import qc.levis.contrat.reference.application.dto.DeclarationCategorie;

/** Déclare les listes du socle de rédaction et leurs valeurs de plateforme, issues des énumérations métier. */
@Component
@Order(4)
public class ReferenceSeeder implements CommandLineRunner {

    private static final String MODULE = "redaction";

    private final ReferenceService references;

    public ReferenceSeeder(ReferenceService references) {
        this.references = references;
    }

    @Override
    public void run(String... args) {
        seed("TYPE_CONTRAT", "Types de contrat", 10, new String[][]{
                {"DEMANDE_PRIX", "Demande de prix"},
                {"APPEL_OFFRES_PUBLIC", "Appel d'offres public"},
                {"GRE_A_GRE", "Gré à gré"},
                {"BAIL", "Bail"},
                {"PRET_A_USAGE", "Prêt à usage"},
                {"CONTRAT_SERVICES", "Contrat de services"},
                {"AVIS_INTENTION", "Avis d'intention"},
        });
        seed("MODE_SOLLICITATION", "Modes de sollicitation", 20, new String[][]{
                {"DEMANDE_PRIX", "Demande de prix"},
                {"APPEL_OFFRES_PUBLIC", "Appel d'offres public"},
                {"GRE_A_GRE", "Gré à gré"},
        });
        seed("CATEGORIE_CLAUSE", "Catégories de clause", 30, new String[][]{
                {"STANDARD", "Standard"},
                {"OPTIONNELLE", "Optionnelle"},
                {"PERSONNALISEE", "Personnalisée"},
        });
        seed("TYPE_OBLIGATION", "Types d'obligation", 40, new String[][]{
                {"LIVRABLE", "Livrable"},
                {"RENOUVELLEMENT", "Renouvellement"},
                {"PAIEMENT", "Paiement"},
                {"GARANTIE", "Garantie"},
                {"AUTRE", "Autre"},
        });
    }

    private void seed(String categorie, String libelle, int ordre, String[][] valeurs) {
        references.declarer(new DeclarationCategorie(categorie, MODULE, libelle, null, null, ordre,
                Arrays.stream(valeurs).map(v -> new DeclarationCategorie.Valeur(v[0], v[1], null)).toList()));
    }
}
