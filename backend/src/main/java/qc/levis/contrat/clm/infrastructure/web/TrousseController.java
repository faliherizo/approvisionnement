package qc.levis.contrat.clm.infrastructure.web;

import qc.levis.contrat.clm.domain.FormatDocument;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import qc.levis.contrat.clm.application.DocumentClmService;
import qc.levis.contrat.clm.application.PreuveService;
import qc.levis.contrat.clm.application.TrousseService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.domain.DocumentTrousse;

/** Trousses, documents, participants, parcours et preuve (UI-02 à UI-06, UI-10, UI-11, UI-15, UI-19). */
@RestController
@RequestMapping("/api/clm/trousses")
@PreAuthorize("@modules.actif('clm')")
public class TrousseController {

    private static final String GESTION = "@modules.actif('clm') and hasAnyRole('REDACTEUR','APPRO','JURIDIQUE','DIRECTION','ADMIN','SUPERADMIN')";

    private final TrousseService trousses;
    private final DocumentClmService documents;
    private final PreuveService preuves;
    private final WebClm.Acteurs acteurs;

    public TrousseController(TrousseService trousses, DocumentClmService documents, PreuveService preuves, WebClm.Acteurs acteurs) {
        this.trousses = trousses;
        this.documents = documents;
        this.preuves = preuves;
        this.acteurs = acteurs;
    }

    @GetMapping
    public List<ClmDto.TrousseResume> lister(@RequestParam(required = false) String q, @RequestParam(required = false) String statut,
                                             @RequestParam(required = false) String initiateur, @RequestParam(required = false) String type,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
                                             Authentication auth) {
        return trousses.lister(acteurs.de(auth), new ClmDto.CritereListe(q, statut, initiateur, type, du, au));
    }

    @PostMapping
    @PreAuthorize(GESTION)
    public ClmDto.TrousseDetail creer(@RequestBody ClmDto.TrousseRequest req, Authentication auth) {
        return trousses.creer(req, acteurs.de(auth));
    }

    @GetMapping("/{id}")
    public ClmDto.TrousseDetail detail(@PathVariable Long id, Authentication auth) {
        return trousses.detail(id, acteurs.de(auth));
    }

    @PutMapping("/{id}")
    public ClmDto.TrousseDetail modifier(@PathVariable Long id, @RequestBody ClmDto.TrousseRequest req, Authentication auth) {
        return trousses.modifier(id, req, acteurs.de(auth));
    }

    @GetMapping("/{id}/historique")
    public List<ClmDto.EvenementVue> historique(@PathVariable Long id, Authentication auth) {
        return trousses.historique(id, acteurs.de(auth));
    }

    @PostMapping("/{id}/preremplissage")
    public ClmDto.PreremplissageVue preremplir(@PathVariable Long id, @RequestBody ClmDto.PreremplissageRequest req, Authentication auth) {
        return trousses.preremplir(id, req, acteurs.de(auth));
    }

    @PutMapping("/{id}/valeurs")
    public ClmDto.PreremplissageVue saisirValeurs(@PathVariable Long id, @RequestBody ClmDto.ValeursRequest req, Authentication auth) {
        return trousses.saisirValeurs(id, req, acteurs.de(auth));
    }

    @GetMapping("/{id}/suggestions/{cle}")
    public List<String> suggestions(@PathVariable Long id, @PathVariable String cle, Authentication auth) {
        return trousses.suggestions(id, cle, acteurs.de(auth));
    }

    @PostMapping("/{id}/participants")
    public ClmDto.ParticipantVue ajouterParticipant(@PathVariable Long id, @RequestBody ClmDto.ParticipantRequest req, Authentication auth) {
        return trousses.ajouterParticipant(id, req, acteurs.de(auth));
    }

    @PutMapping("/{id}/participants/{participantId}/role")
    public List<ClmDto.ParticipantVue> attribuerZone(@PathVariable Long id, @PathVariable Long participantId, @RequestBody ClmDto.ZoneRequest req,
                                                     Authentication auth) {
        return trousses.attribuerZone(id, participantId, req, acteurs.de(auth));
    }

