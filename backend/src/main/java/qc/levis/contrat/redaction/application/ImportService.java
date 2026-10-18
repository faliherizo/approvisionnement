package qc.levis.contrat.redaction.application;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.application.dto.ClauseRequest;
import qc.levis.contrat.redaction.application.dto.ImportRapport;
import qc.levis.contrat.redaction.application.dto.ImportWordRapport;
import qc.levis.contrat.redaction.application.dto.LigneErreur;
import qc.levis.contrat.redaction.application.dto.TemplateRequest;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.DocumentTemplate;
import qc.levis.contrat.redaction.infrastructure.WordTemplateParser.SectionWord;
import qc.levis.contrat.redaction.infrastructure.WordTemplateParser;

/**
 * Import / migration de contenus réutilisables (art. 22) : depuis un CSV (clauses) ou un
 * <strong>modèle Word (.docx)</strong> (exig. 3, 16 de l'addenda). Prend en charge une
 * <strong>migration d'essai</strong> (dry-run) qui valide sans persister, puis un import réel qui
 * réutilise {@link ClauseService} et {@link TemplateService} — aucune duplication de la création.
 *
 * <p>Format CSV attendu (en-tête obligatoire) : {@code code,titre,categorie,corps[,numero,section]}.
 */
@Service
public class ImportService {

    private final ClauseService clauses;
    private final TemplateService templates;
    private final WordTemplateParser wordParser;
    private final AuditService audit;

    public ImportService(ClauseService clauses, TemplateService templates,
                         WordTemplateParser wordParser, AuditService audit) {
        this.clauses = clauses;
        this.templates = templates;
        this.wordParser = wordParser;
        this.audit = audit;
    }

    // --- Import d'un modèle Word (.docx) → modèle + clauses (exig. 3, 16) ---

    /** Migration d'essai d'un modèle Word : détecte les sections, sans rien persister. */
    public ImportWordRapport analyserWord(byte[] docx) {
        List<SectionWord> sections = wordParser.extraire(docx);
        List<LigneErreur> erreurs = new ArrayList<>();
        if (sections.isEmpty()) {
            erreurs.add(new LigneErreur(0, "Aucune section détectée dans le document Word."));
        }
        return new ImportWordRapport(true, sections.size(), 0, null, null, erreurs);
    }

    /** Import réel : crée une clause par section puis un modèle qui les regroupe. */
    @Transactional
    public ImportWordRapport importerWord(byte[] docx, String nomModele,
                                               Enums.TypeContrat type, String utilisateur) {
        List<SectionWord> sections = wordParser.extraire(docx);
        if (sections.isEmpty()) {
            return new ImportWordRapport(false, 0, 0, null, null,
                    List.of(new LigneErreur(0, "Aucune section détectée dans le document Word.")));
        }
        String prefixe = "W-" + System.currentTimeMillis();
        List<String> clauseCodes = new ArrayList<>();
        int i = 0;
        for (SectionWord s : sections) {
            i++;
            String titre = (s.titre() != null && !s.titre().isBlank()) ? s.titre() : "Section " + i;
            String corps = (s.corps() != null && !s.corps().isBlank()) ? s.corps() : titre;
            String code = prefixe + "-" + i;
            clauses.creer(new ClauseRequest(code, titre, Enums.ClauseCategorie.PERSONNALISEE,
                    corps, null, null, false, List.of(), List.of(), List.of()));
            clauseCodes.add(code);
        }
        DocumentTemplate modele = templates.creer(new TemplateRequest(
                "MODELE-" + prefixe, nomModele,
                type != null ? type : Enums.TypeContrat.APPEL_OFFRES_PUBLIC,
                null, null, "Importé depuis un modèle Word", nomModele, null, null, clauseCodes, List.of()));
        audit.log(utilisateur, "IMPORT_WORD", "DocumentTemplate", modele.getId(),
                clauseCodes.size() + " clause(s) depuis « " + nomModele + " »");
        return new ImportWordRapport(false, sections.size(), clauseCodes.size(),
                modele.getId(), modele.getNom(), List.of());
    }

