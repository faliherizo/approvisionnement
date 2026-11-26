package qc.levis.contrat.clm.application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.EtiquetagePurview;
import qc.levis.contrat.clm.application.repository.PolitiquePurviewRepository;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.EtiquettePurview;
import qc.levis.contrat.clm.domain.PolitiquePurview;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Étiquettes Microsoft Purview du module (Annexe 10, SEC-27 ; ADR-036). L'administration choisit, dans le catalogue du
 * locataire Microsoft 365, une étiquette de sensibilité et une étiquette de rétention par défaut, et au besoin par type de
 * contrat. Chaque document déposé ou produit reçoit les étiquettes de la politique applicable, les garde, et elles sont
 * propagées au fichier déposé dans SharePoint ou OneDrive.
 */
@Service
public class EtiquetagePurviewService {

    private final PolitiquePurviewRepository politiques;
    private final EtiquetagePurview port;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public EtiquetagePurviewService(PolitiquePurviewRepository politiques, EtiquetagePurview port, JournalClm journal, ContexteClm ctx) {
        this.politiques = politiques;
        this.port = port;
        this.journal = journal;
        this.ctx = ctx;
    }

    public List<ClmDto.EtiquettePurviewVue> catalogue() {
        return port.catalogue(ctx.tenant()).stream()
                .map(e -> new ClmDto.EtiquettePurviewVue(e.type().name(), e.id(), e.nom(), e.retentionJours())).toList();
    }

    public List<ClmDto.PolitiquePurviewVue> politiques() {
        return politiques.findByTenantIdOrderByTypeContratAsc(ctx.tenant()).stream().map(EtiquetagePurviewService::vue).toList();
    }

    /** Définit (ou remplace) la politique par défaut ou celle d'un type de contrat ; les étiquettes viennent du catalogue. */
    @Transactional
    public ClmDto.PolitiquePurviewVue definir(ClmDto.PolitiquePurviewRequest req, String acteur) {
        Long tenant = ctx.tenant();
        String type = req.typeContrat() == null || req.typeContrat().isBlank() ? null : req.typeContrat().strip();
        List<EtiquettePurview> catalogue = port.catalogue(tenant);
        EtiquettePurview sensibilite = choisir(catalogue, EtiquettePurview.Type.SENSIBILITE, req.sensibiliteId());
        EtiquettePurview retention = choisir(catalogue, EtiquettePurview.Type.RETENTION, req.retention());
        PolitiquePurview p = politiqueDe(tenant, type).orElseGet(() -> new PolitiquePurview(tenant, type));
        p.definir(sensibilite, retention, acteur, ctx.maintenant());
        p = politiques.save(p);
        journal.configuration(acteur, "PURVIEW", (type == null ? "défaut" : type) + " : sensibilité " + nom(sensibilite)
                + " · rétention " + nom(retention));
        return vue(p);
    }

    @Transactional
    public void supprimer(Long id, String acteur) {
        PolitiquePurview p = politiques.findByIdAndTenantId(id, ctx.tenant())
                .orElseThrow(() -> new NoSuchElementException("Politique Purview introuvable."));
        politiques.delete(p);
        journal.configuration(acteur, "PURVIEW", (p.estDefaut() ? "défaut" : p.getTypeContrat()) + " : politique retirée");
    }

    /** Étiquettes de la politique du type de contrat, sinon de la politique par défaut ; aucune s'il n'y en a pas. */
    public void etiqueter(Trousse t, DocumentTrousse d) {
        politiqueApplicable(t).ifPresent(p -> d.etiqueter(p.sensibilite(), p.retention()));
    }

    /**
     * Propage au fichier déposé les étiquettes conservées avec le document ; rend le résultat à noter sur le dépôt, ou vide
     * si le document n'en porte aucune.
     */
    public Optional<String> propager(Trousse t, DocumentTrousse d, String urlDepot) {
        if (d.sensibilite() == null && d.retention() == null) {
            return Optional.empty();
        }
        EtiquetagePurview.Resultat r = port.apposer(t.getTenantId(), urlDepot, d.sensibilite(), d.retention());
        String etiquettes = nom(d.sensibilite()) + " · " + nom(d.retention());
        String resultat = r.succes() ? etiquettes + " — apposées" : etiquettes + " — échec : " + r.message();
        journal.consigner(t, "ETIQUETTE_PURVIEW", "systeme", d.getNom() + " : " + resultat);
        return Optional.of(resultat);
    }

    private Optional<PolitiquePurview> politiqueApplicable(Trousse t) {
        Optional<PolitiquePurview> propre = t.getTypeContrat() == null ? Optional.empty() : politiqueDe(t.getTenantId(), t.getTypeContrat());
        return propre.isPresent() ? propre : politiqueDe(t.getTenantId(), null);
    }

    private Optional<PolitiquePurview> politiqueDe(Long tenant, String type) {
        return politiques.findByTenantIdOrderByTypeContratAsc(tenant).stream()
                .filter(p -> type == null ? p.estDefaut() : type.equals(p.getTypeContrat())).findFirst();
    }

    private static EtiquettePurview choisir(List<EtiquettePurview> catalogue, EtiquettePurview.Type type, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return catalogue.stream().filter(e -> e.type() == type && e.id().equals(id.strip())).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Étiquette absente du catalogue Purview de l'organisation : " + id + "."));
    }

    private static String nom(EtiquettePurview e) {
        return e == null ? "aucune" : e.nom();
    }

    private static ClmDto.PolitiquePurviewVue vue(PolitiquePurview p) {
        EtiquettePurview s = p.sensibilite();
        EtiquettePurview r = p.retention();
        return new ClmDto.PolitiquePurviewVue(p.getId(), p.getTypeContrat(), s == null ? null : s.id(), s == null ? null : s.nom(),
                r == null ? null : r.nom(), r == null ? null : r.retentionJours(), p.getModifiePar(), p.getModifieLe());
    }
}
