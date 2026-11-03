package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.domain.Parcours.Affectation;
import qc.levis.contrat.clm.domain.Parcours.DefinitionEtape;
import qc.levis.contrat.clm.domain.Parcours.EtatEtape;
import qc.levis.contrat.clm.domain.Parcours.PolitiqueRejet;
import qc.levis.contrat.clm.domain.Parcours.StatutEtape;
import qc.levis.contrat.clm.domain.Parcours.TypeAffecte;
import qc.levis.contrat.clm.domain.Parcours.TypeEtape;
import qc.levis.contrat.regle.domain.ConditionDef;

/** Règles pures du domaine CLM : parcours, identité, formats, scellement, modèles, préremplissage, relances, IA. */
class DomaineClmTest {

    private static DefinitionEtape etape(String cle, int phase, TypeEtape type, int quorum, Affectation... affs) {
        return new DefinitionEtape(cle, null, phase, type, List.of(affs), quorum, null, null);
    }

    private static final Affectation JURISTE = new Affectation(TypeAffecte.UTILISATEUR, "j@x.ca");

    // --- Parcours (ST-07/ST-08) ---
    @Test
    void parcours_valide_sequentiel_parallele_mixte() {
        assertTrue(Parcours.valider(List.of(etape("a", 1, TypeEtape.VALIDATION, 1, JURISTE),
                etape("b", 2, TypeEtape.APPROBATION, 1, new Affectation(TypeAffecte.ROLE, "DIRECTION")),
                etape("c", 2, TypeEtape.APPROBATION, 1, new Affectation(TypeAffecte.GROUPE, "Finances")),
                etape("d", 3, TypeEtape.SIGNATURE, 1, new Affectation(TypeAffecte.PARTICIPANT, "7")))).isEmpty());
    }

    @Test
    void parcours_invalide_signale_chaque_incoherence() {
        assertEquals(List.of("Le parcours doit contenir au moins une étape."), Parcours.valider(List.of()));
        List<String> e = Parcours.valider(List.of(
                etape("a", 1, TypeEtape.VALIDATION, 1),
                etape("a", 3, null, 0, JURISTE),
                etape("q", 1, TypeEtape.APPROBATION, 2, JURISTE),
                etape("s", 1, TypeEtape.SIGNATURE, 1, JURISTE),
                new DefinitionEtape("d", "D", 0, TypeEtape.VALIDATION, List.of(JURISTE), 1, PolitiqueRejet.UN_REFUS, 0)));
        String tout = String.join("|", e);
        assertTrue(tout.contains("Étape sans responsable : a"));
        assertTrue(tout.contains("doublon : a"));
        assertTrue(tout.contains("Type d'étape requis"));
        assertTrue(tout.contains("Quorum incohérent pour l'étape q"));
        assertTrue(tout.contains("Quorum incohérent pour l'étape a"));
        assertTrue(tout.contains("n'affecte que des participants"));
        assertTrue(tout.contains("Phase invalide pour l'étape D"));
        assertTrue(tout.contains("Échéance invalide"));
        assertTrue(tout.contains("consécutives"));
        assertThrows(IllegalArgumentException.class, () -> new Affectation(TypeAffecte.ROLE, " "));
        assertThrows(IllegalArgumentException.class, () -> new Affectation(null, "x"));
    }

    @Test
    void quorum_et_politiques_de_rejet() {
        assertEquals(StatutEtape.APPROUVEE, Parcours.statutApresDecisions(2, PolitiqueRejet.UN_REFUS, 3, 2, 0));
        assertEquals(StatutEtape.ACTIVE, Parcours.statutApresDecisions(2, PolitiqueRejet.UN_REFUS, 3, 1, 0));
        assertEquals(StatutEtape.REJETEE, Parcours.statutApresDecisions(2, PolitiqueRejet.UN_REFUS, 3, 1, 1));
        assertEquals(StatutEtape.ACTIVE, Parcours.statutApresDecisions(2, PolitiqueRejet.QUORUM_INATTEIGNABLE, 3, 1, 1));
        assertEquals(StatutEtape.REJETEE, Parcours.statutApresDecisions(2, PolitiqueRejet.QUORUM_INATTEIGNABLE, 3, 0, 2));
    }

