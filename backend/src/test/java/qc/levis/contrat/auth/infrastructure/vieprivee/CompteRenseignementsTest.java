package qc.levis.contrat.auth.infrastructure.vieprivee;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.vieprivee.domain.EffacementRefuse;
import qc.levis.contrat.vieprivee.domain.Pseudonyme;

/** Défense en profondeur : même appelé hors du parcours habituel, l'effacement ne laisse pas l'organisation sans administrateur. */
class CompteRenseignementsTest {

    UserRepository comptes = mock(UserRepository.class);
    CompteRenseignements source = new CompteRenseignements(comptes);
    Pseudonyme p = Pseudonyme.nouveau(UUID.randomUUID());

    private User compte(long id, Long tenant, Enums.Role role) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        u.setEmail("u" + id + "@levis.qc.ca");
        u.setTenantId(tenant);
        u.setRoles(new java.util.HashSet<>(Set.of(role)));
        return u;
    }

    @Test
    void le_dernier_administrateur_actif_ne_s_efface_pas() {
        User seul = compte(1, 3L, Enums.Role.ADMIN);
        when(comptes.findByEmail("u1@levis.qc.ca")).thenReturn(Optional.of(seul));
        when(comptes.findByTenantIdAndActifTrueOrderByNomAsc(3L)).thenReturn(List.of(seul));
        assertThrows(EffacementRefuse.class, () -> source.effacer(3L, "u1@levis.qc.ca", p));
        verify(comptes, never()).save(any());
    }

    @Test
    void un_administrateur_s_efface_s_il_en_reste_un_autre() {
        User a = compte(1, 3L, Enums.Role.ADMIN);
        when(comptes.findByEmail("u1@levis.qc.ca")).thenReturn(Optional.of(a));
        when(comptes.findByTenantIdAndActifTrueOrderByNomAsc(3L)).thenReturn(List.of(a, compte(2, 3L, Enums.Role.REDACTEUR), compte(4, 3L, Enums.Role.ADMIN)));
        assertEquals(1, source.effacer(3L, "u1@levis.qc.ca", p).effaces());
        assertEquals(p.courriel(), a.getEmail());
        assertEquals(CompteRenseignements.EMPREINTE_INUTILISABLE, ReflectionTestUtils.getField(a, "motDePasseHash"));
    }

    @Test
    void un_compte_d_une_autre_organisation_est_ignore() {
        when(comptes.findByEmail("u1@levis.qc.ca")).thenReturn(Optional.of(compte(1, 9L, Enums.Role.REDACTEUR)));
        assertEquals(0, source.effacer(3L, "u1@levis.qc.ca", p).effaces());
        assertTrue(source.extraire(3L, "u1@levis.qc.ca").elements().isEmpty());
        assertEquals(100, source.ordre());
        verify(comptes, never()).save(any());
    }
}
