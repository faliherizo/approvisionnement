package qc.levis.contrat.redaction.application;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.redaction.application.dto.GabaritRequest;
import qc.levis.contrat.redaction.application.repository.GabaritRepository;
import qc.levis.contrat.redaction.domain.CorpsGabarit;
import qc.levis.contrat.redaction.domain.Gabarit;
import qc.levis.contrat.redaction.infrastructure.Branding;
import qc.levis.contrat.redaction.infrastructure.GabaritRendu;

/**
 * Gestion des gabarits de documents (SRP). Fournit le catalogue effectif du tenant
 * courant (globaux + surcharges du tenant, fusionnés par code), la résolution du
 * gabarit à appliquer à un export, le mapping vers le modèle de rendu et la surcouche
 * de branding propre au gabarit.
 */
@Service
public class GabaritService {

    private static final Set<String> POLICES = Set.of("HELVETICA", "TIMES", "COURIER");

    private final GabaritRepository gabarits;

    public GabaritService(GabaritRepository gabarits) {
        this.gabarits = gabarits;
    }

    /** Catalogue effectif du tenant courant : globaux + gabarits du tenant (override par code). */
    public List<Gabarit> lister() {
        return listerPour(TenantContext.get());
    }

    public List<Gabarit> listerPour(Long tenantId) {
        Map<String, Gabarit> parCode = new LinkedHashMap<>();
        for (Gabarit g : gabarits.findByTenantIdIsNullOrderByOrdreAscNomAsc()) parCode.put(g.getCode(), g);
        if (tenantId != null) {
            for (Gabarit g : gabarits.findByTenantIdOrderByOrdreAscNomAsc(tenantId)) parCode.put(g.getCode(), g);
        }
        List<Gabarit> res = new ArrayList<>();
        for (Gabarit g : parCode.values()) if (g.isActif()) res.add(g);
        return res;
    }

    /** Gabarit à appliquer : code explicite (catalogue effectif), sinon déduit du type du document. */
    public Gabarit resoudre(String code, Enums.TypeContrat type) {
        List<Gabarit> catalogue = lister();
        String recherche = (code != null && !code.isBlank()) ? code.trim() : codePourType(type);
        return catalogue.stream()
                .filter(g -> g.getCode().equalsIgnoreCase(recherche))
                .findFirst()
                .orElseGet(() -> catalogue.stream()
                        .filter(g -> g.getCode().equalsIgnoreCase(codePourType(type)))
                        .findFirst()
                        .orElseGet(GabaritService::gabaritDefaut));
    }

    /** Projection du gabarit vers le modèle de rendu (valeurs de repli incluses). */
    public GabaritRendu versRendu(Gabarit g) {
        if (g == null) return GabaritRendu.defaut();
        return new GabaritRendu(
                premier(g.getSurLabel(), "APPEL D'OFFRES - SERVICES"),
                premier(g.getLabel(), "CONTRAT"),
                g.getSousTitre(),
                premier(g.getPiedLabel(), premier(g.getLabel(), "CONTRAT")),
                g.getCorps() != null ? g.getCorps() : CorpsGabarit.CONTRAT,
                nombre(g.getMargeGauche(), 56), nombre(g.getMargeDroite(), 56),
                nombre(g.getMargeHaut(), 64), nombre(g.getMargeBas(), 54));
    }

    /** Branding effectif : branding du tenant surchargé par les champs de design du gabarit. */
    public Branding brandingEffectif(Gabarit g, Branding base) {
        if (g == null) return base;
        String logo = (g.getLogoImage() != null && !g.getLogoImage().isBlank())
                ? g.getLogoImage() : base.logoImage();
        boolean couverture = g.getAfficherCouverture() != null
                ? g.getAfficherCouverture() : base.afficherCouverture();
        return new Branding(
                base.marque(),
                premier(g.getCouleurTitre(), base.couleurTitreHex()),
                premier(g.getCouleurAccent(), base.couleurAccentHex()),
                policeValide(premier(g.getPolice(), base.police())),
                logo,
                base.piedDePage(),
                couverture);
    }

    // --- CRUD (scopé au tenant courant ; superadmin => catalogue global) ---

    public Gabarit obtenir(Long id) {
        return gabarits.findById(id)
                .filter(this::accessible)
                .orElseThrow(() -> new NoSuchElementException("Gabarit introuvable: " + id));
    }

