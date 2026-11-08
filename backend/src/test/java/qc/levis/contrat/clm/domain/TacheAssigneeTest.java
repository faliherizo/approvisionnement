package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Tâche confiée à une personne : saisie validée, intervenants, cycle de vie et retard. */
class TacheAssigneeTest {

    private static final Instant T0 = Instant.parse("2026-11-02T14:00:00Z");
    private static final Instant T1 = T0.plusSeconds(3_600);
    private static final LocalDate LUNDI = LocalDate.of(2026, 11, 2);

    private static TacheAssignee tache(String assigne) {
        return TacheAssignee.creer(7L, new TacheAssignee.Saisie(3L, "  Relire la clause 7  ", "  Vérifier la garantie  ",
                assigne, "HAUTE", LUNDI), "createur@iq.ca", T0);
    }

    @Test
    void la_creation_normalise_la_saisie_et_ouvre_la_tache() {
        TacheAssignee t = tache("  Julie@IQ.ca ");
        assertEquals(7L, t.getTenantId());
        assertEquals(3L, t.getTrousseId());
        assertEquals("Relire la clause 7", t.getTitre());
        assertEquals("Vérifier la garantie", t.getDescription());
        assertEquals("julie@iq.ca", t.getAssigneA());
        assertEquals("HAUTE", t.getPriorite());
        assertEquals(LUNDI, t.getEcheance());
        assertEquals(TacheAssignee.Statut.A_FAIRE, t.getStatut());
        assertEquals("createur@iq.ca", t.getCreePar());
        assertEquals(T0, t.getCreeLe());
        assertEquals(T0, t.getModifieLe());
        assertNull(t.getTermineeLe());
        assertNull(t.getId());
        assertTrue(t.estOuverte());
    }

    @Test
    void une_description_blanche_est_absente() {
        TacheAssignee t = TacheAssignee.creer(1L, new TacheAssignee.Saisie(null, "Titre", "   ", "a@b.ca", null, null), "c@b.ca", T0);
        assertNull(t.getDescription());
        assertNull(TacheAssignee.creer(1L, new TacheAssignee.Saisie(null, "Titre", null, "a@b.ca", null, null), "c@b.ca", T0)
                .getDescription());
    }

    @Test
    void la_saisie_invalide_est_refusee_avec_un_motif() {
        assertMotif("Titre de la tâche requis.", new TacheAssignee.Saisie(null, "  ", null, "a@b.ca", null, null));
        assertMotif("Titre de la tâche requis.", new TacheAssignee.Saisie(null, null, null, "a@b.ca", null, null));
        assertMotif("Titre trop long (200 caractères au plus).",
                new TacheAssignee.Saisie(null, "x".repeat(201), null, "a@b.ca", null, null));
        assertMotif("Description trop longue (2000 caractères au plus).",
                new TacheAssignee.Saisie(null, "Titre", "x".repeat(2001), "a@b.ca", null, null));
        assertMotif("Personne assignée invalide : courriel attendu.", new TacheAssignee.Saisie(null, "Titre", null, "julie", null, null));
        assertMotif("Personne assignée invalide : courriel attendu.", new TacheAssignee.Saisie(null, "Titre", null, null, null, null));
        assertEquals(200, TacheAssignee.creer(1L, new TacheAssignee.Saisie(null, "x".repeat(200), "y".repeat(2000), "a@b.ca", null, null),
                "c@b.ca", T0).getTitre().length(), "les bornes elles-mêmes sont acceptées");
    }

    private static void assertMotif(String motif, TacheAssignee.Saisie s) {
        assertEquals(motif, assertThrows(IllegalArgumentException.class, () -> TacheAssignee.creer(1L, s, "c@b.ca", T0)).getMessage());
    }

    @Test
    void seuls_le_createur_et_l_assigne_sont_intervenants() {
        TacheAssignee t = tache("julie@iq.ca");
        assertTrue(t.concerne("JULIE@iq.ca"));
        assertTrue(t.concerne("createur@IQ.ca"));
        assertFalse(t.concerne("autre@iq.ca"));
        assertTrue(t.peutDemarrer("julie@iq.ca"));
        assertFalse(t.peutDemarrer("createur@iq.ca"));
        assertTrue(t.peutAnnuler("createur@iq.ca"));
        assertFalse(t.peutAnnuler("julie@iq.ca"));
    }

    @Test
    void demarrer_puis_terminer_puis_rouvrir() {
        TacheAssignee t = tache("julie@iq.ca");
        t.demarrer(T1);
        assertEquals(TacheAssignee.Statut.EN_COURS, t.getStatut());
        assertEquals(T1, t.getModifieLe());
        assertEquals("Tâche déjà démarrée ou close.", assertThrows(IllegalStateException.class, () -> t.demarrer(T1)).getMessage());

        t.terminer(T1);
        assertEquals(TacheAssignee.Statut.TERMINEE, t.getStatut());
        assertEquals(T1, t.getTermineeLe());
        assertFalse(t.estOuverte());
        assertThrows(IllegalStateException.class, () -> t.terminer(T1), "une tâche close ne se termine pas deux fois");

        t.rouvrir(T1);
        assertEquals(TacheAssignee.Statut.A_FAIRE, t.getStatut());
        assertNull(t.getTermineeLe());
        assertEquals("Seule une tâche terminée peut être rouverte.", assertThrows(IllegalStateException.class, () -> t.rouvrir(T1)).getMessage());
    }

    @Test
    void une_tache_annulee_reste_close() {
        TacheAssignee t = tache("julie@iq.ca");
        t.annuler(T1);
        assertEquals(TacheAssignee.Statut.ANNULEE, t.getStatut());
        assertEquals("Tâche close : Relire la clause 7", assertThrows(IllegalStateException.class, () -> t.annuler(T1)).getMessage());
        assertThrows(IllegalStateException.class, () -> t.rouvrir(T1));
        assertThrows(IllegalStateException.class, () -> t.reassigner("marc@iq.ca", T1));
        assertThrows(IllegalStateException.class, () -> t.demarrer(T1));
    }

    @Test
    void reassigner_change_l_assigne_et_remet_la_tache_a_faire() {
        TacheAssignee t = tache("julie@iq.ca");
        t.demarrer(T0);
        t.reassigner(" Marc@IQ.ca ", T1);
        assertEquals("marc@iq.ca", t.getAssigneA());
        assertEquals(TacheAssignee.Statut.A_FAIRE, t.getStatut(), "le nouvel assigné n'a pas encore commencé");
        assertEquals(T1, t.getModifieLe());
        assertEquals("La tâche est déjà assignée à marc@iq.ca.",
                assertThrows(IllegalArgumentException.class, () -> t.reassigner("MARC@iq.ca", T1)).getMessage());
        assertThrows(IllegalArgumentException.class, () -> t.reassigner("pas-un-courriel", T1));
    }

    @Test
    void le_retard_ne_vaut_que_pour_une_tache_ouverte_echue_hier_ou_avant() {
        TacheAssignee t = tache("julie@iq.ca");
        assertFalse(t.enRetard(LUNDI), "due aujourd'hui : pas encore en retard");
        assertTrue(t.enRetard(LUNDI.plusDays(1)));
        t.terminer(T1);
        assertFalse(t.enRetard(LUNDI.plusDays(1)), "une tâche close n'est jamais en retard");
        TacheAssignee sansEcheance = TacheAssignee.creer(1L, new TacheAssignee.Saisie(null, "Titre", null, "a@b.ca", null, null), "c@b.ca", T0);
        assertFalse(sansEcheance.enRetard(LUNDI.plusYears(1)));
    }
}
