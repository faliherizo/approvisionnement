package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** RST-55 (IA-05) : règles du domaine de la négociation assistée avec un tiers. */
class NegociationDomaineTest {

    private static final Instant T0 = Instant.parse("2026-11-26T10:00:00Z");
    private static final ClauseNegociee PAIEMENT = new ClauseNegociee("paiement", "Modalités de paiement",
            "Le paiement est fait dans les 30 jours suivant la réception de la facture.");

    private static Negociation negociation() {
        Negociation.Contrepartie c = new Negociation.Contrepartie("Agrinord inc.", "juriste@agrinord.example", "Agrinord", Langue.EN);
        return new Negociation(new Negociation.Ouverture(7L, 9L, c, "h".repeat(64), T0.plusSeconds(3600), "initiateur", T0));
    }

    private static PropositionNegociation proposition(Negociation n, String texte) {
        return new PropositionNegociation(n, PAIEMENT, new PropositionNegociation.Saisie(texte, "Délai trop court"), T0);
    }

    private static PropositionNegociation.Arbitrage arbitrage(PropositionNegociation.Decision d, String contre, String motif) {
        return new PropositionNegociation.Arbitrage(d, contre, motif, "juriste");
    }

    // --- Clause ---

    @Test
    void une_clause_exige_sa_cle_et_reprend_la_cle_comme_titre_a_defaut() {
        assertThrows(IllegalArgumentException.class, () -> new ClauseNegociee(null, "t", "x"));
        assertThrows(IllegalArgumentException.class, () -> new ClauseNegociee("  ", "t", "x"));
        ClauseNegociee c = new ClauseNegociee(" duree ", null, null);
        assertEquals("duree", c.cle());
        assertEquals("duree", c.titre());
        assertEquals("", c.texte());
        assertEquals("duree", new ClauseNegociee("duree", " ", "x").titre());
        assertEquals(new ClauseNegociee("duree", "Durée", "12 mois"), new ClauseNegociee("duree", " Durée ", " 12 mois ").avecTexte("12 mois"));
    }

    // --- États ---

    @Test
    void seules_les_transitions_declarees_sont_permises_et_les_etats_finaux_sont_fermes() {
        assertEquals(EtatNegociation.ATTENTE_INTERNE, EtatNegociation.ATTENTE_CONTREPARTIE.vers(EtatNegociation.ATTENTE_INTERNE));
        assertEquals(EtatNegociation.ENTENTE, EtatNegociation.ATTENTE_INTERNE.vers(EtatNegociation.ENTENTE));
        assertEquals(EtatNegociation.CLOSE, EtatNegociation.ENTENTE.vers(EtatNegociation.CLOSE));
        EtatNegociation.TransitionInterdite refus = assertThrows(EtatNegociation.TransitionInterdite.class,
                () -> EtatNegociation.ATTENTE_CONTREPARTIE.vers(EtatNegociation.CLOSE));
        assertEquals(EtatNegociation.ATTENTE_CONTREPARTIE, refus.depuis());
        assertEquals(EtatNegociation.CLOSE, refus.vers());
        assertTrue(refus.getMessage().contains("ATTENTE_CONTREPARTIE"));
        assertTrue(EtatNegociation.CLOSE.estFinal());
        assertTrue(EtatNegociation.ANNULEE.estFinal());
        assertFalse(EtatNegociation.ENTENTE.estFinal());
        assertThrows(EtatNegociation.TransitionInterdite.class, () -> EtatNegociation.CLOSE.vers(EtatNegociation.ANNULEE));
    }

    // --- Position ---

    @Test
    void la_position_se_lit_dans_une_reponse_libre_et_rien_n_est_invente() {
        assertEquals(Optional.empty(), PositionNegociation.lire(null));
        assertEquals(Optional.empty(), PositionNegociation.lire("Je ne sais pas."));
        assertEquals(Optional.of(PositionNegociation.ACCEPTER), PositionNegociation.lire("Écart mineur.\nPOSITION : accepter"));
        assertEquals(Optional.of(PositionNegociation.CONTRE_PROPOSER), PositionNegociation.lire("position : Contre-proposer"));
        assertEquals(Optional.of(PositionNegociation.CONTRE_PROPOSER), PositionNegociation.lire("CONTRE PROPOSER"));
        assertEquals(Optional.of(PositionNegociation.REFUSER), PositionNegociation.lire("Il faut REFUSER puis accepter plus tard"));
        assertEquals(Optional.empty(), PositionNegociation.lire("inacceptable"));
    }

