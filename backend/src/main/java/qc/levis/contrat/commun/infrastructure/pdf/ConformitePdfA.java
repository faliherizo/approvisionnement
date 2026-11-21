package qc.levis.contrat.commun.infrastructure.pdf;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import javax.xml.transform.TransformerException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;

/**
 * PDF/A-2b (ISO 19005-2, niveau B ; addenda 4, Q14) des documents produits par la plateforme : police intégrée,
 * métadonnées XMP déclarant la conformité, profil de couleur sRGB en intention de sortie. Le rendu visuel reste
 * reproductible sans ressource externe, condition de la conservation à long terme.
 */
public final class ConformitePdfA {

    /** Police libre livrée avec PDFBox : intégrée (sous-ensemble), elle couvre le français et l'anglais. */
    private static final String POLICE = "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf";

    private ConformitePdfA() {
    }

    public static PDFont police(PDDocument doc) throws IOException {
        try (InputStream in = ConformitePdfA.class.getResourceAsStream(POLICE)) {
            return PDType0Font.load(doc, in, true);
        }
    }

    /** Remplace par « ? » les caractères absents de la police plutôt que de faire échouer la génération. */
    public static String affichable(PDFont police, String texte) {
        StringBuilder sb = new StringBuilder();
        (texte == null ? "" : texte).codePoints().forEach(c -> sb.append(encodable(police, c) ? Character.toString(c) : "?"));
        return sb.toString();
    }

    private static boolean encodable(PDFont police, int c) {
        if (Character.isISOControl(c)) {
            return false;
        }
        try {
            police.encode(Character.toString(c));
            return true;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }

    public static void declarer(PDDocument doc, String titre, Calendar le) throws IOException, BadFieldValueException, TransformerException {
        doc.getDocumentInformation().setTitle(titre);
        doc.getDocumentInformation().setCreationDate(le);
        PDMetadata metadonnees = new PDMetadata(doc);
        metadonnees.importXMPMetadata(xmp(titre, le));
        doc.getDocumentCatalog().setMetadata(metadonnees);
        PDOutputIntent intention = new PDOutputIntent(doc, new ByteArrayInputStream(
                ICC_Profile.getInstance(ColorSpace.CS_sRGB).getData()));
        intention.setInfo("sRGB IEC61966-2.1");
        intention.setOutputCondition("sRGB IEC61966-2.1");
        intention.setOutputConditionIdentifier("sRGB IEC61966-2.1");
        intention.setRegistryName("http://www.color.org");
        doc.getDocumentCatalog().addOutputIntent(intention);
    }

    private static byte[] xmp(String titre, Calendar le) throws BadFieldValueException, TransformerException {
        XMPMetadata xmp = XMPMetadata.createXMPMetadata();
        PDFAIdentificationSchema id = xmp.createAndAddPFAIdentificationSchema();
        id.setPart(2);
        id.setConformance("B");
        DublinCoreSchema dc = xmp.createAndAddDublinCoreSchema();
        dc.setTitle(titre);
        XMPBasicSchema base = xmp.createAndAddXMPBasicSchema();
        base.setCreateDate(le);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new XmpSerializer().serialize(xmp, out, true);
        return out.toByteArray();
    }
}
