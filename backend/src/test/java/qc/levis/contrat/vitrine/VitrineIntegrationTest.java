package qc.levis.contrat.vitrine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.tenant.application.repository.TenantRepository;

/**
 * Sites vitrines : la vitrine publiée d'une organisation est publique, son contenu et sa vidéo ne se changent que par le
 * superadministrateur ; la médiathèque se lit par plages et ne laisse rien sortir de son répertoire.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.media.repertoire=${java.io.tmpdir}/vina-media-test", "app.clm.planificateur.actif=false"})
class VitrineIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired TenantRepository tenants;

    private MockHttpServletResponse appel(AbstractMockHttpServletRequestBuilder<?> req, Cookie c) throws Exception {
        if (c != null) {
            req.cookie(c);
        }
        return mvc.perform(req).andReturn().getResponse();
    }

    private Cookie login(String email, String motDePasse) throws Exception {
        return appel(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + motDePasse + "\"}"), null).getCookie("ACCESS_TOKEN");
    }

    private static MockHttpServletRequestBuilder put(String url, String corps) {
        return MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).content(corps);
    }

    @Test
    void la_vitrine_publiee_est_publique_et_seul_le_superadministrateur_la_modifie() throws Exception {
        Long iq = tenants.findBySlug("iq").orElseThrow().getId();
        JsonNode publique = json.readTree(appel(MockMvcRequestBuilders.get("/api/vitrines/iq"), null).getContentAsString());
        assertTrue(publique.get("titre").asText().startsWith("Investissement Québec"), "vitrine de départ amorcée");
        assertTrue(publique.get("fonctionnalites").size() >= 5);
        assertEquals(404, appel(MockMvcRequestBuilders.get("/api/vitrines/inconnue"), null).getStatus());

        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        Cookie admin = login("admin.ti@iq.example", "Passw0rd!");
        String url = "/api/superadmin/tenants/" + iq + "/vitrine";
        String corps = "{\"titre\":\"IQ — démonstration\",\"accroche\":\"Accroche\",\"fonctionnalites\":[{\"titre\":\"Lot\",\"texte\":\"Un dossier par ligne\"}],"
                + "\"publiee\":%s}";
        assertEquals(403, appel(put(url, corps.formatted(true)), admin).getStatus(), "l'administration de l'organisation ne gère pas sa vitrine");
        assertEquals(200, appel(put(url, corps.formatted(true)), sa).getStatus());
        assertEquals("IQ — démonstration", json.readTree(appel(MockMvcRequestBuilders.get("/api/vitrines/iq"), null).getContentAsString())
                .get("titre").asText());
        assertEquals("Lot", json.readTree(appel(MockMvcRequestBuilders.get(url), sa).getContentAsString()).get("fonctionnalites").get(0).get("titre").asText());

        appel(put(url, corps.formatted(false)), sa);
        assertEquals(404, appel(MockMvcRequestBuilders.get("/api/vitrines/iq"), null).getStatus(), "vitrine retirée : celle de la plateforme s'affiche");
        assertEquals(400, appel(put(url, "{\"titre\":\"IQ\",\"video\":\"vitrines/iq/absente.mp4\",\"publiee\":true}"), sa).getStatus(),
                "un média choisi doit exister dans la médiathèque");
        assertEquals(400, appel(put(url, "{\"titre\":\"IQ\",\"video\":\"../../etc/passwd\",\"publiee\":true}"), sa).getStatus());
    }

    @Test
    void une_video_televersee_rejoint_la_mediatheque_et_se_lit_par_plages() throws Exception {
        Long iq = tenants.findBySlug("iq").orElseThrow().getId();
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        byte[] video = new byte[2048];
        video[0] = 7;
        JsonNode depot = json.readTree(appel(MockMvcRequestBuilders.multipart("/api/superadmin/tenants/" + iq + "/vitrine/video")
                .file(new MockMultipartFile("fichier", "presentation.mp4", "video/mp4", video)), sa).getContentAsString());
        String chemin = depot.get("chemin").asText();
        assertTrue(chemin.startsWith("vitrines/iq/televersee-") && chemin.endsWith(".mp4"), chemin);
        assertEquals(400, appel(MockMvcRequestBuilders.multipart("/api/superadmin/tenants/" + iq + "/vitrine/video")
                .file(new MockMultipartFile("fichier", "virus.exe", "application/octet-stream", video)), sa).getStatus());
        assertTrue(appel(MockMvcRequestBuilders.get("/api/superadmin/mediatheque").param("prefixe", "vitrines/iq/"), sa)
                .getContentAsString().contains(chemin));

        MockHttpServletResponse plage = appel(MockMvcRequestBuilders.get("/api/media/" + chemin).header("Range", "bytes=0-99"), null);
        assertEquals(206, plage.getStatus(), "lecture par plages pour le lecteur vidéo");
        assertEquals(100, plage.getContentAsByteArray().length);
        assertEquals("video/mp4", plage.getContentType());

        Path racine = Path.of(System.getProperty("java.io.tmpdir"), "vina-media-test");
        Files.createDirectories(racine.resolve("aide"));
        Files.writeString(racine.resolve("aide/index.json"), "[]");
        Files.writeString(racine.resolve("aide/notes.txt"), "x");
        assertEquals(200, appel(MockMvcRequestBuilders.get("/api/media/aide/index.json"), null).getStatus());
        assertEquals(404, appel(MockMvcRequestBuilders.get("/api/media/aide/notes.txt"), null).getStatus(), "type de fichier non servi");
        assertEquals(404, appel(MockMvcRequestBuilders.get("/api/media/aide/absent.mp4"), null).getStatus());
        assertEquals(403, appel(MockMvcRequestBuilders.get("/api/superadmin/mediatheque"), login("admin.ti@iq.example", "Passw0rd!")).getStatus());
    }
}
