package qc.levis.contrat.saisie.application;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.saisie.domain.ContenuFiche;
import qc.levis.contrat.saisie.domain.Saisie;

class DocumentFicheServiceTest {

    final ObjectMapper json = new ObjectMapper();
    final DocumentFicheService service = new DocumentFicheService(null, null, null, null, null, null);

    @Test
    void chaque_reponse_se_lit_en_clair() throws Exception {
        assertEquals("—", DocumentFicheService.valeur(null));
        assertEquals("—", DocumentFicheService.valeur(json.readTree("null")));
        assertEquals("—", DocumentFicheService.valeur(json.readTree("\" \"")));
        assertEquals("Non", DocumentFicheService.valeur(json.readTree("false")));
        assertEquals("a, 2", DocumentFicheService.valeur(json.readTree("[\"a\",2]")));
        assertEquals("{\"l\":1}", DocumentFicheService.valeur(json.readTree("{\"l\":1}")));
        assertEquals("12.5", DocumentFicheService.valeur(json.readTree("12.5")));
    }

    @Test
    void les_lignes_suivent_les_pages_et_tolerent_une_structure_illisible() {
        List<ContenuFiche.Ligne> l = service.lignes(
                "{\"pages\":[{\"champs\":[{\"code\":\"a\"},{\"cle\":\"b\",\"libelle\":\"B\"}]}]}", "{\"a\":\"x\"}");
        assertEquals(List.of(new ContenuFiche.Ligne("", "a", "x"), new ContenuFiche.Ligne("", "B", "—")), l);
        assertTrue(service.lignes("{pas du json", null).isEmpty());
        assertTrue(service.lignes(null, "").isEmpty());
    }

    @Test
    void le_personnel_et_le_demandeur_sont_reconnus() {
        Saisie s = new Saisie(1L, 2L, 1, "Alice@chu.example");
        assertTrue(AccesFiches.estDemandeur(s, new AccesFiches.Acteur("alice@chu.example", List.of())));
        assertFalse(AccesFiches.estDemandeur(s, new AccesFiches.Acteur("bob@chu.example", List.of())));
        assertFalse(AccesFiches.estDemandeur(new Saisie(1L, 2L, 1, null), new AccesFiches.Acteur("x", List.of())));
        assertTrue(new AccesFiches.Acteur("x", List.of("ROLE_APPRO")).estPersonnel());
        assertFalse(new AccesFiches.Acteur("x", List.of("ROLE_LECTEUR", "ROLE_REDACTEUR")).estPersonnel());
    }
}