    @Test
    void avancement_active_la_phase_suivante_puis_termine() {
        Parcours.Avancement a = Parcours.avancer(List.of(new EtatEtape(1L, 1, StatutEtape.EN_ATTENTE),
                new EtatEtape(2L, 1, StatutEtape.EN_ATTENTE), new EtatEtape(3L, 2, StatutEtape.EN_ATTENTE)));
        assertEquals(Set.of(1L, 2L), a.aActiver());
        assertTrue(a.resultat().isEmpty());
        a = Parcours.avancer(List.of(new EtatEtape(1L, 1, StatutEtape.APPROUVEE), new EtatEtape(2L, 1, StatutEtape.ACTIVE),
                new EtatEtape(3L, 2, StatutEtape.EN_ATTENTE)));
        assertTrue(a.aActiver().isEmpty(), "la phase 2 attend la fin de la phase 1");
        a = Parcours.avancer(List.of(new EtatEtape(1L, 1, StatutEtape.APPROUVEE), new EtatEtape(9L, 1, StatutEtape.ANNULEE),
                new EtatEtape(3L, 2, StatutEtape.EN_ATTENTE)));
        assertEquals(Set.of(3L), a.aActiver());
        assertEquals(StatutTrousse.TERMINEE, Parcours.avancer(List.of(new EtatEtape(1L, 1, StatutEtape.APPROUVEE))).resultat().orElseThrow());
        assertEquals(StatutTrousse.REJETEE, Parcours.avancer(List.of(new EtatEtape(1L, 1, StatutEtape.REJETEE))).resultat().orElseThrow());
    }

    @Test
    void cycle_de_vie_de_la_trousse() {
        assertEquals(StatutTrousse.EN_TRAITEMENT, StatutTrousse.BROUILLON.vers(StatutTrousse.EN_TRAITEMENT));
        assertThrows(IllegalStateException.class, () -> StatutTrousse.TERMINEE.vers(StatutTrousse.BROUILLON));
        assertThrows(IllegalStateException.class, () -> StatutTrousse.BROUILLON.vers(StatutTrousse.TERMINEE));
        assertTrue(StatutTrousse.BROUILLON.estModifiable());
        assertFalse(StatutTrousse.REJETEE.estModifiable());
        assertTrue(StatutTrousse.ANNULEE.estFinal());
        assertFalse(StatutTrousse.REJETEE.estFinal());
    }

    // --- Identité (RST-17) ---
    @Test
    void niveau_identite_jamais_sous_le_minimum_du_risque() {
        assertEquals(Identite.Niveau.COURRIEL, Identite.minimumRequis(Identite.Risque.FAIBLE, true));
        assertEquals(Identite.Niveau.CODE_SMS, Identite.minimumRequis(null, true));
        assertEquals(Identite.Niveau.FORTE, Identite.minimumRequis(Identite.Risque.ELEVE, true));
        assertEquals(Identite.Niveau.CODE_SMS, Identite.minimumRequis(Identite.Risque.ELEVE, false));
        assertEquals(Identite.Niveau.COURRIEL, Identite.minimumRequis(Identite.Risque.MOYEN, false));
        assertEquals(Identite.Niveau.FORTE, Identite.niveauApplique(Identite.Risque.ELEVE, true, Identite.Niveau.COURRIEL));
        assertEquals(Identite.Niveau.QUESTION, Identite.niveauApplique(Identite.Risque.FAIBLE, true, Identite.Niveau.QUESTION));
        assertEquals(Identite.Niveau.COURRIEL, Identite.niveauApplique(Identite.Risque.FAIBLE, true, null));
        assertEquals(List.of("COURRIEL", "SMS"), Identite.canauxDuCode(Identite.Niveau.FORTE));
        assertTrue(Identite.canauxDuCode(Identite.Niveau.QUESTION).isEmpty());
    }

