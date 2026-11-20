package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.domain.Modele;

/**
 * PDF/A-2b des documents produits (addenda 4, Q14) : identification XMP part 2 / niveau B, intention de sortie sRGB,
 * polices intégrées — y compris après l'apposition des signatures —, titre et date concordants entre Info et XMP.
 */
class ConformitePdfATest {

    private static final Instant T0 = Instant.parse("2026-11-20T12:00:00Z");
    private final RenduDocumentsPdf rendu = new RenduDocumentsPdf(Clock.fixed(T0, ZoneOffset.UTC));

    @Test
    void le_pdf_genere_se_declare_pdf_a_2b_avec_profil_srgb_et_polices_integrees() throws Exception {
        byte[] pdf = rendu.genererPdf("Convention d'aide — Québec", "Montant : 250 000 $ — « garantie »");
        try (PDDocument doc = PDDocument.load(pdf)) {
            XMPMetadata xmp = new DomXmpParser().parse(doc.getDocumentCatalog().getMetadata().exportXMPMetadata());
            PDFAIdentificationSchema id = xmp.getPDFIdentificationSchema();
            assertEquals(2, id.getPart());
            assertEquals("B", id.getConformance());
            assertEquals("Convention d'aide — Québec", xmp.getDublinCoreSchema().getTitle());
            assertEquals(doc.getDocumentInformation().getTitle(), xmp.getDublinCoreSchema().getTitle(), "Info et XMP concordent");
            assertEquals(T0.toEpochMilli(), doc.getDocumentInformation().getCreationDate().getTimeInMillis());
            assertEquals(T0.toEpochMilli(), xmp.getXMPBasicSchema().getCreateDate().getTimeInMillis());
            List<PDOutputIntent> intentions = doc.getDocumentCatalog().getOutputIntents();
            assertEquals(1, intentions.size());
            assertEquals("sRGB IEC61966-2.1", intentions.get(0).getOutputConditionIdentifier());
            assertNotNull(intentions.get(0).getDestOutputIntent(), "profil ICC intégré");
            assertTrue(policesIntegrees(doc));
        }
        assertTrue(rendu.texte(qc.levis.contrat.clm.domain.FormatDocument.Type.PDF, pdf).contains("« garantie »"));
    }

    @Test
    void un_long_texte_passe_a_la_ligne_et_a_la_page_suivante() throws Exception {
        String mot = "approvisionnement ";
        String paragraphe = mot.repeat(60);
        byte[] pdf = rendu.genererPdf(null, (paragraphe + "\n").repeat(30) + "fin\u0007");
        try (PDDocument doc = PDDocument.load(pdf)) {
            assertTrue(doc.getNumberOfPages() > 1, "saut de page");
        }
        String texte = rendu.texte(qc.levis.contrat.clm.domain.FormatDocument.Type.PDF, pdf);
        assertTrue(texte.lines().allMatch(l -> l.length() < 120), "retour à la ligne au mot");
        assertTrue(texte.contains("fin?"), "caractère de contrôle remplacé");
        assertNotNull(rendu.genererPdf(null, null));
        assertEquals("", rendu.texte(qc.levis.contrat.clm.domain.FormatDocument.Type.DOCX, rendu.genererDocx(null, null)).strip(),
                "Word vide sans titre ni texte");
    }

    @Test
    void l_apposition_des_signatures_garde_des_polices_integrees() throws Exception {
        byte[] pdf = rendu.genererPdf("Convention", "Texte");
        byte[] signe = rendu.apposer(pdf, List.of(
                new RenduDocuments.Apposition("Marie Tremblay", T0.toString(), "TR-1", new Modele.Position(1, 10, 70, 40, 10)),
                new RenduDocuments.Apposition("Jean 名", T0.toString(), "TR-1", null)));
        try (PDDocument doc = PDDocument.load(signe)) {
            assertEquals(2, doc.getNumberOfPages());
            assertTrue(policesIntegrees(doc));
            assertEquals(2, doc.getDocumentCatalog().getMetadata() == null ? 0
                    : new DomXmpParser().parse(doc.getDocumentCatalog().getMetadata().exportXMPMetadata()).getPDFIdentificationSchema().getPart());
        }
        assertTrue(rendu.texte(qc.levis.contrat.clm.domain.FormatDocument.Type.PDF, signe).contains("Jean ?"), "glyphe absent remplacé");
    }

    @Test
    void affichable_remplace_ce_que_la_police_ne_couvre_pas() throws Exception {
        try (PDDocument doc = new PDDocument()) {
            PDFont police = ConformitePdfA.police(doc);
            assertEquals("é→€?", ConformitePdfA.affichable(police, "é→€名"));
            assertEquals("", ConformitePdfA.affichable(police, null));
        }
    }

    private static boolean policesIntegrees(PDDocument doc) throws IOException {
        for (PDPage page : doc.getPages()) {
            for (COSName nom : page.getResources().getFontNames()) {
                if (!page.getResources().getFont(nom).isEmbedded()) {
                    return false;
                }
            }
        }
        return true;
    }
}
