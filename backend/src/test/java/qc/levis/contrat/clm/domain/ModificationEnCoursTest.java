package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Modification contrôlée des champs en cours de traitement (Annexe 10, AF-34) : refus par défaut, étape, rôle, effet. */
class ModificationEnCoursTest {

    private static final ModificationEnCours.Regle MONTANT = new ModificationEnCours.Regle("montant", null, null, null);
    private static final ModificationEnCours.Regle BENEFICIAIRE = new ModificationEnCours.Regle("beneficiaire", Arrays.asList(" v1 ", "", null),
            " REDACTEUR ", ModificationEnCours.Effet.AUCUN);
    private static final ModificationEnCours.Regle CONDITIONS = new ModificationEnCours.Regle("conditions", List.of(), " ",
            ModificationEnCours.Effet.REPRISE_ETAPE);
    private static final List<ModificationEnCours.Regle> REGLES = List.of(MONTANT, BENEFICIAIRE, CONDITIONS);

    @Test
    void une_regle_se_normalise_et_l_effet_par_defaut_est_le_plus_prudent() {
        assertEquals(List.of(), MONTANT.etapes());
        assertNull(MONTANT.roleRequis());
        assertEquals(ModificationEnCours.Effet.NOUVEAU_CYCLE, MONTANT.effet(), "sans effet précisé, toutes les approbations tombent");
        assertEquals(List.of("v1"), BENEFICIAIRE.etapes());
        assertEquals("REDACTEUR", BENEFICIAIRE.roleRequis());
        assertNull(CONDITIONS.roleRequis());
    }

    @Test
    void les_regles_visent_des_champs_et_des_etapes_du_modele() {
        assertEquals(List.of(), ModificationEnCours.valider(REGLES, Set.of("montant", "beneficiaire", "conditions"), Set.of("v1")));
        List<String> erreurs = ModificationEnCours.valider(List.of(new ModificationEnCours.Regle(null, null, null, null),
                new ModificationEnCours.Regle("x", null, null, null), MONTANT, MONTANT, BENEFICIAIRE), Set.of("montant", "beneficiaire"), Set.of());
        assertEquals(List.of("Modification en cours : champ inconnu « null ».", "Modification en cours : champ inconnu « x ».",
                "Modification en cours : champ « montant » défini deux fois.",
                "Modification en cours de « beneficiaire » : étape inconnue « v1 »."), erreurs);
    }

    @Test
    void un_champ_sans_regle_hors_etape_ou_sans_le_role_est_refuse_en_le_nommant() {
        ModificationEnCours.Verdict v = ModificationEnCours.autoriser(REGLES, List.of("programme", "beneficiaire"),
                Map.of("programme", "Programme"), Set.of("a1"), Set.of("REDACTEUR"));
        assertFalse(v.admis());
        assertEquals(List.of("« Programme » n'est pas modifiable en cours de traitement.",
                "« beneficiaire » n'est modifiable qu'aux étapes v1."), v.refus());
        ModificationEnCours.Verdict role = ModificationEnCours.autoriser(REGLES, List.of("beneficiaire"), Map.of(), Set.of("v1", "v2"),
                Set.of("LECTEUR"));
        assertEquals(List.of("« beneficiaire » exige le rôle REDACTEUR."), role.refus());
    }

    @Test
    void l_effet_retenu_est_le_plus_lourd_des_champs_admis() {
        assertEquals(new ModificationEnCours.Verdict(List.of(), ModificationEnCours.Effet.AUCUN),
                ModificationEnCours.autoriser(REGLES, List.of("beneficiaire"), Map.of(), Set.of("v1"), Set.of("REDACTEUR")));
        ModificationEnCours.Verdict v = ModificationEnCours.autoriser(REGLES, List.of("conditions", "beneficiaire", "montant", "conditions"),
                Map.of(), Set.of("v1"), Set.of("REDACTEUR"));
        assertTrue(v.admis());
        assertEquals(ModificationEnCours.Effet.NOUVEAU_CYCLE, v.effet());
        assertEquals(ModificationEnCours.Effet.REPRISE_ETAPE,
                ModificationEnCours.autoriser(REGLES, List.of("beneficiaire", "conditions"), Map.of(), Set.of("v1"), Set.of("REDACTEUR")).effet());
    }

    @Test
    void le_modele_conserve_ses_regles_en_json() {
        ModeleContrat m = new ModeleContrat(1L, "cle", 1, "Convention", "initiateur", Instant.EPOCH);
        assertEquals("[]", m.getModificationsJson());
        m.definirModificationsEnCours("[{\"champ\":\"montant\"}]", Instant.EPOCH.plusSeconds(1));
        assertEquals("[{\"champ\":\"montant\"}]", m.getModificationsJson());
        assertEquals(Instant.EPOCH.plusSeconds(1), m.getMajLe());
    }
}
