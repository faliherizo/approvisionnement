package qc.levis.contrat.clm.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AnalyseAntivirus;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.ModeleContratRepository;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.ModeleContrat;
import qc.levis.contrat.clm.domain.Trousse;
import qc.levis.contrat.regle.application.MoteurRegles;
import qc.levis.contrat.regle.application.ResultatEvaluation;
import qc.levis.contrat.regle.domain.RegleDef;

/** Documents d'une trousse (ST-04, RST-16, UI-05/UI-06) et génération depuis un modèle (ST-05/ST-06). */
@Service
public class DocumentClmService {

    public static final String CLE_GENERE = "contrat-genere";

    private final DocumentTrousseRepository documents;
    private final ModeleContratRepository modeles;
    private final AnalyseAntivirus antivirus;
    private final RenduDocuments rendu;
    private final SourceAleatoire alea;
    private final TrousseService trousses;
    private final LectureTrousse lecture;
    private final JournalClm journal;
    private final ParametresClmService parametres;
    private final ContexteClm ctx;

    public DocumentClmService(DocumentTrousseRepository documents, ModeleContratRepository modeles, AnalyseAntivirus antivirus,
                              RenduDocuments rendu, SourceAleatoire alea, TrousseService trousses, LectureTrousse lecture,
                              JournalClm journal, ParametresClmService parametres, ContexteClm ctx) {
        this.documents = documents;
        this.modeles = modeles;
        this.antivirus = antivirus;
        this.rendu = rendu;
        this.alea = alea;
        this.trousses = trousses;
        this.lecture = lecture;
        this.journal = journal;
        this.parametres = parametres;
        this.ctx = ctx;
    }

    @Transactional
    public ClmDto.DocumentVue televerser(Long trousseId, String nomFichier, byte[] contenu, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(trousseId, acteur);
        t.exigerModifiable();
        return enregistrer(t, alea.uuid(), 1, nomFichier, contenu, acteur.id(), "Téléversement initial");
    }

    @Transactional
    public ClmDto.DocumentVue nouvelleVersion(Long trousseId, String cleDocument, String nomFichier, byte[] contenu, String motif,
                                              Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(trousseId, acteur);
        t.exigerModifiable();
        if (motif == null || motif.isBlank()) {
            throw new IllegalArgumentException("Motif de la nouvelle version requis.");
        }
        List<DocumentTrousse> versions = documents.findByTenantIdAndTrousseIdAndCleDocumentOrderByVersionDesc(t.getTenantId(), t.getId(), cleDocument);
        DocumentTrousse courante = versions.stream().filter(DocumentTrousse::estCourant).findFirst()
                .orElseThrow(() -> new NoSuchElementException("Document introuvable : " + cleDocument));
        courante.exigerNonVerrouille();
        ClmDto.DocumentVue vue = enregistrer(t, cleDocument, versions.get(0).getVersion() + 1, nomFichier, contenu, acteur.id(), motif.trim());
        courante.setStatut(DocumentTrousse.Statut.REMPLACE);
        documents.save(courante);
        return vue;
    }

    private ClmDto.DocumentVue enregistrer(Trousse t, String cle, int version, String nomFichier, byte[] contenu, String auteur, String motif) {
        FormatDocument.Type type = FormatDocument.verifier(nomFichier, contenu, parametres.reglages(t.getTenantId()).tailleMaxOctets());
        DocumentTrousse d = new DocumentTrousse(t.getTenantId(), t.getId(), cle, version, FormatDocument.nomSur(nomFichier), type,
                contenu, auteur, motif, ctx.maintenant());
        d.setAnalyse(antivirus.estSain(contenu) ? DocumentTrousse.Analyse.PROPRE : DocumentTrousse.Analyse.INFECTE);
        d = documents.save(d);
        journal.consigner(t, version == 1 ? "DOCUMENT_AJOUTE" : "DOCUMENT_VERSION", auteur,
                d.getNom() + " v" + version + " · " + d.getEmpreinte().substring(0, 16) + " · " + d.getAnalyse());
        if (d.getAnalyse() == DocumentTrousse.Analyse.INFECTE) {
            journal.consigner(t, "DOCUMENT_INFECTE", "systeme", d.getNom());
        }
        return LectureTrousse.document(d);
    }

    @Transactional
    public void retirer(Long trousseId, Long documentId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(trousseId, acteur);
        t.exigerModifiable();
        DocumentTrousse d = exiger(t, documentId);
        d.exigerNonVerrouille();
        d.setStatut(DocumentTrousse.Statut.RETIRE);
        documents.save(d);
        journal.consigner(t, "DOCUMENT_RETIRE", acteur.id(), d.getNom() + " v" + d.getVersion());
    }

