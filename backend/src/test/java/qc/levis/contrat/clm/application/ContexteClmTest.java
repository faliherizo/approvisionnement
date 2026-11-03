package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;

/** Contexte partagé : refus hors tenant, horloge à la milliseconde (empreintes rejouables), JSON tolérant en lecture. */
class ContexteClmTest {

    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), new PorteeTenantMemoire(), new ObjectMapper(),
            Clock.fixed(Instant.parse("2026-11-02T10:15:30.123456Z"), ZoneOffset.UTC));

    @Test
    void une_operation_clm_hors_espace_tenant_est_refusee() {
        assertThrows(IllegalStateException.class, ctx::tenant);
        assertEquals(7L, ctx.dansTenant(7L, ctx::tenant));
        assertThrows(IllegalStateException.class, ctx::tenant, "le tenant du traitement ne survit pas au traitement");
    }

    @Test
    void l_horodatage_est_tronque_a_la_milliseconde_pour_que_l_empreinte_relue_soit_identique() {
        assertEquals(Instant.parse("2026-11-02T10:15:30.123Z"), ctx.maintenant());
    }

    @Test
    void un_json_absent_ou_illisible_donne_la_valeur_par_defaut() {
        assertEquals(Map.of(), ctx.lireObjet(null));
        assertEquals(Map.of(), ctx.lireObjet("{pas du json"));
        assertEquals(List.of(), ctx.affectations("[{\"type\":\"INCONNU\",\"valeur\":\"x\"}]"));
        assertEquals(1, ctx.affectations("[{\"type\":\"ROLE\",\"valeur\":\"DIRECTION\"}]").size());
    }

    @Test
    void une_valeur_non_serialisable_est_signalee_plutot_que_perdue() {
        assertThrows(IllegalStateException.class, () -> ctx.ecrire(new Object()));
        assertThrows(IllegalStateException.class, () -> ctx.ecrireLisible(new Object()));
    }

    @Test
    void le_json_lisible_est_indente_pour_etre_relu_hors_de_l_application() {
        String texte = new String(ctx.ecrireLisible(Map.of("trousse", "TR-1")), StandardCharsets.UTF_8);
        assertTrue(texte.contains("\n") && texte.contains("\"trousse\" : \"TR-1\""), texte);
    }
}
