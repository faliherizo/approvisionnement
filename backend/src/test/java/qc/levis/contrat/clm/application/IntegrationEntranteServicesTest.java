package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.port.PortRappel;
import qc.levis.contrat.clm.application.port.Scelleur;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.ClientIntegrationRepository;
import qc.levis.contrat.clm.application.repository.RappelSortantRepository;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.clm.domain.RappelSortant;
import qc.levis.contrat.clm.domain.Trousse;
import qc.levis.contrat.commun.application.SecretCipher;

/** Jetons d'intégration et rappels sortants : expiration, falsification, révocation, livraison et reprise. */
class IntegrationEntranteServicesTest {

    private static final Instant T0 = Instant.parse("2026-11-11T12:00:00Z");
    private static final Long IQ = 7L;

    private final ClientIntegrationRepository clients = mock(ClientIntegrationRepository.class);
    private final RappelSortantRepository rappels = mock(RappelSortantRepository.class);
    private final SourceAleatoire alea = mock(SourceAleatoire.class);
    private final SecretCipher chiffrement = mock(SecretCipher.class);
    private final PortRappel port = mock(PortRappel.class);
    private final JournalClm journal = mock(JournalClm.class);
    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    private final Scelleur scelleur = new Scelleur() {
        @Override
        public String sceller(byte[] contenu) {
            return Integer.toHexString(java.util.Arrays.hashCode(contenu));
        }

        @Override
        public boolean verifier(byte[] contenu, String sceau) {
            return sceller(contenu).equals(sceau);
        }
    };
    private ContexteClm ctx;
    private ClientsIntegrationService gestion;
    private RappelsExternesService service;
    private ClientIntegration client;

    @BeforeEach
    void preparer() {
        ctx = contexte(T0);
        gestion = new ClientsIntegrationService(clients, alea, chiffrement, scelleur, journal, ctx);
        service = new RappelsExternesService(rappels, clients, gestion, port, ctx);
        client = ClientIntegration.creer(IQ, "Workday", ClientIntegration.Systeme.WORKDAY, "https://wd.example/r", "clm-1",
                scelleur.sceller("client-integration:s3cret".getBytes()), "chiffre", "admin", T0);
        ReflectionTestUtils.setField(client, "id", 1L);
        when(clients.findByClientId("clm-1")).thenReturn(Optional.of(client));
        when(clients.findById(1L)).thenReturn(Optional.of(client));
        when(chiffrement.dechiffrer("chiffre")).thenReturn("cle-rappel");
        when(rappels.save(any(RappelSortant.class))).thenAnswer(i -> i.getArgument(0));
        when(clients.save(any(ClientIntegration.class))).thenAnswer(i -> i.getArgument(0));
    }