    // --- Comparaison mot à mot ---

    @Test
    void la_comparaison_donne_les_mots_egaux_ajoutes_et_retires_dans_l_ordre_de_lecture() {
        List<DiffTexte.Segment> s = DiffTexte.comparer("paiement dans les 30 jours", "paiement dans les 45 jours ouvrables");
        assertEquals(List.of(new DiffTexte.Segment(DiffTexte.Nature.EGAL, "paiement dans les"),
                new DiffTexte.Segment(DiffTexte.Nature.AJOUT, "45"), new DiffTexte.Segment(DiffTexte.Nature.RETRAIT, "30"),
                new DiffTexte.Segment(DiffTexte.Nature.EGAL, "jours"), new DiffTexte.Segment(DiffTexte.Nature.AJOUT, "ouvrables")), s);
        assertArrayEquals(new int[]{1, 2}, DiffTexte.compte(s));
        assertEquals(List.of(new DiffTexte.Segment(DiffTexte.Nature.RETRAIT, "a b")), DiffTexte.comparer("a b", null));
        assertEquals(List.of(new DiffTexte.Segment(DiffTexte.Nature.AJOUT, "a b")), DiffTexte.comparer("  ", "a b"));
        assertEquals(List.of(), DiffTexte.comparer(null, ""));
        assertEquals(List.of(new DiffTexte.Segment(DiffTexte.Nature.EGAL, "x"), new DiffTexte.Segment(DiffTexte.Nature.RETRAIT, "y")),
                DiffTexte.comparer("x y", "x"));
        assertEquals(List.of(new DiffTexte.Segment(DiffTexte.Nature.RETRAIT, "a"), new DiffTexte.Segment(DiffTexte.Nature.EGAL, "b")),
                DiffTexte.comparer("a b", "b"));
    }

    @Test
    void la_similarite_mesure_la_part_de_mots_communs() {
        assertEquals(1.0, DiffTexte.similarite("", null));
        assertEquals(1.0, DiffTexte.similarite("a b c", "a  b c"));
        assertEquals(0.0, DiffTexte.similarite("a b", "c d"));
        assertEquals(0.8, DiffTexte.similarite("a b c d e", "a b c d f"), 1e-9);
    }

    @Test
    void au_dela_du_plafond_de_mots_les_textes_se_comparent_d_un_bloc() {
        String long1 = "mot ".repeat(DiffTexte.MAX_MOTS + 1);
        assertEquals(List.of(new DiffTexte.Segment(DiffTexte.Nature.EGAL, long1.strip())), DiffTexte.comparer(long1, long1));
        List<DiffTexte.Segment> bloc = DiffTexte.comparer("court", long1);
        assertEquals(2, bloc.size());
        assertEquals(DiffTexte.Nature.RETRAIT, bloc.get(0).nature());
        assertEquals(DiffTexte.Nature.AJOUT, bloc.get(1).nature());
        assertEquals(2, DiffTexte.comparer(long1, "court").size());
    }

    @Test
    void la_comparaison_de_versions_classe_chaque_clause() {
        ClauseNegociee duree = new ClauseNegociee("duree", "Durée", "12 mois");
        ClauseNegociee resiliation = new ClauseNegociee("resiliation", "Résiliation", "Préavis de 30 jours");
        ClauseNegociee confidentialite = new ClauseNegociee("conf", "Confidentialité", "Secret");
        List<ComparaisonClauses.Ecart> ecarts = ComparaisonClauses.comparer(List.of(duree, resiliation, PAIEMENT),
                List.of(duree, resiliation.avecTexte("Préavis de 60 jours"), confidentialite));
        assertEquals(List.of(ComparaisonClauses.Changement.INCHANGEE, ComparaisonClauses.Changement.MODIFIEE,
                ComparaisonClauses.Changement.RETIREE, ComparaisonClauses.Changement.AJOUTEE), ecarts.stream().map(ComparaisonClauses.Ecart::changement).toList());
        assertEquals("conf", ecarts.get(3).cle());
        assertEquals("Confidentialité", ecarts.get(3).titre());
        assertEquals(DiffTexte.Nature.AJOUT, ecarts.get(3).segments().get(0).nature());
        assertEquals(DiffTexte.Nature.RETRAIT, ecarts.get(2).segments().get(0).nature());
    }

