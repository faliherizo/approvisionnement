package qc.levis.contrat.saisie.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Instant;
import java.time.Year;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.formulaire.domain.Formulaire;
import qc.levis.contrat.saisie.application.dto.DemandeVue;
import qc.levis.contrat.formulaire.domain.FormulaireVersion;
import qc.levis.contrat.regle.application.MoteurRegles;
import qc.levis.contrat.regle.application.RegleService;
import qc.levis.contrat.regle.application.ResultatEvaluation;
import qc.levis.contrat.saisie.application.dto.SaisieVue;
import qc.levis.contrat.saisie.application.dto.SoumissionRequest;
import qc.levis.contrat.saisie.application.port.CaptchaVerifier;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.Saisie;
import qc.levis.contrat.saisie.domain.StatutSaisie;

/**
 * Cas d'utilisation des fiches (soumissions) : brouillon, autosave, soumission (fenêtre d'activation +
 * captcha + validation par le moteur de règles), puis validation/rejet. Tenant-scopé.
 */
@Service
public class SaisieService {

    private final SaisieRepository repo;
    private final FormulaireVersionRepository versions;
    private final FormulaireRepository formulaires;
    private final RegleService regleService;
    private final CaptchaVerifier captcha;
    private final ApplicationEventPublisher events;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    public SaisieService(SaisieRepository repo, FormulaireVersionRepository versions,
                         FormulaireRepository formulaires, RegleService regleService,
                         CaptchaVerifier captcha, ApplicationEventPublisher events) {
        this.repo = repo;
        this.versions = versions;
        this.formulaires = formulaires;
        this.regleService = regleService;
        this.captcha = captcha;
        this.events = events;
    }

    @Transactional
    public SaisieVue creer(Long formulaireId, String par) {
        int versionNum = versions.findTopByFormulaireIdOrderByNumeroDesc(formulaireId)
                .map(FormulaireVersion::getNumero).orElse(1);
        Saisie s = repo.save(new Saisie(tenant(), formulaireId, versionNum, par));
        s.setNumero(prefixeDomaine(formulaireId) + "-" + Year.now() + "-" + String.format("%06d", s.getId()));
        return vue(repo.save(s));
    }

    /** Préfixe de numéro de demande dérivé du domaine du formulaire (AP, RH, …) ; GEN par défaut. */
    private String prefixeDomaine(Long formulaireId) {
        String domaine = formulaires.findByIdAndTenantId(formulaireId, tenant())
                .map(f -> f.getDomaine()).orElse(null);
        if (domaine == null || domaine.isBlank()) return "GEN";
        if ("APPROVISIONNEMENT".equalsIgnoreCase(domaine)) return "AP";
        if ("RH".equalsIgnoreCase(domaine)) return "RH";
        return domaine.replaceAll("[^A-Za-z]", "").toUpperCase().substring(0, Math.min(3, domaine.length()));
    }

    @Transactional
    public SaisieVue autosave(Long id, String donneesJson) {
        Saisie s = exiger(id);
        if (s.getStatut() != StatutSaisie.BROUILLON)
            throw new IllegalStateException("Fiche non modifiable (déjà soumise).");
        s.setDonneesJson(donneesJson);
        s.setMajLe(Instant.now());
        return vue(repo.save(s));
    }

    @Transactional
    public SaisieVue soumettre(Long id, SoumissionRequest req) {
        return soumettre(id, req, null);
    }

    @Transactional
    public SaisieVue soumettre(Long id, SoumissionRequest req, String ip) {
        Saisie s = exiger(id);
        FormulaireVersion v = versions.findTopByFormulaireIdOrderByNumeroDesc(s.getFormulaireId())
                .orElseThrow(() -> new NoSuchElementException("Formulaire sans version."));
        Activation.Config cfg = extraireActivation(v.getStructureJson());
        long nb = repo.countByFormulaireIdAndStatutNot(s.getFormulaireId(), StatutSaisie.BROUILLON);
        Activation.raisonFermeture(cfg, Instant.now(), nb).ifPresent(r -> { throw new IllegalStateException(r); });
        if (!Activation.ipAutorisee(cfg, ip))
            throw new IllegalStateException("Soumission refusée : réseau non autorisé.");
        if (Activation.captchaRequis(cfg) && !captcha.verifier(req.captchaToken()))
            throw new IllegalArgumentException("Vérification anti-robot échouée.");
        if (req.donneesJson() != null) s.setDonneesJson(req.donneesJson());
        ResultatEvaluation res = MoteurRegles.evaluer(
                regleService.extraireRegles(v.getStructureJson()), parseMap(s.getDonneesJson()));
        if (!res.erreurs().isEmpty())
            throw new IllegalArgumentException("Validation : " + String.join(" ; ", res.erreurs()));
        s.setStatut(StatutSaisie.SOUMIS);
        s.setSoumisLe(Instant.now());
        s.setMajLe(Instant.now());
        Saisie saved = repo.save(s);
        events.publishEvent(new FicheEvent(saved.getTenantId(), saved.getFormulaireId(), saved.getId(),
                "SOUMISSION", parseMap(saved.getDonneesJson())));
        return vue(saved);
    }

