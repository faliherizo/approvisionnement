package qc.levis.contrat.redaction.infrastructure.web;

import java.time.Clock;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.module.infrastructure.web.ModuleRequis;
import qc.levis.contrat.redaction.application.DocumentService;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.FicheSeao;
import qc.levis.contrat.redaction.infrastructure.BrandingProvider;
import qc.levis.contrat.redaction.infrastructure.FicheSeaoXml;

/** Export des métadonnées d'un devis au format SEAO (Lévis DAO 2.3, C-20), pour un document de l'organisation courante. */
@RestController
@ModuleRequis("redaction")
public class ExportSeaoController {

    private final DocumentService documents;
    private final BrandingProvider organisation;
    private final FicheSeaoXml xml;
    private final AuditService audit;
    private final Clock horloge;

    public ExportSeaoController(DocumentService documents, BrandingProvider organisation, FicheSeaoXml xml, AuditService audit,
                                Clock horloge) {
        this.documents = documents;
        this.organisation = organisation;
        this.xml = xml;
        this.audit = audit;
        this.horloge = horloge;
    }

    @GetMapping("/api/documents/{id}/export/seao")
    public ResponseEntity<byte[]> exporter(@PathVariable Long id, Authentication auth) {
        Document d = documents.get(id);
        FicheSeao fiche = FicheSeao.de(d, organisation.courant().marque(), horloge.instant());
        audit.log(auth.getName(), "EXPORT_SEAO", "Document", id, fiche.numero());
        String nom = "seao-" + fiche.numero().replaceAll("[^A-Za-z0-9._-]", "_") + ".xml";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nom).build().toString())
                .body(xml.ecrire(fiche));
    }
}
