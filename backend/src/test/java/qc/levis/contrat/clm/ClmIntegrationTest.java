package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import qc.levis.contrat.audit.application.repository.AuditLogRepository;
import qc.levis.contrat.audit.domain.AuditLog;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.clm.application.port.NotificateurClm;
import qc.levis.contrat.clm.application.repository.MessageSortantRepository;
import qc.levis.contrat.clm.domain.MessageSortant;
import qc.levis.contrat.clm.domain.Relances;
import qc.levis.contrat.commun.module.application.ModuleService;

/**
 * Parcours CLM de bout en bout sur la vraie pile (H2 + Liquibase + sécurité) : modèle, trousse préremplie depuis
 * Salesforce simulé, parcours mixte, portail du signataire avec code SMS, preuve scellée et retour de statuts.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.clm.planificateur.actif=false", "app.clm.documents.taille-max-octets=200000",
        "app.publication.simulateur.repertoire=${java.io.tmpdir}/vina-sim-clm"})
class ClmIntegrationTest {

    static final List<String[]> ENVOIS = Collections.synchronizedList(new ArrayList<>());

    @TestConfiguration
    static class Capture {
        @Bean
        @Primary
        NotificateurClm notificateurCapture() {
            return (canal, destinataire, sujet, corps) -> {
                ENVOIS.add(new String[]{canal, destinataire, sujet, corps});
                return "SIMULE";
            };
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ModuleService modules;
    @Autowired UserRepository users;
    @Autowired MessageSortantRepository sorties;
    @Autowired AuditLogRepository auditLogs;
    @Autowired JdbcTemplate jdbc;

    Cookie admin;
    Cookie redacteur;
    Cookie juriste;
    Cookie direction;

    @BeforeEach
    void preparer() throws Exception {
        Long tenant = users.findByEmail("admin@levis.qc.ca").orElseThrow().getTenantId();
        modules.activer(tenant, "clm_ia", "test");
        admin = login("admin@levis.qc.ca");
        redacteur = login("redacteur@levis.qc.ca");
        juriste = login("juriste@levis.qc.ca");
        direction = login("direction@levis.qc.ca");
        ENVOIS.clear();
    }

    @Test
    void parcours_complet_du_modele_a_la_preuve() throws Exception {
        long modeleId = modelePublie();

        JsonNode t = appel(post("/api/clm/trousses", redacteur, """
                {"titre":"Aide financière Agri-Nord","unite":"Programmes","risque":"MOYEN","modeleId":%d,"sfDossierId":"DEM-2026-0001","copies":"suivi@iq.example"}
                """.formatted(modeleId)), status().isOk());
        long id = t.at("/resume/id").asLong();
        assertEquals("SALESFORCE", t.at("/resume/source").asText());
        assertEquals("250000.00", t.at("/valeurs/montant/valeur").asText());
        assertEquals("SALESFORCE", t.at("/valeurs/montant/provenance").asText());

        JsonNode signataire = appel(post("/api/clm/trousses/" + id + "/participants", redacteur, """
                {"nom":"Marie Tremblay","courriel":"marie@agrinord.example","telephone":"+1 514 555 0101","roleContrat":"SIGNATAIRE_1","externe":true}
                """), status().isOk());
        assertEquals("CODE_SMS", signataire.get("niveauIdentite").asText());
        appel(post("/api/clm/trousses/" + id + "/participants", redacteur, """
                {"nom":"Doublon","courriel":"marie@agrinord.example","telephone":"+15145550101","roleContrat":"SIGNATAIRE_2","externe":true}
                """), status().isConflict());

        JsonNode genere = appel(post("/api/clm/trousses/" + id + "/generer", redacteur, "{}"), status().isOk());
        assertEquals("PDF", genere.get("type").asText());
        assertEquals("PROPRE", genere.get("analyse").asText());
        String texte = mvc.perform(MockMvcRequestBuilders.get("/api/clm/trousses/" + id + "/documents/" + genere.get("id").asLong() + "/texte")
                .cookie(redacteur)).andReturn().getResponse().getContentAsString();
        assertTrue(texte.contains("Coopérative Agri-Nord"), texte);
        assertTrue(texte.contains("Garantie"), "clause conditionnelle retenue au-delà de 100 000 $");

        assertEquals("DEM-2026-0001", appel(post("/api/clm/trousses/" + id + "/preremplissage", redacteur, "{\"sfDossierId\":\"DEM-2026-0001\"}"),
                status().isOk()).get("numero").asText(), "données Salesforce relues avant de régénérer");
        assertEquals(2, appel(post("/api/clm/trousses/" + id + "/generer", redacteur, "{}"), status().isOk()).get("version").asInt());
        JsonNode versions = appel(get("/api/clm/trousses/" + id + "/documents/" + genere.get("cleDocument").asText() + "/versions", redacteur),
                status().isOk());
        assertEquals("REMPLACE", versions.get(1).get("statut").asText(), "la régénération remplace la version précédente");

        JsonNode docx = televerser(id, "annexe.docx", docx("Annexe : conditions du programme Essor."), status().isOk());
        assertEquals("DOCX", docx.get("type").asText());
        televerser(id, "faux.pdf", "pas un pdf".getBytes(), status().isBadRequest());
        televerser(id, "gros.pdf", gros(), status().isBadRequest());

        JsonNode lance = appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isOk());
        assertEquals("EN_TRAITEMENT", lance.at("/resume/statut").asText());

        JsonNode tachesJuriste = appel(get("/api/clm/taches", juriste), status().isOk());
        assertEquals(1, tachesJuriste.size());
        long validation = tachesJuriste.get(0).get("etapeId").asLong();
        appel(post("/api/clm/etapes/" + validation + "/decider", direction, "{\"decision\":\"APPROUVE\"}"), status().isForbidden());
        appel(post("/api/clm/etapes/" + validation + "/decider", juriste, "{\"decision\":\"REJETE\"}"), status().isBadRequest());
        appel(post("/api/clm/etapes/" + validation + "/decider", juriste, "{\"decision\":\"APPROUVE\",\"commentaire\":\"Conforme\"}"), status().isOk());
        appel(post("/api/clm/etapes/" + validation + "/decider", juriste, "{\"decision\":\"APPROUVE\"}"), status().isConflict());

        long approbation = appel(get("/api/clm/taches", direction), status().isOk()).get(0).get("etapeId").asLong();
        appel(post("/api/clm/etapes/" + approbation + "/decider", direction, "{\"decision\":\"APPROUVE\"}"), status().isOk());

        String jeton = dernier("INVITATION", "COURRIEL", "/portail/([A-Za-z0-9_-]+)");
        JsonNode portail = appel(get("/api/clm/portail/" + jeton, null), status().isOk());
        assertEquals("ENVOYEE", portail.get("statut").asText());
        assertTrue(portail.get("documents").isEmpty(), "documents masqués avant vérification");
        appel(post("/api/clm/portail/" + jeton + "/signer", null, "{\"nomComplet\":\"Marie Tremblay\",\"consentement\":true}"), status().isConflict());

        appel(post("/api/clm/portail/" + jeton + "/verification", null, "{}"), status().isOk());
        String code = dernier("CODE", "SMS", "(\\d{6})");
        appel(post("/api/clm/portail/" + jeton + "/verification/reponse", null, "{\"reponse\":\"000000\"}"), status().isBadRequest());
        JsonNode verifie = appel(post("/api/clm/portail/" + jeton + "/verification/reponse", null, "{\"reponse\":\"" + code + "\"}"), status().isOk());
        assertTrue(verifie.get("verifiee").asBoolean());
        assertEquals(2, verifie.get("documents").size());
        mvc.perform(MockMvcRequestBuilders.get("/api/clm/portail/" + jeton + "/documents/" + verifie.at("/documents/0/id").asLong()))
                .andExpect(status().isOk());

        appel(post("/api/clm/portail/" + jeton + "/signer", null, "{\"nomComplet\":\"Marie Tremblay\",\"consentement\":false}"), status().isBadRequest());
        appel(post("/api/clm/portail/" + jeton + "/signer", null, "{\"nomComplet\":\"Marie Tremblay\",\"consentement\":true}"), status().isBadRequest());
        appel(post("/api/clm/portail/" + jeton + "/signer", null,
                "{\"nomComplet\":\"X\",\"consentement\":true,\"champs\":{\"titreSignataire\":\"Présidente\"}}"), status().isBadRequest());
        appel(get("/api/clm/portail/court", null), status().isNotFound());
        JsonNode recu = appel(post("/api/clm/portail/" + jeton + "/signer", null,
                "{\"nomComplet\":\"Marie Tremblay\",\"consentement\":true,\"champs\":{\"titreSignataire\":\"Présidente\"}}"), status().isOk());
        assertTrue(recu.get("attestation").asText().startsWith("LOC-"));
        appel(post("/api/clm/portail/" + jeton + "/signer", null, "{\"nomComplet\":\"X\",\"consentement\":true}"), status().isConflict());
        assertEquals("Marie Tremblay", appel(get("/api/clm/portail/" + jeton, null), status().isOk()).at("/recu/signataire").asText(),
                "le reçu reste consultable après la signature");

        JsonNode fini = appel(get("/api/clm/trousses/" + id, redacteur), status().isOk());
        assertEquals("TERMINEE", fini.at("/resume/statut").asText());
        boolean signe = false;
        for (JsonNode d : fini.get("documents")) {
            signe |= "SIGNE".equals(d.get("statut").asText()) && "PDF".equals(d.get("type").asText()) && d.get("version").asInt() == 3;
        }
        assertTrue(signe, "version PDF signée et verrouillée");
        assertTrue(ENVOIS.stream().anyMatch(e -> "suivi@iq.example".equals(e[1])), "copie de fin de parcours");

        JsonNode integrite = appel(post("/api/clm/trousses/" + id + "/preuve/integrite", redacteur, "{}"), status().isOk());
        assertTrue(integrite.get("integre").asBoolean(), integrite.toString());
        byte[] zip = mvc.perform(MockMvcRequestBuilders.get("/api/clm/trousses/" + id + "/preuve/export").cookie(redacteur))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        List<String> entrees = entrees(zip);
        assertTrue(entrees.containsAll(List.of("manifeste.json", "sceau.json", "journal.json", "certificat.json")), entrees.toString());

        JsonNode avant = appel(get("/api/clm/admin/integrations/salesforce", admin), status().isOk());
        assertTrue(avant.get("enAttente").asLong() >= 2);
        appel(post("/api/clm/admin/integrations/salesforce/relayer", admin, "{}"), status().isOk());
        JsonNode apres = appel(get("/api/clm/admin/integrations/salesforce", admin), status().isOk());
        assertEquals(0, apres.get("enAttente").asLong());

        JsonNode bord = appel(get("/api/clm/tableau-de-bord", redacteur), status().isOk());
        assertTrue(bord.at("/indicateurs/terminees").asLong() >= 1);
        String csv = mvc.perform(MockMvcRequestBuilders.get("/api/clm/suivi/export").cookie(redacteur))
                .andReturn().getResponse().getContentAsString();
        assertTrue(csv.contains("TERMINEE"));

        JsonNode analyse = appel(post("/api/clm/trousses/" + id + "/documents/" + genere.get("id").asLong() + "/analyse", redacteur, "{}"),
                status().isOk());
        assertEquals("A_REVISER", analyse.get("statut").asText());
        appel(post("/api/clm/analyses/" + analyse.get("id").asLong() + "/revision", redacteur, "{\"valide\":true}"), status().isBadRequest());
        appel(post("/api/clm/analyses/" + analyse.get("id").asLong() + "/revision", redacteur,
                "{\"valide\":true,\"justification\":\"Montant confirmé\"}"), status().isOk());
        assertEquals(1, appel(get("/api/clm/trousses/" + id + "/analyses", redacteur), status().isOk()).size());
        assertTrue(appel(get("/api/clm/taches?filtre=terminees", juriste), status().isOk()).toString().contains("\"maDecision\":\"APPROUVE\""));

        // Altération directe en base, hors de l'application : empreinte du document et attestation ne correspondent plus.
        jdbc.update("UPDATE clm_document SET contenu = ? WHERE id = ?", "falsifié".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                docx.get("id").asLong());
        jdbc.update("UPDATE clm_invitation SET nom_signe = ? WHERE trousse_id = ? AND statut = 'SIGNEE'", "Faussaire", id);
        JsonNode alteree = appel(post("/api/clm/trousses/" + id + "/preuve/integrite", redacteur, "{}"), status().isOk());
        assertFalse(alteree.get("integre").asBoolean());
        assertTrue(alteree.get("journalIntegre").asBoolean(), "le journal scellé n'a pas été touché");
        assertEquals("annexe.docx v1", alteree.at("/documentsAlteres/0").asText());
        assertEquals("Faussaire", alteree.at("/attestationsInvalides/0").asText());
    }

    @Test
    void reprises_salesforce_et_retour_pour_correction() throws Exception {
        JsonNode apercu = appel(get("/api/clm/salesforce/dossiers/DEM-2026-0002", redacteur), status().isOk());
        assertEquals("DEM-2026-0002", apercu.get("numero").asText(), "recherche avant création (UI-10)");
        assertTrue(apercu.at("/champs/Demandeur").isTextual());
        appel(get("/api/clm/salesforce/dossiers/INCONNU-999", redacteur), status().isNotFound());
        JsonNode t = appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Retour\",\"sfDossierId\":\"a0B5e00000GRT002\"}"), status().isOk());
        long id = t.at("/resume/id").asLong();
        appel(get("/api/clm/trousses/" + id, direction), status().isNotFound());
        televerser(id, "contrat.pdf", pdfMinimal(), status().isOk());
        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, """
                [{"cle":"v1","libelle":"Validation juridique","phase":1,"type":"VALIDATION","affectations":[{"type":"UTILISATEUR","valeur":"juriste@levis.qc.ca"}]},
                 {"cle":"a1","libelle":"Approbation","phase":1,"type":"APPROBATION","affectations":[{"type":"ROLE","valeur":"DIRECTION"}]}]
                """), status().isOk());
        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, """
                [{"cle":"v1","phase":2,"type":"VALIDATION","affectations":[]}]
                """), status().isBadRequest());
        appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isOk());
        assertEquals(1, appel(get("/api/clm/taches", juriste), status().isOk()).size());
        assertEquals(1, appel(get("/api/clm/taches", direction), status().isOk()).size(), "phase parallèle");

        long etape = appel(get("/api/clm/taches", juriste), status().isOk()).get(0).get("etapeId").asLong();
        appel(put("/api/clm/etapes/" + etape + "/affectations", redacteur, """
                {"affectations":[{"type":"ROLE","valeur":"JURIDIQUE"}],"motif":"Absence"}
                """), status().isOk());
        appel(post("/api/clm/etapes/" + etape + "/decider", juriste, "{\"decision\":\"RETOUR\",\"commentaire\":\"Clause 4 à corriger\"}"), status().isOk());
        assertEquals("BROUILLON", appel(get("/api/clm/trousses/" + id, redacteur), status().isOk()).at("/resume/statut").asText());

        appel(put("/api/clm/admin/integrations/salesforce/simulation/panne", admin, "{\"panne\":true}"), status().isOk());
        appel(post("/api/clm/admin/integrations/salesforce/relayer", admin, "{}"), status().isOk());
        JsonNode etat = appel(get("/api/clm/admin/integrations/salesforce", admin), status().isOk());
        boolean reprise = false;
        for (JsonNode m : etat.get("journal")) {
            reprise |= m.get("tentatives").asInt() > 0 && m.get("derniereErreur").asText().contains("panne");
        }
        assertTrue(reprise, "échec consigné et reprise planifiée");
        long enReprise = -1;
        for (JsonNode m : etat.get("journal")) {
            if (m.get("trousseId").asLong() == id && !"ENVOYE".equals(m.get("statut").asText())) {
                enReprise = m.get("id").asLong();
            }
        }
        assertEquals(0, appel(post("/api/clm/admin/integrations/salesforce/trousses/" + id + "/relancer", admin, "{}"), status().isOk())
                .get("relances").asInt(), "un retour encore en reprise automatique n'est pas en file d'erreur");
        MessageSortant sortie = sorties.findById(enReprise).orElseThrow();
        for (int i = 0; i < Relances.REPRISES_MAX; i++) {
            sortie.echouer("Salesforce indisponible (panne simulée).", java.time.Instant.now());
        }
        sorties.save(sortie);
        assertEquals(1, appel(post("/api/clm/admin/integrations/salesforce/trousses/" + id + "/relancer", admin, "{}"), status().isOk())
                .get("relances").asInt(), "la file d'erreur d'une trousse est remise en file sur demande (UI-18)");
        assertEquals("EN_ATTENTE", appel(post("/api/clm/admin/integrations/salesforce/evenements/" + enReprise + "/relancer", admin, "{}"),
                status().isOk()).get("statut").asText());
        appel(put("/api/clm/admin/integrations/salesforce/simulation/panne", admin, "{\"panne\":false}"), status().isOk());
        appel(post("/api/clm/trousses/" + id + "/preremplissage", redacteur, "{\"sfDossierId\":\"INCONNU\"}"), status().isNotFound());

        JsonNode historique = appel(get("/api/clm/trousses/" + id + "/historique", redacteur), status().isOk());
        assertTrue(historique.toString().contains("REASSIGNATION"));
        assertTrue(historique.toString().contains("RETOUR_CORRECTION"));
    }

    @Test
    void administration_reservee_et_messages_valides() throws Exception {
        appel(get("/api/clm/admin/parametres", redacteur), status().isForbidden());
        JsonNode p = appel(get("/api/clm/admin/parametres", admin), status().isOk());
        assertEquals(200000, p.get("tailleMaxOctets").asLong());
        appel(put("/api/clm/admin/messages", admin, "{\"evenement\":\"RAPPEL\",\"canal\":\"COURRIEL\",\"sujet\":\"R {{inconnue}}\",\"corps\":\"x\"}"),
                status().isBadRequest());
        appel(put("/api/clm/admin/messages", admin, "{\"evenement\":\"RAPPEL\",\"canal\":\"COURRIEL\",\"sujet\":\"Rappel {{reference}}\",\"corps\":\"Merci\"}"),
                status().isOk());
        appel(post("/api/clm/admin/messages/RAPPEL/test", admin, "{\"canal\":\"COURRIEL\",\"destinataire\":\"pas-une-adresse\"}"), status().isBadRequest());
        assertEquals("SIMULE", appel(post("/api/clm/admin/messages/RAPPEL/test", admin, "{\"canal\":\"COURRIEL\",\"destinataire\":\"a@b.ca\"}"),
                status().isOk()).get("statut").asText());
        appel(put("/api/clm/admin/messages", admin, "{\"evenement\":\"RAPPEL\",\"canal\":\"FAX\",\"sujet\":\"s\",\"corps\":\"c\"}"), status().isBadRequest());
        appel(put("/api/clm/admin/messages", admin, "{\"evenement\":\"RAPPEL\",\"canal\":\"SMS\",\"sujet\":\" \",\"corps\":\"c\"}"), status().isBadRequest());
        String rappelSms = "{\"evenement\":\"RAPPEL\",\"canal\":\"SMS\",\"sujet\":\"Rappel\",\"corps\":\"Rappel {{reference}}\",\"actif\":%s}";
        appel(put("/api/clm/admin/messages", admin, rappelSms.formatted("false")), status().isOk());
        assertEquals("DESACTIVE", appel(post("/api/clm/admin/messages/RAPPEL/test", admin, "{\"canal\":\"SMS\",\"destinataire\":\"+15145550101\"}"),
                status().isOk()).get("statut").asText(), "un message désactivé n'est pas envoyé");
        appel(put("/api/clm/admin/messages", admin, rappelSms.formatted("true")), status().isOk());
        appel(get("/api/clm/portail/jeton-inexistant-0123456789", null), status().isNotFound());
    }

    @Test
    void cycle_de_vie_modeles_documents_et_trousse() throws Exception {
        long m = appel(post("/api/clm/modeles", redacteur, "{\"nom\":\"Bail\",\"corps\":\"{{inconnue}}\",\"champs\":[],\"clauses\":[],\"parcours\":[]}"),
                status().isOk()).get("id").asLong();
        assertFalse(appel(get("/api/clm/modeles/" + m + "/validation", redacteur), status().isOk()).get("valide").asBoolean());
        appel(post("/api/clm/modeles/" + m + "/soumettre", redacteur, "{}"), status().isBadRequest());
        JsonNode lu = appel(get("/api/clm/modeles/" + m, redacteur), status().isOk());
        appel(put("/api/clm/modeles/" + m, redacteur, "{\"nom\":\"Bail\",\"corps\":\"Loyer {{loyer}}\",\"champs\":[{\"cle\":\"loyer\",\"libelle\":\"Loyer\",\"type\":\"TEXTE\"}],"
                + "\"clauses\":[],\"parcours\":[],\"verrou\":" + (lu.get("verrou").asLong() + 7) + "}"), status().isConflict());
        appel(put("/api/clm/modeles/" + m, redacteur, "{\"nom\":\"Bail\",\"categorie\":\"BAIL\",\"groupes\":\"Immeubles\",\"corps\":\"Loyer {{loyer}}\","
                + "\"champs\":[{\"cle\":\"loyer\",\"libelle\":\"Loyer\",\"type\":\"TEXTE\"}],\"clauses\":[],\"parcours\":[]}"), status().isOk());
        assertTrue(appel(post("/api/clm/modeles/" + m + "/apercu", redacteur, "{\"loyer\":\"1200\"}"), status().isOk()).get("texte").asText().contains("1200"));
        appel(get("/api/clm/modeles/" + m, direction), status().isNotFound());
        appel(post("/api/clm/modeles/" + m + "/soumettre", redacteur, "{}"), status().isOk());
        appel(post("/api/clm/modeles/" + m + "/renvoyer", admin, "{}"), status().isOk());
        appel(post("/api/clm/modeles/" + m + "/soumettre", redacteur, "{}"), status().isOk());
        appel(post("/api/clm/modeles/" + m + "/publier", admin, "{}"), status().isOk());
        appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"x\",\"modeleId\":999999}"), status().isNotFound());
        JsonNode v2 = appel(post("/api/clm/modeles/" + m + "/nouvelle-version", redacteur, "{}"), status().isOk());
        assertEquals(2, v2.get("numero").asInt());
        appel(post("/api/clm/modeles/" + m + "/nouvelle-version", redacteur, "{}"), status().isConflict());
        appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"x\",\"modeleId\":" + v2.get("id").asLong() + "}"), status().isConflict());
        appel(post("/api/clm/modeles/" + v2.get("id").asLong() + "/soumettre", redacteur, "{}"), status().isOk());
        appel(post("/api/clm/modeles/" + v2.get("id").asLong() + "/publier", admin, "{}"), status().isOk());
        assertEquals("RETIRE", appel(get("/api/clm/modeles/" + m, redacteur), status().isOk()).get("statut").asText(),
                "publier la version 2 retire la version 1");
        appel(post("/api/clm/modeles/" + v2.get("id").asLong() + "/retirer", admin, "{}"), status().isOk());
        assertTrue(appel(get("/api/clm/modeles?statut=RETIRE", redacteur), status().isOk()).size() >= 2);

        appel(post("/api/clm/modeles", admin, "{\"nom\":\" \"}"), status().isBadRequest());
        long regleOrpheline = appel(post("/api/clm/modeles", redacteur, "{\"nom\":\"Règle orpheline\",\"corps\":\"Texte\",\"champs\":[],\"clauses\":[],"
                + "\"parcours\":[{\"cle\":\"a\",\"phase\":1,\"type\":\"APPROBATION\",\"affectations\":[{\"type\":\"ROLE\",\"valeur\":\"DIRECTION\"}],"
                + "\"conditions\":[{\"champ\":\"montant\",\"operateur\":\"SUP\",\"valeur\":\"1\"}]}]}"), status().isOk()).get("id").asLong();
        assertEquals("[\"Règle de l'étape a : « montant » n'est pas un champ que l'initiateur complète.\"]",
                appel(get("/api/clm/modeles/" + regleOrpheline + "/validation", redacteur), status().isOk()).get("erreurs").toString(),
                "un modèle ne publie pas une règle d'étape sur un champ qu'il ne définit pas");
        long auto = appel(post("/api/clm/modeles", admin, "{\"nom\":\"Auto-publication\",\"corps\":\"Texte\",\"champs\":[],\"clauses\":[],\"parcours\":[]}"),
                status().isOk()).get("id").asLong();
        appel(put("/api/clm/modeles/" + auto, redacteur, "{\"nom\":\"Intrus\",\"corps\":\"\",\"champs\":[],\"clauses\":[],\"parcours\":[]}"),
                status().isForbidden());
        appel(post("/api/clm/modeles/" + auto + "/soumettre", admin, "{}"), status().isOk());
        appel(post("/api/clm/modeles/" + auto + "/publier", admin, "{}"), status().isConflict());

        JsonNode t = appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Cycle\",\"reference\":\"cyc-1\",\"risque\":\"ELEVE\",\"rappelFrequenceJours\":3}"), status().isOk());
        long id = t.at("/resume/id").asLong();
        assertEquals("CYC-1", t.at("/resume/reference").asText());
        appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Doublon\",\"reference\":\"CYC-1\"}"), status().isConflict());
        appel(post("/api/clm/trousses", redacteur, "{\"titre\":\" \"}"), status().isBadRequest());
        appel(put("/api/clm/trousses/" + id, redacteur, "{\"titre\":\"Cycle modifié\",\"unite\":\"Finances\",\"rappelsMax\":20}"), status().isBadRequest());
        appel(put("/api/clm/trousses/" + id, redacteur, "{\"titre\":\"Cycle modifié\",\"unite\":\"Finances\",\"echeance\":\"2026-12-31\"}"), status().isOk());
        appel(put("/api/clm/trousses/" + id, juriste, "{\"titre\":\"Intrus\"}"), status().isNotFound());
        appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isBadRequest());

        JsonNode p = appel(post("/api/clm/trousses/" + id + "/participants", redacteur, """
                {"nom":"Paul","courriel":"paul@x.ca","telephone":"+15145550199","roleContrat":"signataire_1","externe":true,"niveauIdentite":"COURRIEL"}
                """), status().isOk());
        assertEquals("FORTE", p.get("niveauIdentite").asText(), "risque élevé : forte imposée");
        appel(post("/api/clm/trousses/" + id + "/participants", redacteur, """
                {"nom":"Q","courriel":"q@x.ca","roleContrat":"S2","externe":false,"niveauIdentite":"QUESTION","question":"Ville ?","reponse":"Lévis"}
                """), status().isOk());
        String etapes = "[{\"cle\":\"s\",\"phase\":1,\"type\":\"SIGNATURE\",\"affectations\":[{\"type\":\"PARTICIPANT\",\"valeur\":\"" + p.get("id").asLong() + "\"}]}]";
        assertTrue(appel(post("/api/clm/trousses/" + id + "/parcours/tester", redacteur, etapes), status().isOk()).get("valide").asBoolean());
        assertFalse(appel(post("/api/clm/trousses/" + id + "/parcours/tester", redacteur, "[{\"cle\":\"s\",\"phase\":1,\"type\":\"INCONNU\"}]"), status().isOk()).get("valide").asBoolean());
        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, etapes), status().isOk());
        appel(delete("/api/clm/trousses/" + id + "/participants/" + p.get("id").asLong(), redacteur), status().isConflict());

        JsonNode doc = televerser(id, "bail.pdf", pdfMinimal(), status().isOk());
        String cle = doc.get("cleDocument").asText();
        appel(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents/" + cle + "/versions")
                .file(new MockMultipartFile("fichier", "bail-v2.pdf", "application/pdf", pdfMinimal())).param("motif", " ").cookie(redacteur), status().isBadRequest());
        JsonNode v = appel(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents/" + cle + "/versions")
                .file(new MockMultipartFile("fichier", "bail-v2.pdf", "application/pdf", pdfMinimal())).param("motif", "Correction").cookie(redacteur), status().isOk());
        assertEquals(2, v.get("version").asInt());
        appel(get("/api/clm/trousses/" + id + "/documents/" + cle + "/versions", juriste), status().isNotFound());
        assertEquals(2, appel(get("/api/clm/trousses/" + id + "/documents/" + cle + "/versions", redacteur), status().isOk()).size());
        JsonNode virus = televerser(id, "eicar.pdf", "%PDF-1.4 EICAR-STANDARD-ANTIVIRUS-TEST-FILE".getBytes(), status().isOk());
        assertEquals("INFECTE", virus.get("analyse").asText());
        appel(post("/api/clm/trousses/" + id + "/documents/" + virus.get("id").asLong() + "/analyse", redacteur, "{}"), status().isConflict());
        appel(get("/api/clm/trousses/" + id + "/documents/" + virus.get("id").asLong() + "/contenu", redacteur), status().isConflict());
        appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isBadRequest());
        appel(delete("/api/clm/trousses/" + id + "/documents/" + virus.get("id").asLong(), redacteur), status().isNoContent());
        mvc.perform(MockMvcRequestBuilders.get("/api/clm/trousses/" + id + "/documents/" + v.get("id").asLong() + "/contenu").cookie(redacteur))
                .andExpect(status().isOk());
        appel(post("/api/clm/trousses/" + id + "/generer", redacteur, "{}"), status().isConflict());

        assertTrue(appel(get("/api/clm/trousses?q=cycle", redacteur), status().isOk()).size() >= 1);
        assertTrue(appel(get("/api/clm/trousses?q=paul@x.ca&statut=BROUILLON", redacteur), status().isOk()).size() >= 1);
        assertEquals(0, appel(get("/api/clm/trousses?statut=TERMINEE&q=cyc-1", redacteur), status().isOk()).size());
        appel(post("/api/clm/trousses/" + id + "/reprendre", redacteur, "{}"), status().isConflict());
        JsonNode suivi = appel(get("/api/clm/suivi?unite=Finances&source=MANUELLE&responsable=redacteur@levis.qc.ca&du=2020-01-01&au=2099-01-01", redacteur), status().isOk());
        assertTrue(suivi.size() >= 1);
        assertTrue(appel(get("/api/clm/suivi?exceptions=true&q=cyc-1", redacteur), status().isOk()).isEmpty());
        JsonNode annulee = appel(post("/api/clm/trousses/" + id + "/annuler", redacteur, "{\"motif\":\"Abandon\"}"), status().isOk());
        assertEquals("ANNULEE", annulee.at("/resume/statut").asText());
        appel(post("/api/clm/trousses/" + id + "/annuler", redacteur, "{}"), status().isConflict());
        assertTrue(appel(get("/api/clm/trousses/" + id + "/preuve", redacteur), status().isOk()).at("/integrite/integre").asBoolean());
        appel(get("/api/clm/trousses/" + id + "/preuve/export", juriste), status().isNotFound());
        assertTrue(appel(get("/api/clm/taches?filtre=terminees", juriste), status().isOk()).isArray());
        assertTrue(appel(get("/api/clm/taches?filtre=en_retard", juriste), status().isOk()).isArray());
        JsonNode msgs = appel(get("/api/clm/admin/messages", admin), status().isOk());
        assertEquals(21, msgs.size(), "7 événements × 3 canaux");
        appel(post("/api/clm/admin/integrations/salesforce/tester", admin, "{}"), status().isOk());
        appel(post("/api/clm/admin/integrations/salesforce/evenements/999999/relancer", admin, "{}"), status().isNotFound());
    }

    @Test
    void module_gere_depuis_le_superadmin_comme_les_autres() throws Exception {
        Cookie sa = login("superadmin@plateforme.ca", "SuperPassw0rd!");
        Long tenant = users.findByEmail("admin@levis.qc.ca").orElseThrow().getTenantId();
        JsonNode etat = appel(get("/api/superadmin/tenants/" + tenant + "/modules", sa), status().isOk());
        JsonNode clm = null;
        JsonNode ia = null;
        for (JsonNode m : etat) {
            if ("clm".equals(m.get("code").asText())) clm = m;
            if ("clm_ia".equals(m.get("code").asText())) ia = m;
        }
        assertNotNull(clm, "le module CLM figure au catalogue du superadmin");
        assertEquals("CLM", clm.get("famille").asText());
        assertEquals("clm", ia.get("requiert").get(0).asText(), "l'option IA requiert le module CLM");

        JsonNode off = appel(put("/api/superadmin/tenants/" + tenant + "/modules/clm/desactiver", sa, "{}"), status().isOk());
        assertTrue(off.get("message").asText().contains("clm_ia"), "désactiver CLM désactive l'option IA");
        appel(get("/api/clm/trousses", redacteur), status().isForbidden());
        appel(get("/api/clm/analyses", redacteur), status().isForbidden());
        boolean actifPourTenant = false;
        for (JsonNode m : appel(get("/api/modules", redacteur), status().isOk())) {
            actifPourTenant |= "clm".equals(m.get("code").asText()) && m.get("actif").asBoolean();
        }
        assertFalse(actifPourTenant, "la navigation masque l'espace CLM");

        JsonNode on = appel(put("/api/superadmin/tenants/" + tenant + "/modules/clm_ia/activer", sa, "{}"), status().isOk());
        assertTrue(on.get("message").asText().contains("clm"), "activer l'option tire son prérequis");
        appel(get("/api/clm/trousses", redacteur), status().isOk());
        appel(put("/api/superadmin/tenants/" + tenant + "/modules/clm/activer", redacteur, "{}"), status().isForbidden());
    }

    @Test
    void relance_commentaire_delegation_certificat_et_journal() throws Exception {
        JsonNode t = appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Délégation\",\"typeContrat\":\"SERVICES\",\"unite\":\"Achats\"}"), status().isOk());
        long id = t.at("/resume/id").asLong();
        String ref = t.at("/resume/reference").asText();
        long infecte = televerser(id, "eicar.pdf", "%PDF-1.4 EICAR-STANDARD-ANTIVIRUS-TEST-FILE".getBytes(), status().isOk()).get("id").asLong();
        appel(delete("/api/clm/trousses/" + id + "/documents/" + infecte, redacteur), status().isNoContent());
        televerser(id, "service.pdf", pdfMinimal(), status().isOk());
        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, """
                [{"cle":"a","libelle":"Approbation achats","phase":1,"type":"APPROBATION","affectations":[{"type":"UTILISATEUR","valeur":"juriste@levis.qc.ca"}]}]
                """), status().isOk());
        appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isOk());
        long etape = appel(get("/api/clm/taches", juriste), status().isOk()).get(0).get("etapeId").asLong();

        appel(post("/api/clm/etapes/" + etape + "/commentaires", juriste, "{\"texte\":\"Vérifier l'annexe B\"}"), status().isNoContent());
        appel(post("/api/clm/etapes/" + etape + "/commentaires", juriste, "{\"texte\":\" \"}"), status().isBadRequest());
        appel(post("/api/clm/etapes/" + etape + "/commentaires", juriste, "{\"texte\":\"" + "x".repeat(1501) + "\"}"), status().isBadRequest());
        appel(post("/api/clm/etapes/" + etape + "/relancer", juriste, "{}"), status().isForbidden());
        assertEquals("ACTIVE", appel(post("/api/clm/etapes/" + etape + "/relancer", redacteur, "{}"), status().isOk()).get("statut").asText());

        appel(post("/api/clm/etapes/" + etape + "/deleguer", juriste, "{\"vers\":\"pas-un-courriel\"}"), status().isBadRequest());
        appel(post("/api/clm/etapes/" + etape + "/deleguer", juriste, "{\"vers\":\"juriste@levis.qc.ca\"}"), status().isBadRequest());
        appel(post("/api/clm/etapes/" + etape + "/deleguer", juriste, "{\"vers\":\"redacteur@levis.qc.ca\"}"), status().isConflict());
        appel(post("/api/clm/etapes/" + etape + "/deleguer", direction, "{\"vers\":\"admin@levis.qc.ca\"}"), status().isForbidden());
        appel(post("/api/clm/etapes/" + etape + "/deleguer", juriste, "{\"vers\":\"direction@levis.qc.ca\",\"motif\":\"Vacances\"}"), status().isOk());
        appel(post("/api/clm/etapes/" + etape + "/deleguer", juriste, "{\"vers\":\"admin@levis.qc.ca\"}"), status().isForbidden());
        JsonNode deleguees = appel(get("/api/clm/taches?filtre=deleguees", direction), status().isOk());
        assertEquals(1, deleguees.size());
        assertTrue(deleguees.get(0).get("reassignee").asBoolean());
        assertEquals(0, appel(get("/api/clm/taches", juriste), status().isOk()).size(), "la tâche a quitté le délégant");
        appel(post("/api/clm/etapes/" + etape + "/decider", direction, "{\"decision\":\"APPROUVE\"}"), status().isOk());
        appel(post("/api/clm/etapes/" + etape + "/relancer", redacteur, "{}"), status().isConflict());
        appel(post("/api/clm/etapes/" + etape + "/deleguer", direction, "{\"vers\":\"admin@levis.qc.ca\"}"), status().isConflict());

        JsonNode fini = appel(get("/api/clm/trousses/" + id, redacteur), status().isOk());
        assertEquals("TERMINEE", fini.at("/resume/statut").asText());
        byte[] certificat = mvc.perform(MockMvcRequestBuilders.get("/api/clm/trousses/" + id + "/preuve/certificat").cookie(redacteur))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertEquals("%PDF", new String(certificat, 0, 4));
        byte[] zip = mvc.perform(MockMvcRequestBuilders.get("/api/clm/trousses/" + id + "/preuve/export").cookie(redacteur))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        assertTrue(entrees(zip).contains("certificat.pdf"));

        assertTrue(appel(get("/api/clm/tableau-de-bord?statut=TERMINEE&unite=Achats&du=2020-01-01&au=2099-12-31", redacteur), status().isOk())
                .at("/indicateurs/terminees").asLong() >= 1);
        assertEquals(0, appel(get("/api/clm/tableau-de-bord?unite=Inexistante", redacteur), status().isOk()).at("/indicateurs/terminees").asLong());
        assertTrue(appel(get("/api/clm/trousses?initiateur=redacteur@levis.qc.ca&type=services&du=2020-01-01&au=2099-12-31", redacteur), status().isOk())
                .toString().contains(ref));
        assertEquals(0, appel(get("/api/clm/trousses?initiateur=autre@x.ca", redacteur), status().isOk()).size());

        JsonNode journal = appel(get("/api/clm/admin/journal?trousse=" + ref, admin), status().isOk());
        String types = journal.toString();
        for (String type : List.of("COMMENTAIRE", "DELEGATION", "RELANCE_MANUELLE", "CERTIFICAT_TELECHARGE", "DOCUMENT_INFECTE")) {
            assertTrue(types.contains(type), type);
        }
        boolean systeme = false;
        for (JsonNode l : journal) systeme |= l.get("systeme").asBoolean();
        assertTrue(systeme, "les événements système sont distingués");
        assertTrue(appel(get("/api/clm/admin/journal?acteur=juriste&action=COMMENT&texte=annexe", admin), status().isOk()).size() >= 1);
        appel(get("/api/clm/admin/journal?du=2020-01-01&au=2026-01-01", admin), status().isBadRequest());
        appel(get("/api/clm/admin/journal?du=2026-02-01&au=2026-01-01", admin), status().isBadRequest());
        appel(get("/api/clm/admin/journal", redacteur), status().isForbidden());
        String csv = mvc.perform(MockMvcRequestBuilders.get("/api/clm/admin/journal/export?trousse=" + ref).cookie(admin))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertTrue(csv.contains("METIER") && csv.contains("SYSTEME"));

        assertEquals(0, appel(post("/api/clm/admin/integrations/salesforce/trousses/" + id + "/relancer", admin, "{}"), status().isOk())
                .get("relances").asInt());
        assertEquals("simulation", appel(get("/api/clm/admin/integrations/salesforce", admin), status().isOk()).at("/config/mode").asText());
        assertEquals(60, appel(get("/api/clm/admin/parametres", admin), status().isOk()).get("sessionMinutes").asLong());
        assertTrue(appel(get("/api/clm/analyses", redacteur), status().isOk()).isArray());
        for (JsonNode m : appel(get("/api/clm/modeles", redacteur), status().isOk())) {
            assertTrue(m.has("trousses"));
        }
    }

    /**
     * Garde-fous du parcours : décision invalide ou en double, quorum incohérent, signature ni décidée ni déléguée,
     * relance des signataires, lien de signature interne, réassignation à chaud d'une signature, refus au portail
     * (question secrète) qui rejette la trousse, puis reprise en brouillon.
     */
    @Test
    void garde_fous_rejet_par_le_signataire_puis_reprise() throws Exception {
        appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Garde-fous\",\"rappelFrequenceJours\":0}"), status().isBadRequest());
        long id = appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Garde-fous\",\"risque\":\"FAIBLE\"}"), status().isOk())
                .at("/resume/id").asLong();
        JsonNode modifiee = appel(put("/api/clm/trousses/" + id, redacteur, "{\"rappelFrequenceJours\":3,\"rappelsMax\":5}"), status().isOk());
        assertEquals(5, modifiee.get("rappelsMax").asInt());
        televerser(id, "contrat.pdf", pdfMinimal(), status().isOk());
        long interne = appel(post("/api/clm/trousses/" + id + "/participants", redacteur,
                "{\"nom\":\"Julie Juriste\",\"courriel\":\"juriste@levis.qc.ca\",\"roleContrat\":\"SIGNATAIRE_1\",\"externe\":false}"),
                status().isOk()).get("id").asLong();
        long externe = appel(post("/api/clm/trousses/" + id + "/participants", redacteur, """
                {"nom":"Paul Externe","courriel":"paul@exemple.ca","roleContrat":"SIGNATAIRE_2","externe":true,
                 "niveauIdentite":"QUESTION","question":"Ville du siège ?","reponse":"Lévis"}
                """), status().isOk()).get("id").asLong();
        long temporaire = appel(post("/api/clm/trousses/" + id + "/participants", redacteur,
                "{\"nom\":\"Temporaire\",\"courriel\":\"tmp@exemple.ca\",\"roleContrat\":\"SIGNATAIRE_3\",\"externe\":true}"),
                status().isOk()).get("id").asLong();
        appel(delete("/api/clm/trousses/" + id + "/participants/" + temporaire, redacteur), status().isNoContent());

        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, """
                [{"cle":"s1","phase":1,"type":"SIGNATURE","affectations":[{"type":"PARTICIPANT","valeur":"999999"}]}]
                """), status().isBadRequest());
        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, """
                [{"cle":"v1","phase":1,"type":"VALIDATION","politique":"AU_HASARD","affectations":[{"type":"UTILISATEUR","valeur":"juriste@levis.qc.ca"}]}]
                """), status().isBadRequest());
        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, """
                [{"cle":"v1","libelle":"Validation","phase":1,"type":"VALIDATION","quorum":2,
                  "affectations":[{"type":"UTILISATEUR","valeur":"juriste@levis.qc.ca"},{"type":"PARTICIPANT","valeur":"%d"}]},
                 {"cle":"s1","libelle":"Signatures","phase":2,"type":"SIGNATURE","affectations":[{"type":"PARTICIPANT","valeur":"%d"}]}]
                """.formatted(externe, interne)), status().isOk());
        appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isOk());
        JsonNode etapes = appel(get("/api/clm/trousses/" + id, redacteur), status().isOk()).get("etapes");
        long v1 = etapes.get(0).get("id").asLong();
        long s1 = etapes.get(1).get("id").asLong();

        appel(post("/api/clm/etapes/" + v1 + "/decider", juriste, "{\"decision\":\"PEUT-ETRE\"}"), status().isBadRequest());
        appel(post("/api/clm/etapes/" + v1 + "/decider", juriste, "{\"decision\":\"APPROUVE\"}"), status().isOk());
        appel(post("/api/clm/etapes/" + v1 + "/decider", juriste, "{\"decision\":\"APPROUVE\"}"), status().isConflict());
        appel(put("/api/clm/etapes/" + v1 + "/affectations", redacteur,
                "{\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"juriste@levis.qc.ca\"}],\"quorum\":3}"), status().isBadRequest());
        appel(put("/api/clm/etapes/" + v1 + "/affectations", redacteur,
                "{\"affectations\":[{\"type\":\"UTILISATEUR\",\"valeur\":\"juriste@levis.qc.ca\"}],\"quorum\":1,\"motif\":\"Externe indisponible\"}"),
                status().isOk());
        assertEquals("ACTIVE", appel(get("/api/clm/trousses/" + id, redacteur), status().isOk()).at("/etapes/1/statut").asText(),
                "le quorum abaissé à chaud approuve la validation et active la signature");

        appel(post("/api/clm/etapes/" + s1 + "/decider", juriste, "{\"decision\":\"APPROUVE\"}"), status().isConflict());
        appel(post("/api/clm/etapes/" + s1 + "/deleguer", juriste, "{\"vers\":\"admin@levis.qc.ca\"}"), status().isConflict());
        appel(post("/api/clm/etapes/" + s1 + "/relancer", redacteur, "{}"), status().isOk());
        appel(post("/api/clm/etapes/" + s1 + "/lien-signature", direction, "{}"), status().isForbidden());
        assertTrue(appel(post("/api/clm/etapes/" + s1 + "/lien-signature", juriste, "{}"), status().isOk()).get("lien").asText()
                .contains("/portail/"));
        appel(post("/api/clm/etapes/" + v1 + "/lien-signature", juriste, "{}"), status().isConflict());
        appel(get("/api/clm/trousses/" + id + "/preuve/export", juriste), status().isForbidden());

        appel(put("/api/clm/etapes/" + s1 + "/affectations", redacteur,
                "{\"affectations\":[{\"type\":\"PARTICIPANT\",\"valeur\":\"" + externe + "\"}]}"), status().isOk());
        String jeton = dernier("INVITATION", "COURRIEL", "/portail/([A-Za-z0-9_-]+)");
        assertEquals("Ville du siège ?", appel(get("/api/clm/portail/" + jeton, null), status().isOk()).get("question").asText());
        appel(post("/api/clm/portail/" + jeton + "/verification", null, "{}"), status().isOk());
        appel(post("/api/clm/portail/" + jeton + "/verification/reponse", null, "{\"reponse\":\"Québec\"}"), status().isBadRequest());
        appel(post("/api/clm/portail/" + jeton + "/verification/reponse", null, "{\"reponse\":\"  LEVIS \"}"), status().isOk());
        assertTrue(appel(post("/api/clm/portail/" + jeton + "/verification", null, "{}"), status().isOk()).get("verifiee").asBoolean());
        appel(post("/api/clm/portail/" + jeton + "/refuser", null, "{\"motif\":\" \"}"), status().isBadRequest());
        appel(post("/api/clm/portail/" + jeton + "/refuser", null, "{\"motif\":\"Montant erroné\"}"), status().isOk());

        JsonNode rejetee = appel(get("/api/clm/trousses/" + id, redacteur), status().isOk());
        assertEquals("REJETEE", rejetee.at("/resume/statut").asText());
        assertTrue(rejetee.get("actions").toString().contains("REPRENDRE"));
        appel(post("/api/clm/etapes/" + s1 + "/relancer", redacteur, "{}"), status().isConflict());
        assertEquals("BROUILLON", appel(post("/api/clm/trousses/" + id + "/reprendre", redacteur, "{}"), status().isOk())
                .at("/resume/statut").asText());
        appel(post("/api/clm/trousses/" + id + "/reprendre", redacteur, "{}"), status().isConflict());
        appel(get("/api/clm/salesforce/dossiers/%20", redacteur), status().isBadRequest());

        appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isOk());
        appel(post("/api/clm/etapes/" + v1 + "/decider", juriste, "{\"decision\":\"APPROUVE\"}"), status().isOk());
        String nouveau = dernier("INVITATION", "COURRIEL", "/portail/([A-Za-z0-9_-]+)");
        for (int essai = 1; essai < 5; essai++) {
            appel(post("/api/clm/portail/" + nouveau + "/verification/reponse", null, "{\"reponse\":\"Québec\"}"), status().isBadRequest());
        }
        JsonNode bloque = appel(post("/api/clm/portail/" + nouveau + "/verification/reponse", null, "{\"reponse\":\"Québec\"}"),
                status().isBadRequest());
        assertTrue(bloque.get("message").asText().contains("bloquée"), "cinq échecs bloquent l'invitation");
        assertEquals("BLOQUEE", appel(get("/api/clm/portail/" + nouveau, null), status().isOk()).get("statut").asText());

        JsonNode preuve = appel(get("/api/clm/trousses/" + id + "/preuve", redacteur), status().isOk());
        assertTrue(preuve.at("/integrite/integre").asBoolean(), "seules les invitations signées portent une attestation à vérifier");
        assertFalse(preuve.get("signatures").isEmpty());
        for (JsonNode s : preuve.get("signatures")) {
            assertNotEquals("ANNULEE", s.get("statut").asText(), "une invitation annulée ne figure pas au dossier de preuve");
        }
    }

    /** ST-02 : l'initiateur ne décide pas son propre dossier ; chaque refus d'accès est expliqué et journalisé. */
    @Test
    void separation_des_responsabilites_et_refus_journalises() throws Exception {
        long id = appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Séparation\",\"risque\":\"FAIBLE\"}"), status().isOk())
                .at("/resume/id").asLong();
        televerser(id, "contrat.pdf", pdfMinimal(), status().isOk());
        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, """
                [{"cle":"a","libelle":"Approbation","phase":1,"type":"APPROBATION",
                  "affectations":[{"type":"UTILISATEUR","valeur":"redacteur@levis.qc.ca"},{"type":"UTILISATEUR","valeur":"juriste@levis.qc.ca"}]},
                 {"cle":"d","libelle":"Direction","phase":2,"type":"APPROBATION","affectations":[{"type":"UTILISATEUR","valeur":"direction@levis.qc.ca"}]}]
                """), status().isOk());
        appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isOk());
        long etape = appel(get("/api/clm/trousses/" + id, redacteur), status().isOk()).at("/etapes/0/id").asLong();

        assertTrue(appel(post("/api/clm/etapes/" + etape + "/decider", redacteur, "{\"decision\":\"APPROUVE\"}"), status().isConflict())
                .get("message").asText().startsWith("Séparation des responsabilités"), "l'initiateur désigné ne peut pas approuver");
        assertEquals("Cette tâche ne vous est pas assignée.",
                appel(post("/api/clm/etapes/" + etape + "/decider", direction, "{\"decision\":\"APPROUVE\"}"), status().isForbidden())
                        .get("message").asText());
        assertEquals("Accès refusé.", appel(get("/api/clm/admin/parametres", redacteur), status().isForbidden()).get("message").asText());

        Long tenant = users.findByEmail("admin@levis.qc.ca").orElseThrow().getTenantId();
        List<AuditLog> refusDirection = auditLogs.rechercher(tenant, "direction@levis.qc.ca", "CLM_ACCES_REFUSE", null, null, null);
        assertTrue(refusDirection.stream().anyMatch(a -> a.getDetails().startsWith("POST /api/clm/etapes/" + etape + "/decider")),
                "refus métier journalisé avec la ressource visée");
        assertTrue(auditLogs.rechercher(tenant, "redacteur@levis.qc.ca", "CLM_ACCES_REFUSE", null, null, null).stream()
                .anyMatch(a -> a.getDetails().startsWith("GET /api/clm/admin/parametres")), "refus par rôle journalisé");

        appel(post("/api/clm/etapes/" + etape + "/decider", juriste, "{\"decision\":\"APPROUVE\"}"), status().isOk());
        JsonNode avance = appel(get("/api/clm/trousses/" + id, redacteur), status().isOk());
        assertEquals("ACTIVE", avance.at("/etapes/1/statut").asText(), "un autre approbateur désigné fait avancer le parcours");
        appel(post("/api/clm/etapes/" + avance.at("/etapes/1/id").asLong() + "/decider", direction, "{\"decision\":\"APPROUVE\"}"),
                status().isOk());
        assertEquals("TERMINEE", appel(get("/api/clm/trousses/" + id, redacteur), status().isOk()).at("/resume/statut").asText());
    }

    /** F-04 : l'initiateur complète les valeurs d'une trousse manuelle. F-05 : une étape à règle n'est requise que si elle est satisfaite. */
    @Test
    void valeurs_saisies_par_l_initiateur_et_etape_ecartee_par_sa_regle() throws Exception {
        long modeleId = modelePublie();
        long sansModele = appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Sans modèle\"}"), status().isOk()).at("/resume/id").asLong();
        appel(put("/api/clm/trousses/" + sansModele + "/valeurs", redacteur, "{\"valeurs\":{\"montant\":\"1\"}}"), status().isConflict());

        JsonNode creee = appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Saisie\",\"risque\":\"FAIBLE\",\"modeleId\":" + modeleId + "}"),
                status().isOk());
        long id = creee.at("/resume/id").asLong();
        assertEquals("[\"demandeur\",\"montant\"]", creee.get("manquants").toString(), "données à compléter avant lancement");
        assertEquals(2, creee.get("champs").size(), "seuls les champs de l'initiateur sont proposés à la saisie");
        televerser(id, "contrat.pdf", pdfMinimal(), status().isOk());
        long ana = appel(post("/api/clm/trousses/" + id + "/participants", redacteur,
                "{\"nom\":\"Ana Lima\",\"courriel\":\"ana@coop.example\",\"roleContrat\":\"SIGNATAIRE_1\",\"externe\":true}"), status().isOk()).get("id").asLong();
        long benoit = appel(post("/api/clm/trousses/" + id + "/participants", redacteur,
                "{\"nom\":\"Benoit Roy\",\"courriel\":\"benoit@coop.example\",\"roleContrat\":\"SIGNATAIRE_2\",\"externe\":true}"), status().isOk()).get("id").asLong();
        JsonNode zones = appel(put("/api/clm/trousses/" + id + "/participants/" + benoit + "/role", redacteur, "{\"roleContrat\":\"signataire_1\"}"),
                status().isOk());
        assertEquals("SIGNATAIRE_2", zones.get(0).get("roleContrat").asText(), "le titulaire précédent de la zone reçoit l'ancien rôle");
        assertEquals("SIGNATAIRE_1", zones.get(1).get("roleContrat").asText());
        appel(put("/api/clm/trousses/" + id + "/participants/" + ana + "/role", redacteur, "{\"roleContrat\":\"TEMOIN\"}"), status().isOk());
        appel(put("/api/clm/trousses/" + id + "/participants/" + ana + "/role", redacteur, "{\"roleContrat\":\" \"}"), status().isBadRequest());
        appel(put("/api/clm/trousses/" + id + "/participants/999999/role", redacteur, "{\"roleContrat\":\"X\"}"), status().isNotFound());
        String regles = """
                [{"cle":"v","libelle":"Validation juridique","phase":1,"type":"VALIDATION","affectations":[{"type":"UTILISATEUR","valeur":"juriste@levis.qc.ca"}]},
                 {"cle":"g","libelle":"Comité des grands dossiers","phase":2,"type":"APPROBATION",
                  "affectations":[{"type":"ROLE","valeur":"ADMIN"}],"conditions":[{"champ":"%s","operateur":"SUP","valeur":"100000"}]},
                 {"cle":"d","libelle":"Approbation direction","phase":2,"type":"APPROBATION",
                  "affectations":[{"type":"UTILISATEUR","valeur":"direction@levis.qc.ca"}],"conditions":[{"champ":"montant","operateur":"INFEGAL","valeur":"100000"}]}]
                """;
        JsonNode essai = appel(post("/api/clm/trousses/" + id + "/parcours/tester", redacteur, regles.formatted("inconnu")), status().isOk());
        assertFalse(essai.get("valide").asBoolean());
        assertEquals("Règle de l'étape g : « inconnu » n'est pas un champ que l'initiateur complète.", essai.at("/erreurs/0").asText());
        assertTrue(appel(post("/api/clm/trousses/" + id + "/parcours/tester", redacteur, regles.formatted("titreSignataire")), status().isOk())
                .toString().contains("« titreSignataire » n'est pas un champ que l'initiateur complète"), "un champ du signataire n'a pas de valeur au lancement");
        assertTrue(appel(post("/api/clm/trousses/" + id + "/parcours/tester", redacteur, regles.formatted(" ")), status().isOk())
                .toString().contains("Règle incomplète pour l'étape Comité des grands dossiers"));
        assertEquals(1, appel(post("/api/clm/trousses/" + id + "/parcours/tester", redacteur, regles.formatted("x").replace("\"x\"", "null")),
                status().isOk()).get("erreurs").size(), "une règle sans champ n'est signalée qu'une fois");
        appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, regles.formatted("inconnu")), status().isBadRequest());
        JsonNode etapes = appel(put("/api/clm/trousses/" + id + "/parcours", redacteur, regles.formatted("montant")), status().isOk());
        assertEquals("SUP", etapes.at("/1/conditions/0/operateur").asText(), "la règle est conservée avec l'étape");

        assertTrue(appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isBadRequest())
                .get("message").asText().contains("Données incomplètes : demandeur, montant"), "sans saisie, la trousse ne part pas");
        assertTrue(appel(put("/api/clm/trousses/" + id + "/valeurs", redacteur, "{\"valeurs\":{\"titreSignataire\":\"x\",\"montant\":\"beaucoup\"}}"),
                status().isBadRequest()).get("message").asText()
                .equals("Champ inconnu ou non modifiable : titreSignataire. Valeur invalide pour « Montant »."));
        appel(put("/api/clm/trousses/" + id + "/valeurs", juriste, "{\"valeurs\":{\"montant\":\"1\"}}"), status().isForbidden());
        JsonNode saisie = appel(put("/api/clm/trousses/" + id + "/valeurs", redacteur,
                "{\"valeurs\":{\"demandeur\":\"Coopérative de Lévis\",\"montant\":\"50000\"}}"), status().isOk());
        assertTrue(saisie.get("manquants").isEmpty(), saisie.toString());
        assertEquals("MANUEL", saisie.at("/correspondances/1/provenance").asText());
        assertEquals("[\"montant\"]", appel(put("/api/clm/trousses/" + id + "/valeurs", redacteur, "{\"valeurs\":{\"montant\":null}}"),
                status().isOk()).get("manquants").toString(), "une valeur vide retire la saisie");
        appel(put("/api/clm/trousses/" + id + "/valeurs", redacteur, "{}"), status().isOk());
        appel(put("/api/clm/trousses/" + id + "/valeurs", redacteur, "{\"valeurs\":{\"montant\":\"50000.00\"}}"), status().isOk());

        JsonNode lance = appel(post("/api/clm/trousses/" + id + "/lancer", redacteur, "{}"), status().isOk());
        assertEquals("NON_REQUISE", lance.at("/etapes/1/statut").asText(), "50 000 $ ne requiert pas le comité");
        assertEquals("EN_ATTENTE", lance.at("/etapes/2/statut").asText());
        appel(post("/api/clm/etapes/" + lance.at("/etapes/0/id").asLong() + "/decider", juriste, "{\"decision\":\"APPROUVE\"}"), status().isOk());
        appel(post("/api/clm/etapes/" + lance.at("/etapes/2/id").asLong() + "/decider", direction, "{\"decision\":\"APPROUVE\"}"), status().isOk());
        assertEquals("TERMINEE", appel(get("/api/clm/trousses/" + id, redacteur), status().isOk()).at("/resume/statut").asText());
        String historique = appel(get("/api/clm/trousses/" + id + "/historique", redacteur), status().isOk()).toString();
        assertTrue(historique.contains("VALEURS_SAISIES") && historique.contains("ETAPE_NON_REQUISE") && historique.contains("ZONE_ATTRIBUEE"), historique);

        long sf = appel(post("/api/clm/trousses", redacteur, "{\"titre\":\"Correction\",\"modeleId\":" + modeleId + ",\"sfDossierId\":\"DEM-2026-0001\"}"),
                status().isOk()).at("/resume/id").asLong();
        JsonNode corrige = appel(put("/api/clm/trousses/" + sf + "/valeurs", redacteur, "{\"valeurs\":{\"montant\":\"90000\"}}"), status().isOk());
        assertEquals("DEM-2026-0001", corrige.get("numero").asText(), "Salesforce est relu à la correction");
        assertEquals("montant", corrige.at("/conflits/0/cle").asText(), "l'écart avec Salesforce reste tracé");
    }

    // --- Données ---
    private long modelePublie() throws Exception {
        JsonNode m = appel(post("/api/clm/modeles", redacteur, """
                {"nom":"Convention d'aide financière","categorie":"SUBVENTION",
                 "corps":"Entre Investissement Québec et {{demandeur}}, pour un montant de {{montant}} $.\\n{{clauses}}",
                 "champs":[
                   {"cle":"demandeur","libelle":"Demandeur","type":"TEXTE","obligatoire":true,"source":"Demandeur"},
                   {"cle":"montant","libelle":"Montant","type":"TEXTE","obligatoire":true,"source":"Montant","format":"\\\\d+(\\\\.\\\\d{2})?"},
                   {"cle":"titreSignataire","libelle":"Titre","type":"TEXTE","obligatoire":true,"roleDestinataire":"SIGNATAIRE_1"},
                   {"cle":"signature1","libelle":"Signature","type":"SIGNATURE","roleDestinataire":"SIGNATAIRE_1",
                    "position":{"page":1,"x":10,"y":70,"largeur":40,"hauteur":10}}],
                 "clauses":[
                   {"cle":"garantie","titre":"Garantie","texte":"Une garantie est exigée.","conditions":[{"champ":"montant","operateur":"SUP","valeur":"100000"}]},
                   {"cle":"petit","titre":"Allègement","texte":"Aucune garantie.","conditions":[{"champ":"montant","operateur":"INFEGAL","valeur":"100000"}]}],
                 "parcours":[
                   {"cle":"juridique","libelle":"Validation juridique","phase":1,"type":"VALIDATION","affectations":[{"type":"ROLE","valeur":"JURIDIQUE"}]},
                   {"cle":"direction","libelle":"Approbation direction","phase":2,"type":"APPROBATION","affectations":[{"type":"UTILISATEUR","valeur":"direction@levis.qc.ca"}],"delaiJours":5},
                   {"cle":"signature","libelle":"Signature bénéficiaire","phase":3,"type":"SIGNATURE","affectations":[{"type":"PARTICIPANT","valeur":"SIGNATAIRE_1"}]}]}
                """), status().isOk());
        long id = m.get("id").asLong();
        assertTrue(appel(get("/api/clm/modeles/" + id + "/validation", redacteur), status().isOk()).get("valide").asBoolean());
        appel(post("/api/clm/modeles/" + id + "/soumettre", redacteur, "{}"), status().isOk());
        appel(post("/api/clm/modeles/" + id + "/publier", redacteur, "{}"), status().isForbidden());
        assertEquals("PUBLIE", appel(post("/api/clm/modeles/" + id + "/publier", admin, "{}"), status().isOk()).get("statut").asText());
        return id;
    }

    private static byte[] docx(String texte) throws Exception {
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.createParagraph().createRun().setText(texte);
            doc.write(out);
            return out.toByteArray();
        }
    }

    private static byte[] gros() {
        byte[] b = new byte[300_000];
        System.arraycopy("%PDF-1.4".getBytes(), 0, b, 0, 8);
        return b;
    }

    private static byte[] pdfMinimal() throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            doc.save(out);
            return out.toByteArray();
        }
    }

    private static List<String> entrees(byte[] zip) throws Exception {
        List<String> res = new ArrayList<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            for (ZipEntry e = in.getNextEntry(); e != null; e = in.getNextEntry()) {
                res.add(e.getName());
            }
        }
        return res;
    }

    private String dernier(String sujetContient, String canal, String motif) {
        Pattern p = Pattern.compile(motif);
        for (int i = ENVOIS.size() - 1; i >= 0; i--) {
            String[] e = ENVOIS.get(i);
            Matcher m = p.matcher(e[3]);
            if (e[0].equals(canal) && (e[2] + e[3]).toUpperCase().contains(sujetContient.equals("INVITATION") ? "SIGNER" : "CODE") && m.find()) {
                return m.group(1);
            }
        }
        throw new AssertionError("Aucun envoi " + sujetContient + " sur " + canal);
    }

    // --- HTTP ---
    private Cookie login(String email) throws Exception {
        return login(email, "Passw0rd!");
    }

    private Cookie login(String email, String motDePasse) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"motDePasse\":\"" + motDePasse + "\"}")).andReturn().getResponse().getCookie("ACCESS_TOKEN");
    }

    private MockHttpServletRequestBuilder post(String url, Cookie c, String corps) {
        return avec(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON).content(corps), c);
    }

    private MockHttpServletRequestBuilder put(String url, Cookie c, String corps) {
        return avec(MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).content(corps), c);
    }

    private MockHttpServletRequestBuilder get(String url, Cookie c) {
        return avec(MockMvcRequestBuilders.get(url), c);
    }

    private MockHttpServletRequestBuilder delete(String url, Cookie c) {
        return avec(MockMvcRequestBuilders.delete(url), c);
    }

    private static MockHttpServletRequestBuilder avec(MockHttpServletRequestBuilder r, Cookie c) {
        return c == null ? r : r.cookie(c);
    }

    private JsonNode televerser(long id, String nom, byte[] contenu, ResultMatcher attendu) throws Exception {
        return appel(MockMvcRequestBuilders.multipart("/api/clm/trousses/" + id + "/documents")
                .file(new MockMultipartFile("fichier", nom, "application/octet-stream", contenu)).cookie(redacteur), attendu);
    }

    private JsonNode appel(MockHttpServletRequestBuilder req, ResultMatcher attendu) throws Exception {
        String s = mvc.perform(req).andExpect(attendu).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return s.isBlank() ? json.createObjectNode() : json.readTree(s);
    }
}
