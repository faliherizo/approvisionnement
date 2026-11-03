package qc.levis.contrat.securite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qc.levis.contrat.securite.application.SecuriteService;
import qc.levis.contrat.securite.application.repository.GroupeMembreRepository;
import qc.levis.contrat.securite.application.repository.GroupeRepository;
import qc.levis.contrat.securite.application.repository.PermissionAccesRepository;
import qc.levis.contrat.securite.domain.Groupe;
import qc.levis.contrat.securite.domain.GroupeMembre;

@ExtendWith(MockitoExtension.class)
class SecuriteServiceTest {

    @Mock PermissionAccesRepository permissions;
    @Mock GroupeRepository groupes;
    @Mock GroupeMembreRepository membres;
    SecuriteService service;

    @BeforeEach void avant() { service = new SecuriteService(permissions, groupes, membres); }

    @Test void groupes_de_un_utilisateur() {
        when(membres.findByTenantIdAndUtilisateur(1L, "u@x.ca"))
                .thenReturn(List.of(new GroupeMembre(1L, "G1", "u@x.ca"), new GroupeMembre(1L, "G2", "u@x.ca")));
        Set<String> g = service.groupesDe(1L, "u@x.ca");
        assertEquals(Set.of("G1", "G2"), g);
    }

    @Test void autorise_refuse_par_defaut_sans_permission() {
        when(permissions.findByTenantId(1L)).thenReturn(List.of());
        when(membres.findByTenantIdAndUtilisateur(1L, "u")).thenReturn(List.of());
        assertFalse(service.autorise(1L, Set.of("REDACTEUR"), "u", "CHAMP", "salaire", "LIRE"));
    }

    @Test void ajouter_groupe_delegue_au_depot() {
        Groupe g = new Groupe();
        when(groupes.save(g)).thenReturn(g);
        assertSame(g, service.ajouterGroupe(g));
        verify(groupes).save(g);
    }

    @Test void supprimer_permission_delegue() {
        service.supprimerPermission(9L);
        verify(permissions).deleteById(9L);
    }
}
