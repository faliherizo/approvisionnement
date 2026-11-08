package qc.levis.contrat.reference.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.application.ModuleAccess;
import qc.levis.contrat.reference.application.dto.CategorieVue;
import qc.levis.contrat.reference.application.dto.DeclarationCategorie;
import qc.levis.contrat.reference.application.dto.Option;
import qc.levis.contrat.reference.application.dto.ReferenceRequest;
import qc.levis.contrat.reference.application.dto.ValeurReference;
import qc.levis.contrat.reference.application.repository.CategorieReferenceRepository;
import qc.levis.contrat.reference.application.repository.ReferenceValueRepository;
import qc.levis.contrat.reference.domain.CategorieReference;
import qc.levis.contrat.reference.domain.ReferenceValue;

/**
 * Listes de référence des sélecteurs. Chaque liste appartient au module qui la déclare et n'est servie qu'aux
 * organisations qui l'ont souscrit. La plateforme (superadmin) fournit les valeurs par défaut ; une organisation (admin)
 * en ajoute, en personnalise une en reprenant son code, ou la retire en la désactivant : supprimer sa personnalisation
 * rétablit la valeur de la plateforme.
 */
@Service
public class ReferenceService {

    /** Provenance d'une valeur dans la liste effective d'une organisation. */
    public enum Origine { PLATEFORME, PERSONNALISEE, ORGANISATION }

    private static final int CODE_MAX = 100;
    private static final int LIBELLE_MAX = 255;
    private static final int DETAIL_MAX = 2000;
    private static final Comparator<ValeurReference> ORDRE = Comparator.comparingInt(ValeurReference::ordre)
            .thenComparing(ValeurReference::libelle, String.CASE_INSENSITIVE_ORDER);

    private final ReferenceValueRepository repo;
    private final CategorieReferenceRepository categories;
    private final ModuleAccess modules;

    public ReferenceService(ReferenceValueRepository repo, CategorieReferenceRepository categories, ModuleAccess modules) {
        this.repo = repo;
        this.categories = categories;
        this.modules = modules;
    }

    /** Déclaration d'une liste par son module ; les valeurs par défaut ne sont amorcées que si la plateforme n'en a aucune. */
    @Transactional
    public void declarer(DeclarationCategorie d) {
        CategorieReference c = categories.findByCode(d.code()).orElseGet(() -> new CategorieReference(d.code(), d.module()));
        c.decrire(d.module(), d.libelle(), d.aide(), d.libelleDetail(), d.ordre());
        categories.save(c);
        if (repo.existsByCategorieAndTenantIdIsNull(d.code())) {
            return;
        }
        int ordre = 0;
        for (DeclarationCategorie.Valeur v : d.valeurs()) {
            ReferenceValue valeur = new ReferenceValue(d.code(), v.code(), v.libelle(), null, ordre);
            valeur.setDetail(c.avecDetail() ? v.detail() : null);
            repo.save(valeur);
            ordre += 10;
        }
    }

    /** Listes des modules souscrits par l'organisation courante ; toutes pour la plateforme. */
    public List<CategorieVue> categories() {
        return categories.findAllByOrderByOrdreAscCodeAsc().stream().filter(c -> modules.actif(c.getModule()))
                .map(c -> new CategorieVue(c.getCode(), c.getModule(), c.getLibelle(), c.getAide(), c.getLibelleDetail()))
                .toList();
    }

    /** Options actives d'un sélecteur, dans la version effective de l'organisation courante. */
    public List<Option> options(String categorie) {
        exigerCategorie(categorie);
        return effectives(categorie).stream().filter(ValeurReference::actif)
                .map(v -> new Option(v.code(), v.libelle(), v.detail())).toList();
    }

    // --- Gestion (superadmin = valeurs de la plateforme ; admin = liste effective de son organisation) ---

    public List<ValeurReference> lister(String categorie, boolean global) {
        exigerCategorie(categorie);
        if (global) {
            return repo.findByCategorieAndTenantIdIsNullOrderByOrdreAsc(categorie).stream()
                    .map(v -> vue(v, Origine.PLATEFORME)).toList();
        }
        return effectives(categorie);
    }