    /** Migration d'essai : valide chaque ligne et produit un rapport, sans rien persister. */
    public ImportRapport analyser(String csv) {
        return traiter(csv, true, null);
    }

    /** Import réel : persiste les lignes valides via {@link ClauseService}. */
    @Transactional
    public ImportRapport importer(String csv, String utilisateur) {
        ImportRapport rapport = traiter(csv, false, utilisateur);
        audit.log(utilisateur, "IMPORT_CLAUSES", "Clause", null,
                rapport.importes() + " importées / " + rapport.lignesLues() + " lues");
        return rapport;
    }

    private ImportRapport traiter(String csv, boolean essai, String utilisateur) {
        List<String[]> lignes = CsvParser.parser(csv);
        List<LigneErreur> erreurs = new ArrayList<>();
        int valides = 0;
        int importes = 0;
        int lues = 0;

        boolean premiere = true;
        int numero = 0;
        for (String[] champs : lignes) {
            numero++;
            if (premiere) { // ligne d'en-tête ignorée
                premiere = false;
                continue;
            }
            lues++;
            ClauseRequest req = versClause(champs, numero, erreurs);
            if (req == null) {
                continue;
            }
            valides++;
            if (!essai) {
                clauses.creer(req);
                importes++;
            }
        }
        return new ImportRapport(essai, lues, valides, importes, erreurs);
    }

    private ClauseRequest versClause(String[] champs, int ligne, List<LigneErreur> erreurs) {
        if (champs.length < 4) {
            erreurs.add(new LigneErreur(ligne, "Colonnes insuffisantes (code,titre,categorie,corps attendus)"));
            return null;
        }
        String code = champs[0].trim();
        String titre = champs[1].trim();
        String categorieTexte = champs[2].trim();
        String corps = champs[3].trim();
        String numeroClause = champs.length > 4 ? champs[4].trim() : null;
        String section = champs.length > 5 ? champs[5].trim() : null;

        if (code.isBlank() || titre.isBlank() || corps.isBlank()) {
            erreurs.add(new LigneErreur(ligne, "Champs obligatoires manquants (code, titre, corps)"));
            return null;
        }
        Enums.ClauseCategorie categorie;
        try {
            categorie = categorieTexte.isBlank()
                    ? Enums.ClauseCategorie.STANDARD
                    : Enums.ClauseCategorie.valueOf(categorieTexte.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            erreurs.add(new LigneErreur(ligne, "Catégorie invalide : " + categorieTexte));
            return null;
        }
        return new ClauseRequest(code, titre, categorie, corps, numeroClause, section,
                false, List.of(), List.of(), List.of());
    }

    /** Analyseur CSV minimal (RFC 4180) : champs entre guillemets, guillemets doublés, virgules internes. */
    static final class CsvParser {
        private CsvParser() {}

        static List<String[]> parser(String csv) {
            List<String[]> lignes = new ArrayList<>();
            if (csv == null || csv.isBlank()) {
                return lignes;
            }
            List<String> champs = new ArrayList<>();
            StringBuilder champ = new StringBuilder();
            boolean entreGuillemets = false;
            for (int i = 0; i < csv.length(); i++) {
                char c = csv.charAt(i);
                if (entreGuillemets) {
                    if (c == '"') {
                        if (i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                            champ.append('"');
                            i++;
                        } else {
                            entreGuillemets = false;
                        }
                    } else {
                        champ.append(c);
                    }
                } else if (c == '"') {
                    entreGuillemets = true;
                } else if (c == ',') {
                    champs.add(champ.toString());
                    champ.setLength(0);
                } else if (c == '\n' || c == '\r') {
                    if (c == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n') {
                        i++;
                    }
                    champs.add(champ.toString());
                    champ.setLength(0);
                    lignes.add(champs.toArray(new String[0]));
                    champs.clear();
                } else {
                    champ.append(c);
                }
            }
            // Dernière ligne sans saut final.
            if (champ.length() > 0 || !champs.isEmpty()) {
                champs.add(champ.toString());
                lignes.add(champs.toArray(new String[0]));
            }
            return lignes;
        }
    }
}
