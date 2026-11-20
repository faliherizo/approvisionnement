package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AlertesExploitation;
import qc.levis.contrat.clm.application.port.NotificateurClm;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.application.port.ReferencesClm;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.MessageSortantRepository;
import qc.levis.contrat.clm.application.repository.ModeleMessageRepository;
import qc.levis.contrat.clm.domain.Langue;
import qc.levis.contrat.clm.domain.MessageSortant;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.ModeleMessage;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.Trousse;

/** Addenda 2 : langue des messages (Q19), correspondance des statuts (Q14) et lecture limitée (Q15), sans la pile Spring. */
class Addenda2ServicesTest {

    private static final Long IQ = 3L;
    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), new PorteeTenantMemoire(), new ObjectMapper(),
            Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC));

    // --- Q19 ---
    private final ModeleMessageRepository modeles = mock(ModeleMessageRepository.class);
    private final NotificateurClm notificateur = mock(NotificateurClm.class);
    private final MessagerieClm messagerie = new MessagerieClm(modeles, notificateur, ctx);

    private String sujetEnvoye() {
        ArgumentCaptor<String> sujet = ArgumentCaptor.forClass(String.class);
        verify(notificateur).envoyer(eq("COURRIEL"), eq("alice@exemple.ca"), sujet.capture(), any());
        return sujet.getValue();
    }

    @Test
    void un_signataire_anglophone_recoit_l_invitation_en_anglais() {
        when(notificateur.envoyer(any(), any(), any(), any())).thenReturn("ENVOYE");
        assertEquals("ENVOYE", messagerie.envoyer(IQ, ModeleMessage.Evenement.INVITATION, "COURRIEL", "alice@exemple.ca",
                Map.of("titre", "Convention", "reference", "TR-1", "lien", "https://p/x?lang=en", "echeance", "2026-12-01"), List.of(), Langue.EN));
        assertEquals("Document to sign: Convention", sujetEnvoye());
    }

    @Test
    void le_francais_reste_la_langue_par_defaut_et_un_message_desactive_le_reste_en_anglais() {
        messagerie.envoyer(IQ, ModeleMessage.Evenement.REJET, "COURRIEL", "alice@exemple.ca", Map.of("reference", "TR-1"), List.of(), Langue.EN);
        assertEquals("Trousse rejetée — TR-1", sujetEnvoye(), "aucune version anglaise : message interne, en français");

        ModeleMessage inactif = new ModeleMessage(IQ, ModeleMessage.Evenement.CODE, "SMS", "Code", "{{code}}");
        inactif.setActif(false);
        when(modeles.findByTenantIdAndEvenement(IQ, ModeleMessage.Evenement.CODE)).thenReturn(List.of(inactif));
        assertEquals("DESACTIVE", messagerie.envoyer(IQ, ModeleMessage.Evenement.CODE, "SMS", "+15145550101", Map.of(), List.of(), Langue.EN));
    }

    @Test
    void le_lien_du_portail_porte_la_langue_anglaise_seulement() {
        qc.levis.contrat.clm.domain.ParametresClm.Reglages r = mock(qc.levis.contrat.clm.domain.ParametresClm.Reglages.class);
        when(r.portailUrl()).thenReturn("https://iq.example/portail");
        assertEquals("https://iq.example/portail/jeton?lang=en", ParcoursService.lienPortail(r, "jeton", Langue.EN));
        assertEquals("https://iq.example/portail/jeton", ParcoursService.lienPortail(r, "jeton", Langue.FR));
    }

    // --- Q14, Q15 ---
    private final PortSalesforce port = mock(PortSalesforce.class);
    private final MessageSortantRepository sorties = mock(MessageSortantRepository.class);
    private final ReferencesClm references = mock(ReferencesClm.class);
    private final SalesforceService salesforce = new SalesforceService(port, sorties, mock(SourceAleatoire.class),
            mock(ConnexionsSalesforce.class), ctx, mock(AlertesExploitation.class), new ListesClm(references));

    @Test
    void la_valeur_de_statut_suit_la_liste_de_l_organisation_ou_reste_le_code() {
        when(references.valeurs("CLM_STATUT_SALESFORCE")).thenReturn(List.of(new ReferencesClm.Valeur("RETOURNEE", "Returned", null),
                new ReferencesClm.Valeur("ANNULEE", " ", null)));
        Trousse t = mock(Trousse.class);
        when(t.getSource()).thenReturn(Trousse.Source.SALESFORCE);
        when(t.getSfDossierId()).thenReturn("a01");
        when(t.getTenantId()).thenReturn(IQ);
        when(t.getStatut()).thenReturn(StatutTrousse.EN_TRAITEMENT);
        ArgumentCaptor<MessageSortant> m = ArgumentCaptor.forClass(MessageSortant.class);
        salesforce.enfiler(t, "RETOURNEE", 1, Map.of());
        salesforce.enfiler(t, "ANNULEE", 2, Map.of());
        salesforce.enfiler(t, "LANCEE", 3, Map.of());
        verify(sorties, org.mockito.Mockito.times(3)).save(m.capture());
        assertTrue(m.getAllValues().get(0).getPayloadJson().contains("\"valeurStatut\":\"Returned\""));
        assertTrue(m.getAllValues().get(1).getPayloadJson().contains("\"valeurStatut\":\"ANNULEE\""), "libellé vide : le code");
        assertTrue(m.getAllValues().get(2).getPayloadJson().contains("\"valeurStatut\":\"LANCEE\""), "événement absent de la liste : le code");
    }

    @Test
    void la_lecture_demande_les_champs_declares_et_les_sources_du_modele_seulement() {
        when(references.valeurs("CLM_CHAMP_SALESFORCE")).thenReturn(List.of(new ReferencesClm.Valeur("Montant", "Montant", null)));
        Modele.Champ avecSource = mock(Modele.Champ.class);
        when(avecSource.source()).thenReturn("Programme__c");
        Modele.Champ sansSource = mock(Modele.Champ.class);
        PortSalesforce.Dossier d = new PortSalesforce.Dossier("a01", "DEM-1", Map.of(), Instant.EPOCH);
        when(port.lireDossier("DEM-1", Set.of("Montant", "Programme__c"))).thenReturn(Optional.of(d));
        assertSame(d, salesforce.lireDossier(" DEM-1 ", List.of(avecSource, sansSource)));
        assertThrows(IllegalArgumentException.class, () -> salesforce.lireDossier(" ", List.of()));
        assertThrows(java.util.NoSuchElementException.class, () -> salesforce.lireDossier("INCONNU"));
    }

    @Test
    void le_certificat_mentionne_la_qualite_l_entreprise_et_l_attestation_d_autorite() {
        java.util.Map<String, Object> rep = new java.util.HashMap<>(Map.of("qualite", "Président", "entreprise", "Agri-Nord",
                "autoriteAttestee", true, "referenceAutorite", "Résolution 2026-14"));
        ClmDto.SignatureVue s = new ClmDto.SignatureVue("Marc", "m@x", "SIGNATAIRE_1", "COURRIEL", null, null, null, null, "SIGNEE",
                Map.of("representation", rep));
        assertEquals(" · pour Agri-Nord (Président) · autorité attestée, réf. Résolution 2026-14", PreuveService.representation(s));
        rep.put("referenceAutorite", " ");
        assertEquals(" · pour Agri-Nord (Président) · autorité attestée", PreuveService.representation(s));
        rep.remove("autoriteAttestee");
        assertEquals(" · pour Agri-Nord (Président)", PreuveService.representation(s));
        assertEquals("", PreuveService.representation(new ClmDto.SignatureVue("A", "a", "R", "N", null, null, null, null, "SIGNEE", Map.of())));
        assertEquals("", PreuveService.representation(new ClmDto.SignatureVue("A", "a", "R", "N", null, null, null, null, "SIGNEE", null)));
    }
}
