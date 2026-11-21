package qc.levis.contrat.redaction.application;

import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.repository.DocumentRepository;
import qc.levis.contrat.redaction.domain.Document;

/**
 * Accès à un devis par son identifiant, borné à l'organisation courante (OWASP A01) : partagé par les modules qui
 * rattachent quelque chose à un devis (addendas, obligations, signataires, versions). Un devis d'une autre organisation
 * est introuvable ; le superadministrateur, sans organisation, n'est pas borné.
 */
@Component
public class DocumentsDuTenant {

    private final DocumentRepository documents;

    public DocumentsDuTenant(DocumentRepository documents) {
        this.documents = documents;
    }

    public Document exiger(Long id) {
        Document d = documents.findById(id).orElseThrow(() -> new NoSuchElementException("Document introuvable: " + id));
        Long tenant = TenantContext.get();
        if (tenant != null && !tenant.equals(d.getTenantId())) throw new NoSuchElementException("Document introuvable: " + id);
        return d;
    }
}