    /** Valeurs de la plateforme, remplacées par celles de l'organisation de même code, puis ses valeurs propres. */
    private List<ValeurReference> effectives(String categorie) {
        Map<String, ReferenceValue> propres = new LinkedHashMap<>();
        Long tenant = TenantContext.get();
        if (tenant != null) {
            repo.findByCategorieAndTenantIdOrderByOrdreAsc(categorie, tenant).forEach(v -> propres.put(v.getCode(), v));
        }
        List<ValeurReference> res = new ArrayList<>();
        for (ReferenceValue v : repo.findByCategorieAndTenantIdIsNullOrderByOrdreAsc(categorie)) {
            ReferenceValue personnalisee = propres.remove(v.getCode());
            res.add(personnalisee == null ? vue(v, Origine.PLATEFORME) : vue(personnalisee, Origine.PERSONNALISEE));
        }
        propres.values().forEach(v -> res.add(vue(v, Origine.ORGANISATION)));
        res.sort(ORDRE);
        return res;
    }

    @Transactional
    public ValeurReference creer(ReferenceRequest req, boolean global) {
        CategorieReference c = exigerCategorie(req.categorie());
        Long tenant = global ? null : TenantContext.get();
        String code = borne(req.code(), CODE_MAX, "Code");
        exigerCodeLibre(c.getCode(), code, tenant);
        ReferenceValue v = new ReferenceValue(c.getCode(), code, borne(req.libelle(), LIBELLE_MAX, "Libellé"), tenant,
                req.ordre() != null ? req.ordre() : 0);
        v.setDetail(detail(c, req.detail()));
        if (req.actif() != null) v.setActif(req.actif());
        return vue(repo.save(v));
    }

    @Transactional
    public ValeurReference modifier(Long id, ReferenceRequest req, boolean global) {
        ReferenceValue v = exiger(id, global);
        CategorieReference c = exigerCategorie(v.getCategorie());
        String code = borne(req.code(), CODE_MAX, "Code");
        if (!code.equals(v.getCode())) {
            exigerCodeLibre(c.getCode(), code, v.getTenantId());
        }
        v.setCode(code);
        v.setLibelle(borne(req.libelle(), LIBELLE_MAX, "Libellé"));
        v.setDetail(detail(c, req.detail()));
        if (req.ordre() != null) v.setOrdre(req.ordre());
        if (req.actif() != null) v.setActif(req.actif());
        return vue(repo.save(v));
    }

    /** Supprimer une personnalisation rétablit la valeur de la plateforme ; une valeur propre disparaît de la liste. */
    @Transactional
    public void supprimer(Long id, boolean global) {
        ReferenceValue v = exiger(id, global);
        exigerCategorie(v.getCategorie());
        repo.delete(v);
    }

    /** Liste déclarée et module souscrit par l'organisation courante (la plateforme les voit toutes). */
    private CategorieReference exigerCategorie(String code) {
        CategorieReference c = categories.findByCode(code)
                .orElseThrow(() -> new NoSuchElementException("Liste de référence inconnue : " + code));
        modules.exiger(c.getModule());
        return c;
    }

    private void exigerCodeLibre(String categorie, String code, Long tenant) {
        boolean pris = tenant == null ? repo.existsByCategorieAndCodeAndTenantIdIsNull(categorie, code)
                : repo.existsByCategorieAndCodeAndTenantId(categorie, code, tenant);
        if (pris) {
            throw new IllegalStateException("Le code « " + code + " » existe déjà dans cette liste.");
        }
    }

    private static String detail(CategorieReference c, String detail) {
        if (!c.avecDetail() || detail == null || detail.isBlank()) {
            return null;
        }
        return borne(detail, DETAIL_MAX, c.getLibelleDetail());
    }

    private static String borne(String valeur, int max, String champ) {
        String v = valeur == null ? "" : valeur.trim();
        if (v.isEmpty() || v.length() > max) {
            throw new IllegalArgumentException(champ + " : entre 1 et " + max + " caractères.");
        }
        return v;
    }

    private ValeurReference vue(ReferenceValue v) {
        if (v.getTenantId() == null) {
            return vue(v, Origine.PLATEFORME);
        }
        boolean personnalisee = repo.existsByCategorieAndCodeAndTenantIdIsNull(v.getCategorie(), v.getCode());
        return vue(v, personnalisee ? Origine.PERSONNALISEE : Origine.ORGANISATION);
    }

    private static ValeurReference vue(ReferenceValue v, Origine origine) {
        return new ValeurReference(v.getId(), v.getCategorie(), v.getCode(), v.getLibelle(), v.getDetail(), v.getOrdre(),
                v.isActif(), origine.name());
    }

    /** Vérifie que la valeur relève de la portée demandée (global ou tenant courant). */
    private ReferenceValue exiger(Long id, boolean global) {
        ReferenceValue v = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Référence introuvable: " + id));
        boolean ok = global ? v.getTenantId() == null
                            : v.getTenantId() != null && v.getTenantId().equals(TenantContext.get());
        if (!ok) throw new NoSuchElementException("Référence introuvable: " + id);
        return v;
    }
}
