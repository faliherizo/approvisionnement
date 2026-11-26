package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Événement normalisé pour le SIEM (Annexe 10, TI-04) : résultat, sévérité, schéma ASIM, CEF échappé, détail confidentiel. */
class EvenementSiemTest {

    private static final Instant T0 = Instant.parse("2026-11-25T12:00:00Z");

    private static EvenementSiem e(String action, String detail) {
        return new EvenementSiem(42, T0, "a@iq.example", action, "TROUSSE", 7L, detail);
    }

    @Test
    void un_echec_ou_un_refus_est_un_echec_de_severite_moyenne() {
        for (String action : new String[]{"CLM_ACCES_REFUSE", "LOGIN_ECHEC", "CLM_VERIFICATION_ECHOUEE", "CLM_DOCUMENT_INFECTE",
                "CLM_INTEGRITE_ALTEREE", "COMPTE_BLOQUE"}) {
            assertTrue(e(action, null).echec(), action);
            assertEquals("Medium", e(action, null).severite(), action);
        }
        assertEquals("Low", e("CLM_ACCES_SOUTIEN", null).severite());
        assertEquals("Low", e("CLM_CONFIGURATION", null).severite());
        assertEquals("Informational", e("CLM_TROUSSE_CREEE", null).severite());
        assertFalse(e(null, null).echec(), "action absente : chaîne vide");
        assertEquals("", e(null, null).action());
    }

    @Test
    void le_format_asim_porte_les_champs_du_schema_audit_event() {
        Map<String, Object> m = e("CLM_ACCES_REFUSE", "GET /api/clm/trousses").asim();
        assertEquals(42L, m.get("EventUid"));
        assertEquals("2026-11-25T12:00:00Z", m.get("TimeGenerated"));
        assertEquals("Vina", m.get("EventVendor"));
        assertEquals("CLM", m.get("EventProduct"));
        assertEquals("AuditEvent", m.get("EventSchema"));
        assertEquals("Failure", m.get("EventResult"));
        assertEquals("CLM_ACCES_REFUSE", m.get("EventOriginalType"));
        assertEquals("a@iq.example", m.get("ActorUsername"));
        assertEquals(7L, m.get("TargetResourceId"));
        assertEquals("GET /api/clm/trousses", m.get("EventMessage"));
        assertEquals("Success", e("CLM_TROUSSE_CREEE", null).asim().get("EventResult"));
    }

    @Test
    void le_detail_d_une_valeur_modifiee_reste_au_journal_de_l_organisation() {
        EvenementSiem v = e("CLM_VALEUR_MODIFIEE", "TR-1 · « Montant » : « 1 » → « 2 »");
        assertEquals("Détail au journal d'audit de l'organisation.", v.asim().get("EventMessage"));
        assertFalse(v.cef().contains("Montant"));
    }

    @Test
    void la_ligne_cef_echappe_l_entete_et_les_extensions() {
        String cef = new EvenementSiem(1, T0, "x=y\\z", "A|B", "DOC", null, "ligne1\r\nligne2=ok").cef();
        assertTrue(cef.startsWith("CEF:0|Vina|CLM|1.0|A\\|B|A\\|B|3|rt=" + T0.toEpochMilli() + " externalId=1"), cef);
        assertTrue(cef.contains("suser=x\\=y\\\\z") && cef.contains("cs1=DOC ") && cef.contains("msg=ligne1\\nligne2\\=ok"), cef);
        assertTrue(e("CLM_ACCES_REFUSE", null).cef().contains("|7|") && e("CLM_ACCES_REFUSE", null).cef().contains("outcome=failure"));
        assertTrue(e("CLM_ACCES_SOUTIEN", "x").cef().contains("|5|") && e("CLM_ACCES_SOUTIEN", "x").cef().contains("cs1=TROUSSE#7"));
        assertEquals("", EvenementSiem.entete(null));
        assertEquals("", EvenementSiem.extension(null));
        assertTrue(new EvenementSiem(2, T0, null, "X", null, null, null).cef().contains("suser= act=X"));
    }
}
