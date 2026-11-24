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
import qc.levis.contrat.redaction.application.port.BibliothequeDeDepart;
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
    private final BibliothequeDeDepart bibliotheque = mock(BibliothequeDeDepart.class);
    private final TenantsDemoSeeder seeder = new TenantsDemoSeeder(tenants, users, modules, references, encoder, bibliotheque);

    /** V3R déjà en place, ses comptes aussi : seul IQ est en jeu dans les cas qui l'éprouvent. */
    private static List<qc.levis.contrat.reference.application.dto.ValeurReference> valeurs(String liste, String... codes) {
        return java.util.Arrays.stream(codes).map(c -> new qc.levis.contrat.reference.application.dto.ValeurReference(
                1L, liste, c, "Renommée par l'administration", null, 10, true, "PERSONNALISEE")).toList();
    }

    @Test
    void une_organisation_existante_recoit_seulement_les_valeurs_de_liste_qui_lui_manquent() throws Exception {
        Tenant iq = new Tenant("iq", "Investissement Québec", null);
        ReflectionTestUtils.setField(iq, "id", 3L);
        when(tenants.findBySlug("iq")).thenReturn(Optional.of(iq));
        v3rExistante();
        when(users.existsByEmail(anyString())).thenReturn(true);
        when(references.lister("CLM_UNITE", false)).thenReturn(valeurs("CLM_UNITE", "FINANCEMENT_ENTREPRISES", "VP_FINANCEMENT",
                "AFFAIRES_JURIDIQUES", "GESTION_CONTRACTUELLE"));
        when(references.lister("CLM_TYPE_CONTRAT", false)).thenReturn(valeurs("CLM_TYPE_CONTRAT", "GARANTIE"));

        seeder.run();

        ArgumentCaptor<ReferenceRequest> creees = ArgumentCaptor.forClass(ReferenceRequest.class);
        verify(references).creer(creees.capture(), eq(false));
        assertEquals("DIRECTION_GENERALE", creees.getValue().code(), "seule la valeur absente est ajoutée");
        assertNull(TenantContext.get(), "le contexte d'organisation est rendu");
    }

    private void v3rExistante() {
        Tenant v3r = new Tenant("v3r", "Ville de Trois-Rivières", null);
        ReflectionTestUtils.setField(v3r, "id", 7L);
        when(tenants.findBySlug("v3r")).thenReturn(Optional.of(v3r));
        when(users.existsByEmail(org.mockito.ArgumentMatchers.endsWith("@v3r.example"))).thenReturn(true);
    }

    @Test
    void cree_l_organisation_ses_modules_et_un_compte_par_profil() throws Exception {
        v3rExistante();
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
        // Seule V3R déclare la bibliothèque de départ (rédaction) ; IQ, en CLM seul, n'en reçoit pas.
        verify(bibliotheque).amorcer(7L);
        verify(bibliotheque, never()).amorcer(42L);
    }

    @Test
    void v3r_recoit_ses_comptes_et_la_bibliotheque_de_depart() throws Exception {
        when(tenants.findBySlug("iq")).thenReturn(Optional.of(new Tenant("iq", "Investissement Québec", null)));
        when(users.existsByEmail(org.mockito.ArgumentMatchers.endsWith("@iq.example"))).thenReturn(true);
        when(tenants.findBySlug("v3r")).thenReturn(Optional.empty());
        when(tenants.save(any(Tenant.class))).thenAnswer(appel -> {
            Tenant t = appel.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 9L);
            return t;
        });

        seeder.run();

        verify(modules).activer(9L, "redaction", "amorçage");
        verify(bibliotheque).amorcer(9L);
        ArgumentCaptor<User> comptes = ArgumentCaptor.forClass(User.class);
        verify(users, times(5)).save(comptes.capture());
        Map<String, Set<Role>> roles = comptes.getAllValues().stream().collect(Collectors.toMap(User::getEmail, User::getRoles));
        assertEquals(Set.of(Role.REDACTEUR, Role.APPRO), roles.get("approvisionnement@v3r.example"));
        assertEquals(Set.of(Role.LECTEUR), roles.get("consultation@v3r.example"));
        assertTrue(comptes.getAllValues().stream().allMatch(u -> u.getTenantId() == 9L));
    }

    @Test
    void une_organisation_existante_garde_ses_modules_et_ses_comptes() throws Exception {
        when(tenants.findBySlug("iq")).thenReturn(Optional.of(new Tenant("iq", "Investissement Québec", null)));
        v3rExistante();
        when(users.existsByEmail(anyString())).thenReturn(true);
        // Listes déjà complètes, dont une valeur que l'administration a renommée : rien n'est recréé ni écrasé.
        when(references.lister("CLM_UNITE", false)).thenReturn(valeurs("CLM_UNITE", "FINANCEMENT_ENTREPRISES", "VP_FINANCEMENT",
                "AFFAIRES_JURIDIQUES", "GESTION_CONTRACTUELLE", "DIRECTION_GENERALE"));
        when(references.lister("CLM_TYPE_CONTRAT", false)).thenReturn(valeurs("CLM_TYPE_CONTRAT", "GARANTIE"));

        seeder.run();

        verify(tenants, never()).save(any());
        verify(users, never()).save(any());
        verify(references, never()).creer(any(), org.mockito.ArgumentMatchers.anyBoolean());
        verifyNoInteractions(modules, encoder);
        // L'amorçage de la bibliothèque est idempotent : il est demandé, et c'est lui qui ne fait rien si elle existe.
        verify(bibliotheque).amorcer(7L);
    }
}
