package qc.levis.contrat.document.infrastructure.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import qc.levis.contrat.saisie.application.AccesFiches;
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
    private final AccesFiches acces;

    public DocumentController(DocumentService service, AccesFiches acces) {
        this.service = service;
        this.acces = acces;
    }

    private static AccesFiches.Acteur acteur(Authentication auth) {
        return new AccesFiches.Acteur(auth.getName(), auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
    }

    @PostMapping("/documents")
    public List<DocumentVue> televerser(@PathVariable Long fid, @PathVariable Long sid,
                                        @RequestParam("fichiers") MultipartFile[] fichiers,
                                        @RequestParam(required = false) String classification,
                                        Authentication auth) throws IOException {
        acces.exigerFiche(fid, sid, acteur(auth));
        List<DocumentVue> res = new ArrayList<>();
        for (MultipartFile f : fichiers) {
            res.add(service.ajouter(fid, sid, f.getOriginalFilename(), f.getContentType(),
                    f.getBytes(), classification, auth != null ? auth.getName() : null));
        }
        return res;
    }

    @GetMapping("/documents")
    public List<DocumentVue> lister(@PathVariable Long fid, @PathVariable Long sid, Authentication auth) {
        acces.exigerFiche(fid, sid, acteur(auth));
        return service.lister(sid);
    }

    /** Le demandeur n'écrit que des commentaires qui lui sont destinés ; les commentaires internes sont au personnel. */
    @PostMapping("/commentaires")
    public CommentaireVue commenter(@PathVariable Long fid, @PathVariable Long sid, @RequestBody CommentaireRequest req, Authentication auth) {
        AccesFiches.Acteur a = acteur(auth);
        acces.exigerFiche(fid, sid, a);
        CommentaireRequest permis = a.estPersonnel() ? req : new CommentaireRequest("DEMANDEUR", req.corps());
        return service.ajouterCommentaire(sid, permis, auth.getName());
    }

    /** Le demandeur ne lit pas les commentaires internes du personnel sur sa demande. */
    @GetMapping("/commentaires")
    public List<CommentaireVue> commentaires(@PathVariable Long fid, @PathVariable Long sid, Authentication auth) {
        AccesFiches.Acteur a = acteur(auth);
        acces.exigerFiche(fid, sid, a);
        return service.listerCommentaires(sid).stream().filter(c -> a.estPersonnel() || "DEMANDEUR".equals(c.visibilite())).toList();
    }
}
