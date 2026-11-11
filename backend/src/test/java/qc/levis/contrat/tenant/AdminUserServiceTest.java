package qc.levis.contrat.tenant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.tenant.application.AdminUserService;
import qc.levis.contrat.tenant.application.dto.AdminUserRequest;

/** Journal des changements de comptes et de rôles (UI-20, zone 05 ; F-11) : qui a accordé quel droit, à qui. */
class AdminUserServiceTest {

    private static final long TENANT = 7L;
    private static final String ADMIN = "admin.ti@iq.example";

    private final UserRepository users = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final AuditService audit = mock(AuditService.class);
    private final AdminUserService service = new AdminUserService(users, encoder, audit);

    @BeforeEach
    void tenant() {
        TenantContext.set(TENANT);
        when(users.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(encoder.encode(anyString())).thenReturn("haché");
    }

    @AfterEach
    void nettoyer() {
        TenantContext.clear();
    }

    @Test
    void creation_consignee_avec_ses_roles_sans_mot_de_passe() {
        when(users.save(any(User.class))).thenAnswer(i -> avecId(i.getArgument(0), 41L));

        service.creer(requete(List.of("REDACTEUR", "AUDITEUR"), true, "Secret123!"), ADMIN);

        verify(audit).log(ADMIN, "COMPTE_CREE", "Utilisateur", 41L, "nouveau@iq.example · rôles [AUDITEUR, REDACTEUR]");
        verify(audit, never()).log(anyString(), anyString(), anyString(), anyLong(), eq("Secret123!"));
    }

    @Test
    void changement_de_roles_consigne_avant_et_apres() {
        when(users.findById(5L)).thenReturn(Optional.of(compte(5L, Set.of(Enums.Role.LECTEUR), true)));

        service.modifier(5L, requete(List.of("ADMIN"), true, null), ADMIN);

        verify(audit).log(ADMIN, "COMPTE_ROLES_MODIFIES", "Utilisateur", 5L, "nouveau@iq.example · [LECTEUR] → [ADMIN]");
        verify(audit).log(ADMIN, "COMPTE_MODIFIE", "Utilisateur", 5L, "nouveau@iq.example");
        verify(audit, never()).log(anyString(), eq("COMPTE_DESACTIVE"), anyString(), anyLong(), anyString());
        verify(audit, never()).log(anyString(), eq("COMPTE_MOT_DE_PASSE_REINITIALISE"), anyString(), anyLong(), anyString());
    }

    @Test
    void desactivation_reactivation_et_mot_de_passe_reinitialise_consignes() {
        when(users.findById(5L)).thenReturn(Optional.of(compte(5L, Set.of(Enums.Role.LECTEUR), true)));
        service.modifier(5L, requete(List.of("LECTEUR"), false, "Nouveau123!"), ADMIN);
        verify(audit).log(ADMIN, "COMPTE_DESACTIVE", "Utilisateur", 5L, "nouveau@iq.example");
        verify(audit).log(ADMIN, "COMPTE_MOT_DE_PASSE_REINITIALISE", "Utilisateur", 5L, "nouveau@iq.example");
        verify(audit, never()).log(anyString(), eq("COMPTE_ROLES_MODIFIES"), anyString(), anyLong(), anyString());

        when(users.findById(6L)).thenReturn(Optional.of(compte(6L, Set.of(Enums.Role.LECTEUR), false)));
        service.modifier(6L, requete(null, true, " "), ADMIN);
        verify(audit).log(ADMIN, "COMPTE_REACTIVE", "Utilisateur", 6L, "nouveau@iq.example");
    }

    @Test
    void suppression_consignee() {
        User u = compte(9L, Set.of(Enums.Role.JURIDIQUE), true);
        when(users.findById(9L)).thenReturn(Optional.of(u));

        service.supprimer(9L, ADMIN);

        verify(users).delete(u);
        verify(audit).log(ADMIN, "COMPTE_SUPPRIME", "Utilisateur", 9L, "nouveau@iq.example · rôles [JURIDIQUE]");
    }

    @Test
    void compte_d_une_autre_organisation_introuvable_et_rien_n_est_consigne() {
        User etranger = compte(3L, Set.of(Enums.Role.ADMIN), true);
        etranger.setTenantId(99L);
        when(users.findById(3L)).thenReturn(Optional.of(etranger));

        assertThatThrownBy(() -> service.supprimer(3L, ADMIN)).isInstanceOf(NoSuchElementException.class);
        verify(users, never()).delete(any(User.class));
        verify(audit, never()).log(anyString(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void superadmin_jamais_accorde_par_un_administrateur() {
        when(users.findById(5L)).thenReturn(Optional.of(compte(5L, Set.of(Enums.Role.LECTEUR), true)));

        User u = service.modifier(5L, requete(List.of("SUPERADMIN", "LECTEUR"), true, null), ADMIN);

        assertThat(u.getRoles()).containsExactly(Enums.Role.LECTEUR);
    }

    private static AdminUserRequest requete(List<String> roles, Boolean actif, String motDePasse) {
        return new AdminUserRequest("nouveau@iq.example", "Nouveau Compte", "TI", motDePasse, roles, actif);
    }

    private static User compte(long id, Set<Enums.Role> roles, boolean actif) {
        User u = new User();
        u.setEmail("nouveau@iq.example");
        u.setTenantId(TENANT);
        u.setRoles(new java.util.HashSet<>(roles));
        u.setActif(actif);
        return avecId(u, id);
    }

    private static User avecId(User u, long id) {
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }
}