    @DeleteMapping("/{id}/participants/{participantId}")
    public ResponseEntity<Void> retirerParticipant(@PathVariable Long id, @PathVariable Long participantId, Authentication auth) {
        trousses.retirerParticipant(id, participantId, acteurs.de(auth));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/parcours")
    public List<ClmDto.EtapeVue> definirParcours(@PathVariable Long id, @RequestBody List<ClmDto.EtapeRequest> req, Authentication auth) {
        return trousses.definirParcours(id, req, acteurs.de(auth));
    }

    @PostMapping("/{id}/parcours/tester")
    public ClmDto.Validation testerParcours(@PathVariable Long id, @RequestBody List<ClmDto.EtapeRequest> req, Authentication auth) {
        return trousses.testerParcours(id, req, acteurs.de(auth));
    }

    @PostMapping("/{id}/lancer")
    public ClmDto.TrousseDetail lancer(@PathVariable Long id, Authentication auth) {
        return trousses.lancer(id, acteurs.de(auth));
    }

    @PostMapping("/{id}/annuler")
    public ClmDto.TrousseDetail annuler(@PathVariable Long id, @RequestBody(required = false) Map<String, String> corps, Authentication auth) {
        return trousses.annuler(id, corps == null ? null : corps.get("motif"), acteurs.de(auth));
    }

    @PostMapping("/{id}/reprendre")
    public ClmDto.TrousseDetail reprendre(@PathVariable Long id, Authentication auth) {
        return trousses.reprendre(id, acteurs.de(auth));
    }

    // --- Documents (UI-05/UI-06) ---
    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClmDto.DocumentVue televerser(@PathVariable Long id, @RequestParam("fichier") MultipartFile fichier, Authentication auth) throws IOException {
        return documents.televerser(id, fichier.getOriginalFilename(), fichier.getBytes(), acteurs.de(auth));
    }

    @PostMapping(value = "/{id}/documents/{cle}/versions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ClmDto.DocumentVue nouvelleVersion(@PathVariable Long id, @PathVariable String cle, @RequestParam("fichier") MultipartFile fichier,
                                              @RequestParam("motif") String motif, Authentication auth) throws IOException {
        return documents.nouvelleVersion(id, cle, fichier.getOriginalFilename(), fichier.getBytes(), motif, acteurs.de(auth));
    }

    @GetMapping("/{id}/documents/{cle}/versions")
    public List<ClmDto.DocumentVue> versions(@PathVariable Long id, @PathVariable String cle, Authentication auth) {
        return documents.versions(id, cle, acteurs.de(auth));
    }

    @DeleteMapping("/{id}/documents/{documentId}")
    public ResponseEntity<Void> retirerDocument(@PathVariable Long id, @PathVariable Long documentId, Authentication auth) {
        documents.retirer(id, documentId, acteurs.de(auth));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/documents/{documentId}/contenu")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id, @PathVariable Long documentId, Authentication auth) {
        DocumentTrousse d = documents.telecharger(id, documentId, acteurs.de(auth));
        return fichier(d.getContenu(), d.getType().mime(), d.getNom());
    }

    @GetMapping(value = "/{id}/documents/{documentId}/texte", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public String apercu(@PathVariable Long id, @PathVariable Long documentId, Authentication auth) {
        return documents.apercuTexte(id, documentId, acteurs.de(auth));
    }

    /** {@code format} : PDF (défaut) ou DOCX, Word modifiable avant l'approbation finale (addenda 3, Q26). */
    @PostMapping("/{id}/generer")
    public ClmDto.DocumentVue generer(@PathVariable Long id, @RequestParam(defaultValue = "PDF") String format, Authentication auth) {
        return documents.generer(id, acteurs.de(auth), FormatDocument.Type.valueOf(format.strip().toUpperCase(java.util.Locale.ROOT)));
    }

    // --- Preuve (UI-19) ---
    @GetMapping("/{id}/preuve")
    public ClmDto.PreuveVue preuve(@PathVariable Long id, Authentication auth) {
        return preuves.dossier(id, acteurs.de(auth));
    }

    @PostMapping("/{id}/preuve/integrite")
    public ClmDto.Integrite integrite(@PathVariable Long id, Authentication auth) {
        return preuves.verifier(id, acteurs.de(auth));
    }

    @GetMapping("/{id}/preuve/export")
    public ResponseEntity<byte[]> exporter(@PathVariable Long id, Authentication auth) {
        return fichier(preuves.exporter(id, acteurs.de(auth)), "application/zip", "preuve-trousse-" + id + ".zip");
    }

    @GetMapping("/{id}/preuve/certificat")
    public ResponseEntity<byte[]> certificat(@PathVariable Long id, Authentication auth) {
        return fichier(preuves.certificat(id, acteurs.de(auth)), "application/pdf", "certificat-trousse-" + id + ".pdf");
    }

    static ResponseEntity<byte[]> fichier(byte[] contenu, String mime, String nom) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nom, java.nio.charset.StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(MediaType.parseMediaType(mime))
                .body(contenu);
    }
}
