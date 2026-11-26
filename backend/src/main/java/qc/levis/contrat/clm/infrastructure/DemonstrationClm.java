package qc.levis.contrat.clm.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ConservationService;
import qc.levis.contrat.clm.application.ContexteClm;
import qc.levis.contrat.clm.application.DocumentClmService;
import qc.levis.contrat.clm.application.GroupesClmService;
import qc.levis.contrat.clm.application.ModeleService;
import qc.levis.contrat.clm.application.TrousseService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.GroupeClmRepository;
import qc.levis.contrat.clm.application.repository.ModeleContratRepository;
import qc.levis.contrat.clm.application.repository.RegleConservationRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.ModeleContrat;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;
import java.util.ArrayList;

/**
 * Jeu de démonstration du module CLM (data/clm-demo.json), par organisation : groupes, règles de conservation, modèles
 * publiés et trousses en cours. Chargé au démarrage d'un environnement de démonstration (DemoClmSeeder) ou à la demande
 * de l'administration de l'organisation. Tout passe par les services, avec les mêmes contrôles que l'interface : un modèle
 * est créé par l'administration d'affaires puis publié par une autre administration. Idempotent (groupe par code, règle
 * par type, modèle par nom, trousse par référence) : seul ce qui manque est ajouté, rien n'est écrasé.
 */
@Component
public class DemonstrationClm {

    private static final Logger log = LoggerFactory.getLogger(DemonstrationClm.class);

    record GroupeDemo(String code, String libelle, List<String> membres) {}
    record TrousseDemo(String proprietaire, String modele, ClmDto.TrousseRequest requete, Map<String, Object> valeurs,
                       List<ClmDto.ParticipantRequest> participants, boolean lancer) {}
    /** {@code acteurs} : comptes de démonstration et leurs rôles, sous lesquels le jeu est chargé. */
    record DemoClm(String administrationAffaires, String administrationPrincipale, Map<String, List<String>> acteurs, List<GroupeDemo> groupes,
                   List<ClmDto.RegleConservationRequest> conservation, List<ClmDto.ModeleRequest> modeles, List<TrousseDemo> trousses) {}

    /** Services et dépôts de lecture du module CLM, regroupés pour garder un constructeur lisible. */
    @Component
    static class Moyens {
        final ModeleService modeles; final TrousseService trousses; final DocumentClmService documents;
        final GroupesClmService groupes; final ConservationService conservation;
        final ModeleContratRepository modelesLus; final TrousseRepository troussesLues;
        final GroupeClmRepository groupesLus; final RegleConservationRepository reglesLues;
        final DocumentTrousseRepository documentsLus;

        Moyens(ModeleService modeles, TrousseService trousses, DocumentClmService documents, GroupesClmService groupes,
               ConservationService conservation, ModeleContratRepository modelesLus, TrousseRepository troussesLues,
               GroupeClmRepository groupesLus, RegleConservationRepository reglesLues, DocumentTrousseRepository documentsLus) {
            this.modeles = modeles; this.trousses = trousses; this.documents = documents; this.groupes = groupes;
            this.conservation = conservation; this.modelesLus = modelesLus; this.troussesLues = troussesLues;
            this.groupesLus = groupesLus; this.reglesLues = reglesLues; this.documentsLus = documentsLus;
        }
    }

    private final Moyens m;
    private final ContexteClm ctx;
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();

    public DemonstrationClm(Moyens moyens, ContexteClm ctx) {
        this.m = moyens;
        this.ctx = ctx;
    }

    /** Organisations (identifiant court) pour lesquelles un jeu de démonstration existe. */
    public java.util.Set<String> organisations() {
        return lire().keySet();
    }

    /** Charge, dans l'organisation courante, ce qui manque de son jeu de démonstration. */
    public ClmDto.BilanDemonstration charger(String organisation) {
        DemoClm demo = lire().get(organisation);
        if (demo == null) {
            throw new java.util.NoSuchElementException("Aucun jeu de démonstration pour l'organisation « " + organisation + " ».");
        }
        Bilan b = new Bilan();
        amorcer(ctx.tenant(), demo, b);
        log.info("Démonstration CLM « {} » : {} groupe(s), {} règle(s), {} modèle(s), {} trousse(s) ajoutés",
                organisation, b.groupes, b.regles, b.modeles, b.trousses);
        return new ClmDto.BilanDemonstration(b.groupes, b.regles, b.modeles, b.trousses, List.copyOf(b.ecarts));
    }

    /** Compte de ce qui a été ajouté, et motif de chaque élément écarté. */
    private static final class Bilan {
        int groupes; int regles; int modeles; int trousses;
        final List<String> ecarts = new ArrayList<>();
    }

    Map<String, DemoClm> lire() {
        try (InputStream in = new ClassPathResource("data/clm-demo.json").getInputStream()) {
            return json.readValue(in, new TypeReference<>() {});
        } catch (IOException e) {
            throw new UncheckedIOException("data/clm-demo.json illisible", e);
        }
    }

