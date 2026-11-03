package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import qc.levis.contrat.clm.domain.Parcours.Affectation;
import qc.levis.contrat.clm.domain.Parcours.DefinitionEtape;
import qc.levis.contrat.clm.domain.Parcours.TypeAffecte;
import qc.levis.contrat.clm.domain.Parcours.TypeEtape;

/** Cas limites des règles du domaine CLM : valeurs absentes, blanches, en bordure, et chemins d'erreur. */
class CasLimitesDomaineClmTest {

    private static final Affectation JURISTE = new Affectation(TypeAffecte.UTILISATEUR, "j@x.ca");

    // --- Parcours ---
    @Test
    void une_affectation_sans_valeur_est_refusee_et_une_etape_sans_liste_n_a_aucun_responsable() {
        assertThrows(IllegalArgumentException.class, () -> new Affectation(TypeAffecte.ROLE, null));
        DefinitionEtape sansListe = new DefinitionEtape("a", null, 1, TypeEtape.VALIDATION, null, 1, null, null);
        assertTrue(sansListe.affectations().isEmpty());
        assertEquals(List.of("Étape sans responsable : a"), Parcours.valider(List.of(sansListe)));
        assertEquals(List.of("Le parcours doit contenir au moins une étape."), Parcours.valider(null));
    }

    @Test
    void une_cle_absente_ou_blanche_est_signalee_et_le_libelle_blanc_cede_la_place_a_la_cle() {
        List<String> erreurs = Parcours.valider(List.of(
                new DefinitionEtape(null, "Sans clé", 1, TypeEtape.VALIDATION, List.of(JURISTE), 1, null, null),
                new DefinitionEtape(" ", "Clé blanche", 1, TypeEtape.VALIDATION, List.of(JURISTE), 1, null, null),
                new DefinitionEtape("c", " ", 1, null, List.of(JURISTE), 1, null, null)));
        assertTrue(erreurs.contains("Clé d'étape absente ou en doublon : null"), erreurs.toString());
        assertTrue(erreurs.contains("Clé d'étape absente ou en doublon :  "), erreurs.toString());
        assertTrue(erreurs.contains("Type d'étape requis : c"), erreurs.toString());
    }

    @Test
    void des_phases_qui_sautent_un_rang_sont_refusees() {
        assertEquals(List.of("Les phases doivent être consécutives à partir de 1."), Parcours.valider(List.of(
                new DefinitionEtape("a", "A", 1, TypeEtape.VALIDATION, List.of(JURISTE), 1, null, null),
                new DefinitionEtape("b", "B", 3, TypeEtape.APPROBATION, List.of(JURISTE), 1, null, null))));
    }

    // --- Identité ---
    @Test
    void le_code_d_un_niveau_courriel_part_par_courriel() {
        assertEquals(List.of("COURRIEL"), Identite.canauxDuCode(Identite.Niveau.COURRIEL));
    }

    @Test
    void chaque_coordonnee_manquante_est_refusee() {
        assertThrows(IllegalArgumentException.class, () -> Identite.verifierCoordonnees(Identite.Niveau.COURRIEL, null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> Identite.verifierCoordonnees(Identite.Niveau.CODE_SMS, "a@b.ca", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> Identite.verifierCoordonnees(Identite.Niveau.QUESTION, "a@b.ca", null, null, "r"));
        assertThrows(IllegalArgumentException.class, () -> Identite.verifierCoordonnees(Identite.Niveau.QUESTION, "a@b.ca", null, " ", "r"));
        assertThrows(IllegalArgumentException.class, () -> Identite.verifierCoordonnees(Identite.Niveau.QUESTION, "a@b.ca", null, "q", null));
        assertDoesNotThrow(() -> Identite.verifierCoordonnees(Identite.Niveau.QUESTION, "a@b.ca", null, "Ville ?", "Lévis"));
    }

    // --- Formats ---
    @Test
    void une_archive_zip_n_est_pas_un_document_word_et_un_nom_vide_devient_document() {
        assertThrows(IllegalArgumentException.class, () -> FormatDocument.verifier("a.zip", new byte[]{'P', 'K', 3, 4, 0}, 100));
        assertEquals("document", FormatDocument.nomSur("dossier/"));
    }

    // --- Préremplissage ---
    @Test
    void une_saisie_identique_a_la_source_n_est_pas_un_conflit_et_une_saisie_blanche_ne_compte_pas() {
        List<Modele.Champ> champs = List.of(
                new Modele.Champ("mnt", "Montant", Modele.TypeChamp.TEXTE, true, null, null, "Montant", null, null, null, null),
                new Modele.Champ("dem", "Demandeur", Modele.TypeChamp.TEXTE, true, null, null, "Demandeur", null, null, null, null));
        Preremplissage.Resultat r = Preremplissage.appliquer(champs, Map.of("Montant", "100", "Demandeur", "Agri"), Instant.EPOCH,
                Map.of("mnt", "100", "dem", "  "));
        assertTrue(r.conflits().isEmpty());
        assertEquals(Preremplissage.Provenance.MANUEL, r.valeurs().get("mnt").provenance());
        assertEquals(Preremplissage.Provenance.SALESFORCE, r.valeurs().get("dem").provenance());
    }

    // --- Étapes, statuts, habilitations ---
    @Test
    void une_etape_en_attente_ou_active_reste_modifiable() {
        EtapeTrousse e = new EtapeTrousse(1L, 10L, new DefinitionEtape("k", "Étape", 1, TypeEtape.VALIDATION, List.of(JURISTE), 1, null, null), "[]");
        assertDoesNotThrow(e::exigerModifiable);
        e.activer(Instant.EPOCH);
        assertDoesNotThrow(e::exigerModifiable);
    }

    @Test
    void une_trousse_terminee_est_finale_et_le_superadministrateur_administre() {
        assertTrue(StatutTrousse.TERMINEE.estFinal());
        assertTrue(new Habilitations.Acteur("sa@x.ca", Set.of("SUPERADMIN"), null).estAdmin());
    }

    // --- Analyse ---
    @Test
    void sans_montant_dans_le_texte_aucune_divergence_n_est_signalee() {
        assertTrue(AnalyseContrat.analyser("Convention sans chiffre.", BigDecimal.TEN).anomalies().stream()
                .noneMatch(a -> a.cle().equals("montantDivergent")));
    }

    // --- Journal scellé ---
    @Test
    void un_maillon_qui_ne_designe_pas_son_predecesseur_rompt_la_chaine() {
        Instant h = Instant.parse("2026-11-02T10:00:00Z");
        String emp = ChaineScellee.empreinte("faux", 5L, 1, "T", "a", h, null);
        assertEquals(1L, ChaineScellee.verifier(5L, List.of(new ChaineScellee.Maillon(1, "T", "a", h, null, "faux", emp)))
                .rupture().orElseThrow());
    }

    @Test
    void un_algorithme_de_condensation_indisponible_est_une_erreur_typee() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> ChaineScellee.condenser("INCONNU-256", "x".getBytes(StandardCharsets.UTF_8)));
        assertTrue(e.getMessage().contains("INCONNU-256"));
    }

