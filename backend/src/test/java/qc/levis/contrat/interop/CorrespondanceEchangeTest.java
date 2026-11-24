package qc.levis.contrat.interop;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.interop.domain.BilanReprise;
import qc.levis.contrat.interop.domain.CorrespondanceEchange;
import qc.levis.contrat.interop.domain.CorrespondanceEchange.ChampHeriteInconnu;
import qc.levis.contrat.interop.domain.CorrespondanceEchange.CorrespondanceInvalide;
import qc.levis.contrat.interop.domain.EchangeHerite;

/** CHU-8.04 (ADR-021) : la correspondance traduit dans les deux sens et refuse, plutôt que de perdre, un nom inconnu. */
class CorrespondanceEchangeTest {

    private static final Set<String> CLES = Set.of("nom", "courriel", "service");

    private static CorrespondanceEchange correspondance() {
        Map<String, String> table = new LinkedHashMap<>();
        table.put("NOM_EMPLOYE", "nom");
        table.put(" COURRIEL ", " courriel ");
        return CorrespondanceEchange.de(table, CLES);
    }

    @Test
    void les_anciens_noms_sont_traduits_vers_les_cles_du_formulaire_et_inversement() {
        CorrespondanceEchange c = correspondance();
        assertEquals(Map.of("NOM_EMPLOYE", "nom", "COURRIEL", "courriel"), c.table());
        assertEquals(Map.of("nom", "Alex", "courriel", "a@x.ca"), c.versFormulaire(Map.of("NOM_EMPLOYE", "Alex", "COURRIEL", "a@x.ca")));
        assertEquals(Map.of("NOM_EMPLOYE", "Alex"), c.versAncien(Map.of("nom", "Alex", "service", "RH")),
                "un champ inconnu de l'ancien système est omis");
    }

    @Test
    void un_nom_absent_de_la_correspondance_est_refuse_et_nomme() {
        ChampHeriteInconnu e = assertThrows(ChampHeriteInconnu.class, () -> correspondance().versFormulaire(Map.of("MATRICULE", "42")));
        assertTrue(e.getMessage().contains("MATRICULE"));
    }

    @Test
    void une_correspondance_invalide_est_refusee_avec_son_motif() {
        assertMotif(null, "Au moins une");
        assertMotif(Map.of(), "Au moins une");
        assertMotif(Map.of("A", "inexistant"), "n'existe pas");
        assertMotif(Map.of(" ", "nom"), "Ancien nom");
        assertMotif(Map.of("A", "x".repeat(121)), "Champ cible");
        Map<String, String> doublon = new LinkedHashMap<>();
        doublon.put("A", "nom");
        doublon.put("B", "nom");
        assertMotif(doublon, "plusieurs anciens noms");
        Map<String, String> cibleNulle = new LinkedHashMap<>();
        cibleNulle.put("A", null);
        assertMotif(cibleNulle, "Champ cible");
    }

    private static void assertMotif(Map<String, String> table, String motif) {
        CorrespondanceInvalide e = assertThrows(CorrespondanceInvalide.class, () -> CorrespondanceEchange.de(table, CLES));
        assertTrue(e.getMessage().contains(motif), e.getMessage());
    }

    @Test
    void un_echange_a_un_code_normalise_et_ne_se_desactive_qu_une_fois() {
        EchangeHerite e = EchangeHerite.declarer(1L, " inscriptions-rh ", 7L, correspondance(), "admin", Instant.EPOCH);
        assertEquals("inscriptions-rh", e.getCode());
        assertEquals(7L, e.getFormulaireId());
        assertEquals(1L, e.getTenantId());
        assertEquals("admin", e.getCreePar());
        assertEquals(Instant.EPOCH, e.getCreeLe());
        assertTrue(e.isActif());
        assertEquals(correspondance().table(), e.correspondance().table());
        e.desactiver();
        assertFalse(e.isActif());
        assertThrows(IllegalStateException.class, e::desactiver);
        for (String code : new String[]{null, "A", "Majuscules", "a", "-tiret", "x".repeat(61), "avec espace"}) {
            assertThrows(CorrespondanceInvalide.class, () -> EchangeHerite.declarer(1L, code, 7L, correspondance(), "a", Instant.EPOCH), code);
        }
    }

    @Test
    void le_bilan_compte_les_lignes_acceptees_et_rejetees() {
        BilanReprise b = BilanReprise.de(true, List.of(
                new BilanReprise.Ligne(1, BilanReprise.Statut.VALIDE, null, null),
                new BilanReprise.Ligne(2, BilanReprise.Statut.REJETEE, null, "motif"),
                new BilanReprise.Ligne(3, BilanReprise.Statut.REPRISE, "RH-1", null)));
        assertTrue(b.essai());
        assertEquals(3, b.total());
        assertEquals(2, b.acceptes());
        assertEquals(1, b.rejetes());
        assertThrows(UnsupportedOperationException.class, () -> b.lignes().clear());
    }
}
