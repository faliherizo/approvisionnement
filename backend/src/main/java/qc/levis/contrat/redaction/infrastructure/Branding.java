package qc.levis.contrat.redaction.infrastructure;

/**
 * Identité visuelle appliquée aux documents générés (PDF/DOCX), issue du modèle de
 * document de l'entreprise : marque, couleurs (titres, accents), police, logo image,
 * pied de page et affichage de la page de couverture.
 */
public record Branding(String marque, String couleurTitreHex, String couleurAccentHex,
                       String police, String logoImage, String piedDePage, boolean afficherCouverture) {

    /** Branding par défaut de la plateforme (contexte global / superadmin). */
    public static final Branding VINA = new Branding(
            "VINA IT Solutions inc.", "#0B4A8A", "#0B4A8A", "HELVETICA", null, "", true);

    public boolean aLogoImage() {
        return logoImage != null && logoImage.startsWith("data:image");
    }

    public boolean aPiedDePage() {
        return piedDePage != null && !piedDePage.isBlank();
    }
}
