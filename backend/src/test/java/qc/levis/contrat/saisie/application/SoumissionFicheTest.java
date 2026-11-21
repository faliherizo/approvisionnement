package qc.levis.contrat.saisie.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.saisie.application.dto.SoumissionRequest;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.ChampAChoix;
import qc.levis.contrat.saisie.domain.PlanDesPlaces;
import qc.levis.contrat.saisie.domain.Saisie;

/** CHU 3.06 : la soumission verrouille le formulaire, contrôle les places, puis délègue à SaisieService inchangé. */
class SoumissionFicheTest {

    SaisieService saisies = mock(SaisieService.class);
    SaisieRepository fiches = mock(SaisieRepository.class);
    FormulaireRepository formulaires = mock(FormulaireRepository.class);
    PlacesService places = mock(PlacesService.class);
    SoumissionFiche soumission = new SoumissionFiche(saisies, fiches, formulaires, places);

    /** Plan où le choix « Atelier » est complet (1 place, 1 inscrit). */
    static final PlanDesPlaces PLEIN = PlanDesPlaces.compter(
            List.of(new ChampAChoix("activite", "Activité", List.of("Atelier", "Formation"), Map.of("Atelier", 1))),
            List.of(Map.of("activite", "Atelier")));

    @BeforeEach
    void avant() {
        TenantContext.set(7L);
        Saisie s = new Saisie(7L, 2L, 1, "a@x.ca");
        ReflectionTestUtils.setField(s, "id", 5L);
        s.setDonneesJson("{\"activite\":\"Formation\"}");
        when(fiches.findByIdAndTenantId(5L, 7L)).thenReturn(Optional.of(s));
        when(places.plan(2L, 5L)).thenReturn(PLEIN);
    }

    @AfterEach
    void apres() { TenantContext.clear(); }

    @Test
    void le_formulaire_est_verrouille_avant_le_controle_puis_la_soumission_est_deleguee() {
        SoumissionRequest req = new SoumissionRequest("{\"activite\":\"Formation\"}", null);
        soumission.soumettre(5L, req, "10.0.0.1");
        InOrder ordre = inOrder(formulaires, places, saisies);
        ordre.verify(formulaires).verrouiller(2L, 7L);
        ordre.verify(places).plan(2L, 5L);
        ordre.verify(saisies).soumettre(5L, req, "10.0.0.1");
    }

    @Test
    void un_choix_complet_dans_les_donnees_soumises_arrete_tout_avant_la_soumission() {
        assertThrows(PlanDesPlaces.ChoixComplet.class,
                () -> soumission.soumettre(5L, new SoumissionRequest("{\"activite\":\"Atelier\"}", null), null));
        verify(saisies, never()).soumettre(any(), any(), any());
    }

    @Test
    void sans_donnees_dans_la_requete_les_reponses_enregistrees_sont_controlees() {
        soumission.soumettre(5L, new SoumissionRequest(null, null), null);
        verify(saisies).soumettre(eq(5L), any(), isNull());
        Saisie pleine = new Saisie(7L, 2L, 1, "a@x.ca");
        ReflectionTestUtils.setField(pleine, "id", 6L);
        pleine.setDonneesJson("{\"activite\":\"Atelier\"}");
        when(fiches.findByIdAndTenantId(6L, 7L)).thenReturn(Optional.of(pleine));
        when(places.plan(2L, 6L)).thenReturn(PLEIN);
        assertThrows(PlanDesPlaces.ChoixComplet.class, () -> soumission.soumettre(6L, new SoumissionRequest(null, null), null));
    }

    @Test
    void des_donnees_illisibles_ou_absentes_ne_retiennent_aucun_choix() {
        soumission.soumettre(5L, new SoumissionRequest("{pas du json", null), null);
        soumission.soumettre(5L, new SoumissionRequest(" ", null), null);
        verify(saisies, times(2)).soumettre(eq(5L), any(), isNull());
    }

    @Test
    void une_fiche_d_une_autre_organisation_est_introuvable() {
        assertThrows(NoSuchElementException.class, () -> soumission.soumettre(99L, new SoumissionRequest(null, null), null));
        verifyNoInteractions(formulaires, saisies);
    }
}
