package qc.levis.contrat.clm.infrastructure;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.PortSalesforce;

/**
 * Salesforce Grantmaking simulé : demandes de financement de démonstration et journal des statuts reçus, pour les
 * organisations en mode SIMULATION (interdit en production). Permet de dérouler ST-06/ST-12 de bout en bout sans
 * sandbox, et de simuler une panne pour éprouver les reprises.
 */
@Component
public class SalesforceSimulation implements PortSalesforce {

    private final Map<String, Map<String, Object>> dossiers = new ConcurrentHashMap<>();
    private final List<Statut> recus = Collections.synchronizedList(new ArrayList<>());
    private final Clock horloge;
    private volatile boolean enPanne;

    public SalesforceSimulation(Clock horloge) {
        this.horloge = horloge;
        ajouter("a0B5e00000GRT001", "DEM-2026-0001", "Coopérative Agri-Nord", "Programme Essor", "250000.00", "Coopérative Agri-Nord inc.",
                "direction@agrinord.example", "+15145550101");
        ajouter("a0B5e00000GRT002", "DEM-2026-0002", "Techno Mauricie", "Programme Innovation", "1200000.00", "Techno Mauricie s.e.c.",
                "finance@technomauricie.example", "+18195550102");
        ajouter("a0B5e00000GRT003", "DEM-2026-0003", "Bois Laurentides", "Programme Relance", "75000.00", null,
                "contact@boislaurentides.example", null);
    }

    private void ajouter(String id, String numero, String demandeur, String programme, String montant, String entite,
                         String courriel, String telephone) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("Id", id);
        m.put("Name", numero);
        m.put("Demandeur", demandeur);
        m.put("Programme", programme);
        m.put("Montant", montant);
        m.put("Entite", entite);
        m.put("CourrielDemandeur", courriel);
        m.put("TelephoneDemandeur", telephone);
        m.put("Responsable", "charge.projet@iq.example");
        m.put("DateDebut", "2026-12-01");
        dossiers.put(id, m);
    }

    @Override
    public Optional<Dossier> lireDossier(String identifiantOuNumero) {
        exigerDisponible();
        return dossiers.values().stream()
                .filter(m -> identifiantOuNumero.equalsIgnoreCase(String.valueOf(m.get("Id"))) || identifiantOuNumero.equalsIgnoreCase(String.valueOf(m.get("Name"))))
                .findFirst()
                .map(m -> new Dossier(String.valueOf(m.get("Id")), String.valueOf(m.get("Name")), new LinkedHashMap<>(m),
                        Instant.now(horloge).truncatedTo(ChronoUnit.MILLIS)));
    }

    @Override
    public void publierStatut(Statut statut) {
        exigerDisponible();
        if (!dossiers.containsKey(statut.sfDossierId())) {
            throw new IllegalArgumentException("Dossier Salesforce inconnu : " + statut.sfDossierId());
        }
        if (recus.stream().noneMatch(s -> s.cleIdempotence().equals(statut.cleIdempotence()))) {
            recus.add(statut);
            dossiers.get(statut.sfDossierId()).put("StatutCLM", statut.evenement());
        }
    }

    @Override
    public EtatConnexion tester() {
        return new EtatConnexion(!enPanne, "simulation", enPanne ? "Panne simulée" : dossiers.size() + " dossier(s) de démonstration");
    }

    public void simulerPanne(boolean panne) {
        this.enPanne = panne;
    }

    public List<Statut> statutsRecus() {
        return List.copyOf(recus);
    }

    private void exigerDisponible() {
        if (enPanne) {
            throw new Indisponible("Salesforce indisponible (panne simulée).");
        }
    }
}