    @Transactional
    public SaisieVue prendreEnCharge(Long id) { return changerStatut(id, StatutSaisie.EN_TRAITEMENT, "PRISE_EN_CHARGE"); }

    @Transactional
    public SaisieVue demanderComplement(Long id) { return changerStatut(id, StatutSaisie.A_COMPLETER, "COMPLEMENT_REQUIS"); }

    @Transactional
    public SaisieVue valider(Long id) { return changerStatut(id, StatutSaisie.VALIDE, "VALIDATION"); }

    @Transactional
    public SaisieVue rejeter(Long id) { return changerStatut(id, StatutSaisie.REJETE, "REJET"); }

    @Transactional
    public SaisieVue cloturer(Long id) { return changerStatut(id, StatutSaisie.CLOTURE, "CLOTURE"); }

    /** Annulation d'un brouillon par le demandeur. */
    @Transactional
    public SaisieVue annuler(Long id) { return changerStatut(id, StatutSaisie.ANNULE, "ANNULATION"); }

    public List<SaisieVue> lister(Long formulaireId) {
        List<SaisieVue> res = new ArrayList<>();
        for (Saisie s : repo.findByTenantIdAndFormulaireIdOrderByMajLeDesc(tenant(), formulaireId)) res.add(vue(s));
        return res;
    }

    /** Mes demandes (toutes formes confondues), enrichies du nom et du domaine du formulaire. */
    public List<DemandeVue> mesDemandes(String creePar) {
        if (creePar == null) return List.of();
        List<DemandeVue> res = new ArrayList<>();
        for (Saisie s : repo.findByTenantIdAndCreeParOrderByMajLeDesc(tenant(), creePar)) {
            Formulaire f = formulaires.findByIdAndTenantId(s.getFormulaireId(), tenant()).orElse(null);
            res.add(new DemandeVue(s.getId(), s.getNumero(), s.getFormulaireId(),
                    f != null ? f.getNom() : null, f != null ? f.getDomaine() : null,
                    s.getStatut().name(), s.getMajLe(), s.getSoumisLe()));
        }
        return res;
    }

    public SaisieVue obtenir(Long id) { return vue(exiger(id)); }

    /**
     * Structure (métadonnées JSON) de la version courante d'un formulaire, pour le rendu côté répondant.
     * Exposée via l'API des fiches (gardée par le module « saisie ») afin qu'un répondant sans rôle de
     * conception puisse afficher le formulaire sans accéder à l'API de conception.
     */
    public String structure(Long formulaireId) {
        return versions.findTopByFormulaireIdOrderByNumeroDesc(formulaireId)
                .map(FormulaireVersion::getStructureJson).orElse(null);
    }

    private SaisieVue changerStatut(Long id, StatutSaisie cible, String declencheur) {
        Saisie s = exiger(id);
        if (!s.getStatut().peutAllerVers(cible))
            throw new IllegalStateException("Transition interdite : " + s.getStatut() + " → " + cible);
        s.setStatut(cible);
        s.setMajLe(Instant.now());
        Saisie saved = repo.save(s);
        events.publishEvent(new FicheEvent(saved.getTenantId(), saved.getFormulaireId(), saved.getId(),
                declencheur, parseMap(saved.getDonneesJson())));
        return vue(saved);
    }

    Activation.Config extraireActivation(String structureJson) {
        if (structureJson == null || structureJson.isBlank()) return null;
        try {
            JsonNode n = mapper.readTree(structureJson).get("activation");
            return n == null ? null : mapper.convertValue(n, Activation.Config.class);
        } catch (Exception e) { return null; }
    }

    private Map<String, Object> parseMap(String donneesJson) {
        if (donneesJson == null || donneesJson.isBlank()) return Map.of();
        try { return mapper.readValue(donneesJson, new TypeReference<Map<String, Object>>() {}); }
        catch (Exception e) { return Map.of(); }
    }

    private Saisie exiger(Long id) {
        return repo.findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new NoSuchElementException("Fiche introuvable : " + id));
    }

    private SaisieVue vue(Saisie s) {
        return new SaisieVue(s.getId(), s.getFormulaireId(), s.getNumero(), s.getVersionNumero(),
                s.getStatut().name(), s.getDonneesJson(), s.getMajLe(), s.getSoumisLe());
    }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }
}
