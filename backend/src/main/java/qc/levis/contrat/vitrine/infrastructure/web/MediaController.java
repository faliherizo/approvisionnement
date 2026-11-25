package qc.levis.contrat.vitrine.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.vitrine.application.port.MediaStore;

/**
 * Médiathèque publique : vidéos des vitrines et de l'aide, sous-titres, affiches, index des tutoriels. Une ressource
 * renvoyée telle quelle est servie par plages (Range) : le lecteur vidéo avance sans tout télécharger.
 */
@RestController
public class MediaController {

    private static final String PREFIXE = "/api/media/";
    private static final Map<String, MediaType> TYPES = Map.of(
            "mp4", MediaType.parseMediaType("video/mp4"), "webm", MediaType.parseMediaType("video/webm"),
            "vtt", MediaType.parseMediaType("text/vtt;charset=UTF-8"), "json", MediaType.APPLICATION_JSON,
            "jpg", MediaType.IMAGE_JPEG, "png", MediaType.IMAGE_PNG);

    private final MediaStore medias;

    public MediaController(MediaStore medias) {
        this.medias = medias;
    }

    @GetMapping(PREFIXE + "**")
    public ResponseEntity<Resource> lire(HttpServletRequest requete) {
        String chemin = requete.getRequestURI().substring(requete.getContextPath().length() + PREFIXE.length());
        String extension = chemin.substring(chemin.lastIndexOf('.') + 1).toLowerCase(java.util.Locale.ROOT);
        MediaType type = TYPES.get(extension);
        if (type == null) {
            return ResponseEntity.notFound().build();
        }
        return medias.fichier(chemin)
                .<ResponseEntity<Resource>>map(p -> ResponseEntity.ok().contentType(type)
                        .cacheControl(extension.equals("json") ? CacheControl.noCache() : CacheControl.maxAge(Duration.ofHours(1)))
                        .body(new FileSystemResource(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