    @Test
    void coordonnees_exigees_par_le_niveau() {
        assertDoesNotThrow(() -> Identite.verifierCoordonnees(Identite.Niveau.CODE_SMS, "a@b.ca", "(514) 555-0101", null, null));
        assertThrows(IllegalArgumentException.class, () -> Identite.verifierCoordonnees(Identite.Niveau.COURRIEL, "invalide", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> Identite.verifierCoordonnees(Identite.Niveau.FORTE, "a@b.ca", "12", null, null));
        assertThrows(IllegalArgumentException.class, () -> Identite.verifierCoordonnees(Identite.Niveau.QUESTION, "a@b.ca", null, "Q ?", " "));
        assertEquals("ecole montreal", Identite.normaliserReponse("  École   MONTRÉAL "));
        assertEquals("", Identite.normaliserReponse(null));
    }

    // --- Formats (RST-16) ---
    @Test
    void formats_deduits_des_octets() {
        assertEquals(FormatDocument.Type.PDF, FormatDocument.verifier("a.PDF", "%PDF-1.7".getBytes(), 100));
        assertEquals(FormatDocument.Type.DOCX, FormatDocument.verifier("a.docx", new byte[]{'P', 'K', 3, 4, 0}, 100));
        assertThrows(IllegalArgumentException.class, () -> FormatDocument.verifier("a.docx", "%PDF-1.7".getBytes(), 100));
        assertThrows(IllegalArgumentException.class, () -> FormatDocument.verifier("a.pdf", new byte[0], 100));
        assertThrows(IllegalArgumentException.class, () -> FormatDocument.verifier("a.pdf", null, 100));
        assertThrows(IllegalArgumentException.class, () -> FormatDocument.verifier(null, "%PD".getBytes(), 100));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> FormatDocument.verifier("a.pdf", new byte[3_000_000], 2_097_152));
        assertTrue(e.getMessage().contains("2,0 Mo"), e.getMessage());
        assertEquals("rapport.pdf", FormatDocument.nomSur("C:\\temp\\rapport.pdf"));
        assertEquals("document.htaccess", FormatDocument.nomSur("../.htaccess"));
        assertEquals("a_b.pdf", FormatDocument.nomSur("a<b.pdf"));
        assertEquals("document", FormatDocument.nomSur(null));
        assertEquals(200, FormatDocument.nomSur("x".repeat(300) + ".pdf").length());
        assertEquals("application/pdf", FormatDocument.Type.PDF.mime());
    }

    // --- Journal scellé (ST-10/ST-14) ---
    @Test
    void chaine_scellee_detecte_toute_alteration() {
        Instant h = Instant.parse("2026-11-02T10:00:00Z");
        List<ChaineScellee.Maillon> chaine = new ArrayList<>();
        String prec = ChaineScellee.ORIGINE;
        for (int i = 1; i <= 3; i++) {
            String emp = ChaineScellee.empreinte(prec, 5L, i, "T" + i, "a|b", h, i == 2 ? null : "d");
            chaine.add(new ChaineScellee.Maillon(i, "T" + i, "a|b", h, i == 2 ? null : "d", prec, emp));
            prec = emp;
        }
        assertTrue(ChaineScellee.verifier(5L, chaine).integre());
        List<ChaineScellee.Maillon> alteree = new ArrayList<>(chaine);
        ChaineScellee.Maillon m = alteree.get(1);
        alteree.set(1, new ChaineScellee.Maillon(2, m.type(), m.acteur(), m.horodatage(), "falsifié", m.empreintePrecedente(), m.empreinte()));
        assertEquals(2L, ChaineScellee.verifier(5L, alteree).rupture().orElseThrow());
        assertEquals(3L, ChaineScellee.verifier(5L, List.of(chaine.get(0), chaine.get(2))).rupture().orElseThrow(), "suppression du maillon 2");
        assertFalse(ChaineScellee.verifier(6L, chaine).integre(), "une chaîne n'est valide que pour sa trousse");
        assertEquals(64, ChaineScellee.sha256("x".getBytes(StandardCharsets.UTF_8)).length());
    }

    // --- Modèles (ST-05) ---
    private static Modele.Champ champ(String cle, Modele.TypeChamp type, ConditionDef... conds) {
        return new Modele.Champ(cle, cle, type, false, null, null, null, null, List.of(conds), null, null);
    }

