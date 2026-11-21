package qc.levis.contrat.addenda.application;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.addenda.application.repository.AddendaRepository;
import qc.levis.contrat.addenda.domain.Addenda;
import qc.levis.contrat.addenda.domain.AddendaChange;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentClause;

/** Rédaction interactive d'addendas et réintégration au document (cf. BPMN). */
@Service
public class AddendaService {

    private final AddendaRepository addendas;
    private final DocumentRepository documents;
    /** Devis bornés à l'organisation courante (OWASP A01). */
    private final qc.levis.contrat.redaction.application.DocumentsDuTenant portee;
    private final AuditService audit;

    public AddendaService(AddendaRepository addendas, DocumentRepository documents, AuditService audit) {
        this.addendas = addendas;
        this.documents = documents;
        this.portee = new qc.levis.contrat.redaction.application.DocumentsDuTenant(documents);
        this.audit = audit;
    }

    public List<Addenda> lister(Long documentId) {
        portee.exiger(documentId);
        return addendas.findByDocumentParentIdOrderByNumeroAsc(documentId);
    }

    /** Un addenda d'un devis d'une autre organisation est introuvable. */
    public Addenda get(Long id) {
        Addenda a = addendas.findById(id).orElseThrow(() -> new NoSuchElementException("Addenda introuvable: " + id));
        portee.exiger(a.getDocumentParentId());
        return a;
    }

    @Transactional
    public Addenda creer(Long documentId, String objet, String utilisateur) {
        Document doc = portee.exiger(documentId);
        int prochainNumero = addendas.findByDocumentParentIdOrderByNumeroAsc(documentId).size() + 1;
        Addenda a = new Addenda();
        a.setDocumentParentId(doc.getId());
        a.setNumero(prochainNumero);
        a.setObjet(objet);
        a.setStatut(Enums.StatutDocument.BROUILLON);
        Addenda saved = addendas.save(a);
        audit.log(utilisateur, "CREER_ADDENDA", "Addenda", saved.getId(), "n°" + prochainNumero);
        return saved;
    }

    /** Ajoute un changement de clause : capture le texte actuel comme « ancien ». */
    @Transactional
    public Addenda ajouterChangement(Long addendaId, Long clauseId, String nouveauTexte, String utilisateur) {
        Addenda a = get(addendaId);
        Document doc = portee.exiger(a.getDocumentParentId());
        DocumentClause cible = doc.getClauses().stream()
                .filter(dc -> clauseId.equals(dc.getClauseId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Clause absente du document: " + clauseId));

        AddendaChange ch = new AddendaChange();
        ch.setClauseId(clauseId);
        ch.setCode(cible.getCode());
        ch.setAncienTexte(cible.getTexteEffectif());
        ch.setNouveauTexte(nouveauTexte);
        a.addChangement(ch);
        Addenda saved = addendas.save(a);
        audit.log(utilisateur, "ADDENDA_CHANGEMENT", "Addenda", addendaId, cible.getCode());
        return saved;
    }

    /**
     * Réintègre l'addenda : applique les nouveaux textes aux clauses du document,
     * marque l'addenda PUBLIE et replace le document en approbation (cf. BPMN).
     */
    @Transactional
    public Addenda reintegrer(Long addendaId, String utilisateur) {
        Addenda a = get(addendaId);
        Document doc = portee.exiger(a.getDocumentParentId());
        for (AddendaChange ch : a.getChangements()) {
            doc.getClauses().stream()
                    .filter(dc -> ch.getClauseId().equals(dc.getClauseId()))
                    .forEach(dc -> dc.setTexteEffectif(ch.getNouveauTexte()));
        }
        doc.setStatut(Enums.StatutDocument.EN_APPROBATION);
        documents.save(doc);

        a.setStatut(Enums.StatutDocument.PUBLIE);
        a.setEmisLe(Instant.now());
        Addenda saved = addendas.save(a);
        audit.log(utilisateur, "REINTEGRER_ADDENDA", "Addenda", addendaId,
                a.getChangements().size() + " changement(s)");
        return saved;
    }
}
