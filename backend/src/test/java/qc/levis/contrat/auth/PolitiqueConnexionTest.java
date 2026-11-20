package qc.levis.contrat.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.auth.domain.PolitiqueConnexion;

/** Connexion locale réservée aux comptes de secours (addenda 4, Q12) : aucun contournement du second facteur. */
class PolitiqueConnexionTest {

    @Test
    void ouverte_elle_admet_tout_compte_reservee_elle_n_admet_que_le_secours_muni_d_un_second_facteur() {
        PolitiqueConnexion p = new PolitiqueConnexion(3L);
        assertNull(p.refus(false, false), "politique par défaut : ouverte");
        p.reserver(true, "admin.technique@iq.example", Instant.EPOCH);
        assertEquals(PolitiqueConnexion.Refus.RESERVEE_AUX_SECOURS, p.refus(false, true));
        assertEquals(PolitiqueConnexion.Refus.SECOURS_SANS_SECOND_FACTEUR, p.refus(true, false));
        assertNull(p.refus(true, true));
        assertTrue(p.isLocaleReservee());
        assertEquals(3L, p.getTenantId());
        assertEquals("admin.technique@iq.example", p.getModifiePar());
        assertEquals(Instant.EPOCH, p.getModifieLe());
    }
}
