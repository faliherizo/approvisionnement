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

    // --- Règle d'affaires d'une étape (F-05, UI-11 bloc 04) ---
    @ParameterizedTest(name = "champ « {0} », opérateur « {1} » : règle incomplète")
    @CsvSource(value = {"NUL,EGAL", "' ',EGAL", "montant,NUL", "montant,' '"}, nullValues = "NUL")
    void une_regle_d_etape_sans_champ_ou_sans_operateur_est_incomplete(String champ, String operateur) {
        DefinitionEtape d = new DefinitionEtape("a", "Direction", 1, TypeEtape.APPROBATION, List.of(JURISTE), 1, null, null,
                List.of(new Modele.Condition(champ, operateur, "1")));
        assertEquals(List.of("Règle incomplète pour l'étape Direction"), Parcours.valider(List.of(d)));
    }

    @Test
    void une_regle_d_etape_complete_est_conservee_et_l_absence_de_regle_vaut_liste_vide() {
        Modele.Condition seuil = new Modele.Condition("montant", "SUP", "100000");
        DefinitionEtape avecRegle = new DefinitionEtape("a", "A", 1, TypeEtape.APPROBATION, List.of(JURISTE), 1, null, null, List.of(seuil));
        assertEquals(List.of(), Parcours.valider(List.of(avecRegle)));
        assertEquals(List.of(seuil), avecRegle.conditions());
        assertEquals(List.of(), new DefinitionEtape("b", "B", 1, TypeEtape.VALIDATION, List.of(JURISTE), 1, null, null, null).conditions());
    }

    @Test
    void une_etape_non_requise_ne_retient_pas_sa_phase_ni_la_fin_du_parcours() {
        Parcours.Avancement a = Parcours.avancer(List.of(new Parcours.EtatEtape(1L, 1, Parcours.StatutEtape.NON_REQUISE),
                new Parcours.EtatEtape(2L, 2, Parcours.StatutEtape.EN_ATTENTE)));
        assertEquals(Set.of(2L), a.aActiver());
        assertEquals(StatutTrousse.TERMINEE, Parcours.avancer(List.of(new Parcours.EtatEtape(1L, 1, Parcours.StatutEtape.APPROUVEE),
                new Parcours.EtatEtape(2L, 2, Parcours.StatutEtape.NON_REQUISE))).resultat().orElseThrow());
    }

    @Test
    void une_etape_ecartee_est_close_sans_decision_et_garde_sa_regle() {
        EtapeTrousse e = new EtapeTrousse(1L, 10L, new DefinitionEtape("k", "Étape", 1, TypeEtape.APPROBATION, List.of(JURISTE), 1, null, null),
                "[]", "[{\"champ\":\"montant\",\"operateur\":\"SUP\",\"valeur\":\"100000\"}]");
        e.ecarter(Instant.EPOCH);
        assertEquals(Parcours.StatutEtape.NON_REQUISE, e.getStatut());
        assertTrue(e.getConditionsJson().contains("montant"));
        assertThrows(IllegalStateException.class, e::exigerModifiable);
        assertEquals("[]", new EtapeTrousse(1L, 10L, new DefinitionEtape("k", "K", 1, TypeEtape.VALIDATION, List.of(JURISTE), 1, null, null), "[]")
                .getConditionsJson());
    }

    // --- Zone de signature attribuée (UI-15) ---
    @Test
    void attribuer_une_zone_exige_un_role_et_le_normalise() {
        Participant p = new Participant(1L, 10L, "Marie", "m@x.ca", null, "SIGNATAIRE_1", true, Identite.Niveau.COURRIEL);
        assertThrows(IllegalArgumentException.class, () -> p.attribuerRole(null));
        assertThrows(IllegalArgumentException.class, () -> p.attribuerRole(" "));
        p.attribuerRole(" signataire_2 ");
        assertEquals("SIGNATAIRE_2", p.getRoleContrat());
    }

    // --- Valeurs saisies par l'initiateur (F-04) ---
    @ParameterizedTest(name = "« {1} » pour un champ {0} au format « {2} » : {3}")
    @CsvSource(value = {
            "TEXTE,QC-123,QC-\\d+,true", "TEXTE,123,QC-\\d+,false", "TEXTE,x,[,false", "TEXTE,libre,NUL,true",
            "LISTE,b,NUL,true", "LISTE,z,NUL,false", "DATE,2026-11-02,NUL,true", "DATE,02/11/2026,NUL,false",
            "CASE,true,NUL,true", "CASE,oui,NUL,false"}, nullValues = "NUL")
    void une_valeur_saisie_respecte_le_format_puis_le_type_du_champ(Modele.TypeChamp type, String valeur, String format, boolean attendu) {
        Modele.Champ c = champ("c", "C", type, format, List.of("a", "b"), null, null, null);
        assertEquals(attendu, Modele.valeurValide(c, valeur));
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
    void une_trousse_terminee_est_finale_et_le_superadministrateur_n_administre_aucune_donnee_d_organisation() {
        assertTrue(StatutTrousse.TERMINEE.estFinal());
        // Annexe 10, SEC-17 (ADR-031) : l'exploitant n'administre pas les trousses d'une organisation.
        assertFalse(new Habilitations.Acteur("sa@x.ca", Set.of("SUPERADMIN"), null).estAdmin());
        assertTrue(new Habilitations.Acteur("ti@x.ca", Set.of("ADMIN"), null).estAdmin());
        assertTrue(new Habilitations.Acteur("aff@x.ca", Set.of("ADMIN_AFFAIRES"), null).estAdmin());
        assertFalse(new Habilitations.Acteur("tec@x.ca", Set.of("ADMIN_TECHNIQUE"), null).estAdmin());
    }

    @Test
    void l_auditeur_consulte_tout_le_tenant_sans_y_intervenir() {
        Trousse t = new Trousse(1L, "TR-1", "T", "init@x.ca", Instant.EPOCH);
        Habilitations.Acteur auditeur = new Habilitations.Acteur("audit@x.ca", Set.of("AUDITEUR"), null);
        Habilitations.Acteur lecteur = new Habilitations.Acteur("lecteur@x.ca", Set.of("LECTEUR"), null);
        Map<Long, String> courriels = Map.of(7L, "lecteur@x.ca");

        assertTrue(auditeur.estAuditeur());
        assertFalse(lecteur.estAuditeur());
        assertTrue(Habilitations.peutConsulter(t, auditeur, List.of(), Map.of()), "l'auditeur voit une trousse où il n'intervient pas");
        assertFalse(Habilitations.peutVoir(t, auditeur, List.of(), Map.of()), "la consultation n'en fait pas un intervenant");
        assertFalse(Habilitations.peutModifier(t, auditeur));
        assertFalse(Habilitations.peutConsulter(t, lecteur, List.of(), Map.of()), "hors audit, la consultation suit le périmètre");
        assertTrue(Habilitations.peutConsulter(t, lecteur, List.of(), courriels), "un participant désigné consulte sa trousse");
    }

    // --- Analyse ---
    @Test
    void sans_montant_dans_le_texte_aucune_divergence_n_est_signalee() {
        assertTrue(AnalyseContrat.analyser("Convention sans chiffre.", BigDecimal.TEN, Map.of(), Map.of()).anomalies().stream()
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