    @Test
    void modele_valide_et_assemblage_avec_clauses() {
        List<Modele.Champ> champs = List.of(champ("montant", Modele.TypeChamp.TEXTE),
                new Modele.Champ("sig", "Signature", Modele.TypeChamp.SIGNATURE, true, null, null, null, null, null, "SIGNATAIRE_1",
                        new Modele.Position(1, 10, 80, 30, 10)));
        List<Modele.Clause> clauses = List.of(new Modele.Clause("g", "Garantie", "Garantie de {{montant}}.", "ET",
                List.of(new ConditionDef("montant", "SUP", "100"))));
        assertTrue(Modele.valider("Montant : {{montant}} {{clauses}}", champs, clauses).isEmpty());
        assertEquals("Montant : 500\n\nGarantie\nGarantie de 500.", Modele.assembler("Montant : {{montant}}", clauses, Map.of("montant", "500"), Set.of()));
        assertEquals("Montant : [montant]", Modele.assembler("Montant : {{montant}}", clauses, Map.of(), Set.of("g")));
        assertEquals("A Garantie\nGarantie de 1. B", Modele.assembler("A {{clauses}} B", clauses, Map.of("montant", "1"), Set.of()));
        assertEquals("", Modele.assembler(null, null, Map.of(), Set.of()));
        assertTrue(champs.get(0).completeALaGeneration());
        assertFalse(champs.get(1).completeALaGeneration());
        assertEquals(Set.of(), Modele.variables(null));
    }

    @Test
    void modele_invalide_signale_chaque_erreur() {
        List<Modele.Champ> champs = List.of(
                new Modele.Champ("1bad", "x", Modele.TypeChamp.TEXTE, false, null, null, null, null, null, null, null),
                new Modele.Champ("dup", " ", null, false, null, null, null, null, null, null, null),
                new Modele.Champ("dup", "D", Modele.TypeChamp.LISTE, false, "[", List.of(), null, "z", null, null, null),
                new Modele.Champ("d", "D", Modele.TypeChamp.DATE, false, null, null, null, "2026-13-40", null, null, null),
                new Modele.Champ("c", "C", Modele.TypeChamp.CASE, false, "\\w+", null, null, "oui", null, null, null),
                new Modele.Champ("s", "S", Modele.TypeChamp.SIGNATURE, false, null, null, null, null, null, null, new Modele.Position(1, 90, 0, 20, 5)),
                champ("a", Modele.TypeChamp.TEXTE, new ConditionDef("b", "EGAL", "1")),
                champ("b", Modele.TypeChamp.TEXTE, new ConditionDef("a", "EGAL", "1"), new ConditionDef("fantome", "EGAL", "1")));
        List<Modele.Clause> clauses = List.of(new Modele.Clause("k", "K", "t", null, List.of(new ConditionDef("zz", "EGAL", "1"))),
                new Modele.Clause("k", "K", "t", null, null));
        String tout = String.join("|", Modele.valider("{{inconnue}} {{a}}", champs, clauses));
        for (String attendu : List.of("Nom technique invalide : 1bad", "Nom technique en doublon : dup", "Libellé requis", "Type requis",
                "Une liste exige des options", "Format invalide pour « dup »", "Valeur par défaut invalide pour « d »",
                "Valeur par défaut invalide pour « c »", "Zone sans signataire", "Position hors du document", "Condition circulaire",
                "condition de « b » : fantome", "clause « k » : zz", "Clé de clause invalide ou en doublon : k", "corps : inconnue")) {
            assertTrue(tout.contains(attendu), attendu + " absent de : " + tout);
        }
        assertTrue(Modele.valider(null, null, null).isEmpty());
        assertFalse(Modele.positionValide(new Modele.Position(0, 0, 0, 1, 1)));
    }

    @Test
    void valeur_par_defaut_controlee_par_le_format_puis_par_le_type() {
        List<Modele.Champ> valides = List.of(
                new Modele.Champ("note", "Note", Modele.TypeChamp.TEXTE, false, null, null, null, "libre", null, null, null),
                new Modele.Champ("debut", "Début", Modele.TypeChamp.DATE, false, null, null, null, "2026-07-01", null, null, null),
                new Modele.Champ("code", "Code", Modele.TypeChamp.TEXTE, false, "\\d{3}", null, null, "123", null, null, null));
        assertTrue(Modele.valider("{{note}} {{debut}} {{code}}", valides, List.of()).isEmpty());

        Modele.Champ horsFormat = new Modele.Champ("code", "Code", Modele.TypeChamp.TEXTE, false, "\\d{3}", null, null, "12a", null, null, null);
        assertEquals(List.of("Valeur par défaut invalide pour « code »."), Modele.valider("", List.of(horsFormat), List.of()));
    }

    @Test
    void une_clause_sans_texte_ne_garde_que_son_titre() {
        Modele.Clause annexe = new Modele.Clause("annexe", "Annexe A", null, "ET", List.of());
        assertEquals("Corps\n\nAnnexe A", Modele.assembler("Corps", List.of(annexe), Map.of(), Set.of()));
    }

