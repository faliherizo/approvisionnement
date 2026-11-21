package qc.levis.contrat.saisie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.formulaire.application.repository.FormulaireRepository;
import qc.levis.contrat.formulaire.application.repository.FormulaireVersionRepository;
import qc.levis.contrat.formulaire.domain.Formulaire;
import qc.levis.contrat.formulaire.domain.FormulaireVersion;
import qc.levis.contrat.regle.application.RegleService;
import qc.levis.contrat.saisie.application.FicheEvent;
import qc.levis.contrat.saisie.application.SaisieService;
import qc.levis.contrat.saisie.application.dto.SoumissionRequest;
import qc.levis.contrat.saisie.application.port.CaptchaVerifier;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.Saisie;
import qc.levis.contrat.saisie.domain.StatutSaisie;

@ExtendWith(MockitoExtension.class)
class SaisieServiceTest {

    @Mock SaisieRepository repo;
    @Mock FormulaireVersionRepository versions;
    @Mock FormulaireRepository formulaires;
    @Mock RegleService regleService;
    @Mock CaptchaVerifier captcha;
    @Mock ApplicationEventPublisher events;
    @Mock qc.levis.contrat.saisie.application.PlacesService places;
    SaisieService service;

    @BeforeEach void avant() {
        service = new SaisieService(repo, versions, formulaires, regleService, captcha, events, places);
        lenient().when(places.plan(any(), any())).thenReturn(qc.levis.contrat.saisie.domain.PlanDesPlaces.compter(List.of(), List.of()));
        TenantContext.set(1L);
    }
    @AfterEach void apres() { TenantContext.clear(); }

    private Saisie saisie(long id, StatutSaisie statut) {
        Saisie s = new Saisie(1L, 2L, 1, "u@x.ca");
        s.setStatut(statut);
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }
    private FormulaireVersion version(String json) { return new FormulaireVersion(2L, 1, json, "n"); }

    @Test void creer_genere_un_numero_prefixe_par_domaine() {
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(2L)).thenReturn(Optional.of(version("{}")));
        when(repo.save(any())).thenAnswer(i -> { Saisie s = i.getArgument(0); if (s.getId() == null) ReflectionTestUtils.setField(s, "id", 7L); return s; });
        Formulaire f = new Formulaire(1L, "da", "Demande achat", null); f.setDomaine("APPROVISIONNEMENT");
        when(formulaires.findByIdAndTenantId(2L, 1L)).thenReturn(Optional.of(f));
        assertTrue(service.creer(2L, "u@x.ca").numero().matches("AP-\\d{4}-000007"));
    }

    @Test void autosave_modifie_un_brouillon() {
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(saisie(5, StatutSaisie.BROUILLON)));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("{\"a\":1}", service.autosave(5L, "{\"a\":1}").donneesJson());
    }

    @Test void autosave_refuse_si_deja_soumis() {
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(saisie(5, StatutSaisie.SOUMIS)));
        assertThrows(IllegalStateException.class, () -> service.autosave(5L, "{}"));
    }

    @Test void soumettre_passe_a_soumis_et_publie_un_evenement() {
        Saisie s = saisie(5, StatutSaisie.BROUILLON); s.setDonneesJson("{}");
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(s));
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(2L)).thenReturn(Optional.of(version("{}")));
        when(regleService.extraireRegles("{}")).thenReturn(List.of());
        when(repo.countByFormulaireIdAndStatutNot(eq(2L), any())).thenReturn(0L);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("SOUMIS", service.soumettre(5L, new SoumissionRequest("{}", null)).statut());
        verify(events).publishEvent(any(FicheEvent.class));
    }

    @Test void l_evenement_porte_le_numero_de_la_fiche_pour_les_gabarits() {
        Saisie s = saisie(5, StatutSaisie.BROUILLON); s.setDonneesJson("{\"nom\":\"Tremblay\"}"); s.setNumero("RH-2026-000005");
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(s));
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(2L)).thenReturn(Optional.of(version("{}")));
        when(regleService.extraireRegles("{}")).thenReturn(List.of());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        service.soumettre(5L, new SoumissionRequest(null, null));
        org.mockito.ArgumentCaptor<FicheEvent> e = org.mockito.ArgumentCaptor.forClass(FicheEvent.class);
        verify(events).publishEvent(e.capture());
        assertEquals("RH-2026-000005", e.getValue().valeurs().get("numero"));
        assertEquals("Tremblay", e.getValue().valeurs().get("nom"));
    }

    @Test void un_champ_numero_du_formulaire_prevaut_sur_le_numero_de_fiche() {
        Saisie s = saisie(5, StatutSaisie.SOUMIS); s.setDonneesJson("{\"numero\":\"saisi\"}"); s.setNumero("RH-2026-000005");
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(s));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        service.prendreEnCharge(5L);
        org.mockito.ArgumentCaptor<FicheEvent> e = org.mockito.ArgumentCaptor.forClass(FicheEvent.class);
        verify(events).publishEvent(e.capture());
        assertEquals("saisi", e.getValue().valeurs().get("numero"));
    }

    @Test void valider_et_prendre_en_charge_suivent_les_transitions() {
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(saisie(5, StatutSaisie.SOUMIS)));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("EN_TRAITEMENT", service.prendreEnCharge(5L).statut());
    }

    @Test void transition_interdite_est_refusee() {
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(saisie(5, StatutSaisie.BROUILLON)));
        assertThrows(IllegalStateException.class, () -> service.valider(5L)); // BROUILLON ne peut pas aller VALIDE directement
    }

    @Test void annuler_un_brouillon() {
        when(repo.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(saisie(5, StatutSaisie.BROUILLON)));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals("ANNULE", service.annuler(5L).statut());
    }

    @Test void mes_demandes_enrichit_du_formulaire() {
        Saisie s = saisie(5, StatutSaisie.SOUMIS); s.setNumero("AP-2026-000005");
        when(repo.findByTenantIdAndCreeParOrderByMajLeDesc(1L, "u@x.ca")).thenReturn(List.of(s));
        Formulaire f = new Formulaire(1L, "da", "Demande achat", null); f.setDomaine("APPROVISIONNEMENT");
        when(formulaires.findByIdAndTenantId(2L, 1L)).thenReturn(Optional.of(f));
        var d = service.mesDemandes("u@x.ca");
        assertEquals(1, d.size());
        assertEquals("Demande achat", d.get(0).formulaireNom());
        assertEquals("APPROVISIONNEMENT", d.get(0).domaine());
    }

    @Test void structure_retourne_le_json_de_la_version() {
        when(versions.findTopByFormulaireIdOrderByNumeroDesc(2L)).thenReturn(Optional.of(version("{\"pages\":[]}")));
        assertEquals("{\"pages\":[]}", service.structure(2L));
    }
}
