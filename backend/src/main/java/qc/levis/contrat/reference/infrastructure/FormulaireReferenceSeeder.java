package qc.levis.contrat.reference.infrastructure;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import qc.levis.contrat.reference.application.repository.ReferenceValueRepository;
import qc.levis.contrat.reference.domain.ReferenceValue;

/**
 * Amorce les données de référence du moteur de formulaires : TYPES DE CHAMPS et NIVEAUX DE
 * CLASSIFICATION. Ce sont des données de référence (surchargables par tenant) — aucun type n'est figé
 * dans le code : en ajouter un revient à insérer une ligne de référence, sans migration.
 */
@Component
@Order(6)
public class FormulaireReferenceSeeder implements CommandLineRunner {

    private final ReferenceValueRepository repo;

    public FormulaireReferenceSeeder(ReferenceValueRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        seed("TYPE_CHAMP", new String[][]{
                {"texte", "Texte"},
                {"zone_texte", "Zone de texte"},
                {"case_a_cocher", "Case à cocher"},
                {"radio", "Bouton radio"},
                {"liste", "Liste déroulante"},
                {"date", "Date"},
                {"courriel", "Adresse courriel"},
                {"hyperlien", "Hyperlien"},
                {"nombre", "Nombre"},
                {"decimale", "Décimale"},
                {"montant", "Montant"},
                {"pourcentage", "Pourcentage"},
                {"fichier", "Fichier téléversé"},
                {"matrice", "Matrice"},
                {"calcule", "Valeur calculée"},
                {"dynamique", "Valeur dynamique"},
        });
        seed("CLASSIFICATION", new String[][]{
                {"public", "Public"},
                {"interne", "Interne"},
                {"confidentiel", "Confidentiel"},
                {"secret", "Secret"},
        });
        // Domaines métier : cloisonnement RH / Approvisionnement (maquettes §4.2, architecture §9.2).
        // Donnée de référence surchargeable par tenant — aucun domaine figé dans le code.
        seed("DOMAINE", new String[][]{
                {"RH", "Ressources humaines"},
                {"APPROVISIONNEMENT", "Approvisionnements"},
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
