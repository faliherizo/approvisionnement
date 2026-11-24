package qc.levis.contrat.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import qc.levis.contrat.support.Api;

/**
 * CHU 6.06 : une organisation qui n'a souscrit que les formulaires et la saisie récupère, en une archive, ses formulaires,
 * leurs versions et ses fiches — et rien du socle de rédaction qu'elle n'a pas.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReversibiliteIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void l_export_integral_reunit_les_donnees_de_chaque_module_souscrit() throws Exception {
        Api api = new Api(mvc, json);
        Cookie admin = api.organisation("essai-reversibilite", "formulaire", "saisie");
        desactiver(api, "essai-reversibilite", "redaction");
        String code = "REV" + UUID.randomUUID().toString().substring(0, 6);
        long formulaire = api.post("/api/formulaires", admin, Map.of("code", code, "nom", "Inscription", "domaine", "RH")).id();
        assertEquals(200, api.post("/api/formulaires/" + formulaire + "/saisies", admin, null).statut());

        Api.Reponse r = api.get("/api/reversibilite/export", admin);
        assertEquals(200, r.statut());
        Map<String, String> archive = lire(r.octets());

        assertTrue(archive.get("formulaire/formulaires.json").contains(code));
        assertTrue(archive.containsKey("formulaire/formulaire-versions.json"));
        assertTrue(archive.get("saisie/fiches.json").contains("\"formulaireId\" : " + formulaire));
        assertTrue(archive.get("manifeste.json").contains("saisie/fiches.json"));
        assertFalse(archive.containsKey("clauses.json"), "socle de rédaction non souscrit");
    }

    @Test
    void une_organisation_sans_module_exportable_est_refusee() throws Exception {
        Api api = new Api(mvc, json);
        Cookie admin = api.organisation("essai-reversibilite-rh", "rh");
        desactiver(api, "essai-reversibilite-rh", "redaction", "formulaire", "saisie");
        assertEquals(403, api.get("/api/reversibilite/export", admin).statut());
    }

    /** Un module sans réglage suit le défaut du catalogue, que d'autres essais peuvent changer : on le fixe ici. */
    private static void desactiver(Api api, String slug, String... modules) throws Exception {
        Cookie sa = api.connecter("superadmin@plateforme.ca", "SuperPassw0rd!");
        long id = 0;
        for (var t : api.get("/api/superadmin/tenants", sa).corps()) if (slug.equals(t.get("slug").asText())) id = t.get("id").asLong();
        for (String m : modules) assertEquals(200, api.put("/api/superadmin/tenants/" + id + "/modules/" + m + "/desactiver", sa, null).statut());
    }

    private static Map<String, String> lire(byte[] octets) throws Exception {
        Map<String, String> entrees = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(octets))) {
            for (ZipEntry e; (e = zip.getNextEntry()) != null; ) {
                entrees.put(e.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return entrees;
    }
}
