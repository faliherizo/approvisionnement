package qc.levis.contrat.redaction.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.application.DiffUtil;
import qc.levis.contrat.redaction.application.dto.ClauseSnapshot;
import qc.levis.contrat.redaction.application.repository.DocumentVersionRepository;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.DocumentVersion;

/** Versionnage de documents + comparaison (redlining) — écart CLM E-4. */
@Service
public class DocumentVersionService {

    /** Résultat de comparaison d'une clause entre deux versions. */
    public record DiffClause(String code, String titre, String changement,
                             List<DiffUtil.Ligne> lignes) {}

    private final DocumentVersionRepository versions;
    private final DocumentService documents;
    private final AuditService audit;
    private final ObjectMapper mapper = new ObjectMapper();

    public DocumentVersionService(DocumentVersionRepository versions, DocumentService documents,
                                  AuditService audit) {
        this.versions = versions;
        this.documents = documents;
        this.audit = audit;
    }

    public List<DocumentVersion> lister(Long documentId) {
        documents.get(documentId);
        return versions.findByDocumentIdOrderByNumeroVersionAsc(documentId);
    }

    /** Crée un instantané versionné de l'état courant du document. */
    @Transactional
    public DocumentVersion capturer(Long documentId, String note, String utilisateur) {
        Document doc = documents.get(documentId);
        List<ClauseSnapshot> snap = doc.getClauses().stream()
                .map(dc -> new ClauseSnapshot(dc.getCode(), dc.getTitre(),
                        dc.getTexteEffectif(), dc.getOrdre()))
                .toList();
        int numero = versions.findByDocumentIdOrderByNumeroVersionAsc(documentId).size() + 1;
        DocumentVersion v = new DocumentVersion();
        v.setDocumentId(documentId);
        v.setNumeroVersion(numero);
        v.setNote(note);
        v.setCreePar(utilisateur);
        v.setContenuJson(serialiser(snap));
        DocumentVersion saved = versions.save(v);
        audit.log(utilisateur, "CAPTURER_VERSION", "Document", documentId, "v" + numero);
        return saved;
    }

    /** Compare deux versions clause par clause (redlining). */
    public List<DiffClause> comparer(Long versionAId, Long versionBId) {
        Map<String, ClauseSnapshot> a = indexer(charger(versionAId));
        Map<String, ClauseSnapshot> b = indexer(charger(versionBId));

        List<DiffClause> res = new ArrayList<>();
        Set<String> codes = new LinkedHashSet<>();
        codes.addAll(a.keySet());
        codes.addAll(b.keySet());

        for (String code : codes) {
            ClauseSnapshot av = a.get(code);
            ClauseSnapshot bv = b.get(code);
            if (av != null && bv == null) {
                res.add(new DiffClause(code, av.titre(), "SUPPRIMEE",
                        DiffUtil.diff(av.texteEffectif(), "")));
            } else if (av == null) {
                res.add(new DiffClause(code, bv.titre(), "AJOUTEE",
                        DiffUtil.diff("", bv.texteEffectif())));
            } else {
                boolean identique = Objects.equals(av.texteEffectif(), bv.texteEffectif());
                res.add(new DiffClause(code, bv.titre(),
                        identique ? "INCHANGEE" : "MODIFIEE",
                        DiffUtil.diff(av.texteEffectif(), bv.texteEffectif())));
            }
        }
        return res;
    }

    private List<ClauseSnapshot> charger(Long versionId) {
        DocumentVersion v = versions.findById(versionId)
                .orElseThrow(() -> new NoSuchElementException("Version introuvable: " + versionId));
        return deserialiser(v.getContenuJson());
    }

    private Map<String, ClauseSnapshot> indexer(List<ClauseSnapshot> liste) {
        Map<String, ClauseSnapshot> m = new LinkedHashMap<>();
        for (ClauseSnapshot s : liste) m.put(s.code(), s);
        return m;
    }

    private String serialiser(List<ClauseSnapshot> snap) {
        try {
            return mapper.writeValueAsString(snap);
        } catch (Exception e) {
            throw new IllegalStateException("Sérialisation de version impossible", e);
        }
    }

    private List<ClauseSnapshot> deserialiser(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return mapper.readValue(json, new TypeReference<List<ClauseSnapshot>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Lecture de version impossible", e);
        }
    }
}
