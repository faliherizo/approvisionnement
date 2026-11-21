package qc.levis.contrat.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.notification.domain.NotificationEnvoi;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.notification.application.NotificationService;
import qc.levis.contrat.notification.application.port.CanalNotification;
import qc.levis.contrat.notification.application.repository.NotificationEnvoiRepository;
import qc.levis.contrat.notification.application.repository.NotificationModeleRepository;
import qc.levis.contrat.notification.application.repository.NotificationRegleRepository;
import qc.levis.contrat.notification.domain.NotificationModele;
import qc.levis.contrat.notification.domain.NotificationRegle;
import qc.levis.contrat.saisie.application.FicheEvent;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock NotificationModeleRepository modeles;
    @Mock NotificationRegleRepository regles;
    @Mock NotificationEnvoiRepository journal;
    @Mock ModuleService modules;

    /** Canal de test qui enregistre les envois. */
    static class CanalTest implements CanalNotification {
        String dernierDest;
        public String canal() { return "COURRIEL"; }
        public String envoyer(String dest, String sujet, String corps) { dernierDest = dest; return "SIMULE"; }
    }

    CanalTest canal;
    NotificationService service;

    @BeforeEach void avant() {
        canal = new CanalTest();
        service = new NotificationService(modeles, regles, journal, modules, List.of(canal));
    }

    private FicheEvent event() {
        return new FicheEvent(1L, 2L, 3L, "SOUMISSION", Map.of("courriel", "marie@levis.qc.ca", "nom", "Marie"));
    }

    @Test void module_inactif_ne_notifie_pas() {
        when(modules.estActif(1L, "notification")).thenReturn(false);
        service.surFiche(event());
        verifyNoInteractions(regles, modeles, journal);
    }

    @Test void sur_soumission_fusionne_et_journalise_lenvoi() {
        when(modules.estActif(1L, "notification")).thenReturn(true);
        when(regles.findByTenantIdAndFormulaireIdAndDeclencheurAndActifTrue(1L, 2L, "SOUMISSION"))
                .thenReturn(List.of(new NotificationRegle(1L, 2L, "SOUMISSION", "accuse", "{{courriel}}")));
        when(modeles.findByTenantIdAndCode(1L, "accuse"))
                .thenReturn(Optional.of(new NotificationModele(1L, "accuse", "COURRIEL", "Bonjour {{nom}}", "Votre demande est reçue.")));
        service.surFiche(event());
        assertEquals("marie@levis.qc.ca", canal.dernierDest);  // destinataire résolu depuis {{courriel}}
        verify(journal).save(argThat(env -> "SIMULE".equals(env.getStatut()) && env.getSujet().equals("Bonjour Marie")));
    }

    @Test void gabarit_introuvable_est_ignore() {
        when(modules.estActif(1L, "notification")).thenReturn(true);
        when(regles.findByTenantIdAndFormulaireIdAndDeclencheurAndActifTrue(1L, 2L, "SOUMISSION"))
                .thenReturn(List.of(new NotificationRegle(1L, 2L, "SOUMISSION", "absent", "{{courriel}}")));
        when(modeles.findByTenantIdAndCode(1L, "absent")).thenReturn(Optional.empty());
        service.surFiche(event());
        verify(journal, never()).save(any());
    }

    @Test void le_numero_de_la_fiche_se_cite_dans_le_gabarit_sans_masquer_un_champ_numero() {
        when(modules.estActif(1L, "notification")).thenReturn(true);
        when(regles.findByTenantIdAndFormulaireIdAndDeclencheurAndActifTrue(1L, 2L, "SOUMISSION"))
                .thenReturn(List.of(new NotificationRegle(1L, 2L, "SOUMISSION", "accuse", "{{courriel}}")));
        when(modeles.findByTenantIdAndCode(1L, "accuse"))
                .thenReturn(Optional.of(new NotificationModele(1L, "accuse", "COURRIEL", "Demande {{numero}}", "Bonjour {{nom}}")));
        NotificationService avecNumeros = new NotificationService(modeles, regles, journal, modules, List.of(canal),
                (tenant, fiche) -> tenant == 1L && fiche == 3L ? Optional.of("RH-2026-000003") : Optional.empty());
        ArgumentCaptor<NotificationEnvoi> envoi = ArgumentCaptor.forClass(NotificationEnvoi.class);

        avecNumeros.surFiche(event());
        verify(journal).save(envoi.capture());
        assertEquals("Demande RH-2026-000003", envoi.getValue().getSujet());

        avecNumeros.surFiche(new FicheEvent(1L, 2L, 3L, "SOUMISSION", Map.of("courriel", "m@x.ca", "numero", "saisi", "nom", "M")));
        verify(journal, times(2)).save(envoi.capture());
        assertEquals("Demande saisi", envoi.getValue().getSujet(), "un champ du formulaire nommé numero prévaut");
    }

    @Test void sans_lecture_des_numeros_le_marqueur_reste_vide() {
        when(modules.estActif(1L, "notification")).thenReturn(true);
        when(regles.findByTenantIdAndFormulaireIdAndDeclencheurAndActifTrue(1L, 2L, "SOUMISSION"))
                .thenReturn(List.of(new NotificationRegle(1L, 2L, "SOUMISSION", "accuse", "{{courriel}}")));
        when(modeles.findByTenantIdAndCode(1L, "accuse"))
                .thenReturn(Optional.of(new NotificationModele(1L, "accuse", "COURRIEL", "Demande {{numero}}", "x")));
        ArgumentCaptor<NotificationEnvoi> envoi = ArgumentCaptor.forClass(NotificationEnvoi.class);
        service.surFiche(new FicheEvent(1L, 2L, 3L, "SOUMISSION", null));
        verify(journal, never()).save(any());
        service.surFiche(event());
        verify(journal).save(envoi.capture());
        assertEquals("Demande ", envoi.getValue().getSujet());
    }
}