    @Transactional
    public DocumentTrousse telecharger(Long trousseId, Long documentId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        DocumentTrousse d = exiger(t, documentId);
        if (d.getAnalyse() == DocumentTrousse.Analyse.INFECTE) {
            throw new IllegalStateException("Document non accessible : analyse antivirus en échec.");
        }
        journal.consigner(t, "DOCUMENT_TELECHARGE", acteur.id(), d.getNom() + " v" + d.getVersion());
        return d;
    }

    /** Toutes les versions des documents d'une trousse, la plus récente d'abord pour chaque document. */
    public List<ClmDto.DocumentVue> documentsDe(Trousse t) {
        return documents.findByTenantIdAndTrousseIdOrderByCleDocumentAscVersionDesc(t.getTenantId(), t.getId()).stream()
                .map(LectureTrousse::document).toList();
    }

    public List<ClmDto.DocumentVue> versions(Long trousseId, String cleDocument, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        return documents.findByTenantIdAndTrousseIdAndCleDocumentOrderByVersionDesc(t.getTenantId(), t.getId(), cleDocument)
                .stream().map(LectureTrousse::document).toList();
    }

    public String apercuTexte(Long trousseId, Long documentId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        DocumentTrousse d = exiger(t, documentId);
        return rendu.texte(d.getType(), d.getContenu());
    }

    /** Génère le contrat depuis le modèle publié et les valeurs préremplies ; remplace la génération précédente. */
    @Transactional
    public ClmDto.DocumentVue generer(Long trousseId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(trousseId, acteur);
        t.exigerModifiable();
        if (t.getModeleId() == null) {
            throw new IllegalStateException("Aucun modèle associé à la trousse.");
        }
        ModeleContrat m = modeles.findByIdAndTenantId(t.getModeleId(), t.getTenantId())
                .orElseThrow(() -> new NoSuchElementException("Modèle introuvable : " + t.getModeleId()));
        Map<String, Object> valeurs = new HashMap<>();
        trousses.valeurs(t).forEach((k, v) -> valeurs.put(k, v.valeur()));
        String texte = assembler(m, valeurs, ctx).texte();
        byte[] pdf = rendu.genererPdf(t.getTitre(), texte);
        List<DocumentTrousse> precedentes = documents.findByTenantIdAndTrousseIdAndCleDocumentOrderByVersionDesc(t.getTenantId(), t.getId(), CLE_GENERE);
        int version = precedentes.isEmpty() ? 1 : precedentes.get(0).getVersion() + 1;
        precedentes.stream().filter(DocumentTrousse::estCourant).forEach(p -> {
            p.setStatut(DocumentTrousse.Statut.REMPLACE);
            documents.save(p);
        });
        return enregistrer(t, CLE_GENERE, version, t.getReference() + ".pdf", pdf, acteur.id(),
                "Généré depuis « " + m.getNom() + " » v" + m.getNumero());
    }

    /** Clauses conditionnelles évaluées par le moteur de règles partagé (VISIBILITE), puis assemblage du texte. */
    static ClmDto.ApercuModele assembler(ModeleContrat m, Map<String, Object> valeurs, ContexteClm ctx) {
        List<Modele.Champ> champs = ctx.lire(m.getChampsJson(), TrousseService.CHAMPS, List.of());
        List<Modele.Clause> clauses = ctx.lire(m.getClausesJson(), ModeleService.CLAUSES, List.of());
        List<RegleDef> regles = new ArrayList<>();
        for (Modele.Clause c : clauses) {
            regles.add(new RegleDef("VISIBILITE", c.combinateur(), ReglesClm.versMoteur(c.conditions()), "clause:" + c.cle(), null));
        }
        for (Modele.Champ c : champs) {
            regles.add(new RegleDef("VISIBILITE", "ET", ReglesClm.versMoteur(c.conditions()), "champ:" + c.cle(), null));
        }
        ResultatEvaluation r = MoteurRegles.evaluer(regles, valeurs);
        Set<String> clausesExclues = new HashSet<>();
        List<String> champsMasques = new ArrayList<>();
        for (String cible : r.champsMasques()) {
            if (cible.startsWith("clause:")) {
                clausesExclues.add(cible.substring(7));
            } else {
                champsMasques.add(cible.substring(6));
            }
        }
        Map<String, Object> visibles = new HashMap<>(valeurs);
        champsMasques.forEach(c -> visibles.put(c, ""));
        String texte = Modele.assembler(m.getCorps(), clauses, visibles, clausesExclues);
        List<String> retenues = clauses.stream().map(Modele.Clause::cle).filter(c -> !clausesExclues.contains(c)).toList();
        return new ClmDto.ApercuModele(texte, retenues, champsMasques, List.of());
    }

    private DocumentTrousse exiger(Trousse t, Long documentId) {
        return documents.findByIdAndTenantId(documentId, t.getTenantId()).filter(d -> d.getTrousseId().equals(t.getId()))
                .orElseThrow(() -> new NoSuchElementException("Document introuvable : " + documentId));
    }
}
