package qc.levis.contrat.regle.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.regle.domain.RegleDef;

/**
 * Applique les règles configurées dans la structure JSON d'un formulaire à un jeu de réponses.
 * Les règles vivent dans {@code structure_json} (clé « regles ») — aucune règle en dur.
 */
@Service
public class RegleService {

    private final FormulaireVersionRepository versions;
    private final ObjectMapper mapper = new ObjectMapper();

    public RegleService(FormulaireVersionRepository versions) {
        this.versions = versions;
    }

    public ResultatEvaluation evaluer(Long formulaireId, Map<String, Object> valeurs) {
        var v = versions.findTopByFormulaireIdOrderByNumeroDesc(formulaireId)
                .orElseThrow(() -> new NoSuchElementException("Aucune version pour le formulaire : " + formulaireId));
        return MoteurRegles.evaluer(extraireRegles(v.getStructureJson()), valeurs == null ? Map.of() : valeurs);
    }

    /** Lit la clé « regles » de la structure JSON ; liste vide si absente ou illisible. */
    public List<RegleDef> extraireRegles(String structureJson) {
        if (structureJson == null || structureJson.isBlank()) return List.of();
        try {
            JsonNode racine = mapper.readTree(structureJson);
            JsonNode regles = racine.get("regles");
            if (regles == null || !regles.isArray()) return List.of();
            return mapper.convertValue(regles, new TypeReference<List<RegleDef>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
