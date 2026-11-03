package qc.levis.contrat.document.infrastructure.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import qc.levis.contrat.document.application.DocumentService;
import qc.levis.contrat.document.application.dto.CommentaireRequest;
import qc.levis.contrat.document.application.dto.CommentaireVue;
import qc.levis.contrat.document.application.dto.DocumentVue;

/**
 * Pièces jointes et commentaires d'une demande (écran 8 onglets Documents/Historique). Gardé par le
 * module « document ». Le binaire n'est jamais renvoyé ici (métadonnées seules) ; voir le téléchargement.
 */
@RestController("documentDemandeController")
@RequestMapping("/api/formulaires/{fid}/saisies/{sid}")
@PreAuthorize("@modules.actif('document')")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping("/documents")
    public List<DocumentVue> televerser(@PathVariable Long fid, @PathVariable Long sid,
                                        @RequestParam("fichiers") MultipartFile[] fichiers,
                                        @RequestParam(required = false) String classification,
                                        Authentication auth) throws IOException {
        List<DocumentVue> res = new ArrayList<>();
        for (MultipartFile f : fichiers) {
            res.add(service.ajouter(fid, sid, f.getOriginalFilename(), f.getContentType(),
                    f.getBytes(), classification, auth != null ? auth.getName() : null));
        }
        return res;
    }

    @GetMapping("/documents")
    public List<DocumentVue> lister(@PathVariable Long sid) {
        return service.lister(sid);
    }

    @PostMapping("/commentaires")
    public CommentaireVue commenter(@PathVariable Long sid, @RequestBody CommentaireRequest req, Authentication auth) {
        return service.ajouterCommentaire(sid, req, auth != null ? auth.getName() : null);
    }

    @GetMapping("/commentaires")
    public List<CommentaireVue> commentaires(@PathVariable Long sid) {
        return service.listerCommentaires(sid);
    }
}