    @Test
    void cycle_de_vie_du_modele() {
        assertEquals(Modele.Statut.EN_REVISION, Modele.Statut.BROUILLON.vers(Modele.Statut.EN_REVISION));
        assertEquals(Modele.Statut.BROUILLON, Modele.Statut.EN_REVISION.vers(Modele.Statut.BROUILLON));
        assertEquals(Modele.Statut.RETIRE, Modele.Statut.PUBLIE.vers(Modele.Statut.RETIRE));
        assertThrows(IllegalStateException.class, () -> Modele.Statut.BROUILLON.vers(Modele.Statut.PUBLIE));
        assertThrows(IllegalStateException.class, () -> Modele.Statut.RETIRE.vers(Modele.Statut.PUBLIE));
    }

    // --- Préremplissage (ST-06) ---
    @Test
    void preremplissage_trace_la_provenance_les_manquants_et_les_conflits() {
        Instant lu = Instant.parse("2026-11-01T12:00:00Z");
        List<Modele.Champ> champs = List.of(
                new Modele.Champ("dem", "Demandeur", Modele.TypeChamp.TEXTE, true, null, null, "Demandeur", null, null, null, null),
                new Modele.Champ("mnt", "Montant", Modele.TypeChamp.TEXTE, true, null, null, "Montant", null, null, null, null),
                new Modele.Champ("dev", "Devise", Modele.TypeChamp.TEXTE, true, null, null, null, "CAD", null, null, null),
                new Modele.Champ("ent", "Entité", Modele.TypeChamp.TEXTE, true, null, null, "Entite", null, null, "INITIATEUR", null),
                new Modele.Champ("opt", "Optionnel", Modele.TypeChamp.TEXTE, false, null, null, null, null, null, null, null),
                new Modele.Champ("tit", "Titre", Modele.TypeChamp.TEXTE, true, null, null, null, null, null, "SIGNATAIRE_1", null));
        Preremplissage.Resultat r = Preremplissage.appliquer(champs, Map.of("Demandeur", "Agri", "Montant", "100"), lu, Map.of("mnt", "120"));
        assertEquals(Preremplissage.Provenance.SALESFORCE, r.valeurs().get("dem").provenance());
        assertEquals(lu, r.valeurs().get("dem").recupereLe());
        assertEquals(Preremplissage.Provenance.MANUEL, r.valeurs().get("mnt").provenance());
        assertEquals(Preremplissage.Provenance.DEFAUT, r.valeurs().get("dev").provenance());
        assertEquals(List.of("ent"), r.manquants());
        assertEquals("mnt", r.conflits().get(0).cle());
        assertEquals("120", r.brutes().get("mnt"));
        assertEquals(List.of("dem", "mnt", "ent"), Preremplissage.appliquer(champs, null, lu, null).manquants().subList(0, 3));
    }

    // --- Relances (ST-11/ST-12) ---
    @Test
    void rappels_echeances_et_reprises() {
        Instant debut = Instant.parse("2026-11-01T00:00:00Z");
        assertFalse(Relances.rappelDu(debut, null, 0, debut.plusSeconds(86_400), 2, 3));
        assertTrue(Relances.rappelDu(debut, null, 0, debut.plusSeconds(2 * 86_400), 2, 3));
        assertFalse(Relances.rappelDu(debut, debut.plusSeconds(2 * 86_400), 1, debut.plusSeconds(3 * 86_400), 2, 3));
        assertFalse(Relances.rappelDu(debut, null, 3, debut.plusSeconds(9 * 86_400), 2, 3));
        assertFalse(Relances.rappelDu(null, null, 0, debut, 2, 3));
        assertFalse(Relances.rappelDu(debut, null, 0, debut, 0, 3));
        assertEquals(debut.plusSeconds(5 * 86_400), Relances.echeance(debut, 5));
        assertNull(Relances.echeance(debut, null));
        assertNull(Relances.echeance(null, 5));
        assertTrue(Relances.enRetard(debut, debut.plusSeconds(1)));
        assertFalse(Relances.enRetard(null, debut));
        assertEquals(debut.plusSeconds(60), Relances.prochainEssai(1, debut));
        assertEquals(debut.plusSeconds(240), Relances.prochainEssai(3, debut));
        assertEquals(debut.plusSeconds(3600), Relances.prochainEssai(20, debut));
        assertEquals(debut.plusSeconds(60), Relances.prochainEssai(0, debut));
        assertTrue(Relances.abandonner(Relances.REPRISES_MAX));
        assertFalse(Relances.abandonner(1));
    }

