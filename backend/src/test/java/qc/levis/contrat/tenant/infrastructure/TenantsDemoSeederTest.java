package qc.levis.contrat.tenant.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums.Role;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.reference.application.ReferenceService;
import qc.levis.contrat.reference.application.dto.ReferenceRequest;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;

/** Amorçage de l'organisation IQ : modules, listes propres et un compte par profil du client, sans jamais écraser l'existant. */
class TenantsDemoSeederTest {

    private final TenantRepository tenants = mock(TenantRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final ModuleService modules = mock(ModuleService.class);
    private final ReferenceService references = mock(ReferenceService.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final TenantsDemoSeeder seeder = new TenantsDemoSeeder(tenants, users, modules, references, encoder);

    @Test
    void cree_l_organisation_ses_modules_et_un_compte_par_profil() throws Exception {
        when(tenants.findBySlug("iq")).thenReturn(Optional.empty());
        when(tenants.save(any(Tenant.class))).thenAnswer(appel -> {
            Tenant t = appel.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 42L);
            return t;
        });
        when(encoder.encode("Passw0rd!")).thenReturn("empreinte");
        List<Long> tenantsDesListes = new ArrayList<>();
        when(references.creer(any(ReferenceRequest.class), eq(false))).thenAnswer(appel -> {
            tenantsDesListes.add(TenantContext.get());
            return null;
        });

        seeder.run();

        verify(modules).activer(42L, "clm", "amorçage");
        verify(modules).activer(42L, "clm_ia", "amorçage");
        verify(modules).desactiver(42L, "redaction", "amorçage");
        ArgumentCaptor<ReferenceRequest> valeurs = ArgumentCaptor.forClass(ReferenceRequest.class);
        verify(references, times(6)).creer(valeurs.capture(), eq(false));
        assertTrue(valeurs.getAllValues().stream().anyMatch(v -> "CLM_UNITE".equals(v.categorie())
                && "FINANCEMENT_ENTREPRISES".equals(v.code()) && Boolean.TRUE.equals(v.actif())));
        assertTrue(tenantsDesListes.stream().allMatch(id -> id == 42L), "chaque valeur est créée dans le contexte de l'organisation");
        assertNull(TenantContext.get(), "le contexte est retiré après l'amorçage");
        ArgumentCaptor<User> comptes = ArgumentCaptor.forClass(User.class);
        verify(users, times(8)).save(comptes.capture());
        assertTrue(comptes.getAllValues().stream().allMatch(u -> u.getTenantId() == 42L && "empreinte".equals(u.getMotDePasseHash())));
        Map<String, Set<Role>> roles = comptes.getAllValues().stream().collect(Collectors.toMap(User::getEmail, User::getRoles));
        assertEquals(Set.of(Role.AUDITEUR), roles.get("auditeur@iq.example"));
        assertEquals(Set.of(Role.LECTEUR), roles.get("signataire@iq.example"));
        assertEquals(Set.of(Role.REDACTEUR), roles.get("initiateur@iq.example"));
    }

    @Test
    void une_organisation_existante_garde_ses_modules_et_ses_comptes() throws Exception {
        when(tenants.findBySlug("iq")).thenReturn(Optional.of(new Tenant("iq", "Investissement Québec", null)));
        when(users.existsByEmail(anyString())).thenReturn(true);

        seeder.run();

        verify(tenants, never()).save(any());
        verify(users, never()).save(any());
        verifyNoInteractions(modules, references, encoder);
    }
}
