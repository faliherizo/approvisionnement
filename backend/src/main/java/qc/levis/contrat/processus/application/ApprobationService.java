package qc.levis.contrat.processus.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.processus.application.dto.*;
import qc.levis.contrat.processus.application.repository.DemandeApprobationRepository;
import qc.levis.contrat.processus.application.repository.EtapeApprobationRepository;
import qc.levis.contrat.processus.application.repository.RegleApprobationRepository;
import qc.levis.contrat.processus.domain.Approbations;
import qc.levis.contrat.processus.domain.DemandeApprobation;
import qc.levis.contrat.processus.domain.EtapeApprobation;
import qc.levis.contrat.processus.domain.RegleApprobation;

/**
 * Gestionnaire d'approbations multi-niveaux : s'appuie sur le moteur pur {@link Approbations} (porté de
 * Nexu VINA) pour sélectionner, selon des règles configurables (champ/opérateur/valeur/niveau), les étapes
 * d'approbation d'une demande, puis pilote le statut global à chaque décision. Tenant-scopé.
 */
@Service
public class ApprobationService {

    private final RegleApprobationRepository regles;
    private final DemandeApprobationRepository demandes;
    private final EtapeApprobationRepository etapes;
    private final ObjectMapper mapper = new ObjectMapper();

    public ApprobationService(RegleApprobationRepository regles, DemandeApprobationRepository demandes,
                              EtapeApprobationRepository etapes) {
        this.regles = regles;
        this.demandes = demandes;
        this.etapes = etapes;
    }

    // --- Règles ---
    public List<RegleApprobation> lesRegles(String documentType) {
        return documentType == null || documentType.isBlank()
                ? regles.findByTenantIdOrderByDocumentTypeAscNiveauAsc(tenant())
                : regles.findByTenantIdAndDocumentType(tenant(), documentType);
    }

    @Transactional
    public RegleApprobation enregistrerRegle(RegleApproRequest r) {
        Approbations.validerRegle(r.documentType(), r.champ(), r.operateur(), r.valeur(), r.niveau(), r.approbateurRole(), r.approbateurId());
        return regles.save(new RegleApprobation(tenant(), r.documentType().trim(), r.champ().trim(),
                r.operateur(), r.valeur(), r.niveau(), vide(r.approbateurRole()), vide(r.approbateurId())));
    }

    // --- Demandes ---
    @Transactional
    public DemandeApproVue creerDemande(DemandeApproRequest req, String demandeur) {
        Map<String, Object> contexte = req.contexte() == null ? Map.of() : req.contexte();
        List<Approbations.Regle> reglesMoteur = new ArrayList<>();
        for (RegleApprobation r : regles.findByTenantIdAndDocumentType(tenant(), req.documentType())) {
            reglesMoteur.add(new Approbations.Regle(String.valueOf(r.getId()), r.getDocumentType(), r.getChamp(),
                    r.getOperateur(), r.getValeur(), r.getNiveau(), r.getApprobateurRole(), r.getApprobateurId()));
        }
        List<Approbations.Etape> etapesMoteur = Approbations.selectionner(reglesMoteur, contexte);

        DemandeApprobation d = demandes.save(new DemandeApprobation(tenant(), req.documentType(), req.documentId(),
                ecrire(contexte), demandeur));
        for (Approbations.Etape e : etapesMoteur) {
            etapes.save(new EtapeApprobation(tenant(), d.getId(), e.level(), e.approverRole(), e.approverId(), e.ruleId()));
        }
        if (etapesMoteur.isEmpty()) {       // aucune règle applicable → approuvée d'emblée
            d.setStatut("APPROVED");
            demandes.save(d);
        }
        return vue(d);
    }

    public DemandeApproVue demande(Long id) {
        return vue(exiger(id));
    }

    public List<DemandeApproVue> toutesLesDemandes() {
        List<DemandeApproVue> res = new ArrayList<>();
        for (DemandeApprobation d : demandes.findByTenantIdOrderByCreeLeDesc(tenant())) res.add(vue(d));
        return res;
    }

