package qc.levis.contrat.reference.application;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.reference.application.dto.Option;
import qc.levis.contrat.reference.application.dto.ReferenceRequest;
import qc.levis.contrat.reference.application.repository.ReferenceValueRepository;
import qc.levis.contrat.reference.domain.ReferenceValue;

/**
 * Valeurs de référence des sélecteurs. Portée globale (superadmin, tenantId null) ou
 * spécifique au tenant (admin). Les options servies fusionnent global + tenant courant.
 */
@Service
public class ReferenceService {

    private final ReferenceValueRepository repo;

    public ReferenceService(ReferenceValueRepository repo) {
        this.repo = repo;
    }

    /** Options actives pour un sélecteur : globales + celles du tenant courant. */
    public List<Option> options(String categorie) {
        List<Option> res = new ArrayList<>();
        repo.findByCategorieAndActifTrueAndTenantIdIsNullOrderByOrdreAsc(categorie)
                .forEach(v -> res.add(new Option(v.getCode(), v.getLibelle())));
        Long tenant = TenantContext.get();
        if (tenant != null) {
            repo.findByCategorieAndActifTrueAndTenantIdOrderByOrdreAsc(categorie, tenant)
                    .forEach(v -> res.add(new Option(v.getCode(), v.getLibelle())));
        }
        return res;
    }

    // --- Gestion (superadmin = global ; admin = tenant courant) ---

    public List<ReferenceValue> lister(String categorie, boolean global) {
        return global
                ? repo.findByCategorieAndTenantIdIsNullOrderByOrdreAsc(categorie)
                : repo.findByCategorieAndTenantIdOrderByOrdreAsc(categorie, TenantContext.get());
    }

    @Transactional
    public ReferenceValue creer(ReferenceRequest req, boolean global) {
        ReferenceValue v = new ReferenceValue(req.categorie(), req.code(), req.libelle(),
                global ? null : TenantContext.get(), req.ordre() != null ? req.ordre() : 0);
        if (req.actif() != null) v.setActif(req.actif());
        return repo.save(v);
    }

    @Transactional
    public ReferenceValue modifier(Long id, ReferenceRequest req, boolean global) {
        ReferenceValue v = exiger(id, global);
        v.setLibelle(req.libelle());
        v.setCode(req.code());
        if (req.ordre() != null) v.setOrdre(req.ordre());
        if (req.actif() != null) v.setActif(req.actif());
        return repo.save(v);
    }

    @Transactional
    public void supprimer(Long id, boolean global) {
        repo.delete(exiger(id, global));
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
