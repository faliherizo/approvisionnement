package qc.levis.contrat.redaction.application;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import qc.levis.contrat.addenda.application.AddendaService;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.Gabarit;
import qc.levis.contrat.redaction.infrastructure.Branding;
import qc.levis.contrat.redaction.infrastructure.BrandingProvider;
import qc.levis.contrat.redaction.infrastructure.ContractComposer;
import qc.levis.contrat.redaction.infrastructure.ContractModel;
import qc.levis.contrat.redaction.infrastructure.DocumentRenderer;
import qc.levis.contrat.redaction.infrastructure.PdfRasterizer;
import qc.levis.contrat.soumission.application.BordereauService;

/**
 * Rend un document contractuel final (PDF/DOCX) via le pipeline composer + renderer.
 * Source unique du rendu, réutilisée par l'export (téléchargement) et la publication
 * externe (M365/SAP) — évite toute duplication du pipeline.
 */
@Service
public class DocumentRenderingService {

    private final DocumentService documents;
    private final AddendaService addendas;
    private final AnnexeService annexes;
    private final BordereauService bordereaux;
    private final ContractComposer composer;
    private final BrandingProvider branding;
    private final GabaritService gabarits;
    private final PdfRasterizer rasterizer;
    private final Map<DocumentRenderer.Format, DocumentRenderer> renderers =
            new EnumMap<>(DocumentRenderer.Format.class);

    public DocumentRenderingService(DocumentService documents, AddendaService addendas,
                                    AnnexeService annexes, BordereauService bordereaux,
                                    ContractComposer composer, BrandingProvider branding,
                                    GabaritService gabarits, PdfRasterizer rasterizer,
                                    List<DocumentRenderer> renderers) {
        this.documents = documents;
        this.addendas = addendas;
        this.annexes = annexes;
        this.bordereaux = bordereaux;
        this.composer = composer;
        this.branding = branding;
        this.gabarits = gabarits;
        this.rasterizer = rasterizer;
        renderers.forEach(r -> this.renderers.put(r.format(), r));
    }

    /** Aperçu image (PNG) de la première page du PDF rendu : visible partout, y compris sans module PDF. */
    public byte[] apercuPng(Long id, String codeGabarit) {
        byte[] pdf = rendre(id, DocumentRenderer.Format.PDF, codeGabarit);
        return rasterizer.pagePng(pdf, 0, 110);
    }

    /** Rend le document {@code id} au format demandé, avec le gabarit explicite ou déduit. */
    public byte[] rendre(Long id, DocumentRenderer.Format format, String codeGabarit) {
        Document doc = documents.get(id);
        // Priorité : gabarit explicite → gabarit du document (issu du template) → type.
        String code = (codeGabarit != null && !codeGabarit.isBlank()) ? codeGabarit : doc.getGabaritCode();
        Gabarit gabarit = gabarits.resoudre(code, doc.getType());
        ContractModel modele = composer.composer(doc, addendas.lister(id),
                annexes.lister(id), bordereaux.get(id), gabarits.versRendu(gabarit));
        Branding brandingEffectif = gabarits.brandingEffectif(gabarit, branding.courant());
        return renderers.get(format).rendre(modele, brandingEffectif);
    }
}
