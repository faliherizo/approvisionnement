package qc.levis.contrat.integration.infrastructure.web;

import java.util.List;
import java.util.Locale;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.integration.infrastructure.SimulateurDepotStore;

/**
 * Visualisation des dépôts <em>simulés</em> (SharePoint/OneDrive/Teams/Outlook/SAP) du tenant courant :
 * liste par environnement et téléchargement/aperçu du fichier réellement écrit par la simulation.
 * Permet aux clients de constater ce qui « part » avant de brancher leur vrai environnement.
 */
@RestController
@RequestMapping("/api/simulateur/depots")
@PreAuthorize("hasAnyRole('REDACTEUR','APPRO','ADMIN')")
public class SimulateurController {

    private final SimulateurDepotStore store;

    public SimulateurController(SimulateurDepotStore store) {
        this.store = store;
    }

    @GetMapping
    public List<SimulateurDepotStore.Depot> lister() {
        return store.lister(TenantContext.get());
    }

    @GetMapping("/contenu")
    public ResponseEntity<byte[]> contenu(@RequestParam String chemin) {
        byte[] octets = store.lire(TenantContext.get(), chemin);
        String nom = chemin.contains("/") ? chemin.substring(chemin.lastIndexOf('/') + 1) : chemin;
        return ResponseEntity.ok()
                .contentType(typeMedia(nom))
                .header("Content-Disposition",
                        ContentDisposition.inline().filename(nom).build().toString())
                .body(octets);
    }

    private MediaType typeMedia(String nom) {
        String n = nom.toLowerCase(Locale.ROOT);
        if (n.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        if (n.endsWith(".txt")) return MediaType.TEXT_PLAIN;
        if (n.endsWith(".docx")) {
            return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