    private void amorcer(Long tenantId, DemoClm demo, Bilan b) {
        String affaires = demo.administrationAffaires();
        demo.groupes().forEach(g -> tenter(b, "groupe " + g.code(), () -> { if (groupe(tenantId, g, affaires)) b.groupes++; }));
        demo.conservation().forEach(r -> tenter(b, "règle de conservation " + (r.typeContrat() == null ? "par défaut" : r.typeContrat()),
                () -> { if (regle(tenantId, r, affaires)) b.regles++; }));
        demo.modeles().forEach(mo -> tenter(b, "modèle « " + mo.nom() + " »",
                () -> { if (modele(tenantId, mo, acteur(demo, affaires), acteur(demo, demo.administrationPrincipale()))) b.modeles++; }));
        demo.trousses().forEach(tr -> tenter(b, "trousse " + tr.requete().reference(),
                () -> { if (trousse(tenantId, tr, acteur(demo, tr.proprietaire()))) b.trousses++; }));
    }

    private boolean groupe(Long tenantId, GroupeDemo g, String acteur) {
        if (m.groupesLus.existsByTenantIdAndCode(tenantId, g.code())) {
            return false;
        }
        Long id = m.groupes.creer(new ClmDto.GroupeRequest(g.code(), g.libelle()), acteur).id();
        g.membres().forEach(courriel -> m.groupes.ajouterMembre(id, new ClmDto.MembreRequest(courriel), acteur));
        return true;
    }

    private boolean regle(Long tenantId, ClmDto.RegleConservationRequest r, String acteur) {
        boolean existe = m.reglesLues.findByTenantIdOrderByTypeContratAsc(tenantId).stream()
                .anyMatch(x -> java.util.Objects.equals(x.getTypeContrat(), r.typeContrat()));
        if (!existe) {
            m.conservation.definir(r, acteur);
        }
        return !existe;
    }

    private boolean modele(Long tenantId, ClmDto.ModeleRequest req, Habilitations.Acteur auteur, Habilitations.Acteur publieur) {
        if (modeleNomme(tenantId, req.nom()) != null) {
            return false;
        }
        Long id = m.modeles.creer(req, auteur).id();
        m.modeles.soumettre(id, auteur);
        m.modeles.publier(id, publieur);
        return true;
    }

    /**
     * Trousse de démonstration : créée si elle manque ; reprise si un chargement précédent l'a laissée en brouillon sans
     * document (ex. génération en échec) ; laissée telle quelle sinon.
     */
    private boolean trousse(Long tenantId, TrousseDemo tr, Habilitations.Acteur initiateur) {
        String reference = tr.requete().reference().toUpperCase(java.util.Locale.ROOT);
        java.util.Optional<Trousse> existante = m.troussesLues.findByTenantIdAndReference(tenantId, reference);
        if (existante.isPresent() && !aReprendre(tenantId, existante.get())) {
            return false;
        }
        Long id = existante.map(Trousse::getId).orElseGet(() -> creer(tenantId, tr, initiateur));
        completer(id, tr, initiateur);
        return true;
    }

    private boolean aReprendre(Long tenantId, Trousse t) {
        return t.getStatut() == StatutTrousse.BROUILLON
                && m.documentsLus.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(tenantId, t.getId()).isEmpty();
    }

    private Long creer(Long tenantId, TrousseDemo tr, Habilitations.Acteur initiateur) {
        Long id = m.trousses.creer(avecModele(tr.requete(), modeleNomme(tenantId, tr.modele())), initiateur).resume().id();
        m.trousses.saisirValeurs(id, new ClmDto.ValeursRequest(tr.valeurs()), initiateur);
        return id;
    }

    private void completer(Long id, TrousseDemo tr, Habilitations.Acteur initiateur) {
        m.documents.generer(id, initiateur, FormatDocument.Type.PDF);
        for (ClmDto.ParticipantRequest p : tr.participants()) {
            Long participant = m.trousses.ajouterParticipant(id, p, initiateur).id();
            m.trousses.attribuerZone(id, participant, new ClmDto.ZoneRequest(p.roleContrat()), initiateur);
        }
        if (tr.lancer()) {
            m.trousses.lancer(id, initiateur);
        }
    }

    private static ClmDto.TrousseRequest avecModele(ClmDto.TrousseRequest r, ModeleContrat modele) {
        return new ClmDto.TrousseRequest(r.reference(), r.titre(), r.typeContrat(), r.unite(), r.risque(), r.echeance(),
                modele == null ? null : modele.getId(), r.sfDossierId(), r.rappelFrequenceJours(), r.rappelsMax(), r.copies(), r.priorite());
    }

    private ModeleContrat modeleNomme(Long tenantId, String nom) {
        return m.modelesLus.findByTenantIdOrderByNomAscNumeroDesc(tenantId).stream()
                .filter(x -> x.getNom().equals(nom)).findFirst().orElse(null);
    }

    private Habilitations.Acteur acteur(DemoClm demo, String courriel) {
        List<String> roles = demo.acteurs() == null ? null : demo.acteurs().get(courriel);
        if (roles == null) {
            throw new IllegalStateException("Compte absent des acteurs du jeu de démonstration : " + courriel);
        }
        return ctx.acteur(courriel, roles);
    }

    /** Un élément en échec (compte absent, règle de l'organisation changée) est écarté et signalé ; les autres se chargent. */
    private static void tenter(Bilan b, String quoi, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            b.ecarts.add(quoi + " : " + e.getMessage());
            log.warn("Démonstration CLM : {} écarté — {}", quoi, e.getMessage());
        }
    }
}
