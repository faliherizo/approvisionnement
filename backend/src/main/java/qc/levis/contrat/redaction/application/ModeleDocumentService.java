package qc.levis.contrat.redaction.application;

import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.dto.ModeleDocumentRequest;
import qc.levis.contrat.redaction.application.repository.ModeleDocumentRepository;
import qc.levis.contrat.redaction.domain.ModeleDocument;

/**
 * Gestion du modèle de document (gabarit de rendu) propre à chaque entreprise.
 * Responsabilité unique : lire/écrire le modèle du tenant courant (SRP).
 */
@Service
public class ModeleDocumentService {

    private static final Set<String> POLICES = Set.of("HELVETICA", "TIMES", "COURIER");

    private final ModeleDocumentRepository modeles;

    public ModeleDocumentService(ModeleDocumentRepository modeles) {
        this.modeles = modeles;
    }

    /** Modèle du tenant courant, ou un modèle vierge (valeurs par défaut) si non défini. */
    public ModeleDocument courant() {
        return pour(TenantContext.get());
    }

    @Transactional
    public ModeleDocument enregistrer(ModeleDocumentRequest req) {
        return enregistrer(TenantContext.get(), req);
    }

    /** Modèle d'un tenant précis (tenantId nul = gabarit global par défaut). */
    public ModeleDocument pour(Long tenantId) {
        return charger(tenantId).orElseGet(ModeleDocument::new);
    }

    @Transactional
    public ModeleDocument enregistrer(Long tenantId, ModeleDocumentRequest req) {
        ModeleDocument m = charger(tenantId).orElseGet(ModeleDocument::new);
        m.setTenantId(tenantId);
        m.setPolice(policeValide(req.police()));
        m.setCouleurTitre(req.couleurTitre());
        m.setCouleurAccent(req.couleurAccent());
        m.setMarque(req.marque());
        m.setPiedDePage(req.piedDePage());
        m.setAfficherCouverture(req.afficherCouverture() == null || req.afficherCouverture());
        m.setLogoImage(req.logoImage());
        return modeles.save(m);
    }

    /** Charge le modèle d'un tenant (ou le gabarit global si tenantId nul). */
    private java.util.Optional<ModeleDocument> charger(Long tenantId) {
        return tenantId == null ? modeles.findByTenantIdIsNull() : modeles.findByTenantId(tenantId);
    }

    private String policeValide(String police) {
        String p = police == null ? "" : police.toUpperCase();
        return POLICES.contains(p) ? p : "HELVETICA";
    }
}
