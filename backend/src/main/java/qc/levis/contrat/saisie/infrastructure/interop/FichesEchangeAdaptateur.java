package qc.levis.contrat.saisie.infrastructure.interop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.interop.application.port.FichesEchange;
import qc.levis.contrat.regle.application.RegleService;
import qc.levis.contrat.saisie.application.SaisieService;
import qc.levis.contrat.saisie.application.SoumissionFiche;
import qc.levis.contrat.saisie.application.dto.SaisieVue;
import qc.levis.contrat.saisie.application.dto.SoumissionRequest;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.Saisie;
import qc.levis.contrat.saisie.domain.StatutSaisie;

/** Fiches des échanges hérités, déposées par les cas d'utilisation de l'interface (règles, fenêtre, places). */
@Component
class FichesEchangeAdaptateur implements FichesEchange {

    private final SaisieService saisies;
    private final SoumissionFiche soumission;
    private final SaisieRepository depot;
    private final RegleService regles;
    private final TransactionTemplate parFiche;
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();

    FichesEchangeAdaptateur(SaisieService saisies, SoumissionFiche soumission, SaisieRepository depot, RegleService regles,
                            PlatformTransactionManager transactions) {
        this.saisies = saisies;
        this.soumission = soumission;
        this.depot = depot;
        this.regles = regles;
        this.parFiche = new TransactionTemplate(transactions);
        this.parFiche.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public Optional<String> verifier(Long tenantId, Long formulaireId, Map<String, Object> donnees) {
        List<String> erreurs = dansTenant(tenantId, () -> regles.evaluer(formulaireId, donnees).erreurs());
        return erreurs.isEmpty() ? Optional.empty() : Optional.of("Validation : " + String.join(" ; ", erreurs));
    }

    @Override
    public FicheDeposee deposer(Long tenantId, Long formulaireId, String par, Map<String, Object> donnees) {
        String corps = ecrire(donnees);
        return dansTenant(tenantId, () -> parFiche.execute(statut -> {
            SaisieVue creee = saisies.creer(formulaireId, par);
            SaisieVue soumise = soumission.soumettre(creee.id(), new SoumissionRequest(corps, null), null);
            return new FicheDeposee(soumise.id(), soumise.numero(), soumise.statut());
        }));
    }

    @Override
    public List<FicheLue> soumises(Long tenantId, Long formulaireId, Instant depuis) {
        return depot.findByTenantIdAndFormulaireIdOrderByMajLeDesc(tenantId, formulaireId).stream()
                .filter(s -> s.getStatut() != StatutSaisie.BROUILLON && s.getSoumisLe() != null)
                .filter(s -> depuis == null || !s.getSoumisLe().isBefore(depuis))
                .map(s -> new FicheLue(s.getNumero(), s.getStatut().name(), s.getSoumisLe(), lire(s)))
                .toList();
    }

    private Map<String, Object> lire(Saisie s) {
        try {
            return s.getDonneesJson() == null ? Map.of() : json.readValue(s.getDonneesJson(), new TypeReference<Map<String, Object>>() { });
        } catch (JsonProcessingException illisible) {
            return Map.of();
        }
    }

    private String ecrire(Map<String, Object> donnees) {
        try {
            return json.writeValueAsString(donnees);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Données de fiche non sérialisables.", e);
        }
    }

    /** Le cas d'utilisation lit l'organisation dans le contexte : on la pose, puis on rétablit celle de l'appelant. */
    private static <T> T dansTenant(Long tenantId, Supplier<T> action) {
        Long avant = TenantContext.get();
        TenantContext.set(tenantId);
        try {
            return action.get();
        } finally {
            if (avant == null) {
                TenantContext.clear();
            } else {
                TenantContext.set(avant);
            }
        }
    }
}
