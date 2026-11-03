package qc.levis.contrat.clm.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.application.repository.AnalyseIaRepository;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.domain.AnalyseContrat;
import qc.levis.contrat.clm.domain.AnalyseIa;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Preremplissage;
import qc.levis.contrat.clm.domain.Trousse;

/** Option CLM/IA (ST-15, UI-24) : analyse proposée, jamais appliquée sans validation humaine justifiée. */
@Service
public class AnalyseService {

    private final AnalyseIaRepository analyses;
    private final DocumentTrousseRepository documents;
    private final RenduDocuments rendu;
    private final TrousseService trousses;
    private final LectureTrousse lecture;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public AnalyseService(AnalyseIaRepository analyses, DocumentTrousseRepository documents, RenduDocuments rendu,
                          TrousseService trousses, LectureTrousse lecture, JournalClm journal, ContexteClm ctx) {
        this.analyses = analyses;
        this.documents = documents;
        this.rendu = rendu;
        this.trousses = trousses;
        this.lecture = lecture;
        this.journal = journal;
        this.ctx = ctx;
    }

    @Transactional
    public ClmDto.AnalyseVue lancer(Long trousseId, Long documentId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        DocumentTrousse d = documents.findByIdAndTenantId(documentId, t.getTenantId()).filter(x -> x.getTrousseId().equals(t.getId()))
                .orElseThrow(() -> new NoSuchElementException("Document introuvable : " + documentId));
        if (d.getAnalyse() != DocumentTrousse.Analyse.PROPRE) {
            throw new IllegalStateException("Données non admissibles : document non analysé ou infecté.");
        }
        String texte = rendu.texte(d.getType(), d.getContenu());
        AnalyseContrat.Resultat r = AnalyseContrat.analyser(texte, montantAttendu(t));
        AnalyseIa a = analyses.save(new AnalyseIa(t.getTenantId(), t.getId(), d.getId(), ctx.ecrire(r), acteur.id(), ctx.maintenant()));
        journal.consigner(t, "ANALYSE_IA", acteur.id(), d.getNom() + " · " + r.categorie() + " · " + r.anomalies().size() + " anomalie(s)");
        return vue(a);
    }

    private BigDecimal montantAttendu(Trousse t) {
        Preremplissage.Valeur v = trousses.valeurs(t).get("montant");
        if (v == null || v.valeur() == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(v.valeur()).replace(" ", "").replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<ClmDto.AnalyseVue> lister(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        return analyses.findByTenantIdAndTrousseIdOrderByCreeLeDesc(t.getTenantId(), t.getId()).stream().map(this::vue).toList();
    }

    /** Analyses récentes du tenant, limitées aux trousses visibles par l'acteur (UI-24). */
    public List<ClmDto.AnalyseVue> recentes(Habilitations.Acteur acteur) {
        java.util.Map<Long, Boolean> visibles = new java.util.HashMap<>();
        return analyses.findTop100ByTenantIdOrderByCreeLeDesc(ctx.tenant()).stream()
                .filter(a -> visibles.computeIfAbsent(a.getTrousseId(), id -> lecture.visible(lecture.exiger(id), acteur)))
                .map(this::vue).toList();
    }

    @Transactional
    public ClmDto.AnalyseVue reviser(Long analyseId, ClmDto.RevisionRequest req, Habilitations.Acteur acteur) {
        AnalyseIa a = analyses.findByIdAndTenantId(analyseId, ctx.tenant())
                .orElseThrow(() -> new NoSuchElementException("Analyse introuvable : " + analyseId));
        Trousse t = lecture.exigerVisible(a.getTrousseId(), acteur);
        a.reviser(req.valide(), acteur.id(), req.justification(), ctx.maintenant());
        analyses.save(a);
        journal.consigner(t, req.valide() ? "ANALYSE_VALIDEE" : "ANALYSE_REJETEE", acteur.id(), req.justification());
        return vue(a);
    }

    private ClmDto.AnalyseVue vue(AnalyseIa a) {
        Map<String, Object> resultat = ctx.lireObjet(a.getResultatJson());
        return new ClmDto.AnalyseVue(a.getId(), a.getTrousseId(), a.getDocumentId(), a.getStatut().name(), resultat, a.getLanceePar(),
                a.getReviseePar(), a.getJustification(), a.getCreeLe(), a.getReviseeLe());
    }
}
