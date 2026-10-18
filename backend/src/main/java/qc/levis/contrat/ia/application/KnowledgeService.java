package qc.levis.contrat.ia.application;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.ia.application.repository.KnowledgeRepository;
import qc.levis.contrat.ia.domain.KnowledgeSnippet;

/**
 * Base de connaissances (RAG-lite) spécialisant l'IA en rédaction contractuelle.
 * Récupération par recouvrement de mots-clés (sans dépendance d'embeddings).
 */
@Service
public class KnowledgeService {

    private final KnowledgeRepository repo;
    private final AuditService audit;

    public KnowledgeService(KnowledgeRepository repo, AuditService audit) {
        this.repo = repo;
        this.audit = audit;
    }

    public List<KnowledgeSnippet> lister() {
        return repo.findByTenantId(TenantContext.get());
    }

    @Transactional
    public KnowledgeSnippet ajouter(String titre, String source, String contenu, String utilisateur) {
        KnowledgeSnippet s = new KnowledgeSnippet();
        s.setTitre(titre);
        s.setSource(source);
        s.setContenu(contenu);
        s.setTenantId(TenantContext.get());
        KnowledgeSnippet saved = repo.save(s);
        audit.log(utilisateur, "AJOUT_CONNAISSANCE", "KnowledgeSnippet", saved.getId(), titre);
        return saved;
    }

    @Transactional
    public void supprimer(Long id) {
        repo.findById(id)
                .filter(s -> Objects.equals(s.getTenantId(), TenantContext.get()))
                .ifPresent(repo::delete);
    }

    /** Base RAG : socle de connaissances global (référentiel SCT) + fragments du tenant. */
    private List<KnowledgeSnippet> baseRag() {
        List<KnowledgeSnippet> res = new ArrayList<>(repo.findByTenantIdIsNull());
        Long tenant = TenantContext.get();
        if (tenant != null) res.addAll(repo.findByTenantId(tenant));
        return res;
    }

    /** Contexte RAG : les {@code k} fragments les plus pertinents pour la question. */
    public String contextePour(String question, int k) {
        Set<String> termes = motsCles(question);
        return baseRag().stream()
                .map(s -> Map.entry(s, score(s, termes)))
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.<KnowledgeSnippet, Integer>comparingByValue().reversed())
                .limit(k)
                .map(e -> "• " + e.getKey().getTitre()
                        + (e.getKey().getSource() != null ? " (" + e.getKey().getSource() + ")" : "")
                        + " : " + sansHtml(e.getKey().getContenu()))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
    }

    /** Retire les balises HTML (contenu WYSIWYG) pour un prompt IA propre. */
    private String sansHtml(String s) {
        return s == null ? "" : s.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private int score(KnowledgeSnippet s, Set<String> termes) {
        String texte = (s.getTitre() + " " + s.getContenu()).toLowerCase(Locale.FRENCH);
        int score = 0;
        for (String t : termes) {
            if (texte.contains(t)) score++;
        }
        return score;
    }

    private Set<String> motsCles(String q) {
        Set<String> mots = new HashSet<>();
        for (String m : (q == null ? "" : q).toLowerCase(Locale.FRENCH).split("[^a-zàâäéèêëïîôöùûüç0-9]+")) {
            if (m.length() >= 4) mots.add(m); // ignore les mots courts (stop-words simplifiés)
        }
        return mots;
    }
}
