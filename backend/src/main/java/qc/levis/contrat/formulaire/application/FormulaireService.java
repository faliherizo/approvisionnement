package qc.levis.contrat.formulaire.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.formulaire.application.dto.*;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.formulaire.domain.Formulaire;
import qc.levis.contrat.formulaire.domain.FormulaireVersion;
import qc.levis.contrat.formulaire.domain.StatutFormulaire;

/**
 * Cas d'utilisation du moteur de formulaires (piloté par métadonnées). Tenant-scopé.
 * La structure (pages/champs/types/règles) est portée par {@code structureJson} — aucune colonne figée.
 */
@Service
public class FormulaireService {

    private final FormulaireRepository repo;
    private final FormulaireVersionRepository versions;

    public FormulaireService(FormulaireRepository repo, FormulaireVersionRepository versions) {
        this.repo = repo;
        this.versions = versions;
    }

    /**
     * Liste les formulaires du tenant ; filtre par domaine si {@code domaine} est fourni (cloisonnement). Un formulaire
     * supprimé (désactivé : ses fiches restent conservées) n'y figure plus ; l'export de réversibilité, lui, les garde.
     */
    public List<FormulaireVue> lister(String domaine) {
        List<Formulaire> src = (domaine == null || domaine.isBlank())
                ? repo.findByTenantIdOrderByNomAsc(tenant())
                : repo.findByTenantIdAndDomaineOrderByNomAsc(tenant(), domaine);
        List<FormulaireVue> res = new ArrayList<>();
        for (Formulaire f : src) if (f.isActif()) res.add(vue(f));
        return res;
    }

    /** Catalogue des formulaires PUBLIÉS et actifs, accessible au répondant (optionnellement par domaine). */
    public List<FormulaireVue> catalogue(String domaine) {
        List<FormulaireVue> res = new ArrayList<>();
        for (FormulaireVue v : lister(domaine)) {
            if (v.actif() && "PUBLIE".equals(v.statutCourant())) res.add(v);
        }
        return res;
    }

    @Transactional
    public FormulaireVue creer(FormulaireRequest req) {
        if (repo.existsByTenantIdAndCode(tenant(), req.code()))
            throw new IllegalArgumentException("Code de formulaire déjà utilisé : " + req.code());
        Formulaire nf = new Formulaire(tenant(), req.code(), req.nom(), req.parentId());
        nf.setDomaine(req.domaine());
        nf.setConservationJours(req.conservationJours());
        Formulaire f = repo.save(nf);
        versions.save(new FormulaireVersion(f.getId(), 1, "{\"pages\":[]}", "Version initiale"));
        return vue(f);
    }

    public VersionVue versionCourante(Long id) {
        exiger(id);
        FormulaireVersion v = derniere(id);
        return versionVue(v);
    }

    public List<VersionVue> historique(Long id) {
        exiger(id);
        List<VersionVue> res = new ArrayList<>();
        for (FormulaireVersion v : versions.findByFormulaireIdOrderByNumeroDesc(id)) res.add(versionVue(v));
        return res;
    }

    /** Édite la structure : modifie le brouillon courant, ou crée un nouveau brouillon si la dernière version est publiée/en approbation. */
    @Transactional
    public VersionVue majStructure(Long id, StructureRequest req) {
        exiger(id);
        FormulaireVersion v = derniere(id);
        if (v.getStatut() != StatutFormulaire.BROUILLON) {
            v = versions.save(new FormulaireVersion(id, v.getNumero() + 1, v.getStructureJson(),
                    req.note() != null ? req.note() : "Nouveau brouillon"));
        }
        if (req.structureJson() != null) v.setStructureJson(req.structureJson());
        if (req.note() != null) v.setNote(req.note());
        return versionVue(versions.save(v));
    }

    @Transactional
    public VersionVue soumettre(Long id) {
        return transition(id, StatutFormulaire.EN_APPROBATION);
    }

