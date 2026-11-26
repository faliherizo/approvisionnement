package qc.levis.contrat.clm.application;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AnnuaireOrganisation;
import qc.levis.contrat.clm.application.repository.ClientIntegrationRepository;
import qc.levis.contrat.clm.application.repository.ElementLotRepository;
import qc.levis.contrat.clm.application.repository.LotSignatureRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.clm.domain.ElementLot;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.LotSignature;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Trousses demandées hors de l'écran de création (addenda 1) : par un système externe — Workday (TI-22), Salesforce
 * (AF-47) — ou en lot depuis la plateforme (AF-47). Chaque trousse suit les mêmes règles qu'à l'écran : elle appartient à
 * son initiateur, membre de l'organisation, et passe par les mêmes services (modèle, valeurs, documents, parcours).
 */
@Service
public class DemandesSignatureService {

    private final TrousseService trousses;
    private final DocumentClmService documents;
    private final TrousseRepository depot;
    private final LotSignatureRepository lots;
    private final ElementLotRepository elements;
    private final ClientIntegrationRepository clients;
    private final AnnuaireOrganisation annuaire;
    private final JournalClm journal;
    private final ContexteClm ctx;
    private final TransactionTemplate parElement;

    public DemandesSignatureService(TrousseService trousses, DocumentClmService documents, TrousseRepository depot,
                                    LotSignatureRepository lots, ElementLotRepository elements, ClientIntegrationRepository clients,
                                    AnnuaireOrganisation annuaire, JournalClm journal, ContexteClm ctx,
                                    PlatformTransactionManager transactions) {
        this.trousses = trousses;
        this.documents = documents;
        this.depot = depot;
        this.lots = lots;
        this.elements = elements;
        this.clients = clients;
        this.annuaire = annuaire;
        this.journal = journal;
        this.ctx = ctx;
        this.parElement = new TransactionTemplate(transactions);
        this.parElement.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // --- Système externe (jeton d'intégration) ---

    public ClmDto.TrousseExterneVue creerPourClient(ClientIntegration client, ClmDto.DemandeTrousse d) {
        return ctx.dansTenant(client.getTenantId(), () -> parElement.execute(s ->
                vue(creerUne(initiateur(d.initiateur()), client, d, null, Boolean.TRUE.equals(d.lancer())))));
    }

    public ClmDto.LotVue creerLotPourClient(ClientIntegration client, ClmDto.DemandeLot lot) {
        return ctx.dansTenant(client.getTenantId(), () -> creerLot(initiateur(lot.initiateur()), client, lot));
    }

    public ClmDto.LotVue lotDuClient(ClientIntegration client, Long id) {
        return ctx.dansTenant(client.getTenantId(), () -> {
            LotSignature lot = lots.findByIdAndTenantId(id, client.getTenantId())
                    .filter(l -> client.getId().equals(l.getClientIntegrationId()))
                    .orElseThrow(() -> new NoSuchElementException("Lot introuvable."));
            return vue(lot);
        });
    }

    /** Une trousse n'est lisible par un client que s'il l'a créée. */
    public ClmDto.TrousseExterneVue trousseDuClient(ClientIntegration client, Long id) {
        return ctx.dansTenant(client.getTenantId(), () -> vue(trousseDuClientInterne(client, id)));
    }

    /** Acteur technique d'un client pour une lecture du catalogue : il voit ce que voit l'administration de l'organisation. */
    public Habilitations.Acteur acteurCatalogue(ClientIntegration client) {
        return new Habilitations.Acteur("integration:" + client.getClientId(), Set.of("ADMIN"), Set.of());
    }

    /** Exécute une lecture pour un client dans le contexte de son organisation. */
    public <T> T dansOrganisation(ClientIntegration client, java.util.function.Supplier<T> lecture) {
        return ctx.dansTenant(client.getTenantId(), lecture);
    }

    public Habilitations.Acteur acteurDuClient(ClientIntegration client, Long trousseId) {
        trousseDuClientInterne(client, trousseId);
        return new Habilitations.Acteur("integration:" + client.getClientId(), Set.of("ADMIN"), Set.of());
    }

    private Trousse trousseDuClientInterne(ClientIntegration client, Long id) {
        return depot.findByIdAndTenantId(id, client.getTenantId())
                .filter(t -> client.getId().equals(t.getClientIntegrationId()))
                .orElseThrow(() -> new NoSuchElementException("Trousse introuvable."));
    }

    // --- Utilisateur de la plateforme (signature en lot, AF-47) ---

    public ClmDto.LotVue creerLot(ClmDto.DemandeLot lot, Habilitations.Acteur acteur) {
        return creerLot(acteur, null, lot);
    }

    public List<ClmDto.LotVue> listerLots() {
        return lots.findTop50ByTenantIdOrderByCreeLeDesc(ctx.tenant()).stream().map(this::vue).toList();
    }

    public ClmDto.LotVue lot(Long id) {
        return vue(lots.findByIdAndTenantId(id, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Lot introuvable.")));
    }

    // --- Commun ---

    private ClmDto.LotVue creerLot(Habilitations.Acteur acteur, ClientIntegration client, ClmDto.DemandeLot demande) {
        List<ClmDto.DemandeTrousse> liste = demande.elements() == null ? List.of() : demande.elements();
        boolean lancer = Boolean.TRUE.equals(demande.lancer());
        LotSignature lot = parElement.execute(s -> lots.save(LotSignature.ouvrir(ctx.tenant(), demande.libelle(), demande.modeleId(),
                client == null ? null : client.getId(), liste.size(), lancer, acteur.id(), ctx.maintenant())));
        for (int i = 0; i < liste.size(); i++) {
            ClmDto.DemandeTrousse d = completer(liste.get(i), demande);
            int rang = i + 1;
            ElementLot e;
            try {
                Trousse t = parElement.execute(s -> creerUne(acteur, client, d, lot.getId(), lancer));
                e = ElementLot.reussi(lot, rang, d.referenceExterne(), t.getId(), lancer);
                lot.noterReussite();
            } catch (RuntimeException x) {
                e = ElementLot.echoue(lot, rang, d.referenceExterne(), x.getMessage());
                lot.noterEchec();
            }
            ElementLot aEnregistrer = e;
            parElement.executeWithoutResult(s -> elements.save(aEnregistrer));
        }
        LotSignature fini = parElement.execute(s -> lots.save(lot));
        journal.configuration(acteur.id(), "LOT_SIGNATURE", fini.getLibelle() + " : " + fini.getReussis() + " trousse(s), "
                + fini.getEchecs() + " échec(s)" + (client == null ? "" : " — " + client.getNom()));
        return vue(fini);
    }

    private static ClmDto.DemandeTrousse completer(ClmDto.DemandeTrousse d, ClmDto.DemandeLot lot) {
        return new ClmDto.DemandeTrousse(d.titre(), d.referenceExterne(), d.modeleId() != null ? d.modeleId() : lot.modeleId(),
                d.sfDossierId(), d.initiateur(), d.unite(), d.risque(), d.echeance(), d.valeurs(), d.documents(), d.signataires(),
                lot.lancer());
    }

    private Trousse creerUne(Habilitations.Acteur acteur, ClientIntegration client, ClmDto.DemandeTrousse d, Long lotId, boolean lancer) {
        String titre = titre(d);
        ClmDto.TrousseDetail cree = trousses.creer(new ClmDto.TrousseRequest(null, titre, null, d.unite(), d.risque(), d.echeance(),
                d.modeleId(), d.sfDossierId(), null, null, null), acteur);
        Long id = cree.resume().id();
        Trousse t = depot.findByIdAndTenantId(id, ctx.tenant()).orElseThrow();
        t.rattacherExterne(origine(client), client == null ? null : client.getId(), d.referenceExterne(), lotId);
        depot.save(t);
        if (d.valeurs() != null && !d.valeurs().isEmpty()) {
            trousses.saisirValeurs(id, new ClmDto.ValeursRequest(d.valeurs()), acteur);
        }
        deposer(id, d, acteur);
        List<Long> signataires = new ArrayList<>();
        for (ClmDto.ParticipantRequest p : d.signataires() == null ? List.<ClmDto.ParticipantRequest>of() : d.signataires()) {
            signataires.add(trousses.ajouterParticipant(id, p, acteur).id());
        }
        if (trousses.detail(id, acteur).etapes().isEmpty() && !signataires.isEmpty()) {
            trousses.definirParcours(id, List.of(signatureParallele(signataires)), acteur);
        }
        if (lancer) {
            trousses.lancer(id, acteur);
        }
        return depot.findByIdAndTenantId(id, ctx.tenant()).orElseThrow();
    }

    /** Documents joints s'il y en a ; sinon contrat généré depuis le modèle ; sans l'un ni l'autre, rien à signer. */
    private void deposer(Long id, ClmDto.DemandeTrousse d, Habilitations.Acteur acteur) {
        List<ClmDto.DocumentJoint> joints = d.documents() == null ? List.of() : d.documents();
        for (ClmDto.DocumentJoint j : joints) {
            byte[] contenu;
            try {
                contenu = Base64.getDecoder().decode(j.contenuBase64() == null ? "" : j.contenuBase64());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Document « " + j.nom() + " » : contenu base64 illisible.", e);
            }
            documents.televerser(id, j.nom(), contenu, acteur);
        }
        if (joints.isEmpty() && d.modeleId() != null) {
            documents.generer(id, acteur, qc.levis.contrat.clm.domain.FormatDocument.Type.PDF);
        }
    }

    /** Sans parcours du modèle : une seule étape de signature, tous les signataires en parallèle, tous requis. */
    private static ClmDto.EtapeRequest signatureParallele(List<Long> participants) {
        List<ClmDto.AffectationDto> affectations = participants.stream()
                .map(p -> new ClmDto.AffectationDto("PARTICIPANT", String.valueOf(p))).toList();
        return new ClmDto.EtapeRequest("signature", "Signature", 1, "SIGNATURE", affectations, participants.size(), null, null, null);
    }

    private static String titre(ClmDto.DemandeTrousse d) {
        if (d.titre() != null && !d.titre().isBlank()) {
            return d.titre();
        }
        if (d.sfDossierId() != null && !d.sfDossierId().isBlank()) {
            return "Dossier " + d.sfDossierId().strip();
        }
        if (d.referenceExterne() != null && !d.referenceExterne().isBlank()) {
            return d.referenceExterne().strip();
        }
        throw new IllegalArgumentException("Titre, dossier Salesforce ou référence externe requis.");
    }

    private static Trousse.Source origine(ClientIntegration client) {
        if (client == null) {
            return Trousse.Source.MANUELLE;
        }
        return switch (client.getSysteme()) {
            case SALESFORCE -> Trousse.Source.SALESFORCE;
            case WORKDAY -> Trousse.Source.WORKDAY;
            // Un client SIEM ne crée aucune trousse (refusé par l'API) ; le cas complète seulement l'énumération.
            case AUTRE, SIEM -> Trousse.Source.INTEGRATION;
        };
    }

    /** L'initiateur désigné par le système externe doit être un compte actif de l'organisation. */
    private Habilitations.Acteur initiateur(String courriel) {
        if (courriel == null || courriel.isBlank()) {
            throw new IllegalArgumentException("Initiateur requis : courriel d'un compte de l'organisation.");
        }
        if (!annuaire.estMembre(ctx.tenant(), courriel.strip())) {
            throw new AccessDeniedException("L'initiateur n'est pas un compte actif de l'organisation.");
        }
        return new Habilitations.Acteur(courriel.strip(), Set.of("REDACTEUR"), Set.of());
    }

    private ClmDto.TrousseExterneVue vue(Trousse t) {
        return new ClmDto.TrousseExterneVue(t.getId(), t.getReference(), t.getStatut().name(), t.getSource().name(),
                t.getReferenceExterne(), t.getSfDossierId(), t.getLotId(), documents.documentsDe(t));
    }

    private ClmDto.LotVue vue(LotSignature l) {
        String origine = l.getClientIntegrationId() == null ? l.getCreePar()
                : clients.findById(l.getClientIntegrationId()).map(ClientIntegration::getNom).orElse("—");
        List<ClmDto.ElementLotVue> liste = elements.findByTenantIdAndLotIdOrderByRangAsc(l.getTenantId(), l.getId()).stream()
                .map(e -> new ClmDto.ElementLotVue(e.getRang(), e.getReferenceExterne(), e.getTrousseId(),
                        e.getTrousseId() == null ? null : depot.findById(e.getTrousseId()).map(Trousse::getReference).orElse(null),
                        e.getStatut().name(), e.getErreur()))
                .toList();
        return new ClmDto.LotVue(l.getId(), l.getLibelle(), l.getModeleId(), origine, l.getStatut().name(), l.getTotal(),
                l.getReussis(), l.getEchecs(), l.isLancer(), l.getCreePar(), l.getCreeLe(), liste);
    }
}
