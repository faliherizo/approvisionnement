package qc.levis.contrat.clm.application;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.DepotMicrosoft365;
import qc.levis.contrat.clm.application.repository.DepotM365Repository;
import qc.levis.contrat.clm.application.repository.DiffusionM365Repository;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.DepotM365;
import qc.levis.contrat.clm.domain.DiffusionM365;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Diffusion Microsoft 365 des trousses terminées (Annexe 10, TI-01) : à l'achèvement, chaque document signé et, au choix de
 * l'organisation, le dossier de preuve sont déposés dans la bibliothèque SharePoint ou OneDrive désignée, puis un avis est
 * déposé dans le canal Teams. Les dépôts sont inscrits dans la transaction qui termine la trousse et livrés ensuite par le
 * connecteur Microsoft 365 de la plateforme, avec reprises ; un dépôt abandonné se relance depuis l'administration.
 */
@Service
public class DepotsM365Service implements AbonneTransition {

    private static final Logger log = LoggerFactory.getLogger(DepotsM365Service.class);

    static final String ACHEVEMENT = "SIGNATURE_TERMINEE";
    /** Le dépôt agit pour l'organisation : il lit la trousse comme l'administration d'affaires, sans être une personne. */
    static final Habilitations.Acteur SYSTEME = new Habilitations.Acteur("systeme", Set.of("ADMIN_AFFAIRES"), Set.of());

    private final DiffusionM365Repository configurations;
    private final DepotM365Repository depots;
    private final DocumentTrousseRepository documents;
    private final TrousseRepository trousses;
    private final PreuveService preuves;
    private final DepotMicrosoft365 port;
    private final JournalClm journal;
    private final ContexteClm ctx;
    private final EtiquetagePurviewService purview;

    public DepotsM365Service(DiffusionM365Repository configurations, DepotM365Repository depots, DocumentTrousseRepository documents,
                             TrousseRepository trousses, PreuveService preuves, DepotMicrosoft365 port, JournalClm journal,
                             ContexteClm ctx, EtiquetagePurviewService purview) {
        this.purview = purview;
        this.configurations = configurations;
        this.depots = depots;
        this.documents = documents;
        this.trousses = trousses;
        this.preuves = preuves;
        this.port = port;
        this.journal = journal;
        this.ctx = ctx;
    }

