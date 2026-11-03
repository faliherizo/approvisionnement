package qc.levis.contrat.temps.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.temps.application.dto.FeuilleTempsRequest;
import qc.levis.contrat.temps.application.dto.FeuilleTempsVue;
import qc.levis.contrat.temps.application.repository.FeuilleTempsRepository;
import qc.levis.contrat.temps.domain.FeuilleTemps;

/** Feuilles de temps (RH). Tenant-scopé ; chaque utilisateur gère ses propres feuilles. */
@Service
public class FeuilleTempsService {

    private final FeuilleTempsRepository repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public FeuilleTempsService(FeuilleTempsRepository repo) {
        this.repo = repo;
    }

    public List<FeuilleTempsVue> mesFeuilles(String proprietaire) {
        List<FeuilleTempsVue> res = new ArrayList<>();
        if (proprietaire != null) {
            for (FeuilleTemps f : repo.findByTenantIdAndProprietaireOrderByMajLeDesc(tenant(), proprietaire)) res.add(vue(f));
        }
        return res;
    }

    /** Feuilles soumises (à approuver par un gestionnaire). */
    public List<FeuilleTempsVue> aApprouver() {
        List<FeuilleTempsVue> res = new ArrayList<>();
        for (FeuilleTemps f : repo.findByTenantIdAndStatutOrderByMajLeDesc(tenant(), "SOUMISE")) res.add(vue(f));
        return res;
    }

    @Transactional
    public FeuilleTempsVue creer(FeuilleTempsRequest req, String proprietaire) {
        FeuilleTemps f = new FeuilleTemps(tenant(), proprietaire, null, req.periodeDebut(), req.periodeFin());
        appliquerLignes(f, req.lignes());
        return vue(repo.save(f));
    }

    @Transactional
    public FeuilleTempsVue majLignes(Long id, FeuilleTempsRequest req) {
        FeuilleTemps f = exiger(id);
        if (!"BROUILLON".equals(f.getStatut()) && !"REFUSEE".equals(f.getStatut()))
            throw new IllegalStateException("Feuille non modifiable (déjà soumise ou approuvée).");
        if (req.periodeDebut() != null) f.setPeriodeDebut(req.periodeDebut());
        if (req.periodeFin() != null) f.setPeriodeFin(req.periodeFin());
        appliquerLignes(f, req.lignes());
        return vue(repo.save(f));
    }

    @Transactional
    public FeuilleTempsVue soumettre(Long id) { return transition(id, "SOUMISE", "BROUILLON", "REFUSEE"); }

    @Transactional
    public FeuilleTempsVue approuver(Long id, String approbateur) {
        FeuilleTemps f = transitionEntite(id, "APPROUVEE", "SOUMISE");
        f.setApprobateur(approbateur);
        return vue(repo.save(f));
    }

    @Transactional
    public FeuilleTempsVue refuser(Long id, String approbateur, String motif) {
        FeuilleTemps f = transitionEntite(id, "REFUSEE", "SOUMISE");
        f.setApprobateur(approbateur);
        f.setCommentaire(motif);
        return vue(repo.save(f));
    }

    public FeuilleTempsVue obtenir(Long id) { return vue(exiger(id)); }

    // --- interne ---
    private void appliquerLignes(FeuilleTemps f, List<Map<String, Object>> lignes) {
        List<Map<String, Object>> l = lignes == null ? List.of() : lignes;
        double total = 0;
        for (Map<String, Object> ligne : l) {
            Object h = ligne.get("heures");
            if (h instanceof Number n) total += n.doubleValue();
        }
        try { f.setLignesJson(mapper.writeValueAsString(l)); } catch (Exception e) { f.setLignesJson("[]"); }
        f.setTotalHeures(Math.round(total * 100.0) / 100.0);
        f.setMajLe(Instant.now());
    }

    private FeuilleTempsVue transition(Long id, String cible, String... depuis) {
        return vue(repo.save(transitionEntite(id, cible, depuis)));
    }

    private FeuilleTemps transitionEntite(Long id, String cible, String... depuis) {
        FeuilleTemps f = exiger(id);
        if (java.util.Arrays.stream(depuis).noneMatch(s -> s.equals(f.getStatut())))
            throw new IllegalStateException("Transition interdite depuis " + f.getStatut() + " vers " + cible);
        f.setStatut(cible);
        f.setMajLe(Instant.now());
        return f;
    }

    private FeuilleTemps exiger(Long id) {
        return repo.findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new NoSuchElementException("Feuille de temps introuvable : " + id));
    }

    private FeuilleTempsVue vue(FeuilleTemps f) {
        return new FeuilleTempsVue(f.getId(), f.getProprietaire(), f.getEmployeId(), f.getPeriodeDebut(),
                f.getPeriodeFin(), f.getStatut(), f.getLignesJson(), f.getTotalHeures(), f.getApprobateur(),
                f.getCommentaire(), f.getMajLe());
    }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }
}