    /** Étapes en attente assignées à l'un des rôles de l'utilisateur (ou nominativement à lui). */
    public List<EtapeApproVue> mesEtapes(List<String> roles, String user) {
        List<EtapeApproVue> res = new ArrayList<>();
        if (roles != null && !roles.isEmpty()) {
            for (EtapeApprobation e : etapes.findByTenantIdAndStatutAndApprobateurRoleIn(tenant(), "PENDING", roles)) {
                res.add(etapeVue(e));
            }
        }
        return res;
    }

    @Transactional
    public DemandeApproVue decider(Long etapeId, List<String> roles, String user, DecisionApproRequest req) {
        EtapeApprobation e = etapes.findByIdAndTenantId(etapeId, tenant())
                .orElseThrow(() -> new NoSuchElementException("Étape introuvable : " + etapeId));
        if (!"PENDING".equals(e.getStatut()))
            throw new IllegalStateException("Étape déjà décidée.");
        boolean parRole = e.getApprobateurRole() != null && roles != null && roles.contains(e.getApprobateurRole());
        boolean parId = e.getApprobateurId() != null && e.getApprobateurId().equals(user);
        if (!parRole && !parId)
            throw new IllegalStateException("Cette étape ne vous est pas assignée.");
        boolean approuve = "APPROUVE".equalsIgnoreCase(req.decision());
        e.setStatut(approuve ? "APPROVED" : "REJECTED");
        e.setDecidePar(user);
        e.setCommentaire(req.commentaire());
        e.setDecideLe(Instant.now());
        etapes.save(e);
        recalculerStatut(e.getDemandeId());
        return vue(exiger(e.getDemandeId()));
    }

    // --- interne ---
    private void recalculerStatut(Long demandeId) {
        List<EtapeApprobation> liste = etapes.findByTenantIdAndDemandeIdOrderByNiveauAsc(tenant(), demandeId);
        DemandeApprobation d = exiger(demandeId);
        if (liste.stream().anyMatch(x -> "REJECTED".equals(x.getStatut()))) d.setStatut("REJECTED");
        else if (liste.stream().allMatch(x -> "APPROVED".equals(x.getStatut()))) d.setStatut("APPROVED");
        else d.setStatut("PENDING");
        demandes.save(d);
    }

    private DemandeApprobation exiger(Long id) {
        return demandes.findByIdAndTenantId(id, tenant())
                .orElseThrow(() -> new NoSuchElementException("Demande introuvable : " + id));
    }

    private DemandeApproVue vue(DemandeApprobation d) {
        List<EtapeApproVue> es = new ArrayList<>();
        for (EtapeApprobation e : etapes.findByTenantIdAndDemandeIdOrderByNiveauAsc(tenant(), d.getId())) es.add(etapeVue(e, d));
        return new DemandeApproVue(d.getId(), d.getDocumentType(), d.getDocumentId(), d.getDemandeur(), d.getStatut(), d.getCreeLe(), es);
    }

    private EtapeApproVue etapeVue(EtapeApprobation e) {
        return etapeVue(e, demandes.findByIdAndTenantId(e.getDemandeId(), tenant()).orElse(null));
    }

    private EtapeApproVue etapeVue(EtapeApprobation e, DemandeApprobation d) {
        return new EtapeApproVue(e.getId(), e.getDemandeId(), d != null ? d.getDocumentType() : null,
                d != null ? d.getDocumentId() : null, e.getNiveau(), e.getApprobateurRole(), e.getApprobateurId(),
                e.getStatut(), e.getDecidePar(), e.getCommentaire(), e.getDecideLe());
    }

    private String ecrire(Map<String, Object> m) {
        try { return mapper.writeValueAsString(m); } catch (Exception ex) { return "{}"; }
    }

    private String vide(String s) { return s == null || s.isBlank() ? null : s.trim(); }

    private Long tenant() {
        Long t = TenantContext.get();
        if (t == null) throw new IllegalStateException("Opération réservée à un espace tenant.");
        return t;
    }

    @SuppressWarnings("unused")
    private Map<String, Object> lire(String json) {
        try { return mapper.readValue(json, new TypeReference<Map<String, Object>>() {}); } catch (Exception e) { return Map.of(); }
    }
}