    /** À l'achèvement seulement, et si l'organisation a activé la diffusion : un dépôt par objet, jamais deux fois. */
    @Override
    public void enfiler(Trousse t, String evenement, long sequence, Map<String, Object> donnees) {
        if (!ACHEVEMENT.equals(evenement)) {
            return;
        }
        DiffusionM365.Reglage r = reglage(t.getTenantId());
        if (!r.actif()) {
            return;
        }
        Instant maintenant = ctx.maintenant();
        List<DepotM365> aFaire = new ArrayList<>();
        documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId()).stream()
                .filter(d -> d.getStatut() == DocumentTrousse.Statut.SIGNE)
                .forEach(d -> aFaire.add(new DepotM365(t.getTenantId(), t.getId(), DepotM365.Nature.DOCUMENT_SIGNE, d.getId(), maintenant)));
        if (r.deposerPreuve()) {
            aFaire.add(new DepotM365(t.getTenantId(), t.getId(), DepotM365.Nature.PREUVE, null, maintenant));
        }
        if (r.avisTeams()) {
            aFaire.add(new DepotM365(t.getTenantId(), t.getId(), DepotM365.Nature.AVIS_TEAMS, null, maintenant));
        }
        aFaire.stream().filter(d -> !depots.existsByCleIdempotence(d.getCleIdempotence())).forEach(depots::save);
    }

    /** Relais périodique, tous tenants ; retourne le nombre de dépôts effectués. */
    @Transactional
    public int relayer() {
        int deposes = 0;
        // L'avis Teams part après les dépôts de sa trousse : il en donne les emplacements.
        for (DepotM365 d : depots.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(DepotM365.Statut.EN_ATTENTE,
                ctx.maintenant()).stream().sorted(java.util.Comparator.comparing(x -> x.getNature() == DepotM365.Nature.AVIS_TEAMS)).toList()) {
            deposes += ctx.dansTenant(d.getTenantId(), () -> livrer(d));
        }
        return deposes;
    }

    private int livrer(DepotM365 d) {
        Instant maintenant = ctx.maintenant();
        Trousse t = trousses.findByIdAndTenantId(d.getTrousseId(), d.getTenantId()).orElse(null);
        if (t == null || t.getDetruiteLe() != null) {
            d.echouer("Trousse détruite ou introuvable : rien à déposer.", maintenant);
            depots.save(d);
            return 0;
        }
        DiffusionM365.Reglage r = reglage(d.getTenantId());
        DepotMicrosoft365.Resultat res;
        try {
            res = switch (d.getNature()) {
                case DOCUMENT_SIGNE -> port.deposer(DepotMicrosoft365.Destination.de(r.bibliotheque()), document(t, d, r));
                case PREUVE -> port.deposer(DepotMicrosoft365.Destination.de(r.bibliotheque()), preuve(t, r));
                case AVIS_TEAMS -> port.deposer(DepotMicrosoft365.Destination.TEAMS, avis(t, r));
            };
        } catch (NoSuchElementException e) {
            res = new DepotMicrosoft365.Resultat(false, null, e.getMessage());
        }
        if (!res.succes()) {
            // L'identifiant du dépôt suffit à l'exploitation ; le message d'erreur ne contient ni contenu ni secret.
            log.warn("Dépôt Microsoft 365 {} ({}) non effectué : {}", d.getId(), d.getNature(), res.message());
            d.echouer(res.message(), maintenant);
            depots.save(d);
            return 0;
        }
        d.reussir(res.url(), maintenant);
        if (d.getNature() == DepotM365.Nature.DOCUMENT_SIGNE) {
            propagerEtiquettes(t, d);
        }
        depots.save(d);
        journal.consigner(t, "DEPOT_M365", "systeme", libelle(d.getNature()) + " → " + res.url());
        return 1;
    }

    /** Les étiquettes Purview conservées avec le document suivent le fichier déposé (ADR-036). */
    private void propagerEtiquettes(Trousse t, DepotM365 d) {
        documents.findByIdAndTenantId(d.getDocumentId(), d.getTenantId())
                .flatMap(doc -> purview.propager(t, doc, d.getUrl())).ifPresent(d::noterEtiquette);
    }

    private DepotMicrosoft365.Fichier document(Trousse t, DepotM365 d, DiffusionM365.Reglage r) {
        DocumentTrousse doc = documents.findByIdAndTenantId(d.getDocumentId(), d.getTenantId())
                .orElseThrow(() -> new NoSuchElementException("Document signé introuvable."));
        return new DepotMicrosoft365.Fichier(t.getId(), t.getTitre(), doc.getNom(), doc.getType().mime(), doc.getContenu(), dossier(t, r));
    }

    private DepotMicrosoft365.Fichier preuve(Trousse t, DiffusionM365.Reglage r) {
        byte[] zip = preuves.exporter(t.getId(), SYSTEME);
        return new DepotMicrosoft365.Fichier(t.getId(), t.getTitre(), "preuve-" + ExportationReferentielService.nomFichier(t.getReference()) + ".zip",
                "application/zip", zip, dossier(t, r));
    }

    /** Avis lisible dans le canal : référence, titre et emplacement des documents déposés, sans leur contenu. */
    private DepotMicrosoft365.Fichier avis(Trousse t, DiffusionM365.Reglage r) {
        StringBuilder sb = new StringBuilder("Trousse terminée : ").append(t.getReference()).append(" — ").append(t.getTitre()).append('\n')
                .append("Terminée le ").append(t.getClotureLe()).append('\n')
                .append("Documents déposés dans ").append(r.bibliotheque()).append(" : ").append(dossier(t, r)).append('\n');
        depots.findByTenantIdAndTrousseIdOrderByIdAsc(t.getTenantId(), t.getId()).stream()
                .filter(x -> x.getStatut() == DepotM365.Statut.DEPOSE)
                .forEach(x -> sb.append("- ").append(libelle(x.getNature())).append(" : ").append(x.getUrl()).append('\n'));
        return new DepotMicrosoft365.Fichier(t.getId(), t.getTitre(), "avis-" + ExportationReferentielService.nomFichier(t.getReference()) + ".txt",
                "text/plain;charset=UTF-8", sb.toString().getBytes(StandardCharsets.UTF_8), r.dossier().strip());
    }

    /** Un sous-dossier par trousse : les métadonnées de classement (référence) restent lisibles dans la bibliothèque. */
    private static String dossier(Trousse t, DiffusionM365.Reglage r) {
        return r.dossier().strip() + "/" + ExportationReferentielService.nomFichier(t.getReference());
    }

    private static String libelle(DepotM365.Nature n) {
        return switch (n) {
            case DOCUMENT_SIGNE -> "document signé";
            case PREUVE -> "dossier de preuve";
            case AVIS_TEAMS -> "avis Teams";
        };
    }

    private DiffusionM365.Reglage reglage(Long tenantId) {
        return configurations.findByTenantId(tenantId).map(DiffusionM365::reglage).orElse(DiffusionM365.Reglage.INACTIF);
    }

    // --- Administration technique -------------------------------------------------------------------------------------

    public ClmDto.DiffusionM365Vue configuration() {
        DiffusionM365 c = configurations.findByTenantId(ctx.tenant()).orElse(null);
        return vue(c == null ? DiffusionM365.Reglage.INACTIF : c.reglage(), c);
    }

    @Transactional
    public ClmDto.DiffusionM365Vue configurer(ClmDto.DiffusionM365Request req, String acteur) {
        DiffusionM365.Reglage r = new DiffusionM365.Reglage(req.actif(), bibliotheque(req.bibliotheque()),
                req.dossier() == null ? null : req.dossier().strip(), req.deposerPreuve(), req.avisTeams());
        List<String> erreurs = r.erreurs();
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        Long tenant = ctx.tenant();
        DiffusionM365 c = configurations.findByTenantId(tenant).orElseGet(() -> new DiffusionM365(tenant));
        c.appliquer(r, acteur, ctx.maintenant());
        c = configurations.save(c);
        journal.configuration(acteur, "DIFFUSION_M365", (r.actif() ? "active" : "inactive") + " · " + r.bibliotheque() + " « " + r.dossier()
                + " » · preuve " + (r.deposerPreuve() ? "oui" : "non") + " · avis Teams " + (r.avisTeams() ? "oui" : "non"));
        return vue(r, c);
    }

    private static DiffusionM365.Bibliotheque bibliotheque(String saisie) {
        return java.util.Arrays.stream(DiffusionM365.Bibliotheque.values()).filter(b -> b.name().equals(saisie)).findFirst().orElse(null);
    }

    public List<ClmDto.DepotM365Vue> lister() {
        Long tenant = ctx.tenant();
        return depots.findTop100ByTenantIdOrderByCreeLeDesc(tenant).stream().map(d -> vue(d, tenant)).toList();
    }

    @Transactional
    public ClmDto.DepotM365Vue relancer(Long id, String acteur) {
        Long tenant = ctx.tenant();
        DepotM365 d = depots.findByIdAndTenantId(id, tenant).orElseThrow(() -> new NoSuchElementException("Dépôt introuvable."));
        d.relancer(ctx.maintenant());
        journal.configuration(acteur, "DIFFUSION_M365", "relance du dépôt " + id + " (" + d.getNature() + ")");
        return vue(depots.save(d), tenant);
    }

    private static ClmDto.DiffusionM365Vue vue(DiffusionM365.Reglage r, DiffusionM365 c) {
        return new ClmDto.DiffusionM365Vue(r.actif(), r.bibliotheque().name(), r.dossier(), r.deposerPreuve(), r.avisTeams(),
                c == null ? null : c.getModifiePar(), c == null ? null : c.getModifieLe());
    }

    private ClmDto.DepotM365Vue vue(DepotM365 d, Long tenant) {
        String reference = trousses.findByIdAndTenantId(d.getTrousseId(), tenant).map(Trousse::getReference).orElse("—");
        return new ClmDto.DepotM365Vue(d.getId(), d.getTrousseId(), reference, d.getNature().name(), d.getStatut().name(), d.getTentatives(),
                d.getUrl(), d.getDerniereErreur(), d.getCreeLe(), d.getDeposeLe(), d.getProchainEssai(), d.getEtiquette());
    }
}
