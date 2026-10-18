package qc.levis.contrat.tenant.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.stereotype.Service;
import qc.levis.contrat.approbation.application.repository.ApprovalWorkflowRepository;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.dossier.application.repository.DossierRepository;
import qc.levis.contrat.redaction.application.repository.ClauseRepository;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.application.repository.DocumentTemplateRepository;
import qc.levis.contrat.redaction.application.repository.GabaritRepository;
import qc.levis.contrat.tenant.domain.Tenant;

/**
 * Réversibilité (art. 20) : exporte <em>toutes</em> les données du tenant courant dans une archive
 * ZIP en formats ouverts (JSON) + un manifeste. Réutilise les dépôts existants — aucun stockage
 * ni format propriétaire (ADR-003).
 */
@Service
public class ExportTenantService {

    private final ObjectMapper json;
    private final ClauseRepository clauses;
    private final DocumentTemplateRepository templates;
    private final GabaritRepository gabarits;
    private final DossierRepository dossiers;
    private final DocumentRepository documents;
    private final ApprovalWorkflowRepository workflows;
    private final AuditService audit;

    public ExportTenantService(ObjectMapper json, ClauseRepository clauses,
                               DocumentTemplateRepository templates, GabaritRepository gabarits,
                               DossierRepository dossiers, DocumentRepository documents,
                               ApprovalWorkflowRepository workflows, AuditService audit) {
        this.json = json;
        this.clauses = clauses;
        this.templates = templates;
        this.gabarits = gabarits;
        this.dossiers = dossiers;
        this.documents = documents;
        this.workflows = workflows;
        this.audit = audit;
    }

    /** Produit l'archive ZIP complète du tenant courant (JSON + manifeste). */
    public byte[] exporterZip(String utilisateur) {
        Long tenant = TenantContext.get();

        Map<String, List<?>> donnees = new LinkedHashMap<>();
        donnees.put("clauses.json", clauses.findByActifTrueAndTenantId(tenant));
        donnees.put("templates.json", templates.findByTenantId(tenant));
        donnees.put("gabarits.json", gabarits.findByTenantIdOrderByOrdreAscNomAsc(tenant));
        donnees.put("dossiers.json", dossiers.findByTenantIdAndActifTrueOrderByCreeLeDesc(tenant));
        donnees.put("documents.json", documents.findByTenantId(tenant));
        donnees.put("workflows.json", workflows.findByTenantId(tenant));

        byte[] archive = ecrireZip(tenant, donnees);
        audit.log(utilisateur, "REVERSIBILITE_EXPORT", "Tenant", tenant, donnees.size() + " jeux de données");
        return archive;
    }

    private byte[] ecrireZip(Long tenant, Map<String, List<?>> donnees) {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        Map<String, Integer> comptes = new LinkedHashMap<>();
        try (ZipOutputStream zip = new ZipOutputStream(sortie)) {
            for (Map.Entry<String, List<?>> e : donnees.entrySet()) {
                byte[] contenu = json.writerWithDefaultPrettyPrinter().writeValueAsBytes(e.getValue());
                comptes.put(e.getKey(), e.getValue().size());
                ecrireEntree(zip, e.getKey(), contenu);
            }
            Map<String, Object> manifeste = new LinkedHashMap<>();
            manifeste.put("tenantId", tenant);
            manifeste.put("exporteLe", Instant.now().toString());
            manifeste.put("format", "JSON (UTF-8), archive ZIP — formats ouverts et documentés");
            manifeste.put("jeuxDeDonnees", comptes);
            ecrireEntree(zip, "manifeste.json",
                    json.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifeste));
        } catch (IOException e) {
            throw new UncheckedIOException("Échec de l'export de réversibilité", e);
        }
        return sortie.toByteArray();
    }

    private void ecrireEntree(ZipOutputStream zip, String nom, byte[] contenu) throws IOException {
        zip.putNextEntry(new ZipEntry(nom));
        zip.write(contenu, 0, contenu.length);
        zip.closeEntry();
    }
}
