package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.JournalSecurite;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.clm.domain.EvenementSiem;

/** Collecte du journal par le SIEM (Annexe 10, TI-04) : client SIEM seulement, pages bornées, curseur qui avance. */
class JournalSiemServiceTest {

    private final JournalSecurite journal = mock(JournalSecurite.class);
    private final JournalSiemService service = new JournalSiemService(journal);

    private static ClientIntegration client(ClientIntegration.Systeme systeme) {
        return ClientIntegration.creer(5L, "Client", systeme, null, "clm-x", "e", "k", "ti", Instant.EPOCH);
    }

    @Test
    void un_systeme_d_affaires_ne_lit_pas_le_journal() {
        assertThrows(AccessDeniedException.class, () -> service.lire(client(ClientIntegration.Systeme.WORKDAY), 0, 10));
        verifyNoInteractions(journal);
        assertFalse(client(ClientIntegration.Systeme.AUTRE).lecteurJournal());
    }

    @Test
    void la_page_est_bornee_et_le_curseur_suit_le_dernier_evenement_lu() {
        EvenementSiem a = new EvenementSiem(11, Instant.EPOCH, "x", "CLM_TROUSSE_CREEE", "TROUSSE", 1L, null);
        EvenementSiem b = new EvenementSiem(12, Instant.EPOCH, "x", "CLM_ACCES_REFUSE", "API", null, "GET /x");
        when(journal.depuis(5L, 10, 2)).thenReturn(List.of(a, b));
        ClmDto.FluxSiem f = service.lire(client(ClientIntegration.Systeme.SIEM), 10, 2);
        assertEquals(12, f.curseurSuivant());
        assertTrue(f.suite(), "page pleine : la suite peut exister");
        assertEquals(2, f.evenements().size());
        assertTrue(f.cef().get(1).contains("outcome=failure"));

        when(journal.depuis(5L, 0, JournalSiemService.LIMITE_MAX)).thenReturn(List.of());
        ClmDto.FluxSiem vide = service.lire(client(ClientIntegration.Systeme.SIEM), -4, 10_000);
        assertEquals(0, vide.curseurSuivant(), "curseur négatif ramené à zéro ; rien lu, il ne bouge pas");
        assertFalse(vide.suite());
        service.lire(client(ClientIntegration.Systeme.SIEM), 3, 0);
        verify(journal).depuis(5L, 3, 1);
    }
}
