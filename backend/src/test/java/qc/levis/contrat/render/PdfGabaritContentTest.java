package qc.levis.contrat.render;

import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.redaction.domain.CorpsGabarit;
import qc.levis.contrat.redaction.infrastructure.Branding;
import qc.levis.contrat.redaction.infrastructure.ContractModel;
import qc.levis.contrat.redaction.infrastructure.GabaritRendu;
import qc.levis.contrat.redaction.infrastructure.PdfContractRenderer;
import qc.levis.contrat.soumission.domain.Bordereau;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valide que le PDF produit reproduit le <b>contenu et le format</b> des gabarits normalisés
 * SCT (pièces jointes). On extrait le texte du PDF généré (Apache PDFBox) et on assère les
 * marqueurs propres à chaque gabarit : sur-label de couverture, libellé du document, numéro
 * d'AO, préambule ENTRE/ET, en-têtes de colonnes du bordereau, bloc de signature.
 */
class PdfGabaritContentTest {

    private static final GabaritRendu CONTRAT_SERVICES = new GabaritRendu(
            "APPEL D'OFFRES - SERVICES", "CONTRAT", "Services de nature technique", "CONTRAT",
            CorpsGabarit.CONTRAT, 56, 56, 64, 54);
    private static final GabaritRendu REGIE = new GabaritRendu(
            "APPEL D'OFFRES - SERVICES", "RÉGIE", "Services de nature technique", "RÉGIE",
            CorpsGabarit.CONTRAT, 56, 56, 64, 54);
    private static final GabaritRendu BORDEREAU_PRIX = new GabaritRendu(
            "APPEL D'OFFRES - SERVICES", "BORDEREAU DE PRIX", null, "BORDEREAU DE PRIX",
            CorpsGabarit.BORDEREAU, 56, 56, 64, 54);

    private final PdfContractRenderer renderer = new PdfContractRenderer();

    /** Extrait le texte de toutes les pages du PDF. */
    private String texte(byte[] pdf) throws Exception {
        try (PDDocument doc = PDDocument.load(pdf)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private ContractModel modeleContrat(GabaritRendu gabarit) {
        return new ContractModel(
                "Services d'entretien des systèmes informatiques",
                "CO2026-7060-AO",
                "CONTRAT_SERVICES", "EN_REDACTION", "2026-07-06",
                "Santé Québec",
                "VINA IT Solutions inc.",
                "Le présent contrat vise la prestation de services techniques.",
                List.of(new ContractModel.Section("Clauses générales",
                        List.of(new ContractModel.Article("0.00", "Interprétation",
                                "Les définitions s'appliquent au présent document.")))),
                List.of(),
                null,
                gabarit);
    }

    @Test
    void gabaritContratServices_reproduitCouvertureEtPreambule() throws Exception {
        String t = texte(renderer.rendre(modeleContrat(CONTRAT_SERVICES), Branding.VINA));

        assertThat(t).contains(
                "APPEL D'OFFRES - SERVICES",   // sur-label de couverture
                "CONTRAT",                      // libellé du document
                "NO CO2026-7060-AO",            // numéro d'AO
                "Services de nature technique", // sous-titre
                "ENTRE :", "ORGANISME PUBLIC",
                "ET :", "PRESTATAIRE DE SERVICES");
    }

    @Test
    void gabaritRegie_afficheLeLibelleRegie() throws Exception {
        String t = texte(renderer.rendre(modeleContrat(REGIE), Branding.VINA));

        assertThat(t).contains("APPEL D'OFFRES - SERVICES", "RÉGIE", "NO CO2026-7060-AO");
    }

    @Test
    void gabaritBordereauPrix_reproduitColonnesEtSignature() throws Exception {
        ContractModel.Bordereau bordereau = new ContractModel.Bordereau(
                "Bordereau de prix",
                List.of(new ContractModel.Bordereau.Ligne(
                        "Technicien spécialisé niveau 2", "1800", "95.00", "171000.00")),
                "171000.00");
        ContractModel m = new ContractModel(
                "Services techniques", "CO2026-7060-AO", "CONTRAT_SERVICES", "EN_REDACTION",
                "2026-07-06", "Santé Québec", "VINA IT Solutions inc.",
                "Prix fermes pour toute la durée du contrat.",
                List.of(), List.of(), bordereau, BORDEREAU_PRIX);

        String t = texte(renderer.rendre(m, Branding.VINA));

        // Les en-têtes multi-mots sont extraits sur plusieurs lignes par PDFBox.
        assertThat(t)
                .contains(
                        "BORDEREAU DE PRIX", "Description", "Taux horaire",
                        "Heures", "ressources", "années",
                        "GRAND TOTAL DE LA SOUMISSION", "SIGNATURE DU SOUMISSIONNAIRE", "171000.00")
                // Le corps « bordereau » ne rend pas le préambule contractuel.
                .doesNotContain("ENTRE :");
    }
}