    private ContexteClm contexte(Instant maintenant) {
        return new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(), Clock.fixed(maintenant, ZoneOffset.UTC));
    }

    @Test
    void creation_remet_les_secrets_une_seule_fois_et_trace_sans_les_journaliser() {
        when(alea.jeton()).thenReturn("secret-1", "cle-1");
        when(alea.uuid()).thenReturn("uuid-1");
        when(chiffrement.chiffrer("cle-1")).thenReturn("cle-chiffree");
        ClmDto.ClientIntegrationCree cree = portee.executerDans(IQ, () -> gestion.creer(
                new ClmDto.ClientIntegrationRequest("Salesforce", " salesforce ", null), "admin.ti@iq.example"));
        assertEquals("secret-1", cree.secret());
        verify(clients).save(argThat(c -> c.authentifie(scelleur.sceller("client-integration:secret-1".getBytes()))
                && !c.authentifie("secret-1")));
        assertEquals("cle-1", cree.cleRappel());
        assertEquals("clm-uuid-1", cree.client().clientId());
        assertEquals("SALESFORCE", cree.client().systeme());
        verify(journal).configuration(eq("admin.ti@iq.example"), eq("CLIENT_INTEGRATION"), argThat(s -> !s.contains("secret-1")));
        assertThrows(IllegalArgumentException.class, () -> portee.executerDans(IQ, () -> gestion.creer(
                new ClmDto.ClientIntegrationRequest("X", "SAP", null), "a")));
        assertThrows(IllegalArgumentException.class, () -> portee.executerDans(IQ, () -> gestion.creer(
                new ClmDto.ClientIntegrationRequest("X", null, null), "a")));
    }

    @Test
    void lister_et_revoquer_dans_l_organisation() {
        when(clients.findByTenantIdOrderByCreeLeDesc(IQ)).thenReturn(List.of(client));
        assertEquals("Workday", portee.executerDans(IQ, gestion::lister).get(0).nom());
        when(clients.findByIdAndTenantId(1L, IQ)).thenReturn(Optional.of(client));
        assertFalse(portee.executerDans(IQ, () -> gestion.revoquer(1L, "admin")).actif());
        assertThrows(NoSuchElementException.class, () -> portee.executerDans(IQ, () -> gestion.revoquer(2L, "admin")));
    }

    @Test
    void un_jeton_valide_designe_son_client_jusqu_a_expiration() {
        String jeton = gestion.emettreJeton("clm-1", "s3cret").access_token();
        assertSame(client, gestion.authentifier(jeton));
        ClientsIntegrationService plusTard = new ClientsIntegrationService(clients, alea, chiffrement, scelleur, journal,
                contexte(T0.plus(ClientsIntegrationService.DUREE_JETON).plusSeconds(1)));
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> plusTard.authentifier(jeton), "expiré");
    }

    @Test
    void un_jeton_falsifie_illisible_ou_d_un_client_disparu_est_refuse() {
        String jeton = gestion.emettreJeton("clm-1", "s3cret").access_token();
        String[] p = jeton.split("\\.");
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> gestion.authentifier(p[0] + ".ffff"));
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> gestion.authentifier("@@@." + p[1]));
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> gestion.authentifier(null));
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> gestion.authentifier("sans-point"));
        String autre = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString("clm-1".getBytes());
        assertThrows(ClientsIntegrationService.AccesRefuse.class,
                () -> gestion.authentifier(autre + "." + scelleur.sceller("clm-1".getBytes())), "charge mal formée");
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> gestion.emettreJeton("clm-1", "faux"));
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> gestion.emettreJeton(null, "s3cret"));
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> gestion.emettreJeton("clm-1", null));
        when(clients.findByClientId("clm-1")).thenReturn(Optional.empty());
        assertThrows(ClientsIntegrationService.AccesRefuse.class, () -> gestion.authentifier(jeton));
    }

    private Trousse trousse(Long clientId) {
        Trousse t = new Trousse(IQ, "TR-1", "T", "init@iq.example", T0);
        ReflectionTestUtils.setField(t, "id", 10L);
        t.rattacherExterne(Trousse.Source.WORKDAY, clientId, "WD-1", null);
        return t;
    }

    @Test
    void seul_le_createur_avec_adresse_de_rappel_recoit_un_rappel_unique() {
        service.enfiler(trousse(null), "LANCEE", 1, Map.of());
        when(clients.findById(2L)).thenReturn(Optional.empty());
        service.enfiler(trousse(2L), "LANCEE", 1, Map.of());
        ClientIntegration sansUrl = ClientIntegration.creer(IQ, "S", ClientIntegration.Systeme.AUTRE, null, "c3", "s", "k", "a", T0);
        when(clients.findById(3L)).thenReturn(Optional.of(sansUrl));
        service.enfiler(trousse(3L), "LANCEE", 1, Map.of());
        verify(rappels, never()).save(any());

        when(rappels.existsByCleIdempotence("7:10:LANCEE:1")).thenReturn(true);
        service.enfiler(trousse(1L), "LANCEE", 1, Map.of());
        verify(rappels, never()).save(any());
        service.enfiler(trousse(1L), "TERMINEE", 2, Map.of("signatures", 1));
        verify(rappels).save(argThat(r -> r.getCleIdempotence().equals("7:10:TERMINEE:2") && r.getPayloadJson().contains("WD-1")));
    }

    @Test
    void relais_livre_signe_reprend_un_refus_et_ecarte_un_client_revoque() {
        RappelSortant livre = new RappelSortant(IQ, 1L, 10L, "LANCEE", "k1", "{\"a\":1}", T0);
        RappelSortant refuse = new RappelSortant(IQ, 1L, 10L, "ANNULEE", "k2", "{}", T0);
        when(rappels.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(RappelSortant.Statut.EN_ATTENTE, T0))
                .thenReturn(List.of(livre, refuse));
        doThrow(new PortRappel.Refuse("HTTP 500")).when(port).envoyer(anyString(), eq("{}"), anyString(), eq("k2"));

        assertEquals(1, service.relayer());
        verify(port).envoyer("https://wd.example/r", "{\"a\":1}", RappelsExternesService.signer("cle-rappel", "{\"a\":1}"), "k1");
        assertEquals(RappelSortant.Statut.ENVOYE, livre.getStatut());
        assertEquals(1, refuse.getTentatives());
        assertEquals("HTTP 500", refuse.getDerniereErreur());

        client.revoquer(T0);
        RappelSortant orphelin = new RappelSortant(IQ, 1L, 10L, "TERMINEE", "k3", "{}", T0);
        when(rappels.findTop50ByStatutAndProchainEssaiLessThanEqualOrderByProchainEssaiAsc(RappelSortant.Statut.EN_ATTENTE, T0))
                .thenReturn(List.of(orphelin));
        assertEquals(0, service.relayer());
        assertTrue(orphelin.getDerniereErreur().contains("révoqué"));
    }

    @Test
    void l_administrateur_consulte_et_relance_les_rappels() {
        RappelSortant r = new RappelSortant(IQ, 1L, 10L, "LANCEE", "k1", "{}", T0);
        RappelSortant orphelin = new RappelSortant(IQ, 9L, 10L, "LANCEE", "k9", "{}", T0);
        when(rappels.findTop100ByTenantIdOrderByCreeLeDesc(IQ)).thenReturn(List.of(r, orphelin));
        when(clients.findById(9L)).thenReturn(Optional.empty());
        List<ClmDto.RappelVue> vues = portee.executerDans(IQ, service::lister);
        assertEquals("Workday", vues.get(0).client());
        assertEquals("—", vues.get(1).client());
        when(rappels.findByIdAndTenantId(5L, IQ)).thenReturn(Optional.of(r));
        assertEquals("EN_ATTENTE", portee.executerDans(IQ, () -> service.relancer(5L)).statut());
        assertThrows(NoSuchElementException.class, () -> portee.executerDans(IQ, () -> service.relancer(6L)));
    }
}