    @Transactional
    public VersionVue publier(Long id) {
        exiger(id);
        FormulaireVersion v = derniere(id);
        exigerTransition(v, StatutFormulaire.PUBLIE);
        v.setStatut(StatutFormulaire.PUBLIE);
        v.setPublieLe(Instant.now());
        return versionVue(versions.save(v));
    }

    /** Retour arrière : crée un nouveau brouillon à partir de la structure d'une version antérieure. */
    @Transactional
    public VersionVue revenirA(Long id, int numero) {
        exiger(id);
        FormulaireVersion src = versions.findByFormulaireIdAndNumero(id, numero)
                .orElseThrow(() -> new NoSuchElementException("Version introuvable : " + numero));
        int prochain = derniere(id).getNumero() + 1;
        FormulaireVersion nv = versions.save(new FormulaireVersion(id, prochain, src.getStructureJson(),
                "Restauration de la version " + numero));
        return versionVue(nv);
    }

    @Transactional
    public FormulaireVue dupliquer(Long id, String nouveauCode, String nouveauNom) {
        Formulaire src = exiger(id);
        if (repo.existsByTenantIdAndCode(tenant(), nouveauCode))
            throw new IllegalArgumentException("Code déjà utilisé : " + nouveauCode);
        Formulaire c = new Formulaire(tenant(), nouveauCode, nouveauNom, src.getParentId());
        c.setDomaine(src.getDomaine());
        Formulaire copie = repo.save(c);
        versions.save(new FormulaireVersion(copie.getId(), 1, derniere(id).getStructureJson(),
                "Dupliqué depuis " + src.getCode()));
        return vue(copie);
    }

    /** Export de la structure (réversibilité / transfert entre environnements). */
    public String exporter(Long id) {
        exiger(id);
        return derniere(id).getStructureJson();
    }

    @Transactional
    public FormulaireVue importer(String code, String nom, String structureJson) {
        if (repo.existsByTenantIdAndCode(tenant(), code))
            throw new IllegalArgumentException("Code déjà utilisé : " + code);
        Formulaire f = repo.save(new Formulaire(tenant(), code, nom, null));
        versions.save(new FormulaireVersion(f.getId(), 1,
                structureJson != null ? structureJson : "{\"pages\":[]}", "Importé"));
        return vue(f);
    }

    @Transactional
    public void supprimer(Long id) {
        Formulaire f = exiger(id);
        f.setActif(false);
        repo.save(f);
    }

    // --- interne ---

    private VersionVue transition(Long id, StatutFormulaire cible) {
        exiger(id);
        FormulaireVersion v = derniere(id);
        exigerTransition(v, cible);
        v.setStatut(cible);
        return versionVue(versions.save(v));
    }

    private void exigerTransition(FormulaireVersion v, StatutFormulaire cible) {
        if (!v.getStatut().peutAllerVers(cible))
            throw new IllegalStateException("Transition interdite : " + v.getStatut() + " → " + cible);
    }

    private Formulaire exiger(Long id) {
        return repo.findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new NoSuchElementException("Formulaire introuvable : " + id));
    }

    private FormulaireVersion derniere(Long id) {
        return versions.findTopByFormulaireIdOrderByNumeroDesc(id)
                .orElseThrow(() -> new NoSuchElementException("Aucune version pour le formulaire : " + id));
    }

    private FormulaireVue vue(Formulaire f) {
        FormulaireVersion v = versions.findTopByFormulaireIdOrderByNumeroDesc(f.getId()).orElse(null);
        return new FormulaireVue(f.getId(), f.getCode(), f.getNom(), f.getDomaine(), f.getParentId(), f.isActif(),
                v != null ? v.getNumero() : null, v != null ? v.getStatut().name() : null, f.getConservationJours());
    }

    private VersionVue versionVue(FormulaireVersion v) {
        return new VersionVue(v.getNumero(), v.getStatut().name(), v.getStructureJson(), v.getNote(), v.getPublieLe());
    }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }
}
