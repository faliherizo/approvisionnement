package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Rapports personnalisables (Annexe 10, AF-37) : définition bornée, colonnes retenues, délais tirés du journal scellé. */
class RapportClmTest {

    private static final Instant T0 = Instant.parse("2026-11-01T12:00:00Z");

    private static RapportClm.Evenement e(String type, String detail, int heures) {
        return new RapportClm.Evenement(type, detail, T0.plus(Duration.ofHours(heures)));
    }

    @Test
    void une_definition_se_normalise_et_signale_chaque_erreur() {
        RapportClm.Definition d = new RapportClm.Definition("  Délais  ", RapportClm.Type.DELAIS, 30, " ", " VP ", null,
                Arrays.asList(" delaiHeures ", "", null, "reference"));
        assertEquals("Délais", d.nom());
        assertNull(d.unite());
        assertEquals("VP", d.typeContrat());
        assertEquals(List.of("delaiHeures", "reference"), d.colonnesRetenues(), "ordre choisi conservé");
        assertEquals(List.of(), d.erreurs());
        assertEquals(RapportClm.Type.DELAIS.colonnes(), new RapportClm.Definition("x", RapportClm.Type.DELAIS, 1, null, null, null, null)
                .colonnesRetenues(), "aucune colonne choisie : toutes");
        assertEquals(List.of("Nom du rapport : 1 à 120 caractères.", "Type de rapport requis."),
                new RapportClm.Definition(null, null, 0, null, null, null, null).erreurs());
        assertEquals(List.of("Nom du rapport : 1 à 120 caractères.", "Période : de 1 à 366 jours.", "Colonne « inconnue » inconnue pour ce type de rapport."),
                new RapportClm.Definition("x".repeat(121), RapportClm.Type.INCIDENTS, 367, null, null, null, List.of("inconnue", "detail")).erreurs());
        assertEquals(List.of("Nom du rapport : 1 à 120 caractères."), new RapportClm.Definition(" ", RapportClm.Type.UTILISATION, 1, null, null, null, null).erreurs());
    }

    @Test
    void la_definition_enregistree_se_relit_a_l_identique() {
        RapportClm r = new RapportClm(4L, "admin.affaires");
        RapportClm.Definition d = new RapportClm.Definition("Rejets", RapportClm.Type.REJETS, 90, "VP", "GARANTIE", "REJETEE", List.of("reference", "motif"));
        r.definir(d, T0);
        assertEquals(d, r.definition());
        r.definir(new RapportClm.Definition("Tout", RapportClm.Type.REJETS, 7, null, null, null, List.of()), T0);
        assertEquals(List.of(), r.definition().colonnes());
        assertNull(new RapportClm(4L, "x").definition().type(), "jamais définie : définition vide");
        assertEquals(List.of(), new RapportClm(4L, "x").definition().colonnes());
        assertEquals(4L, r.getTenantId());
        assertEquals("admin.affaires", r.getProprietaire());
        assertEquals(T0, r.getModifieLe());
        assertNull(r.getId());
    }

    @Test
    void le_delai_de_traitement_va_du_lancement_a_l_achevement() {
        assertEquals(Optional.of(Duration.ofHours(30)), RapportClm.delaiTraitement(List.of(e("TROUSSE_CREEE", null, 0), e("TROUSSE_LANCEE", null, 2),
                e("TROUSSE_LANCEE", null, 5), e("TROUSSE_TERMINEE", null, 32))));
        assertEquals(Optional.empty(), RapportClm.delaiTraitement(List.of(e("TROUSSE_LANCEE", null, 2))), "pas encore terminée");
        assertEquals(Optional.empty(), RapportClm.delaiTraitement(List.of(e("TROUSSE_TERMINEE", null, 2))), "jamais lancée");
    }

    @Test
    void chaque_etape_decidee_compte_de_son_activation_a_sa_decision() {
        Map<String, List<Duration>> d = RapportClm.delaisEtapes(List.of(e("TROUSSE_LANCEE", null, 0), e("ETAPE_ACTIVEE", "Validation", 0), e("ETAPE_APPROUVEE", "Validation", 4),
                e("ETAPE_ACTIVEE", "Approbation", 4), e("ETAPE_REJETEE", "Approbation", 10), e("ETAPE_APPROUVEE", "Orpheline", 11),
                e("ETAPE_ACTIVEE", "Validation", 12), e("ETAPE_APPROUVEE", "Validation", 14), e("ETAPE_ACTIVEE", "En cours", 15)));
        assertEquals(List.of(Duration.ofHours(4), Duration.ofHours(2)), d.get("Validation"));
        assertEquals(List.of(Duration.ofHours(6)), d.get("Approbation"));
        assertFalse(d.containsKey("Orpheline") || d.containsKey("En cours"), "sans activation ou sans décision, pas de délai");
    }

    @Test
    void la_moyenne_est_en_heures_a_une_decimale() {
        assertEquals(Optional.empty(), RapportClm.moyenneHeures(List.of()));
        assertEquals(Optional.of(1.5), RapportClm.moyenneHeures(List.of(Duration.ofHours(1), Duration.ofHours(2))));
        assertEquals(Optional.of(0.3), RapportClm.moyenneHeures(List.of(Duration.ofMinutes(20))));
        assertEquals(2.5, RapportClm.heures(Duration.ofMinutes(150)));
        assertEquals(List.of("indicateur", "valeur"), RapportClm.Type.UTILISATION.colonnes());
    }
}
