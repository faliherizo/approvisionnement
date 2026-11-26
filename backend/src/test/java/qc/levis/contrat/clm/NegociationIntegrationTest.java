package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.clm.application.port.ConseillerNegociation;
import qc.levis.contrat.clm.application.port.NotificateurClm;
import qc.levis.contrat.clm.domain.PositionNegociation;

/**
 * IA-05 (RST-55), vraie pile : l'organisation ouvre une négociation sur les clauses normatives du modèle ; la contrepartie,
 * par le portail externe, propose clause par clause ; l'IA suggère (ou les règles, en repli) sans jamais décider ; une
 * personne accepte, refuse ou contre-propose ; chaque tour fait une version comparable ; la clôture sur entente verse la
 * version négociée au contrat, au journal scellé et au dossier de preuve.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false"})
class NegociationIntegrationTest {

    static final List<String[]> ENVOIS = Collections.synchronizedList(new ArrayList<>());
    static final AtomicReference<Optional<ConseillerNegociation.Avis>> CONSEIL = new AtomicReference<>(Optional.empty());

    @TestConfiguration
    static class Doubles {
        @Bean
        @Primary
        NotificateurClm notificateurNegociation() {
            return (canal, destinataire, sujet, corps) -> {
                ENVOIS.add(new String[]{canal, destinataire, sujet, corps});
                return "SIMULE";
            };
        }

        @Bean
        @Primary
        ConseillerNegociation conseillerDeTest() {
            return demande -> CONSEIL.get();
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private Cookie admin;
    private Cookie redacteur;

    @BeforeEach
    void preparer() throws Exception {
        admin = login("admin.affaires@iq.example");
        redacteur = login("initiateur@iq.example");
        ENVOIS.clear();
        CONSEIL.set(Optional.empty());
    }

    @Test
    void negociation_complete_jusqu_a_la_version_negociee_au_contrat() throws Exception {
        long trousse = trousse(modelePublie());
        assertEquals(1, corps(post("/api/clm/trousses/" + trousse + "/generer", redacteur, "{}"), 200).get("version").asInt());
        String courriel = "juriste@agrinord.example";
        corps(post("/api/clm/trousses/" + trousse + "/negociations", redacteur, "{\"nom\":\"Agrinord\",\"courriel\":\"pas-un-courriel\"}"), 400);
        JsonNode ouverte = corps(post("/api/clm/trousses/" + trousse + "/negociations", redacteur, ouverture(courriel)), 200);
        long id = ouverte.get("id").asLong();
        assertEquals("ATTENTE_CONTREPARTIE", ouverte.get("etat").asText());
        assertEquals(3, ouverte.get("clauses").size());
        assertEquals("Le paiement est fait dans les 30 jours suivant la réception de la facture.", ouverte.at("/clauses/0/texte").asText());
        assertEquals("[\"ANNULER\"]", ouverte.get("actions").toString());
        assertTrue(ouverte.at("/versions/0/integre").asBoolean());
        assertTrue(corps(post("/api/clm/trousses/" + trousse + "/negociations", redacteur, ouverture(courriel)), 409)
                .get("message").asText().contains("déjà en cours"));
        assertTrue(corps(post("/api/clm/trousses/" + trousse + "/lancer", redacteur, "{}"), 400).get("message").asText()
                .contains("Négociation en cours"));

        String jeton = jeton(courriel);
        String[] invitation = ENVOIS.stream().filter(e -> e[1].equals(courriel)).findFirst().orElseThrow();
        assertEquals("Contract to negotiate: Bail de machinerie", invitation[2], "invitation dans la langue de la contrepartie");
        corps(get("/api/clm/portail/negociation/court"), 404);
        corps(get("/api/clm/portail/negociation/" + "x".repeat(40)), 404);
        JsonNode portail = corps(get("/api/clm/portail/negociation/" + jeton), 200);
        assertEquals("ATTENTE_CONTREPARTIE", portail.get("etat").asText());
        assertEquals("en", portail.get("langue").asText());
        assertEquals("[1]", portail.get("versions").toString());

        String base = "/api/clm/portail/negociation/" + jeton;
        corps(post(base + "/propositions", null, "{\"propositions\":[]}"), 400);
        corps(post(base + "/propositions", null, proposition("inconnue", "x")), 400);
        corps(post(base + "/propositions", null, "{\"propositions\":[" + item("paiement", "a") + "," + item("paiement", "b") + "]}"), 400);
        corps(post(base + "/propositions", null, proposition("paiement", "Le paiement est fait dans les 30 jours suivant la réception de la facture.")), 400);
        JsonNode proposee = corps(post(base + "/propositions", null, "{\"propositions\":["
                + item("paiement", "Le paiement est fait dans les 60 jours suivant la réception de la facture.") + ","
                + item("duree", "Le contrat dure 24 mois à compter de sa signature.") + "]}"), 200);
        assertEquals("ATTENTE_INTERNE", proposee.get("etat").asText());
        assertEquals(2, proposee.get("propositions").size());
        corps(post(base + "/propositions", null, proposition("duree", "x")), 409);
        corps(post(base + "/acceptation", null, "{}"), 409);

        JsonNode interne = corps(get("/api/clm/trousses/" + trousse + "/negociations", redacteur), 200).get(0);
        assertEquals("[\"SUGGERER\",\"DECIDER\",\"ANNULER\"]", interne.get("actions").toString());
        long paiement = interne.at("/propositions/0/id").asLong();
        long duree = interne.at("/propositions/1/id").asLong();
        assertTrue(interne.at("/propositions/0/suggestion").isNull());

        CONSEIL.set(Optional.of(new ConseillerNegociation.Avis("Le délai double.\nPOSITION : CONTRE_PROPOSER",
                Optional.of(PositionNegociation.CONTRE_PROPOSER), "llama-test")));
        JsonNode suggeree = corps(post("/api/clm/negociations/" + id + "/suggestions", redacteur, "{}"), 200);
        assertEquals("CONTRE_PROPOSER", suggeree.at("/propositions/0/suggestion/position").asText());
        assertEquals("IA locale (llama-test)", suggeree.at("/propositions/0/suggestion/source").asText());
        assertEquals("EN_ATTENTE", suggeree.at("/propositions/0/decision").asText(), "la suggestion ne décide jamais");
        CONSEIL.set(Optional.of(new ConseillerNegociation.Avis("Sans avis tranché.", Optional.empty(), "llama-test")));
        JsonNode sansPosition = corps(post("/api/clm/negociations/" + id + "/suggestions", redacteur, "{}"), 200);
        assertEquals("ACCEPTER", sansPosition.at("/propositions/0/suggestion/position").asText(), "la position des règles comble l'avis muet");
        CONSEIL.set(Optional.empty());
        JsonNode repli = corps(post("/api/clm/negociations/" + id + "/suggestions", redacteur, "{}"), 200);
        assertEquals("Règles de la bibliothèque (repli déterministe)", repli.at("/propositions/1/suggestion/source").asText());
        assertTrue(repli.at("/propositions/1/suggestion/resume").asText().startsWith("Écart sur « Durée » :"));

        corps(post("/api/clm/negociations/" + id + "/decisions", redacteur, decisions(decision(paiement, "ACCEPTEE", null, null))), 400);
        corps(post("/api/clm/negociations/" + id + "/decisions", redacteur, decisions(decision(999_999L, "ACCEPTEE", null, null))), 404);
        corps(post("/api/clm/negociations/" + id + "/decisions", redacteur, decisions(decision(paiement, "REFUSEE", null, null),
                decision(duree, "ACCEPTEE", null, null))), 400);
        JsonNode tour1 = corps(post("/api/clm/negociations/" + id + "/decisions", redacteur, decisions(
                decision(paiement, "CONTRE_PROPOSEE", "Le paiement est fait dans les 40 jours suivant la réception de la facture.", "Compromis"),
                decision(duree, "ACCEPTEE", null, null))), 200);
        assertEquals("ATTENTE_CONTREPARTIE", tour1.get("etat").asText());
        assertEquals(2, tour1.get("versionCourante").asInt());
        assertEquals(2, tour1.get("tour").asInt());
        corps(post("/api/clm/negociations/" + id + "/cloture", redacteur, "{}"), 409);

        JsonNode ecarts = corps(get("/api/clm/negociations/" + id + "/comparaison?de=1&a=2", redacteur), 200).get("ecarts");
        assertEquals("MODIFIEE", ecarts.at("/0/changement").asText());
        assertEquals("MODIFIEE", ecarts.at("/1/changement").asText());
        assertEquals("INCHANGEE", ecarts.at("/2/changement").asText());
        assertTrue(ecarts.at("/0/segments").toString().contains("{\"nature\":\"AJOUT\",\"texte\":\"40\"}"), ecarts.toString());
        corps(get("/api/clm/negociations/" + id + "/comparaison?de=1&a=9", redacteur), 404);

        JsonNode reponse = corps(get(base + "/comparaison?de=1&a=2"), 200);
        assertEquals(3, reponse.get("ecarts").size());
        JsonNode vueExterne = corps(get(base), 200);
        assertEquals("CONTRE_PROPOSEE", vueExterne.at("/propositions/0/decision").asText());
        assertEquals("Compromis", vueExterne.at("/propositions/0/justification").asText());
        assertTrue(vueExterne.at("/propositions/0/decidePar").isNull(), "la contrepartie ne voit pas qui a décidé");
        assertTrue(vueExterne.at("/propositions/0/suggestion").isNull(), "ni la suggestion interne");
        assertEquals("ENTENTE", corps(post(base + "/acceptation", null, "{}"), 200).get("etat").asText());

        JsonNode entente = corps(get("/api/clm/trousses/" + trousse + "/negociations", redacteur), 200).get(0);
        assertEquals("[\"CLORE\",\"ANNULER\"]", entente.get("actions").toString());
        assertEquals("[]", corps(get("/api/clm/trousses/" + trousse + "/negociations", login("auditeur@iq.example")), 200)
                .at("/0/actions").toString(), "l'audit consulte sans agir");
        JsonNode close = corps(post("/api/clm/negociations/" + id + "/cloture", redacteur, "{}"), 200);
        assertEquals("CLOSE", close.get("etat").asText());
        corps(get(base), 409);

        JsonNode detail = corps(get("/api/clm/trousses/" + trousse, redacteur), 200);
        assertTrue(detail.get("documents").toString().contains("-negocie.pdf"), "la version négociée devient le contrat de la trousse");
        List<String> versions = new ArrayList<>();
        detail.get("documents").forEach(d -> versions.add(d.get("cleDocument").asText() + " v" + d.get("version").asInt() + " " + d.get("statut").asText()));
        assertTrue(versions.containsAll(List.of("contrat-genere v1 REMPLACE", "contrat-genere v2 BROUILLON")), versions.toString());
        String journal = corps(get("/api/clm/trousses/" + trousse + "/historique", redacteur), 200).toString();
        for (String evt : List.of("NEGOCIATION_OUVERTE", "NEGOCIATION_INVITATION", "NEGOCIATION_VERSION", "NEGOCIATION_PROPOSITION",
                "NEGOCIATION_SUGGESTION", "NEGOCIATION_DECISION", "NEGOCIATION_REPONSE", "NEGOCIATION_ACCEPTEE", "NEGOCIATION_CLOSE")) {
            assertTrue(journal.contains(evt), evt);
        }
        assertTrue(corps(get("/api/clm/trousses/" + trousse + "/preuve", redacteur), 200).at("/integrite/integre").asBoolean());
        Map<String, String> archive = archive(trousse);
        String piece = archive.get("negociations/negociation-" + id + ".json");
        assertNotNull(piece, archive.keySet().toString());
        assertTrue(piece.contains("\"v2\"") && piece.contains("40 jours") && piece.contains("CONTRE_PROPOSEE"), piece);
        assertTrue(archive.get("manifeste.json").contains("negociations/negociation-" + id + ".json"), "la pièce est scellée au manifeste");
        String lancement = corps(post("/api/clm/trousses/" + trousse + "/lancer", redacteur, "{}"), 400).get("message").asText();
        assertFalse(lancement.contains("Négociation en cours"), lancement);
    }

    @Test
    void entente_directe_annulation_et_refus() throws Exception {
        long sansModele = corps(post("/api/clm/trousses", redacteur, "{\"titre\":\"Sans modèle\",\"risque\":\"FAIBLE\"}"), 200)
                .at("/resume/id").asLong();
        assertTrue(corps(post("/api/clm/trousses/" + sansModele + "/negociations", redacteur, ouverture("a@b.example")), 409)
                .get("message").asText().contains("modèle"));
        corps(post("/api/clm/trousses/" + sansModele + "/negociations", redacteur, "null"), 400);
        long sansClause = trousse(modelePublie("[]"));
        assertTrue(corps(post("/api/clm/trousses/" + sansClause + "/negociations", redacteur, ouverture("b@c.example")), 409)
                .get("message").asText().contains("n'en retient aucune"));

        long trousse = trousse(modelePublie());
        String courriel = "direct@agrinord.example";
        long id = corps(post("/api/clm/trousses/" + trousse + "/negociations", redacteur,
                "{\"nom\":\"Agrinord\",\"courriel\":\"" + courriel + "\"}"), 200).get("id").asLong();
        assertTrue(ENVOIS.stream().anyMatch(e -> e[1].equals(courriel) && e[2].startsWith("Contrat à négocier")));
        String base = "/api/clm/portail/negociation/" + jeton(courriel);
        corps(post("/api/clm/negociations/" + id + "/suggestions", redacteur, "{}"), 409);
        corps(post(base + "/propositions", null, proposition("duree", "Le contrat dure 12 mois, renouvelable une fois.")), 200);
        long p = corps(get("/api/clm/trousses/" + trousse + "/negociations", redacteur), 200).at("/0/propositions/0/id").asLong();
        corps(post("/api/clm/negociations/" + id + "/decisions", redacteur, "{\"decisions\":[{\"propositionId\":" + p
                + ",\"decision\":\"PEUT-ETRE\"}]}"), 400);
        JsonNode entente = corps(post("/api/clm/negociations/" + id + "/decisions", redacteur, decisions(decision(p, "ACCEPTEE", null, null))), 200);
        assertEquals("ENTENTE", entente.get("etat").asText(), "tout accepté tel quel : entente");
        corps(post("/api/clm/negociations/" + id + "/annulation", redacteur, "{\"motif\":\" \"}"), 400);
        JsonNode annulee = corps(post("/api/clm/negociations/" + id + "/annulation", redacteur, "{\"motif\":\"Projet abandonné\"}"), 200);
        assertEquals("ANNULEE", annulee.get("etat").asText());
        assertEquals("[]", annulee.get("actions").toString());
        assertTrue(corps(get(base), 409).get("message").asText().contains("terminée"));
        corps(post("/api/clm/negociations/999999/annulation", redacteur, "{\"motif\":\"x\"}"), 404);
        corps(post("/api/clm/trousses/" + trousse + "/negociations", redacteur, ouverture("autre@agrinord.example")), 200);
    }

    // --- Données ---
    private long modelePublie() throws Exception {
        return modelePublie("""
                [{"cle":"paiement","titre":"Modalités de paiement","texte":"Le paiement est fait dans les 30 jours suivant la réception de la facture."},
                 {"cle":"duree","titre":"Durée","texte":"Le contrat dure 12 mois à compter de sa signature."},
                 {"cle":"confidentialite","titre":"Confidentialité","texte":"Les parties gardent confidentielles les informations échangées."}]""");
    }

    private long modelePublie(String clauses) throws Exception {
        long id = corps(post("/api/clm/modeles", redacteur, "{\"nom\":\"Bail de machinerie négociable\",\"corps\":"
                + "\"Bail entre Investissement Québec et la contrepartie.\\n{{clauses}}\",\"champs\":[],\"clauses\":" + clauses + "}"), 200)
                .get("id").asLong();
        corps(post("/api/clm/modeles/" + id + "/soumettre", redacteur, "{}"), 200);
        corps(post("/api/clm/modeles/" + id + "/publier", admin, "{}"), 200);
        return id;
    }

    private long trousse(long modele) throws Exception {
        return corps(post("/api/clm/trousses", redacteur, "{\"titre\":\"Bail de machinerie\",\"risque\":\"FAIBLE\",\"modeleId\":" + modele + "}"), 200)
                .at("/resume/id").asLong();
    }

    private static String ouverture(String courriel) {
        return "{\"nom\":\"Me Lise Gagnon\",\"courriel\":\"" + courriel + "\",\"entreprise\":\"Agrinord inc.\",\"langue\":\"EN\"}";
    }

    private static String item(String cle, String texte) {
        return "{\"cle\":\"" + cle + "\",\"texte\":\"" + texte + "\",\"commentaire\":\"Ajustement demandé\"}";
    }

    private static String proposition(String cle, String texte) {
        return "{\"propositions\":[" + item(cle, texte) + "]}";
    }

    private static String decision(long id, String decision, String texte, String justification) {
        return "{\"propositionId\":" + id + ",\"decision\":\"" + decision + "\"" + (texte == null ? "" : ",\"texte\":\"" + texte + "\"")
                + (justification == null ? "" : ",\"justification\":\"" + justification + "\"") + "}";
    }

    private static String decisions(String... d) {
        return "{\"decisions\":[" + String.join(",", d) + "]}";
    }

    private String jeton(String destinataire) {
        Pattern p = Pattern.compile("/portail/negociation/([A-Za-z0-9_-]+)");
        for (int i = ENVOIS.size() - 1; i >= 0; i--) {
            Matcher m = p.matcher(ENVOIS.get(i)[3]);
            if (ENVOIS.get(i)[1].equals(destinataire) && m.find()) {
                return m.group(1);
            }
        }
        throw new AssertionError("Aucune invitation de négociation pour " + destinataire);
    }

    private Map<String, String> archive(long trousse) throws Exception {
        byte[] zip = mvc.perform(get("/api/clm/trousses/" + trousse + "/preuve/export", redacteur)).andReturn().getResponse().getContentAsByteArray();
        Map<String, String> fichiers = new HashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
                fichiers.put(e.getName(), new String(in.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return fichiers;
    }

    // --- HTTP ---
    private Cookie login(String email) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"Passw0rd!\"}")).andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private static AbstractMockHttpServletRequestBuilder<?> post(String url, Cookie c, String corps) {
        AbstractMockHttpServletRequestBuilder<?> r = MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(corps);
        return c == null ? r : r.cookie(c);
    }

    private static AbstractMockHttpServletRequestBuilder<?> get(String url) {
        return MockMvcRequestBuilders.get(url);
    }

    private static AbstractMockHttpServletRequestBuilder<?> get(String url, Cookie c) {
        return MockMvcRequestBuilders.get(url).cookie(c);
    }

    private JsonNode corps(AbstractMockHttpServletRequestBuilder<?> req, int statut) throws Exception {
        var r = mvc.perform(req).andReturn().getResponse();
        String s = r.getContentAsString(StandardCharsets.UTF_8);
        assertEquals(statut, r.getStatus(), s);
        return s.isBlank() ? json.createObjectNode() : json.readTree(s);
    }
}
