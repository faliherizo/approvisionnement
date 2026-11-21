package qc.levis.contrat.redaction;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.domain.Document;
import qc.levis.contrat.redaction.domain.FicheSeao;
import qc.levis.contrat.redaction.infrastructure.FicheSeaoXml;

class FicheSeaoTest {

    static final Instant T = Instant.parse("2026-11-20T15:00:00Z");

    private Document devis(Enums.TypeContrat type, Enums.ModeSollicitation mode) {
        Document d = new Document();
        ReflectionTestUtils.setField(d, "id", 42L);
        d.setTitre("Déneigement <secteur Est> & stationnements");
        d.setType(type);
        d.setModeSollicitation(mode);
        d.setNumeroAO("AO-2026-17");
        d.setValeurEstimee(new BigDecimal("250000.00"));
        return d;
    }

    @Test
    void la_fiche_reprend_les_metadonnees_et_l_organisme_de_l_organisation() {
        FicheSeao f = FicheSeao.de(devis(Enums.TypeContrat.CONTRAT_SERVICES, Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC), "Ville de Lévis", T);
        assertEquals("AO-2026-17", f.numero());
        assertEquals("Ville de Lévis", f.organisme());
        assertEquals("Avis d'appel d'offres", f.typeAvis());
        assertEquals("Services", f.nature());
        assertEquals(42L, f.referenceInterne());
        assertEquals("BROUILLON", f.statut());
        Document d = devis(Enums.TypeContrat.BAIL, Enums.ModeSollicitation.DEMANDE_PRIX);
        d.setOrganismePublic("Société de transport de Lévis");
        FicheSeao g = FicheSeao.de(d, "Ville de Lévis", T);
        assertEquals("Société de transport de Lévis", g.organisme());
        assertEquals("Appel d'offres sur invitation", g.typeAvis());
        assertEquals("Autre", g.nature());
    }

    @Test
    void type_d_avis_et_nature_suivent_le_mode_de_sollicitation_et_le_type() {
        assertEquals("Contrat conclu de gré à gré", FicheSeao.de(devis(Enums.TypeContrat.GRE_A_GRE, Enums.ModeSollicitation.GRE_A_GRE), "V", T).typeAvis());
        assertEquals("Avis d'intention", FicheSeao.de(devis(Enums.TypeContrat.AVIS_INTENTION, null), "V", T).typeAvis());
        FicheSeao sansMode = FicheSeao.de(devis(Enums.TypeContrat.APPEL_OFFRES_PUBLIC, null), "V", T);
        assertEquals("Avis d'appel d'offres", sansMode.typeAvis());
        assertEquals(FicheSeao.NATURE_A_PRECISER, sansMode.nature());
        assertEquals(FicheSeao.NATURE_A_PRECISER, FicheSeao.de(devis(null, null), "V", T).nature());
        assertEquals("Autre", FicheSeao.de(devis(Enums.TypeContrat.PRET_A_USAGE, null), "V", T).nature());
    }

    @Test
    void sans_numero_ou_sans_titre_aucune_fiche_n_est_produite() {
        Document sansNumero = devis(Enums.TypeContrat.CONTRAT_SERVICES, null);
        sansNumero.setNumeroAO("  ");
        assertThrows(FicheSeao.ExportSeaoImpossible.class, () -> FicheSeao.de(sansNumero, "V", T));
        Document sansNumeroNull = devis(Enums.TypeContrat.CONTRAT_SERVICES, null);
        sansNumeroNull.setNumeroAO(null);
        assertThrows(FicheSeao.ExportSeaoImpossible.class, () -> FicheSeao.de(sansNumeroNull, "V", T));
        Document sansTitre = devis(Enums.TypeContrat.CONTRAT_SERVICES, null);
        sansTitre.setTitre(" ");
        assertThrows(FicheSeao.ExportSeaoImpossible.class, () -> FicheSeao.de(sansTitre, "V", T));
    }

    @Test
    void le_xml_est_bien_forme_et_echappe_les_valeurs() throws Exception {
        Document d = devis(Enums.TypeContrat.CONTRAT_SERVICES, Enums.ModeSollicitation.APPEL_OFFRES_PUBLIC);
        byte[] octets = new FicheSeaoXml().ecrire(FicheSeao.de(d, "Ville de Lévis", T));
        var doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new java.io.ByteArrayInputStream(octets));
        assertEquals("Déneigement <secteur Est> & stationnements", doc.getElementsByTagName("titre").item(0).getTextContent());
        assertEquals("250000.00", doc.getElementsByTagName("montantestime").item(0).getTextContent());
        assertTrue(new String(octets, StandardCharsets.UTF_8).contains("&lt;secteur Est&gt; &amp;"));
        d.setValeurEstimee(null);
        String sansMontant = new String(new FicheSeaoXml().ecrire(FicheSeao.de(d, "V", T)), StandardCharsets.UTF_8);
        assertTrue(sansMontant.contains("<montantestime></montantestime>"), sansMontant);
    }
}
