package qc.levis.contrat.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.PolitiqueSessionService;
import qc.levis.contrat.auth.application.dto.PolitiqueSessionVue;
import qc.levis.contrat.auth.application.repository.PolitiqueSessionRepository;
import qc.levis.contrat.auth.domain.LimitesSession;
import qc.levis.contrat.auth.domain.PolitiqueSession;

class PolitiqueSessionServiceTest {

    private static final Instant T0 = Instant.parse("2026-11-22T08:00:00Z");

    private final PolitiqueSessionRepository depot = mock(PolitiqueSessionRepository.class);
    private final AuditService audit = mock(AuditService.class);
    private final AtomicReference<Instant> maintenant = new AtomicReference<>(T0);
    private final Clock horloge = new Clock() {
        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId z) { return this; }
        @Override public Instant instant() { return maintenant.get(); }
    };
    private final PolitiqueSessionService service = new PolitiqueSessionService(depot, audit, horloge, 60, 480);

    @Test
    void la_plateforme_et_une_organisation_sans_politique_heritent_des_limites_par_defaut() {
        when(depot.findById(3L)).thenReturn(Optional.empty());
        LimitesSession defaut = new LimitesSession(Duration.ofMinutes(60), Duration.ofMinutes(480));
        assertEquals(defaut, service.limites(null));
        assertEquals(defaut, service.limites(3L));
        PolitiqueSessionVue vue = service.etat(3L);
        assertFalse(vue.propre());
        assertEquals(60, vue.inactiviteMinutes());
        assertNull(vue.modifiePar());
    }

    @Test
    void les_limites_sont_gardees_une_minute_puis_relues() {
        when(depot.findById(3L)).thenReturn(Optional.empty());
        service.limites(3L);
        maintenant.set(T0.plusSeconds(59));
        service.limites(3L);
        verify(depot, times(1)).findById(3L);
        maintenant.set(T0.plusSeconds(61));
        service.limites(3L);
        verify(depot, times(2)).findById(3L);
    }

    @Test
    void definir_enregistre_journalise_et_prend_effet_aussitot() {
        when(depot.findById(3L)).thenReturn(Optional.empty());
        service.limites(3L);
        PolitiqueSession enregistree = new PolitiqueSession(3L);
        enregistree.definir(new LimitesSession(Duration.ofMinutes(15), Duration.ofHours(8)), "tech", T0);
        when(depot.findById(3L)).thenReturn(Optional.empty(), Optional.of(enregistree));
        PolitiqueSessionVue vue = service.definir(3L, 15, 480, "tech");
        verify(depot).save(any(PolitiqueSession.class));
        verify(audit).log(eq(3L), eq("tech"), eq("POLITIQUE_SESSION"), eq("Tenant"), eq(3L), contains("15 min"));
        assertTrue(vue.propre());
        assertEquals(Duration.ofMinutes(15), service.limites(3L).inactivite(), "le cache est vidé au changement");
    }

    @Test
    void un_reglage_hors_bornes_n_est_ni_enregistre_ni_journalise() {
        assertThrows(IllegalArgumentException.class, () -> service.definir(3L, 1, 480, "tech"));
        verify(depot, never()).save(any());
        verifyNoInteractions(audit);
    }
}