    // --- Repli déterministe ---

    @Test
    void les_regles_recommandent_selon_l_ecart_au_texte_normatif_et_les_elements_attendus() {
        Map<String, List<String>> attendus = Map.of("DELAI_PAIEMENT", List.of("30 jours"), "PENALITE", List.of("penalite"));
        RecommandationNegociation.Recommandation mineure = RecommandationNegociation.recommander(PAIEMENT,
                "Le paiement est fait dans les 30 jours suivant la réception conforme de la facture.", attendus);
        assertEquals(PositionNegociation.ACCEPTER, mineure.position());
        assertTrue(mineure.similarite() >= RecommandationNegociation.SIMILARITE_MINEURE);
        assertEquals(List.of(), mineure.elementsRetires());
        assertTrue(mineure.resume().startsWith("Écart sur « Modalités de paiement » : 1 mot(s) ajouté(s), 0 retiré(s) ;"), mineure.resume());

        RecommandationNegociation.Recommandation retrait = RecommandationNegociation.recommander(PAIEMENT,
                "Le paiement est fait dans les 90 jours suivant la réception de la facture.", attendus);
        assertEquals(PositionNegociation.REFUSER, retrait.position());
        assertEquals(List.of("DELAI_PAIEMENT"), retrait.elementsRetires());
        assertTrue(retrait.resume().endsWith("Retire un élément attendu : DELAI_PAIEMENT."), retrait.resume());

        RecommandationNegociation.Recommandation majeure = RecommandationNegociation.recommander(PAIEMENT,
                "Le fournisseur facture à la livraison ; tout retard porte intérêt, 30 jours au plus.", attendus);
        assertEquals(PositionNegociation.CONTRE_PROPOSER, majeure.position());
        assertTrue(majeure.similarite() < RecommandationNegociation.SIMILARITE_MINEURE);
    }

    @Test
    void le_seuil_d_ecart_mineur_est_inclusif_et_le_resume_donne_le_pourcentage() {
        ClauseNegociee vingt = new ClauseNegociee("c", "Vingt mots", "a b c d e f g h i j k l m n o p q r s t");
        RecommandationNegociation.Recommandation seuil = RecommandationNegociation.recommander(vingt, "a b c d e f g h i j k l m n o p q x y z", Map.of());
        assertEquals(0.85, seuil.similarite());
        assertEquals(PositionNegociation.ACCEPTER, seuil.position(), "85 % de mots communs : écart mineur");
        assertTrue(seuil.resume().contains(" 85 % de similarité"), seuil.resume());
    }

    @Test
    void au_plafond_exact_de_mots_la_comparaison_reste_mot_a_mot() {
        String plafond = "mot ".repeat(DiffTexte.MAX_MOTS - 1) + "fin";
        List<DiffTexte.Segment> s = DiffTexte.comparer(plafond, plafond.replace("fin", "terme"));
        assertEquals(DiffTexte.Nature.EGAL, s.get(0).nature(), "mot à mot : le début commun est reconnu");
        assertEquals(3, s.size());
    }

    @Test
    void la_normalisation_ignore_casse_et_accents() {
        assertEquals("penalite", RecommandationNegociation.normaliser("Pénalité"));
        assertEquals("", RecommandationNegociation.normaliser(null));
    }

    // --- Cycle ---

