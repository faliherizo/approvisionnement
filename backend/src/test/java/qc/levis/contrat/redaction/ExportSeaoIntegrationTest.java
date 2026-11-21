package qc.levis.contrat.redaction;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/** Lévis DAO 2.3 (C-20) : les métadonnées d'un devis s'exportent au format SEAO, pour l'organisation qui le détient. */
@SpringBootTest
@AutoConfigureMockMvc
class ExportSeaoIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    Api api;
    Cookie redacteur;

    @BeforeEach
    void client() throws Exception {
        api = new Api(mvc, json);
        redacteur = api.connecter("redacteur@levis.qc.ca");
    }

    private long devis(String numeroAO) throws Exception {
        Map<String, Object> corps = new HashMap<>(Map.of("titre", "Déneigement & stationnements", "type", "APPEL_OFFRES_PUBLIC",
                "modeSollicitation", "APPEL_OFFRES_PUBLIC", "valeurEstimee", 250000));
        if (numeroAO != null) corps.put("numeroAO", numeroAO);
        return api.post("/api/documents", redacteur, corps).id();
    }

    @Test
    void le_devis_s_exporte_en_fiche_xml_seao_et_l_export_est_journalise() throws Exception {
        String numero = "AO-" + UUID.randomUUID().toString().substring(0, 6);
        Api.Reponse r = api.get("/api/documents/" + devis(numero) + "/export/seao", redacteur);
        assertEquals(200, r.statut());
        assertTrue(r.texte().contains("<numero>" + numero + "</numero>"), r.texte());
        assertTrue(r.texte().contains("<titre>Déneigement &amp; stationnements</titre>"), r.texte());
        assertTrue(r.texte().contains("<type>Avis d'appel d'offres</type>"), r.texte());
        assertTrue(api.get("/api/audit?action=EXPORT_SEAO", api.connecter("admin@levis.qc.ca")).texte().contains(numero));
    }

    @Test
    void un_devis_sans_numero_d_appel_d_offres_ne_s_exporte_pas() throws Exception {
        Api.Reponse r = api.get("/api/documents/" + devis(null) + "/export/seao", redacteur);
        assertEquals(409, r.statut());
        assertTrue(r.corps().get("message").asText().contains("numéro d'appel d'offres"));
    }

    @Test
    void une_autre_organisation_n_exporte_pas_le_devis() throws Exception {
        long id = devis("AO-ISO-" + UUID.randomUUID().toString().substring(0, 4));
        Cookie autre = api.organisation("essai-seao", "redaction");
        assertEquals(404, api.get("/api/documents/" + id + "/export/seao", autre).statut());
    }
}
