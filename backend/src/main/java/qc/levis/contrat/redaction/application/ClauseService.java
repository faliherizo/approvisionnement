package qc.levis.contrat.redaction.application;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.dto.ClauseRequest;
import qc.levis.contrat.redaction.application.dto.ConditionRequest;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.ClauseVersionRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.ClauseCondition;
import qc.levis.contrat.redaction.domain.ClauseVersion;
import qc.levis.contrat.redaction.domain.ClauseVersion.Motif;
import qc.levis.contrat.tenant.application.dto.PageResponse;

/**
 * Gestion de la bibliothèque de clauses (SRP) : recherche paginée, consultation,
 * création, mise à jour et suppression logique. Les conditions d'éligibilité,
 * définies par l'admin, sont ici traduites en {@link ClauseCondition} évaluées
 * ensuite par le {@link RuleEngine}. Chaque création, modification ou restauration fige une {@link ClauseVersion}
 * consultable (Lévis EF1, C-03). Une clause n'est accessible que dans son organisation.
 */
@Service
public class ClauseService {

    static final String AUTEUR_AMORCAGE = "système (bibliothèque de référence)";

    private final ClauseRepository clauses;
    private final ClauseVersionRepository versions;
    private final Clock horloge;

    public ClauseService(ClauseRepository clauses, ClauseVersionRepository versions, Clock horloge) {
        this.clauses = clauses;
        this.versions = versions;
        this.horloge = horloge;
    }

    /** Recherche paginée sur le périmètre du tenant courant. */
    public PageResponse<Clause> lister(int page, int taille, String q) {
        Page<Clause> resultat = clauses.rechercher(TenantContext.get(), q == null ? "" : q,
                PageRequest.of(page, taille, Sort.by("code")));
        return new PageResponse<>(resultat.getContent(),
                resultat.getTotalElements(), page, taille);
    }

    /** Une clause d'une autre organisation est traitée comme inexistante : son existence n'est pas révélée. */
    public Clause get(Long id) {
        return clauses.findById(id).filter(c -> Objects.equals(c.getTenantId(), TenantContext.get()))
                .orElseThrow(() -> new NoSuchElementException("Clause introuvable : " + id));
    }

    @Transactional
    public Clause creer(ClauseRequest req, String auteur) {
        Clause c = new Clause();
        c.setCode(req.code());
        c.setTenantId(TenantContext.get());
        appliquer(c, req);
        c.setMajLe(horloge.instant());
        Clause creee = clauses.save(c);
        versions.save(ClauseVersion.de(creee, Motif.CREATION, null, auteur, horloge.instant()));
        return creee;
    }

    @Transactional
    public Clause modifier(Long id, ClauseRequest req, String auteur) {
        Clause c = get(id);
        figerOrigine(c);
        appliquer(c, req);
        c.setVersion(c.getVersion() + 1);
        c.setMajLe(horloge.instant());
        Clause modifiee = clauses.save(c);
        versions.save(ClauseVersion.de(modifiee, Motif.MODIFICATION, null, auteur, horloge.instant()));
        return modifiee;
    }

    /** Historique de la clause, de la plus récente à la plus ancienne. */
    public List<ClauseVersion> versions(Long id) {
        Clause c = get(id);
        List<ClauseVersion> historique = versions.findByClauseIdOrderByNumeroDesc(c.getId());
        return historique.isEmpty() ? List.of(origine(c)) : historique;
    }

    /** Une clause amorcée (bibliothèque de référence) n'a pas d'historique : son état initial est attribué au système. */
    private static ClauseVersion origine(Clause c) {
        return ClauseVersion.de(c, Motif.CREATION, null, AUTEUR_AMORCAGE, c.getMajLe());
    }

    private void figerOrigine(Clause c) {
        if (versions.findByClauseIdOrderByNumeroDesc(c.getId()).isEmpty()) versions.save(origine(c));
    }

    /** Restaure le texte d'une version antérieure : la restauration devient elle-même une nouvelle version. */
    @Transactional
    public Clause restaurer(Long id, int numero, String auteur) {
        Clause c = get(id);
        figerOrigine(c);
        ClauseVersion cible = versions.findFirstByClauseIdAndNumeroOrderByIdDesc(c.getId(), numero)
                .orElseThrow(() -> new NoSuchElementException("Version " + numero + " introuvable pour la clause " + id));
        cible.restaurerDans(c, horloge.instant());
        Clause restauree = clauses.save(c);
        versions.save(ClauseVersion.de(restauree, Motif.RESTAURATION, numero, auteur, horloge.instant()));
        return restauree;
    }

    /** Suppression logique (préserve les documents assemblés référençant la clause). */
    @Transactional
    public void supprimer(Long id) {
        Clause c = get(id);
        c.setActif(false);
        c.setMajLe(horloge.instant());
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
