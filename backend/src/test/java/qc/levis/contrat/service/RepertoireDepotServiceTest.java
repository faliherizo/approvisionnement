package qc.levis.contrat.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.dossier.application.RepertoireDepotService;
import qc.levis.contrat.dossier.application.repository.RepertoireDepotRepository;
import qc.levis.contrat.dossier.domain.RepertoireDepot;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Tests unitaires des répertoires de dépôt personnels (normalisation, doublon, suppression). */
class RepertoireDepotServiceTest {

    private RepertoireDepotRepository repo;
    private RepertoireDepotService service;

    @BeforeEach
    void setUp() {
        repo = mock(RepertoireDepotRepository.class);
        service = new RepertoireDepotService(repo);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void ajouter_normaliseLeChemin() {
        when(repo.existsByUserIdAndChemin(1L, "Contrats/2027")).thenReturn(false);
        RepertoireDepot r = service.ajouter(1L, "/Contrats/2027/");
        assertEquals("Contrats/2027", r.getChemin());
        assertEquals(1L, r.getUserId());
    }

    @Test
    void ajouter_vide_refuse() {
        assertThrows(IllegalArgumentException.class, () -> service.ajouter(1L, "   "));
    }

    @Test
    void ajouter_traversee_refuse() {
        assertThrows(IllegalArgumentException.class, () -> service.ajouter(1L, "Contrats/../secret"));
    }

    @Test
    void ajouter_doublon_renvoieExistant_sansRecreer() {
        RepertoireDepot existant = new RepertoireDepot(1L, "Contrats");
        when(repo.existsByUserIdAndChemin(1L, "Contrats")).thenReturn(true);
        when(repo.findByUserIdOrderByCheminAsc(1L)).thenReturn(List.of(existant));

        assertSame(existant, service.ajouter(1L, "Contrats"));
        verify(repo, never()).save(any());
    }

    @Test
    void supprimer_inexistant_leve() {
        when(repo.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.supprimer(1L, 9L));
    }

    @Test
    void supprimer_existant_supprime() {
        RepertoireDepot r = new RepertoireDepot(1L, "Contrats");
        when(repo.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(r));
        service.supprimer(1L, 5L);
        verify(repo).delete(r);
    }

    @Test
    void entite_accesseurs() {
        RepertoireDepot r = new RepertoireDepot();
        r.setUserId(3L);
        r.setChemin("Contrats");
        assertEquals(3L, r.getUserId());
        assertEquals("Contrats", r.getChemin());
    }

    @Test
    void lister_delegueAuDepot() {
        when(repo.findByUserIdOrderByCheminAsc(1L)).thenReturn(List.of(new RepertoireDepot(1L, "A")));
        assertEquals(1, service.lister(1L).size());
        verify(repo).findByUserIdOrderByCheminAsc(eq(1L));
    }
}
