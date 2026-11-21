package qc.levis.contrat.redaction.infrastructure;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.springframework.stereotype.Component;
import qc.levis.contrat.redaction.domain.FicheSeao;

/**
 * Écrit la fiche SEAO en XML (UTF-8). L'écriture passe par StAX, qui échappe chaque valeur : un titre contenant « < » ou
 * « & » ne peut pas altérer la structure du document.
 */
@Component
public class FicheSeaoXml {

    public byte[] ecrire(FicheSeao f) {
        ByteArrayOutputStream sortie = new ByteArrayOutputStream();
        try {
            XMLStreamWriter x = XMLOutputFactory.newFactory().createXMLStreamWriter(sortie, StandardCharsets.UTF_8.name());
            x.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            x.writeStartElement("export");
            x.writeStartElement("avis");
            element(x, "numero", f.numero());
            element(x, "organisme", f.organisme());
            element(x, "titre", f.titre());
            element(x, "type", f.typeAvis());
            element(x, "nature", f.nature());
            element(x, "montantestime", f.valeurEstimee() == null ? null : f.valeurEstimee().toPlainString());
            element(x, "statutinterne", f.statut());
            element(x, "adjudicataire", f.adjudicataire());
            element(x, "referenceinterne", String.valueOf(f.referenceInterne()));
            element(x, "produitele", f.produiteLe().toString());
            x.writeEndElement();
            x.writeEndElement();
            x.writeEndDocument();
            x.close();
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Écriture de la fiche SEAO impossible.", e);
        }
        return sortie.toByteArray();
    }

    private static void element(XMLStreamWriter x, String nom, String valeur) throws XMLStreamException {
        x.writeStartElement(nom);
        if (valeur != null) x.writeCharacters(valeur);
        x.writeEndElement();
    }
}
