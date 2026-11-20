package qc.levis.contrat.clm.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Départ d'un employé (P3) : transfert de propriété d'une trousse ouverte, remplacement nominatif dans un parcours. */
class DepartEmployeDomaineTest {

    private static final Instant T0 = Instant.parse("2026-11-20T12:00:00Z");

    @Test
    void une_trousse_ouverte_change_de_proprietaire_une_trousse_close_garde_le_sien() {
        Trousse t = new Trousse(1L, "IQ-1", "Convention", "parti@iq.example", T0);
        assertEquals("parti@iq.example", t.transfererA(" Reprise@IQ.example ", T0.plusSeconds(60)));
        assertEquals("reprise@iq.example", t.getProprietaire());
        assertEquals(T0.plusSeconds(60), t.getMajLe());
        assertThrows(IllegalArgumentException.class, () -> t.transfererA("REPRISE@iq.example", T0), "identique");
        assertThrows(IllegalArgumentException.class, () -> t.transfererA(" ", T0));
        assertThrows(IllegalArgumentException.class, () -> t.transfererA(null, T0));
        t.changerStatut(StatutTrousse.ANNULEE, T0);
        assertTrue(assertThrows(IllegalStateException.class, () -> t.transfererA("autre@iq.example", T0)).getMessage().contains("IQ-1"));
    }

    @Test
    void la_personne_designee_nommement_est_remplacee_sans_doublon() {
        Parcours.Affectation parti = new Parcours.Affectation(Parcours.TypeAffecte.UTILISATEUR, "Parti@iq.example");
        Parcours.Affectation reprise = new Parcours.Affectation(Parcours.TypeAffecte.UTILISATEUR, "reprise@iq.example");
        Parcours.Affectation groupe = new Parcours.Affectation(Parcours.TypeAffecte.GROUPE, "parti@iq.example");
        assertTrue(Parcours.designe(List.of(groupe, parti), "parti@iq.example"));
        assertFalse(Parcours.designe(List.of(groupe, reprise), "parti@iq.example"), "un groupe ne désigne personne nommément");
        assertEquals(List.of(reprise, groupe), Parcours.remplacer(List.of(parti, groupe), "parti@iq.example", "reprise@iq.example"));
        assertEquals(List.of(reprise), Parcours.remplacer(List.of(parti, reprise), "parti@iq.example", "reprise@iq.example"), "sans doublon");
    }
}