    @Test
    void la_contrepartie_exige_un_nom_et_un_courriel_valides() {
        assertThrows(IllegalArgumentException.class, () -> new Negociation.Contrepartie(null, "a@b.ca", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Negociation.Contrepartie("A", "a@b.ca", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Negociation.Contrepartie("Agrinord", null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new Negociation.Contrepartie("Agrinord", "pas-un-courriel", null, null));
        Negociation.Contrepartie c = new Negociation.Contrepartie(" Agrinord ", " a@b.ca ", "  ", null);
        assertEquals("Agrinord", c.nom());
        assertEquals("a@b.ca", c.courriel());
        assertNull(c.entreprise());
        assertEquals(Langue.FR, c.langue());
    }

    @Test
    void une_negociation_ouverte_attend_la_contrepartie_sur_la_version_1() {
        Negociation n = negociation();
        assertNull(n.getId());
        assertEquals(7L, n.getTenantId());
        assertEquals(9L, n.getTrousseId());
        assertEquals("Agrinord inc.", n.getContrepartieNom());
        assertEquals("juriste@agrinord.example", n.getContrepartieCourriel());
        assertEquals("Agrinord", n.getContrepartieEntreprise());
        assertEquals(Langue.EN, n.getLangue());
        assertEquals("h".repeat(64), n.getJetonHash());
        assertEquals(T0.plusSeconds(3600), n.getExpireLe());
        assertEquals(EtatNegociation.ATTENTE_CONTREPARTIE, n.getEtat());
        assertEquals(1, n.getVersionCourante());
        assertEquals(1, n.getTour());
        assertEquals("initiateur", n.getOuvertePar());
        assertEquals(T0, n.getOuverteLe());
        assertNull(n.getTermineePar());
        assertNull(n.getTermineeLe());
        assertNull(n.getMotifFin());
        assertTrue(n.estOuverte());
        assertDoesNotThrow(() -> n.exigerAccessible(T0));
        assertThrows(Negociation.NegociationFermee.class, () -> n.exigerAccessible(T0.plusSeconds(3601)));
    }

    @Test
    void un_tour_passe_la_main_a_l_organisation_puis_produit_la_version_suivante() {
        Negociation n = negociation();
        assertThrows(IllegalArgumentException.class, () -> n.recevoirPropositions(0));
        assertThrows(EtatNegociation.TransitionInterdite.class, () -> n.conclureTour(true));
        n.recevoirPropositions(2);
        assertEquals(EtatNegociation.ATTENTE_INTERNE, n.getEtat());
        assertThrows(EtatNegociation.TransitionInterdite.class, () -> n.recevoirPropositions(1));
        assertThrows(EtatNegociation.TransitionInterdite.class, n::accepterVersion);
        assertEquals(2, n.conclureTour(false));
        assertEquals(EtatNegociation.ATTENTE_CONTREPARTIE, n.getEtat());
        assertEquals(2, n.getTour());
        n.recevoirPropositions(1);
        assertEquals(3, n.conclureTour(true));
        assertEquals(EtatNegociation.ENTENTE, n.getEtat());
        n.clore("juriste", T0);
        assertEquals(EtatNegociation.CLOSE, n.getEtat());
        assertEquals("juriste", n.getTermineePar());
        assertEquals(T0, n.getTermineeLe());
        assertNull(n.getMotifFin());
        assertFalse(n.estOuverte());
        assertThrows(Negociation.NegociationFermee.class, () -> n.exigerAccessible(T0));
    }

    @Test
    void la_contrepartie_peut_accepter_la_version_telle_quelle_et_l_annulation_se_motive() {
        Negociation n = negociation();
        n.accepterVersion();
        assertEquals(EtatNegociation.ENTENTE, n.getEtat());
        Negociation m = negociation();
        assertThrows(EtatNegociation.TransitionInterdite.class, () -> m.clore("x", T0));
        assertThrows(IllegalArgumentException.class, () -> m.annuler("x", null, T0));
        assertThrows(IllegalArgumentException.class, () -> m.annuler("x", " ", T0));
        m.annuler("juriste", " Contrepartie retirée ", T0);
        assertEquals(EtatNegociation.ANNULEE, m.getEtat());
        assertEquals("Contrepartie retirée", m.getMotifFin());
        assertThrows(EtatNegociation.TransitionInterdite.class, () -> m.annuler("x", "encore", T0));
    }

    @Test
    void une_seule_negociation_ouverte_par_trousse() {
        Negociation close = negociation();
        close.annuler("x", "fin", T0);
        assertDoesNotThrow(() -> Negociation.exigerAucuneOuverte(List.of(close)));
        assertDoesNotThrow(() -> Negociation.exigerAucuneOuverte(List.of()));
        Negociation.DejaEnCours e = assertThrows(Negociation.DejaEnCours.class,
                () -> Negociation.exigerAucuneOuverte(List.of(close, negociation())));
        assertTrue(e.getMessage().contains("déjà en cours"));
    }

    // --- Versions ---

    @Test
    void une_version_figee_porte_l_empreinte_de_ses_clauses_et_decele_toute_alteration() throws Exception {
        VersionNegociation v = new VersionNegociation(negociation(), new VersionNegociation.Contenu(2, "[{\"cle\":\"a\"}]", "Tour 1"), "juriste", T0);
        assertNull(v.getId());
        assertNull(v.getNegociationId());
        assertEquals(2, v.getNumero());
        assertEquals("[{\"cle\":\"a\"}]", v.getClausesJson());
        assertEquals(ChaineScellee.sha256("[{\"cle\":\"a\"}]".getBytes(java.nio.charset.StandardCharsets.UTF_8)), v.getEmpreinte());
        assertEquals("juriste", v.getAuteur());
        assertEquals("Tour 1", v.getMotif());
        assertEquals(T0, v.getCreeLe());
        assertTrue(v.estIntegre());
        Field champ = VersionNegociation.class.getDeclaredField("clausesJson");
        champ.setAccessible(true);
        champ.set(v, "[{\"cle\":\"b\"}]");
        assertFalse(v.estIntegre());
    }

    // --- Propositions ---

    @Test
    void une_decision_saisie_est_lue_sans_casse_et_une_valeur_inconnue_est_refusee() {
        assertEquals(PropositionNegociation.Decision.ACCEPTEE, PropositionNegociation.Decision.de(" acceptee "));
        assertEquals(PropositionNegociation.Decision.EN_ATTENTE, PropositionNegociation.Decision.de("EN_ATTENTE"));
        assertThrows(IllegalArgumentException.class, () -> PropositionNegociation.Decision.de(null));
        assertThrows(IllegalArgumentException.class, () -> PropositionNegociation.Decision.de("PEUT-ETRE"));
    }

    @Test
    void une_proposition_doit_modifier_la_clause_et_porter_un_texte() {
        Negociation n = negociation();
        assertThrows(IllegalArgumentException.class, () -> new PropositionNegociation.Saisie(null, null));
        assertThrows(IllegalArgumentException.class, () -> new PropositionNegociation.Saisie("  ", "x"));
        assertNull(new PropositionNegociation.Saisie("x", " ").commentaire());
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> proposition(n, PAIEMENT.texte()));
        assertTrue(e.getMessage().contains("Modalités de paiement"));
        PropositionNegociation p = proposition(n, "Le paiement est fait dans les 45 jours.");
        assertNull(p.getId());
        assertNull(p.getNegociationId());
        assertEquals(1, p.getTour());
        assertEquals("paiement", p.getCleClause());
        assertEquals("Modalités de paiement", p.getTitreClause());
        assertEquals(PAIEMENT.texte(), p.getTexteBase());
        assertEquals("Le paiement est fait dans les 45 jours.", p.getTextePropose());
        assertEquals("Délai trop court", p.getCommentaire());
        assertEquals(T0, p.getProposeeLe());
        assertEquals(PropositionNegociation.Decision.EN_ATTENTE, p.getDecision());
        assertFalse(p.estDecidee());
        assertEquals(PAIEMENT.texte(), p.texteRetenu());
    }

    @Test
    void un_arbitrage_exige_une_decision_un_motif_de_refus_ou_un_texte_de_contre_proposition() {
        assertThrows(IllegalArgumentException.class, () -> arbitrage(null, null, null));
        assertThrows(IllegalArgumentException.class, () -> arbitrage(PropositionNegociation.Decision.EN_ATTENTE, null, null));
        assertThrows(IllegalArgumentException.class, () -> arbitrage(PropositionNegociation.Decision.CONTRE_PROPOSEE, " ", "ok"));
        assertThrows(IllegalArgumentException.class, () -> arbitrage(PropositionNegociation.Decision.REFUSEE, null, " "));
        PropositionNegociation.Arbitrage a = arbitrage(PropositionNegociation.Decision.ACCEPTEE, null, null);
        assertEquals("", a.contreTexte());
        assertNull(a.justification());
    }

    @Test
    void chaque_decision_donne_son_texte_retenu_et_ne_se_reprend_pas() {
        Negociation n = negociation();
        PropositionNegociation acceptee = proposition(n, "45 jours");
        acceptee.decider(arbitrage(PropositionNegociation.Decision.ACCEPTEE, "ignoré", " Raisonnable "), T0);
        assertEquals("45 jours", acceptee.texteRetenu());
        assertNull(acceptee.getTexteContre());
        assertEquals("Raisonnable", acceptee.getJustification());
        assertEquals("juriste", acceptee.getDecidePar());
        assertEquals(T0, acceptee.getDecideLe());
        assertTrue(acceptee.estDecidee());
        PropositionNegociation.PropositionDejaDecidee deja = assertThrows(PropositionNegociation.PropositionDejaDecidee.class,
                () -> acceptee.decider(arbitrage(PropositionNegociation.Decision.ACCEPTEE, null, null), T0));
        assertTrue(deja.getMessage().contains("Modalités de paiement"));
        assertThrows(PropositionNegociation.PropositionDejaDecidee.class,
                () -> acceptee.suggerer(new PropositionNegociation.Suggestion(PositionNegociation.ACCEPTER, "r", "s"), T0));

        PropositionNegociation contre = proposition(n, "60 jours");
        contre.decider(arbitrage(PropositionNegociation.Decision.CONTRE_PROPOSEE, " 40 jours ", null), T0);
        assertEquals("40 jours", contre.texteRetenu());
        assertEquals("40 jours", contre.getTexteContre());

        PropositionNegociation refusee = proposition(n, "90 jours");
        refusee.decider(arbitrage(PropositionNegociation.Decision.REFUSEE, null, "Politique de paiement"), T0);
        assertEquals(PAIEMENT.texte(), refusee.texteRetenu());
    }

    @Test
    void la_suggestion_s_attache_sans_decider() {
        PropositionNegociation p = proposition(negociation(), "45 jours");
        assertNull(p.getSuggestionPosition());
        p.suggerer(new PropositionNegociation.Suggestion(PositionNegociation.CONTRE_PROPOSER, "Écart majeur", "IA locale (llama3.1)"), T0);
        assertEquals(PositionNegociation.CONTRE_PROPOSER, p.getSuggestionPosition());
        assertEquals("Écart majeur", p.getSuggestionResume());
        assertEquals("IA locale (llama3.1)", p.getSuggestionSource());
        assertEquals(T0, p.getSuggereeLe());
        assertEquals(PropositionNegociation.Decision.EN_ATTENTE, p.getDecision());
    }

    // --- Fin de tour ---

    @Test
    void un_tour_ne_se_conclut_qu_une_fois_chaque_proposition_decidee() {
        Negociation n = negociation();
        ClauseNegociee duree = new ClauseNegociee("duree", "Durée", "12 mois");
        PropositionNegociation p = proposition(n, "45 jours");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> TourNegociation.appliquer(List.of(PAIEMENT, duree), List.of(p)));
        assertTrue(e.getMessage().contains("1 restante"));
        p.decider(arbitrage(PropositionNegociation.Decision.ACCEPTEE, null, null), T0);
        assertEquals(List.of(PAIEMENT.avecTexte("45 jours"), duree), TourNegociation.appliquer(List.of(PAIEMENT, duree), List.of(p)));
        assertTrue(TourNegociation.toutAccepte(List.of(p)));
        PropositionNegociation q = proposition(n, "60 jours");
        q.decider(arbitrage(PropositionNegociation.Decision.REFUSEE, null, "non"), T0);
        assertFalse(TourNegociation.toutAccepte(List.of(p, q)));
        assertEquals(List.of(PAIEMENT), TourNegociation.appliquer(List.of(PAIEMENT), List.of(p, q)), "la dernière décision sur une clause prévaut");
        assertEquals(List.of(PAIEMENT.avecTexte("45 jours")), TourNegociation.appliquer(List.of(PAIEMENT), List.of(q, p)));
    }
}
