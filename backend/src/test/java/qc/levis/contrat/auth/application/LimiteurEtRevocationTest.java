package qc.levis.contrat.auth.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.repository.JetonRevoqueRepository;
import qc.levis.contrat.auth.domain.JetonRevoque;

/** OWASP A07 : frein à la force brute et révocation des jetons à la déconnexion. */
class LimiteurEtRevocationTest {

    static final Instant T = Instant.parse("2026-11-20T12:00:00Z");
    final Clock horloge = Clock.fixed(T, ZoneOffset.UTC);

    @Test
    void au_seuil_d_echecs_dans_la_fenetre_la_connexion_est_suspendue() {
        AuditService audit = mock(AuditService.class);
        LimiteurConnexions l = new LimiteurConnexions(audit, horloge, 10, 15);
        when(audit.compter(eq("marie@x.ca"), eq("CONNEXION_ECHEC"), eq(T.minusSeconds(900)))).thenReturn(9L, 10L);
        assertFalse(l.estSuspendu(" marie@x.ca "));
        assertTrue(l.estSuspendu("marie@x.ca"));
        assertFalse(l.estSuspendu(null));
        assertFalse(l.estSuspendu(" "));
        assertEquals(15, l.fenetreMinutes());
    }

    @Test
    void un_jeton_revoque_est_reconnu_par_son_empreinte_et_les_expires_sont_purges() {
        JetonRevoqueRepository depot = mock(JetonRevoqueRepository.class);
        JwtService jwt = new JwtService("x".repeat(64), 3_600_000);
        RevocationJetons r = new RevocationJetons(depot, jwt, horloge);
        String jeton = jwt.genererToken("marie@x.ca", List.of("LECTEUR"), 3L);
        r.revoquer(jeton);
        verify(depot).purger(T);
        verify(depot).save(argThat((JetonRevoque j) -> j.getEmpreinte().equals(RevocationJetons.empreinte(jeton)) && j.getExpireLe().isAfter(T)));
        when(depot.existsById(RevocationJetons.empreinte(jeton))).thenReturn(true);
        assertTrue(r.estRevoque(jeton));
        assertFalse(r.estRevoque("autre"));
        assertEquals(64, RevocationJetons.empreinte(jeton).length());
        assertNotEquals(jeton, jwt.genererToken("marie@x.ca", List.of("LECTEUR"), 3L), "deux sessions simultanées ont des jetons distincts");
    }

    @Test
    void un_jeton_absent_ou_illisible_n_est_pas_revoque() {
        JetonRevoqueRepository depot = mock(JetonRevoqueRepository.class);
        RevocationJetons r = new RevocationJetons(depot, new JwtService("x".repeat(64), 3_600_000), horloge);
        r.revoquer(null);
        r.revoquer(" ");
        r.revoquer("pas.un.jeton");
        verifyNoInteractions(depot);
    }
}
