package qc.levis.contrat.formulaire.infrastructure.lecture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.rapport.application.port.LectureFormulaires;

/** Description d'un formulaire de l'organisation pour les rapports : nom et libellés tirés de la dernière version. */
@Component
class LectureFormulairesJpa implements LectureFormulaires {

    private final FormulaireRepository formulaires;
    private final FormulaireVersionRepository versions;
    private final ObjectMapper json = new ObjectMapper();

    LectureFormulairesJpa(FormulaireRepository formulaires, FormulaireVersionRepository versions) {
        this.formulaires = formulaires;
        this.versions = versions;
    }

    @Override
    public Optional<Description> decrire(Long tenantId, Long formulaireId) {
        return formulaires.findByIdAndTenantId(formulaireId, tenantId).map(f -> new Description(f.getNom(),
                libelles(versions.findTopByFormulaireIdOrderByNumeroDesc(formulaireId).map(v -> v.getStructureJson()).orElse(null))));
    }

    Map<String, String> libelles(String structure) {
        Map<String, String> libelles = new LinkedHashMap<>();
        try {
            for (JsonNode page : json.readTree(structure == null ? "{}" : structure).path("pages")) {
                for (JsonNode c : page.path("champs")) {
                    String cle = c.hasNonNull("cle") ? c.get("cle").asText() : c.path("code").asText();
                    libelles.put(cle, c.path("libelle").asText(cle));
                }
            }
        } catch (Exception illisible) {
            return Map.of();
        }
        return libelles;
    }
}