    // --- Habilitations (ST-02) ---
    @Test
    void perimetre_et_separation_des_responsabilites() {
        Trousse t = new Trousse(1L, "TR-1", "T", "init@x.ca", Instant.now());
        t.setUnite("Programmes");
        Habilitations.Acteur init = new Habilitations.Acteur("init@x.ca", Set.of("REDACTEUR"), Set.of());
        Habilitations.Acteur collegue = new Habilitations.Acteur("c@x.ca", null, Set.of("Programmes"));
        Habilitations.Acteur externe = new Habilitations.Acteur("e@x.ca", Set.of("JURIDIQUE"), null);
        Habilitations.Acteur admin = new Habilitations.Acteur("a@x.ca", Set.of("ADMIN"), Set.of());
        Map<Long, String> courriels = Map.of(7L, "sig@x.ca");
        assertTrue(Habilitations.peutVoir(t, init, List.of(), courriels));
        assertTrue(Habilitations.peutVoir(t, collegue, List.of(), courriels));
        assertTrue(Habilitations.peutVoir(t, admin, List.of(), courriels));
        assertFalse(Habilitations.peutVoir(t, externe, List.of(), courriels));
        assertTrue(Habilitations.peutVoir(t, externe, List.of(new Affectation(TypeAffecte.ROLE, "JURIDIQUE")), courriels));
        assertTrue(Habilitations.peutVoir(t, new Habilitations.Acteur("sig@x.ca", null, null), List.of(), courriels));
        assertTrue(Habilitations.correspond(new Affectation(TypeAffecte.PARTICIPANT, "7"), new Habilitations.Acteur("SIG@x.ca", null, null), courriels));
        assertFalse(Habilitations.correspond(new Affectation(TypeAffecte.PARTICIPANT, "abc"), init, courriels));
        assertTrue(Habilitations.correspond(new Affectation(TypeAffecte.GROUPE, "Programmes"), collegue, courriels));
        assertTrue(Habilitations.correspond(new Affectation(TypeAffecte.UTILISATEUR, "INIT@x.ca"), init, courriels));
        assertFalse(Habilitations.peutModifier(t, collegue));
        assertThrows(IllegalStateException.class, () -> Habilitations.exigerSeparation(t, TypeEtape.APPROBATION, init));
        assertDoesNotThrow(() -> Habilitations.exigerSeparation(t, TypeEtape.SIGNATURE, init));
        assertDoesNotThrow(() -> Habilitations.exigerSeparation(t, TypeEtape.VALIDATION, externe));
    }

    // --- Option IA (ST-15) ---
    @Test
    void analyse_extrait_classe_et_signale_les_anomalies() {
        String texte = "Convention d'aide financière du programme Essor. Le bénéficiaire reçoit 250 000,00 $ à compter du 2026-12-01 "
                + "pour 36 mois. Clause de résiliation. Confidentialité des renseignements personnels. Lois du Québec.";
        AnalyseContrat.Resultat r = AnalyseContrat.analyser(texte, new BigDecimal("250000.00"));
        assertEquals("SUBVENTION", r.categorie());
        assertTrue(r.confianceCategorie() > 0);
        assertTrue(r.metadonnees().stream().anyMatch(m -> m.cle().equals("montant") && m.valeur().equals("250000.00")));
        assertTrue(r.metadonnees().stream().anyMatch(m -> m.cle().equals("dateDebut")));
        assertTrue(r.metadonnees().stream().anyMatch(m -> m.cle().equals("duree")));
        assertTrue(r.anomalies().isEmpty(), r.anomalies().toString());
        AnalyseContrat.Resultat vide = AnalyseContrat.analyser(null, null);
        assertEquals("INDETERMINEE", vide.categorie());
        assertEquals(4, vide.anomalies().size());
        assertTrue(AnalyseContrat.analyser("Prix 10 $", BigDecimal.ONE).anomalies().stream().anyMatch(a -> a.cle().equals("montantDivergent")));
        assertEquals(List.of(new BigDecimal("1500"), new BigDecimal("12.50")), AnalyseContrat.montants("1 500 $ et 12,50 $"));
    }
}
