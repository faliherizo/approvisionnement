package qc.levis.contrat.redaction.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.dto.ClauseRequest;
import qc.levis.contrat.redaction.application.dto.ConditionRequest;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.ClauseCondition;
import qc.levis.contrat.tenant.application.dto.PageResponse;

/**
 * Gestion de la bibliothèque de clauses (SRP) : recherche paginée, consultation,
 * création, mise à jour et suppression logique. Les conditions d'éligibilité,
 * définies par l'admin, sont ici traduites en {@link ClauseCondition} évaluées
 * ensuite par le {@link RuleEngine}.
 */
@Service
public class ClauseService {

    private final ClauseRepository clauses;

    public ClauseService(ClauseRepository clauses) {
        this.clauses = clauses;
    }

    /** Recherche paginée sur le périmètre du tenant courant. */
    public PageResponse<Clause> lister(int page, int taille, String q) {
        Page<Clause> resultat = clauses.rechercher(TenantContext.get(), q == null ? "" : q,
                PageRequest.of(page, taille, Sort.by("code")));
        return new PageResponse<>(resultat.getContent(),
                resultat.getTotalElements(), page, taille);
    }

    public Clause get(Long id) {
        return clauses.findById(id).orElseThrow();
    }

    @Transactional
    public Clause creer(ClauseRequest req) {
        Clause c = new Clause();
        c.setCode(req.code());
        c.setTenantId(TenantContext.get());
        appliquer(c, req);
        return clauses.save(c);
    }

    @Transactional
    public Clause modifier(Long id, ClauseRequest req) {
        Clause c = get(id);
        appliquer(c, req);
        c.setVersion(c.getVersion() + 1);
        c.setMajLe(Instant.now());
        return clauses.save(c);
    }

    /** Suppression logique (préserve les documents assemblés référençant la clause). */
    @Transactional
    public void supprimer(Long id) {
        Clause c = get(id);
        c.setActif(false);
        c.setMajLe(Instant.now());
        clauses.save(c);
    }

    /** Copie les champs de la requête vers l'entité, conditions comprises. */
    private void appliquer(Clause c, ClauseRequest req) {
        c.setTitre(req.titre());
        c.setCategorie(req.categorie());
        c.setCorps(req.corps());
        c.setNumero(req.numero());
        c.setSection(req.section());
        c.setNormative(req.normative());
        if (req.dependances() != null) c.setDependances(new ArrayList<>(req.dependances()));
        if (req.exclusions() != null) c.setExclusions(new ArrayList<>(req.exclusions()));
        remplacerConditions(c, req.conditions());
    }

    /** Remplace les conditions (orphanRemoval supprime les anciennes). */
    private void remplacerConditions(Clause c, List<ConditionRequest> demandees) {
        c.getConditions().clear();
        if (demandees == null) return;
        for (ConditionRequest d : demandees) {
            ClauseCondition condition = new ClauseCondition();
            condition.setAttribut(d.attribut());
            condition.setValeur(d.valeur());
            c.addCondition(condition);
        }
    }
}
