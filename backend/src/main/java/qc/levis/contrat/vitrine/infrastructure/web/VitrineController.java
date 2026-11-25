package qc.levis.contrat.vitrine.infrastructure.web;

import java.io.IOException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import qc.levis.contrat.vitrine.application.VitrineService;
import qc.levis.contrat.vitrine.application.dto.VitrineDto;

/** Site vitrine de chaque organisation : lecture publique ; contenu, vidéo et médiathèque réservés au superadministrateur. */
@RestController
public class VitrineController {

    private static final String SUPERADMIN = "hasRole('SUPERADMIN')";

    private final VitrineService vitrines;

    public VitrineController(VitrineService vitrines) {
        this.vitrines = vitrines;
    }

    @GetMapping("/api/vitrines/{organisation}")
    public ResponseEntity<VitrineDto.Publique> publique(@PathVariable String organisation) {
        return vitrines.publique(organisation).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/superadmin/tenants/{id}/vitrine")
    @PreAuthorize(SUPERADMIN)
    public VitrineDto.Edition lire(@PathVariable Long id) {
        return vitrines.lire(id);
    }

    @PutMapping("/api/superadmin/tenants/{id}/vitrine")
    @PreAuthorize(SUPERADMIN)
    public VitrineDto.Edition modifier(@PathVariable Long id, @RequestBody VitrineDto.Requete req, Authentication auth) {
        return vitrines.modifier(id, req, auth.getName());
    }

    @PostMapping("/api/superadmin/tenants/{id}/vitrine/video")
    @PreAuthorize(SUPERADMIN)
    public VitrineDto.Media deposerVideo(@PathVariable Long id, @RequestParam("fichier") MultipartFile fichier) throws IOException {
        return vitrines.deposerVideo(id, fichier.getOriginalFilename(), fichier.getBytes());
    }

    @GetMapping("/api/superadmin/mediatheque")
    @PreAuthorize(SUPERADMIN)
    public List<VitrineDto.Media> mediatheque(@RequestParam(defaultValue = "") String prefixe) {
        return vitrines.mediatheque(prefixe);
    }
}
