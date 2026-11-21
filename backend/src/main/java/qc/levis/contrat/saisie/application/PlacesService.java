package qc.levis.contrat.saisie.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.ChampAChoix;
import qc.levis.contrat.saisie.domain.InventaireChoix;
import qc.levis.contrat.saisie.domain.PlaceChoix;
import qc.levis.contrat.saisie.domain.PlanDesPlaces;
import qc.levis.contrat.saisie.domain.Saisie;

/**
 * Places des choix d'un formulaire (CHU 3.06) : lit les champs à choix et leurs capacités dans la structure publiée, puis
 * compte les fiches qui occupent une place. Le formulaire doit appartenir à l'organisation courante.
 */
@Service
public class PlacesService {

    private final FormulaireRepository formulaires;
    private final FormulaireVersionRepository versions;
    private final SaisieRepository fiches;
    private final ObjectMapper json = new ObjectMapper();

    public PlacesService(FormulaireRepository formulaires, FormulaireVersionRepository versions, SaisieRepository fiches) {
        this.formulaires = formulaires;
        this.versions = versions;
        this.fiches = fiches;
    }

    public PlanDesPlaces plan(Long formulaireId) {
        return plan(formulaireId, null);
    }

    /** {@code sansFiche} : fiche exclue du compte, celle qu'on soumet (une resoumission ne se compte pas deux fois). */
    public PlanDesPlaces plan(Long formulaireId, Long sansFiche) {
        formulaires.findByIdAndTenantId(formulaireId, TenantContext.get())
                .orElseThrow(() -> new NoSuchElementException("Formulaire introuvable : " + formulaireId));
        String structure = versions.findTopByFormulaireIdOrderByNumeroDesc(formulaireId)
                .orElseThrow(() -> new NoSuchElementException("Formulaire sans version : " + formulaireId)).getStructureJson();
        List<Map<String, Object>> occupees = occupantes(formulaireId).stream().filter(s -> !s.getId().equals(sansFiche))
                .map(s -> reponses(s.getDonneesJson())).toList();
        return PlanDesPlaces.compter(champsAChoix(structure), occupees);
    }

    /** Inventaire des inscriptions par choix (CHU 4.08) : places de chaque choix et fiches qui les occupent. */
    public List<InventaireChoix> inventaire(Long formulaireId) {
        PlanDesPlaces plan = plan(formulaireId);
        List<Saisie> occupantes = occupantes(formulaireId);
        return plan.places().stream().map(p -> new InventaireChoix(p, occupantes.stream()
                .filter(f -> PlanDesPlaces.retenus(reponses(f.getDonneesJson()).get(p.champ())).contains(p.choix()))
                .map(f -> new InventaireChoix.Inscription(f.getNumero(), f.getStatut().name(), f.getSoumisLe(), f.getCreePar()))
                .toList())).toList();
    }

    /** Fiches du formulaire qui occupent une place. */
    public List<Saisie> occupantes(Long formulaireId) {
        return fiches.findByTenantIdAndFormulaireIdOrderByMajLeDesc(TenantContext.get(), formulaireId).stream()
                .filter(s -> s.getStatut().occupeUnePlace()).toList();
    }

    /** Champs qui portent des options ; capacités par « capacites » sur le champ ou « capacite » sur une option. */
    List<ChampAChoix> champsAChoix(String structure) {
        List<ChampAChoix> champs = new ArrayList<>();
        JsonNode racine = lire(structure);
        for (JsonNode page : racine.path("pages")) {
            for (JsonNode c : page.path("champs")) {
                if (c.path("options").isArray() && c.path("options").size() > 0) champs.add(champ(c));
            }
        }
        return champs;
    }

    private ChampAChoix champ(JsonNode c) {
        String cle = c.hasNonNull("cle") ? c.get("cle").asText() : c.path("code").asText();
        List<String> choix = new ArrayList<>();
        Map<String, Integer> capacites = new LinkedHashMap<>();
        for (JsonNode o : c.path("options")) {
            String valeur = o.isTextual() ? o.asText() : o.path(o.has("valeur") ? "valeur" : "code").asText();
            choix.add(valeur);
            if (o.has("capacite")) capacites.put(valeur, o.get("capacite").asInt());
        }
        c.path("capacites").fields().forEachRemaining(e -> capacites.put(e.getKey(), e.getValue().asInt()));
        return new ChampAChoix(cle, c.path("libelle").asText(cle), choix, capacites);
    }

    Map<String, Object> reponses(String donneesJson) {
        if (donneesJson == null || donneesJson.isBlank()) return Map.of();
        try {
            return json.readValue(donneesJson, new TypeReference<Map<String, Object>>() { });
        } catch (Exception illisible) {
            return Map.of();
        }
    }

    private JsonNode lire(String structure) {
        try {
            return structure == null ? json.createObjectNode() : json.readTree(structure);
        } catch (Exception illisible) {
            return json.createObjectNode();
        }
    }
}
