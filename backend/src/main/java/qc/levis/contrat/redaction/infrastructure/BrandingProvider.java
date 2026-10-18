package qc.levis.contrat.redaction.infrastructure;

import org.springframework.stereotype.Service;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.ModeleDocumentService;
import qc.levis.contrat.redaction.domain.ModeleDocument;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;

/**
 * Résout le branding à appliquer aux documents générés (SRP). Priorité :
 * modèle de document de l'entreprise → thème du site du tenant → branding VINA
 * de la plateforme. Chaque champ retombe indépendamment sur la source suivante.
 */
@Service
public class BrandingProvider {

    private final ModeleDocumentService modeles;
    private final TenantRepository tenants;

    public BrandingProvider(ModeleDocumentService modeles, TenantRepository tenants) {
        this.modeles = modeles;
        this.tenants = tenants;
    }

    public Branding courant() {
        ModeleDocument modele = modeles.courant();
        Tenant tenant = tenantCourant();

        String couleurTitre = premier(modele.getCouleurTitre(),
                tenant != null ? tenant.getCouleurPrimaire() : null, Branding.VINA.couleurTitreHex());
        String couleurAccent = premier(modele.getCouleurAccent(),
                tenant != null ? tenant.getCouleurAccent() : null, couleurTitre);
        String marque = premier(modele.getMarque(),
                tenant != null ? tenant.getLogoTexte() : null,
                tenant != null ? tenant.getTitre() : null, Branding.VINA.marque());
        String logo = premier(modele.getLogoImage(),
                tenant != null ? tenant.getLogoImage() : null, null);
        String police = premier(modele.getPolice(), Branding.VINA.police());
        String pied = premier(modele.getPiedDePage(), "");

        return new Branding(marque, couleurTitre, couleurAccent, police, logo,
                pied, modele.isAfficherCouverture());
    }

    private Tenant tenantCourant() {
        Long tenantId = TenantContext.get();
        return tenantId == null ? null : tenants.findById(tenantId).orElse(null);
    }

    /** Première valeur non vide parmi les candidats. */
    private String premier(String... candidats) {
        for (String c : candidats) {
            if (c != null && !c.isBlank()) return c;
        }
        return "";
    }
}
