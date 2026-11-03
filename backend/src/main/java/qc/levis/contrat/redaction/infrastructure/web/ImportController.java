package qc.levis.contrat.redaction.infrastructure.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.redaction.application.ImportService;
import qc.levis.contrat.redaction.application.dto.ImportRapport;
import qc.levis.contrat.redaction.application.dto.ImportWordRapport;

/** Import / migration de contenus réutilisables, avec migration d'essai (art. 22). Admin. */
@RestController
@ModuleRequis("redaction")
@RequestMapping("/api/import")
@PreAuthorize("hasRole('ADMIN')")
public class ImportController {

    private final ImportService service;

    public ImportController(ImportService service) {
        this.service = service;
    }

    /**
     * Importe des clauses depuis un CSV. {@code essai=true} (défaut) effectue une migration d'essai
     * (validation sans persistance) ; {@code essai=false} persiste les lignes valides.
     */
    @PostMapping(value = "/clauses", consumes = "text/csv")
    public ImportRapport importerClauses(@RequestBody String csv,
                                              @RequestParam(defaultValue = "true") boolean essai,
                                              Authentication auth) {
        return essai
                ? service.analyser(csv)
                : service.importer(csv, auth != null ? auth.getName() : "anonyme");
    }

    /**
     * Importe un modèle Word (.docx) : chaque section devient une clause, regroupées dans un modèle.
     * {@code essai=true} (défaut) détecte les sections sans persister ; sinon crée modèle + clauses.
     */
    @PostMapping(value = "/word", consumes = "multipart/form-data")
    public ImportWordRapport importerWord(@RequestParam("fichier") MultipartFile fichier,
                                               @RequestParam(defaultValue = "true") boolean essai,
                                               @RequestParam(defaultValue = "Modèle importé") String nom,
                                               @RequestParam(required = false) Enums.TypeContrat type,
                                               Authentication auth) {
        byte[] contenu;
        try {
            contenu = fichier.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Lecture du fichier impossible", e);
        }
        return essai
                ? service.analyserWord(contenu)
                : service.importerWord(contenu, nom, type, auth != null ? auth.getName() : "anonyme");
    }
}
