package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Contexte partagé : refus hors tenant, horloge à la milliseconde (empreintes rejouables), JSON tolérant en lecture. */
class ContexteClmTest {

    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), new ObjectMapper(),
            Clock.fixed(Instant.parse("2026-11-02T10:15:30.123456Z"), ZoneOffset.UTC));

    @AfterEach
    void nettoyer() {
        TenantContext.clear();
    }

    @Test
    void une_operation_clm_hors_espace_tenant_est_refusee() {
        TenantContext.clear();
        assertThrows(IllegalStateException.class, ctx::tenant);
        TenantContext.set(7L);
        assertEquals(7L, ctx.tenant());
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
    }
}
