package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Addenda 3 : mesure par enveloppe envoyée (Q39) et bordereau de destruction (Q48). */
class Addenda3DomaineTest {

    private static final ZoneId FUSEAU = ZoneId.of("America/Toronto");

    private static Enveloppes.Evenement e(long trousse, String type, String le) {
        return new Enveloppes.Evenement(trousse, type, Instant.parse(le));
    }

    @Test
    void une_enveloppe_se_compte_une_fois_au_mois_de_son_premier_envoi() {
        Enveloppes.Releve r = Enveloppes.compter(List.of(
                e(1, "INVITATION_ENVOYEE", "2026-10-05T14:00:00Z"),
                e(1, "INVITATION_ENVOYEE", "2026-10-06T14:00:00Z"),
                e(1, "TROUSSE_TERMINEE", "2026-10-07T14:00:00Z"),
                e(2, "INVITATION_ENVOYEE", "2026-11-02T14:00:00Z"),
                e(2, "ECHEANCE_DEPASSEE", "2026-11-20T14:00:00Z"),
                e(2, "ECHEANCE_DEPASSEE", "2026-11-21T14:00:00Z"),
                e(2, "TROUSSE_ANNULEE", "2026-11-22T14:00:00Z"),
                e(3, "TROUSSE_ANNULEE", "2026-11-03T14:00:00Z"),
                e(4, "ECHEANCE_DEPASSEE", "2026-11-03T14:00:00Z"),
                e(4, "TROUSSE_TERMINEE", "2026-11-03T15:00:00Z"),
                e(4, "TROUSSE_REJETEE", "2026-11-03T16:00:00Z"),
                e(5, "INVITATION_ENVOYEE", "2026-11-04T14:00:00Z"),
                e(5, "TROUSSE_REJETEE", "2026-11-05T14:00:00Z"),
                e(6, "INVITATION_ENVOYEE", "2026-11-06T14:00:00Z"),
                e(6, "VALEURS_SAISIES", "2026-11-06T15:00:00Z")), Set.of(6L), FUSEAU);
        assertEquals(3, r.envoyees(), "trousses 1, 2 et 5 ; la 6 était déjà envoyée, la 3 et la 4 jamais");
        assertEquals(2, r.renvois(), "renvoi de la 1 et envoi de la 6, déjà comptée : non facturés");
        assertEquals(1, r.terminees());
        assertEquals(1, r.rejetees());
        assertEquals(1, r.annuleesApresEnvoi());
        assertEquals(1, r.annuleesAvantEnvoi());
        assertEquals(1, r.echeancesDepassees(), "une trousse échue deux fois compte une fois");
        assertEquals(Map.of(YearMonth.of(2026, 10), 1, YearMonth.of(2026, 11), 2), r.parMois());
    }

    @Test
    void le_bordereau_ne_se_consigne_qu_une_fois_sur_une_trousse_detruite() {
        Instant t0 = Instant.parse("2026-11-17T12:00:00Z");
        Trousse t = new Trousse(3L, "TR-1", "Entente", "i@iq.example", t0);
        assertThrows(IllegalStateException.class, () -> t.consignerBordereau("{}"), "pas encore détruite");
        t.changerStatut(StatutTrousse.ANNULEE, t0);
        t.detruire(t0);
        t.consignerBordereau("{\"resultat\":\"DETRUIT\"}");
        assertEquals("{\"resultat\":\"DETRUIT\"}", t.getBordereauDestruction());
        assertThrows(IllegalStateException.class, () -> t.consignerBordereau("{}"));
    }

    @Test
    void le_compteur_donne_des_numeros_successifs_sur_cinq_chiffres() {
        CompteurReference c = new CompteurReference(3L, "TR", 2026, 41);
        assertEquals("TR-2026-00042", c.suivante());
        assertEquals("TR-2026-00043", c.suivante());
        assertEquals(43, c.getDernier());
    }
}