    // --- Modèles ---
    private static Modele.Champ champ(String cle, String libelle, Modele.TypeChamp type, String format, List<String> options,
                                      String defaut, String role, Modele.Position position) {
        return new Modele.Champ(cle, libelle, type, false, format, options, null, defaut, null, role, position);
    }

    private static Modele.Champ dependant(String cle, String... sources) {
        return new Modele.Champ(cle, cle, Modele.TypeChamp.TEXTE, false, null, null, null, null,
                java.util.Arrays.stream(sources).map(s -> new Modele.Condition(s, "EGAL", "1")).toList(), null, null);
    }

    @Test
    void valeurs_par_defaut_valides_pour_chaque_type() {
        List<Modele.Champ> champs = List.of(
                champ("liste", "Liste", Modele.TypeChamp.LISTE, null, List.of("a", "b"), "a", null, null),
                champ("vrai", "Vrai", Modele.TypeChamp.CASE, null, null, "true", null, null),
                champ("faux", "Faux", Modele.TypeChamp.CASE, null, null, "false", null, null),
                champ("blanc", "Blanc", Modele.TypeChamp.TEXTE, null, null, " ", null, null),
                champ("libre", "Libre", Modele.TypeChamp.TEXTE, "  ", null, "x", null, null));
        assertEquals(List.of(), Modele.valider("", champs, List.of()));
    }

    @Test
    void cle_libelle_et_zone_de_signature_incompletes_sont_signales() {
        Modele.Position p = new Modele.Position(1, 10, 10, 20, 10);
        List<String> erreurs = Modele.valider("", List.of(
                champ(null, "X", Modele.TypeChamp.TEXTE, null, null, null, null, null),
                champ("sansLibelle", null, Modele.TypeChamp.TEXTE, null, null, null, null, null),
                champ("roleBlanc", "R", Modele.TypeChamp.SIGNATURE, null, null, null, " ", p),
                champ("sansPosition", "S", Modele.TypeChamp.SIGNATURE, null, null, null, "SIGNATAIRE_1", null)), List.of(
                new Modele.Clause(null, "T", "t", null, null),
                new Modele.Clause("1bad", "T", "t", null, null)));
        for (String attendu : List.of("Nom technique invalide : null", "Libellé requis pour « sansLibelle »",
                "Zone sans signataire : « roleBlanc »", "Zone sans signataire : « sansPosition »",
                "Clé de clause invalide ou en doublon : null", "Clé de clause invalide ou en doublon : 1bad")) {
            assertTrue(erreurs.stream().anyMatch(e -> e.startsWith(attendu)), attendu + " absent de " + erreurs);
        }
    }

    @ParameterizedTest(name = "page {0}, x {1}, y {2}, {3} × {4} hors du document")
    @CsvSource({"1,-1,0,10,10", "1,0,-1,10,10", "1,0,0,0,10", "1,0,0,10,0", "1,0,95,10,10"})
    void une_zone_qui_deborde_de_la_page_est_refusee(int page, double x, double y, double largeur, double hauteur) {
        assertFalse(Modele.positionValide(new Modele.Position(page, x, y, largeur, hauteur)));
    }

    @Test
    void deux_conditions_vers_une_meme_source_ne_forment_pas_un_cycle() {
        assertEquals(Set.of(), Modele.circulaires(List.of(dependant("a", "b", "c"), dependant("b", "d"), dependant("c", "d"), dependant("d"))));
        assertEquals(Set.of("x", "y"), Modele.circulaires(List.of(dependant("x", "y"), dependant("y", "x"))));
    }

    @Test
    void un_champ_a_role_blanc_se_complete_a_la_generation() {
        assertTrue(champ("c", "C", Modele.TypeChamp.TEXTE, null, null, null, " ", null).completeALaGeneration());
    }

    @Test
    void assemblage_d_une_clause_sans_titre_et_d_une_valeur_blanche() {
        Modele.Clause sansTitre = new Modele.Clause("k", null, "Texte de clause.", "ET", List.of());
        assertEquals("Corps [x]\n\nTexte de clause.", Modele.assembler("Corps {{x}}", List.of(sansTitre), Map.of("x", " "), Set.of()));
    }
}