    @Transactional
    public Gabarit creer(GabaritRequest req) {
        Gabarit g = new Gabarit();
        g.setTenantId(TenantContext.get());
        appliquer(g, req);
        return gabarits.save(g);
    }

    @Transactional
    public Gabarit modifier(Long id, GabaritRequest req) {
        Gabarit g = obtenir(id);
        appliquer(g, req);
        return gabarits.save(g);
    }

    @Transactional
    public void supprimer(Long id) {
        gabarits.delete(obtenir(id));
    }

    /** Clone un gabarit (typiquement un global) dans le périmètre du tenant courant pour le personnaliser. */
    @Transactional
    public Gabarit cloner(Long id) {
        Gabarit src = gabarits.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Gabarit introuvable: " + id));
        Gabarit c = new Gabarit();
        c.setTenantId(TenantContext.get());
        c.setCode(src.getCode());
        c.setNom(src.getNom());
        c.setSurLabel(src.getSurLabel());
        c.setLabel(src.getLabel());
        c.setSousTitre(src.getSousTitre());
        c.setPiedLabel(src.getPiedLabel());
        c.setCorps(src.getCorps());
        c.setPolice(src.getPolice());
        c.setCouleurTitre(src.getCouleurTitre());
        c.setCouleurAccent(src.getCouleurAccent());
        c.setLogoImage(src.getLogoImage());
        c.setAfficherCouverture(src.getAfficherCouverture());
        c.setMargeGauche(src.getMargeGauche());
        c.setMargeDroite(src.getMargeDroite());
        c.setMargeHaut(src.getMargeHaut());
        c.setMargeBas(src.getMargeBas());
        c.setOrdre(src.getOrdre());
        return gabarits.save(c);
    }

    private void appliquer(Gabarit g, GabaritRequest req) {
        if (req.code() != null && !req.code().isBlank()) g.setCode(req.code().trim().toUpperCase());
        g.setNom(req.nom());
        g.setSurLabel(req.surLabel());
        g.setLabel(req.label());
        g.setSousTitre(req.sousTitre());
        g.setPiedLabel(req.piedLabel());
        g.setCorps(corpsValide(req.corps()));
        g.setPolice(vide(req.police()) ? null : req.police().toUpperCase());
        g.setCouleurTitre(req.couleurTitre());
        g.setCouleurAccent(req.couleurAccent());
        g.setLogoImage(req.logoImage());
        g.setAfficherCouverture(req.afficherCouverture());
        g.setMargeGauche(req.margeGauche());
        g.setMargeDroite(req.margeDroite());
        g.setMargeHaut(req.margeHaut());
        g.setMargeBas(req.margeBas());
        if (req.ordre() != null) g.setOrdre(req.ordre());
        if (req.actif() != null) g.setActif(req.actif());
    }

    /** Un gabarit est modifiable s'il appartient au périmètre courant (tenant ou global superadmin). */
    private boolean accessible(Gabarit g) {
        return Objects.equals(g.getTenantId(), TenantContext.get());
    }

    private static String codePourType(Enums.TypeContrat type) {
        if (type == Enums.TypeContrat.AVIS_INTENTION) return "AVIS_INTERET";
        return "CONTRAT_SERVICES";
    }

    /** Gabarit transitoire de secours si le catalogue est vide (labels par défaut). */
    private static Gabarit gabaritDefaut() {
        Gabarit g = new Gabarit();
        g.setCode("CONTRAT_SERVICES");
        g.setNom("Contrat de services");
        g.setSurLabel("APPEL D'OFFRES - SERVICES");
        g.setLabel("CONTRAT");
        g.setSousTitre("Services de nature technique");
        g.setPiedLabel("CONTRAT");
        g.setCorps(CorpsGabarit.CONTRAT);
        return g;
    }

    private CorpsGabarit corpsValide(String corps) {
        try {
            return corps == null ? CorpsGabarit.CONTRAT : CorpsGabarit.valueOf(corps.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CorpsGabarit.CONTRAT;
        }
    }

    private String policeValide(String police) {
        String p = police == null ? "" : police.toUpperCase();
        return POLICES.contains(p) ? p : "HELVETICA";
    }

    private static int nombre(Integer v, int defaut) { return v != null ? v : defaut; }
    private static boolean vide(String s) { return s == null || s.isBlank(); }

    private static String premier(String... candidats) {
        for (String c : candidats) if (c != null && !c.isBlank()) return c;
        return "";
    }
}
