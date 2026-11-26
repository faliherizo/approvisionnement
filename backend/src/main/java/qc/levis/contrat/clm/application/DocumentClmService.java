package qc.levis.contrat.clm.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AnalyseAntivirus;
import qc.levis.contrat.clm.application.port.ComptagePages;
import qc.levis.contrat.clm.application.port.ConversionPdf;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.GabaritWordRepository;
import qc.levis.contrat.clm.application.repository.ModeleContratRepository;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.GabaritWord;
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

    private static final Logger log = LoggerFactory.getLogger(DocumentClmService.class);

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
    private final ParcoursService parcours;
    private final ContexteClm ctx;
    private final GabaritWordRepository gabarits;
    private final ConversionPdf conversion;

    private final EtiquetagePurviewService purview;
    private final ComptagePages comptagePages;

    public DocumentClmService(DocumentTrousseRepository documents, ModeleContratRepository modeles, AnalyseAntivirus antivirus,
                              RenduDocuments rendu, SourceAleatoire alea, TrousseService trousses, LectureTrousse lecture,
                              JournalClm journal, ParametresClmService parametres, ParcoursService parcours, ContexteClm ctx,
                              GabaritWordRepository gabarits, ConversionPdf conversion, EtiquetagePurviewService purview,
                              ComptagePages comptagePages) {
        this.documents = documents;
        this.comptagePages = comptagePages;
        this.purview = purview;
        this.gabarits = gabarits;
        this.conversion = conversion;
        this.modeles = modeles;
        this.antivirus = antivirus;
        this.rendu = rendu;
        this.alea = alea;
        this.trousses = trousses;
        this.lecture = lecture;
        this.journal = journal;
        this.parametres = parametres;
        this.parcours = parcours;
        this.ctx = ctx;
    }

    @Transactional
    public ClmDto.DocumentVue televerser(Long trousseId, String nomFichier, byte[] contenu, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(trousseId, acteur);
        t.exigerOuverte();
        ClmDto.DocumentVue vue = enregistrer(t, alea.uuid(), 1, nomFichier, contenu, acteur.id(), "Téléversement initial");
        parcours.nouveauCycle(t, acteur.id(), "Document ajouté en cours de parcours : " + vue.nom());
        return vue;
    }

    @Transactional
    public ClmDto.DocumentVue nouvelleVersion(Long trousseId, String cleDocument, String nomFichier, byte[] contenu, String motif,
                                              Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(trousseId, acteur);
        t.exigerOuverte();
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
        parcours.nouveauCycle(t, acteur.id(), "Nouvelle version de « " + vue.nom() + " » : " + motif.trim());
        return vue;
    }

    private ClmDto.DocumentVue enregistrer(Trousse t, String cle, int version, String nomFichier, byte[] contenu, String auteur, String motif) {
        FormatDocument.Type type = FormatDocument.verifier(nomFichier, contenu, parametres.reglages(t.getTenantId()).tailleMaxOctets());
        DocumentTrousse d = new DocumentTrousse(t.getTenantId(), t.getId(), cle, version, FormatDocument.nomSur(nomFichier), type,
                contenu, auteur, motif, ctx.maintenant());
        d.setAnalyse(antivirus.estSain(contenu) ? DocumentTrousse.Analyse.PROPRE : DocumentTrousse.Analyse.INFECTE);
        comptagePages.pages(type, contenu).ifPresent(d::paginer);
        purview.etiqueter(t, d);
        if (t.enTraitement()) {
            // En cours de parcours, le document part aussitôt aux intervenants : un fichier non sain est refusé d'emblée.
            if (d.getAnalyse() != DocumentTrousse.Analyse.PROPRE) {
                throw new IllegalStateException("Analyse antivirus en échec : document refusé en cours de parcours.");
            }
            d.setStatut(DocumentTrousse.Statut.ENVOYE);
        }
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
        t.exigerOuverte();
        DocumentTrousse d = exiger(t, documentId);
        d.exigerNonVerrouille();
        if (t.enTraitement() && trousses.documentsCourants(t).size() <= 1) {
            throw new IllegalStateException("Une trousse en cours de parcours garde au moins un document.");
        }
        d.setStatut(DocumentTrousse.Statut.RETIRE);
        documents.save(d);
        journal.consigner(t, "DOCUMENT_RETIRE", acteur.id(), d.getNom() + " v" + d.getVersion());
        parcours.nouveauCycle(t, acteur.id(), "Document retiré en cours de parcours : " + d.getNom());
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

    /**
     * Aperçu fidèle : un PDF tel quel ; un Word converti en PDF, mise en page conservée. Sans convertisseur, conflit (409)
     * et l'interface retombe sur le texte extrait ({@link #apercuTexte}).
     */
    public byte[] apercuPdf(Long trousseId, Long documentId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        DocumentTrousse d = exiger(t, documentId);
        if (d.getType() == FormatDocument.Type.PDF) {
            return d.getContenu();
        }
        if (!conversion.disponible()) {
            throw new IllegalStateException("Aperçu Word indisponible : convertisseur PDF hors service ; le texte du document reste lisible.");
        }
        return conversion.versPdf(d.getContenu());
    }

    public String apercuTexte(Long trousseId, Long documentId, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerVisible(trousseId, acteur);
        DocumentTrousse d = exiger(t, documentId);
        return rendu.texte(d.getType(), d.getContenu());
    }

    /**
     * Génère le contrat depuis le modèle publié et les valeurs préremplies ; remplace la génération précédente. En Word
     * (addenda 3, Q26), le document reste modifiable jusqu'à l'approbation finale : il se corrige puis se redépose comme
     * nouvelle version, ce qui ouvre un nouveau cycle si le parcours est engagé.
     */
    @Transactional
    public ClmDto.DocumentVue generer(Long trousseId, Habilitations.Acteur acteur, FormatDocument.Type format) {
        Trousse t = lecture.exigerGestion(trousseId, acteur);
        t.exigerOuverte();
        if (t.getModeleId() == null) {
            throw new IllegalStateException("Aucun modèle associé à la trousse.");
        }
        ModeleContrat m = modeles.findByIdAndTenantId(t.getModeleId(), t.getTenantId())
                .orElseThrow(() -> new NoSuchElementException("Modèle introuvable : " + t.getModeleId()));
        Map<String, Object> valeurs = new HashMap<>();
        trousses.valeurs(t).forEach((k, v) -> valeurs.put(k, v.valeur()));
        boolean word = format == FormatDocument.Type.DOCX;
        Production production = gabarits.findByModeleIdAndTenantId(m.getId(), m.getTenantId())
                .map(g -> depuisGabarit(g, m, valeurs, word, t.getTitre()))
                .orElseGet(() -> depuisTexte(assembler(m, valeurs, ctx).texte(), word, t.getTitre()));
        byte[] contenu = production.contenu();
        List<DocumentTrousse> precedentes = documents.findByTenantIdAndTrousseIdAndCleDocumentOrderByVersionDesc(t.getTenantId(), t.getId(), CLE_GENERE);
        int version = precedentes.isEmpty() ? 1 : precedentes.get(0).getVersion() + 1;
        precedentes.stream().filter(DocumentTrousse::estCourant).forEach(p -> {
            p.setStatut(DocumentTrousse.Statut.REMPLACE);
            documents.save(p);
        });
        ClmDto.DocumentVue vue = enregistrer(t, CLE_GENERE, version, t.getReference() + (word ? ".docx" : ".pdf"), contenu, acteur.id(),
                "Généré depuis « " + m.getNom() + " » v" + m.getNumero() + production.precision());
        parcours.nouveauCycle(t, acteur.id(), "Contrat régénéré en cours de parcours (v" + version + ")");
        return vue;
    }

    /** Document produit et précision portée au motif de version (gabarit Word, rendu simplifié). */
    private record Production(byte[] contenu, String precision) {
    }

    private Production depuisTexte(String texte, boolean word, String titre) {
        return new Production(word ? rendu.genererDocx(titre, texte) : rendu.genererPdf(titre, texte), "");
    }

    /**
     * Gabarit Word de l'organisation (addenda 4, Q22) : la mise en page d'origine est conservée en Word et, en PDF, par
     * le convertisseur du serveur. Sans convertisseur, le PDF reprend le texte fusionné et le motif de version le dit.
     */
    private Production depuisGabarit(GabaritWord g, ModeleContrat m, Map<String, Object> valeurs, boolean word, String titre) {
        Evaluation e = evaluer(m, valeurs, ctx);
        List<RenduDocuments.ClauseFusionnee> clauses = e.clauses().stream().filter(c -> !e.clausesExclues().contains(c.cle()))
                .map(c -> new RenduDocuments.ClauseFusionnee(c.titre(), Modele.fusionner(c.texte(), e.visibles()))).toList();
        byte[] docx = rendu.fusionnerDocx(g.getContenu(), e.visibles(), clauses);
        if (word) {
            return new Production(docx, " — gabarit Word « " + g.getNom() + " »");
        }
        if (conversion.disponible()) {
            return new Production(conversion.versPdf(docx), " — gabarit Word « " + g.getNom() + " », mise en page conservée");
        }
        log.warn("Convertisseur Word → PDF indisponible : PDF en mise en page simplifiée pour le modèle {}", m.getId());
        return new Production(rendu.genererPdf(titre, rendu.texte(FormatDocument.Type.DOCX, docx)),
                " — mise en page simplifiée : convertisseur PDF indisponible, télécharger la version Word pour la mise en page d'origine");
    }

    /** Clauses et champs après évaluation des conditions de visibilité ; un champ masqué vaut chaîne vide. */
    record Evaluation(List<Modele.Clause> clauses, Set<String> clausesExclues, List<String> champsMasques, Map<String, Object> visibles) {
    }

    static Evaluation evaluer(ModeleContrat m, Map<String, Object> valeurs, ContexteClm ctx) {
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
        return new Evaluation(clauses, clausesExclues, champsMasques, visibles);
    }

    /** Clauses conditionnelles évaluées par le moteur de règles partagé (VISIBILITE), puis assemblage du texte. */
    static ClmDto.ApercuModele assembler(ModeleContrat m, Map<String, Object> valeurs, ContexteClm ctx) {
        Evaluation e = evaluer(m, valeurs, ctx);
        String texte = Modele.assembler(m.getCorps(), e.clauses(), e.visibles(), e.clausesExclues());
        List<String> retenues = e.clauses().stream().map(Modele.Clause::cle).filter(c -> !e.clausesExclues().contains(c)).toList();
        return new ClmDto.ApercuModele(texte, retenues, e.champsMasques(), List.of());
    }

    private DocumentTrousse exiger(Trousse t, Long documentId) {
        return documents.findByIdAndTenantId(documentId, t.getTenantId()).filter(d -> d.getTrousseId().equals(t.getId()))
                .orElseThrow(() -> new NoSuchElementException("Document introuvable : " + documentId));
    }
}
