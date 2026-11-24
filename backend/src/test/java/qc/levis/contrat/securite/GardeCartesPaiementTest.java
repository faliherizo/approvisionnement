package qc.levis.contrat.securite;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import qc.levis.contrat.securite.infrastructure.GardeCartesPaiement;

/** IQ-D-14 (ADR-022) : la garde refuse un PAN avant toute logique métier, et rend le corps intact sinon. */
class GardeCartesPaiementTest {

    private static final String PAN = "4111 1111 1111 1111";

    private static MockHttpServletRequest requete(String methode, String uri, String type, String corps) {
        MockHttpServletRequest r = new MockHttpServletRequest(methode, uri);
        if (type != null) r.setContentType(type);
        if (corps != null) r.setContent(corps.getBytes(StandardCharsets.UTF_8));
        return r;
    }

    /** Statut rendu et corps lu par la suite de la chaîne (null si la chaîne n'est pas atteinte). */
    private static Object[] filtrer(GardeCartesPaiement garde, MockHttpServletRequest req) throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        AtomicReference<String> lu = new AtomicReference<>();
        garde.doFilter(req, res, new MockFilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest r, jakarta.servlet.ServletResponse s) throws java.io.IOException {
                lu.set(new String(((HttpServletRequest) r).getInputStream().readAllBytes(), StandardCharsets.UTF_8));
            }
        });
        return new Object[]{res.getStatus(), lu.get(), res.getContentAsString()};
    }

    private final GardeCartesPaiement garde = new GardeCartesPaiement(true, List.of());

    @Test
    void un_corps_json_qui_porte_un_numero_de_carte_est_refuse_par_un_422() throws Exception {
        Object[] r = filtrer(garde, requete("POST", "/api/formulaires/1/saisies", "application/json", "{\"note\":\"" + PAN + "\"}"));
        assertEquals(422, r[0]);
        assertNull(r[1], "la requête n'atteint pas l'application");
        assertTrue(((String) r[2]).contains("carte_paiement_refusee"));
        assertFalse(((String) r[2]).contains("1111"), "le numéro n'est jamais renvoyé");
    }

    @Test
    void un_corps_sans_numero_est_transmis_intact() throws Exception {
        String corps = "{\"note\":\"Commande 2026-4471, téléphone 514 555 0101\"}";
        Object[] r = filtrer(garde, requete("PUT", "/api/clauses/3", "application/json; charset=UTF-8", corps));
        assertEquals(200, r[0]);
        assertEquals(corps, r[1]);
    }

    @Test
    void un_corps_plus_long_que_la_part_inspectee_est_transmis_en_entier() throws Exception {
        String corps = "x".repeat(2 * 1024 * 1024 + 10) + PAN;
        Object[] r = filtrer(garde, requete("POST", "/api/x", "text/plain", corps));
        assertEquals(200, r[0], "au-delà de 2 Mio, le reste n'est pas inspecté (limite documentée)");
        assertEquals(corps.length(), ((String) r[1]).length());
    }

    @Test
    void la_chaine_de_requete_est_inspectee_pour_toute_methode() throws Exception {
        MockHttpServletRequest r = requete("GET", "/api/recherche", null, null);
        r.setQueryString("q=4111%201111%201111%201111");
        assertEquals(422, filtrer(garde, r)[0]);
    }

    @Test
    void les_parametres_d_un_formulaire_sont_inspectes_sans_consommer_le_corps() throws Exception {
        MockHttpServletRequest r = requete("POST", "/api/interop/clients", "application/x-www-form-urlencoded", null);
        r.addParameter("nom", "Carte " + PAN);
        assertEquals(422, filtrer(garde, r)[0]);
        MockHttpServletRequest sain = requete("POST", "/api/interop/clients", "application/x-www-form-urlencoded", null);
        sain.addParameter("nom", "Système RH");
        assertEquals(200, filtrer(garde, sain)[0]);
    }

    @Test
    void un_fichier_televerse_n_est_pas_inspecte() throws Exception {
        Object[] r = filtrer(garde, requete("POST", "/api/documents", "multipart/form-data; boundary=x", PAN));
        assertEquals(200, r[0]);
    }

    @Test
    void hors_api_desactivee_ou_chemin_exempte_la_garde_s_efface() throws Exception {
        assertEquals(200, filtrer(garde, requete("POST", "/actuator/x", "application/json", PAN))[0]);
        assertEquals(422, filtrer(garde, requete("PATCH", "/scim/v2/Users/1", "application/scim+json", PAN))[0]);
        assertEquals(200, filtrer(new GardeCartesPaiement(false, List.of()), requete("POST", "/api/x", "application/json", PAN))[0]);
        GardeCartesPaiement exemptee = new GardeCartesPaiement(true, List.of(" /api/sap/ ", ""));
        assertEquals(200, filtrer(exemptee, requete("POST", "/api/sap/factures", "application/json", PAN))[0]);
        assertEquals(422, filtrer(exemptee, requete("POST", "/api/autre", "application/json", PAN))[0]);
    }

    @Test
    void une_chaine_de_requete_mal_encodee_est_lue_telle_quelle() throws Exception {
        MockHttpServletRequest r = requete("DELETE", "/api/x", null, null);
        r.setQueryString("q=%zz" + PAN.replace(" ", ""));
        assertEquals(422, filtrer(garde, r)[0]);
    }
}
