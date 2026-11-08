package qc.levis.contrat.clm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.PerimetreUtilisateur;
import qc.levis.contrat.clm.application.port.ReferencesClm;
import qc.levis.contrat.clm.application.repository.ConnexionSalesforceRepository;
import qc.levis.contrat.clm.application.repository.ParametresClmRepository;
import qc.levis.contrat.clm.domain.ConnexionSalesforce;
import qc.levis.contrat.clm.domain.Identite;
import qc.levis.contrat.clm.domain.ParametresClm;
import qc.levis.contrat.commun.application.SecretCipher;

/** Configuration du module par organisation : paramètres (UI-22), connexion Salesforce (UI-21) et listes de référence. */
class ConfigurationServicesClmTest {

    private static final Instant T0 = Instant.parse("2026-11-02T10:00:00Z");
    private static final Long IQ = 7L;

    private final PorteeTenantMemoire portee = new PorteeTenantMemoire();
    private final ContexteClm ctx = new ContexteClm(mock(PerimetreUtilisateur.class), portee, new ObjectMapper(), Clock.fixed(T0, ZoneOffset.UTC));
    private final JournalClm journal = mock(JournalClm.class);
    private final SecretCipher chiffrement = new SecretCipher("cle-de-test", "5c0ff33a1b2c3d4e");

    private static ClmProprietes proprietes(String environnement, String modeSalesforce, String secret) {
        return new ClmProprietes(environnement, "http://localhost:5173/portail", null, null, null, null,
                new ClmProprietes.Salesforce(modeSalesforce, "https://plateforme.my.salesforce.com", "https://login.salesforce.com/t",
                        "client-plateforme", secret, "FundingRequest", "v60.0", List.of()));
    }

    // --- Paramètres du module ---

    private final ParametresClmRepository parametres = mock(ParametresClmRepository.class);

    private ParametresClmService parametres(String environnement) {
        when(parametres.save(any())).thenAnswer(a -> a.getArgument(0));
        return new ParametresClmService(parametres, proprietes(environnement, "simulation", null), journal, ctx);
    }

    @Test
    void sans_parametres_propres_l_organisation_suit_la_plateforme() {
        ParametresClmService service = parametres("DEV");
        when(parametres.findByTenantId(IQ)).thenReturn(Optional.empty());
        ParametresClm.Reglages r = service.reglages(IQ);
        assertEquals(service.plateforme(), r);
        assertEquals(25L * 1024 * 1024, r.tailleMaxOctets());
        ClmDto.ParametresModule vue = portee.executerDans(IQ, service::lire);
        assertFalse(vue.personnalise());
        assertNull(vue.modifiePar());
        assertEquals("TR", vue.prefixeReference());
        assertEquals(30L * 1024 * 1024, vue.plafondOctets());
        ClmDto.ParametresModule retabli = portee.executerDans(IQ, () -> service.retablir("admin@iq.example"));
        assertFalse(retabli.personnalise());
        verify(journal, never()).configuration(anyString(), anyString(), anyString());
        assertEquals(2, ParametresClmService.formats().size());
    }

    @Test
    void une_modification_partielle_garde_les_autres_parametres_et_se_trace() {
        ParametresClmService service = parametres("DEV");
        when(parametres.findByTenantId(IQ)).thenReturn(Optional.empty());
        ClmDto.ParametresModule vue = portee.executerDans(IQ, () -> service.modifier(new ClmDto.ParametresModuleRequest(null, null, null,
                " iq ", null, null, null, new ClmDto.NiveauxIdentite(null, " ", "forte"), null, null, null), "admin@iq.example"));
        assertTrue(vue.personnalise());
        assertEquals("IQ", vue.prefixeReference());
        assertEquals("admin@iq.example", vue.modifiePar());
        assertEquals(T0, vue.modifieLe());
        assertEquals(new ClmDto.NiveauxIdentite("COURRIEL", "COURRIEL", "FORTE"), vue.identiteInterne());
        assertEquals(new ClmDto.NiveauxIdentite("COURRIEL", "CODE_SMS", "FORTE"), vue.identiteExterne(), "les externes sont inchangés");
        assertEquals(14, vue.invitationValiditeJours());
        verify(journal).configuration(eq("admin@iq.example"), eq("PARAMETRES"), contains("préfixe IQ"));

        ParametresClm enregistres = new ParametresClm(IQ);
        enregistres.appliquer(service.plateforme(), "admin@iq.example", T0);
        when(parametres.findByTenantId(IQ)).thenReturn(Optional.of(enregistres));
        ClmDto.ParametresModuleRequest complet = new ClmDto.ParametresModuleRequest(2L * ParametresClm.UN_MO, 10, "https://iq.example/p",
                "IQ", 4, 1, new ClmDto.NiveauxIdentite("CODE_SMS", "CODE_SMS", "FORTE"), new ClmDto.NiveauxIdentite("COURRIEL", "CODE_SMS", "FORTE"),
                4, 20, "montantAide");
        ClmDto.ParametresModule tout = portee.executerDans(IQ, () -> service.modifier(complet, "admin@iq.example"));
        assertEquals(4, tout.tentativesIdentite());
        assertEquals("montantAide", tout.champMontant());
        assertEquals("https://iq.example/p", tout.portailUrl());
        assertEquals(10, tout.invitationValiditeJours());
        assertTrue(portee.executerDans(IQ, service::publics).identiteExterne().faible().equals("CODE_SMS"));
        portee.executerDans(IQ, () -> service.retablir("admin@iq.example"));
        verify(parametres).delete(enregistres);
        verify(journal).configuration("admin@iq.example", "PARAMETRES", "paramètres de la plateforme rétablis");
    }

    @Test
    void une_modification_hors_bornes_ou_un_niveau_inconnu_est_refusee() {
        ParametresClmService service = parametres("PROD");
        when(parametres.findByTenantId(IQ)).thenReturn(Optional.empty());
        IllegalArgumentException http = assertThrows(IllegalArgumentException.class, () -> portee.executerDans(IQ,
                () -> service.modifier(new ClmDto.ParametresModuleRequest(null, null, "http://iq.example/p", null, null, null, null, null,
                        null, null, null), "a")));
        assertTrue(http.getMessage().contains("https requis en production"), http.getMessage());
        assertTrue(portee.executerDans(IQ, () -> service.modifier(new ClmDto.ParametresModuleRequest(null, null, "HTTPS://iq.example/p",
                null, null, null, null, null, null, null, null), "a")).personnalise(), "https accepté en production");
        IllegalArgumentException niveau = assertThrows(IllegalArgumentException.class, () -> portee.executerDans(IQ,
                () -> service.modifier(new ClmDto.ParametresModuleRequest(null, null, null, null, null, null,
                        new ClmDto.NiveauxIdentite("RETINE", null, null), null, null, null, null), "a")));
        assertEquals("Niveau d'identité inconnu : RETINE.", niveau.getMessage());
        assertThrows(IllegalArgumentException.class, () -> portee.executerDans(IQ, () -> service.modifier(
                new ClmDto.ParametresModuleRequest(null, 0, null, null, null, null, null, null, null, null, null), "a")));
    }

    // --- Connexion Salesforce ---

    private final ConnexionSalesforceRepository connexions = mock(ConnexionSalesforceRepository.class);

    private ConnexionsSalesforce connexions(ClmProprietes p) {
        when(connexions.save(any())).thenAnswer(a -> a.getArgument(0));
        return new ConnexionsSalesforce(connexions, p, chiffrement, portee, journal, ctx);
    }

    private static ClmDto.ConnexionRequest requete(String mode, String secret, Integer reprises) {
        return new ClmDto.ConnexionRequest(mode, "https://iq.my.salesforce.com", "https://login.salesforce.com/services/oauth2/token",
                "3MVG9-iq-client", secret, "FundingRequest", "v61.0", "Etat__c", "No__c", "Trace__c", reprises);
    }

    @Test
    void hors_organisation_ou_sans_connexion_propre_la_plateforme_s_applique() {
        ConnexionsSalesforce service = connexions(proprietes("DEV", "rest", "secret-plateforme"));
        ConnexionsSalesforce.Connexion hors = service.courante();
        assertNull(hors.proprietaire());
        assertEquals(ConnexionSalesforce.Mode.REST, hors.mode());
        assertEquals("secret-plateforme", hors.clientSecret());
        assertEquals(ConnexionsSalesforce.CHAMPS_PAR_DEFAUT, hors.champs());
        assertFalse(hors.toString().contains("secret-plateforme"), "jamais le secret dans une trace");
        assertTrue(hors.toString().contains("plateforme"));
        when(connexions.findByTenantId(IQ)).thenReturn(Optional.empty());
        assertNull(portee.executerDans(IQ, service::courante).proprietaire());
        ClmDto.ConfigIntegration vue = portee.executerDans(IQ, service::configuration);
        assertFalse(vue.personnalisee());
        assertTrue(vue.secretConfigure());
        assertEquals("clie…me", vue.clientIdMasque());
        assertEquals(ConnexionSalesforce.Mode.SIMULATION, connexions(proprietes("DEV", "autre", " ")).courante().mode(),
                "un mode inconnu du déploiement retombe sur la simulation");
        assertFalse(portee.executerDans(IQ, connexions(proprietes("DEV", "simulation", null))::configuration).secretConfigure());
        assertFalse(portee.executerDans(IQ, connexions(proprietes("DEV", "simulation", " "))::configuration).secretConfigure());
    }

    @Test
    void la_connexion_propre_chiffre_son_secret_et_le_conserve_sans_nouvelle_saisie() {
        ConnexionsSalesforce service = connexions(proprietes("PROD", "rest", null));
        when(connexions.findByTenantId(IQ)).thenReturn(Optional.empty());
        ClmDto.ConfigIntegration vue = portee.executerDans(IQ, () -> service.modifier(requete(" rest ", " s3cr3t ", null), "admin@iq.example"));
        ArgumentCaptor<ConnexionSalesforce> enregistree = ArgumentCaptor.forClass(ConnexionSalesforce.class);
        verify(connexions).save(enregistree.capture());
        ConnexionSalesforce c = enregistree.getValue();
        assertEquals("s3cr3t", chiffrement.dechiffrer(c.getSecretChiffre()), "chiffré, puis rendu au seul adaptateur");
        assertEquals(8, c.reglages().reprisesMax(), "reprises de la plateforme par défaut");
        verify(journal).configuration(eq("admin@iq.example"), eq("SALESFORCE"), contains("secret remplacé"));

        when(connexions.findByTenantId(IQ)).thenReturn(Optional.of(c));
        ConnexionsSalesforce.Connexion propre = portee.executerDans(IQ, service::courante);
        assertEquals(IQ, propre.proprietaire());
        assertEquals("s3cr3t", propre.clientSecret());
        assertTrue(propre.toString().contains("organisation 7"));
        ClmDto.ConfigIntegration conservee = portee.executerDans(IQ, () -> service.modifier(requete("REST", null, 5), "admin@iq.example"));
        assertTrue(conservee.secretConfigure() && conservee.personnalisee());
        assertEquals(5, conservee.reprisesMax());
        ClmDto.ConnexionRequest sansClient = new ClmDto.ConnexionRequest("REST", "https://iq.my.salesforce.com",
                "https://login.salesforce.com/services/oauth2/token", " ", null, "FundingRequest", "v61.0", "Etat__c", "No__c", "Trace__c", 5);
        assertEquals("3MVG…nt", portee.executerDans(IQ, () -> service.modifier(sansClient, "admin@iq.example")).clientIdMasque(),
                "un identifiant client laissé vide est conservé : l'interface ne le connaît que masqué");
        assertTrue(vue.hotesAutorises().contains("salesforce.com"));

        IllegalArgumentException simulation = assertThrows(IllegalArgumentException.class,
                () -> portee.executerDans(IQ, () -> service.modifier(requete("SIMULATION", null, 3), "a")));
        assertTrue(simulation.getMessage().contains("interdit en production"));
        assertThrows(IllegalArgumentException.class, () -> portee.executerDans(IQ, () -> service.modifier(requete("FAX", null, 3), "a")));
        assertThrows(IllegalArgumentException.class, () -> portee.executerDans(IQ, () -> service.modifier(requete(null, null, 3), "a")));

        portee.executerDans(IQ, () -> service.retablir("admin@iq.example"));
        verify(connexions).delete(c);
        verify(journal).configuration("admin@iq.example", "SALESFORCE", "connexion de la plateforme rétablie");
    }

    @Test
    void une_connexion_propre_sans_secret_n_en_transmet_aucun() {
        ConnexionsSalesforce service = connexions(proprietes("DEV", "simulation", null));
        ConnexionSalesforce simulation = new ConnexionSalesforce(IQ);
        simulation.appliquer(new ConnexionSalesforce.Reglages(ConnexionSalesforce.Mode.SIMULATION, null, null, null, "FundingRequest",
                "v60.0", ConnexionsSalesforce.CHAMPS_PAR_DEFAUT, 2), null, "a", T0);
        when(connexions.findByTenantId(IQ)).thenReturn(Optional.of(simulation));
        assertNull(portee.executerDans(IQ, service::courante).clientSecret());
        assertNull(portee.executerDans(IQ, service::configuration).clientIdMasque());
        when(connexions.findByTenantId(IQ)).thenReturn(Optional.empty());
        portee.executerDans(IQ, () -> service.retablir("a"));
        verify(connexions, never()).delete(any());
    }

    @Test
    void un_identifiant_client_n_est_jamais_affiche_en_clair() {
        assertNull(ConnexionsSalesforce.masque(null));
        assertNull(ConnexionsSalesforce.masque(" "));
        assertEquals("••••", ConnexionsSalesforce.masque("abcdef"));
        assertEquals("3MVG…nt", ConnexionsSalesforce.masque("3MVG9-iq-client"));
        assertEquals(ConnexionSalesforce.Mode.SIMULATION, ConnexionsSalesforce.mode(" Simulation "));
        assertNull(ConnexionsSalesforce.mode(null));
    }

    // --- Listes de référence ---

    @Test
    void une_saisie_suit_la_liste_de_l_organisation_et_reste_libre_sans_liste() {
        ReferencesClm references = mock(ReferencesClm.class);
        ListesClm listes = new ListesClm(references);
        when(references.valeurs("CLM_UNITE")).thenReturn(List.of());
        when(references.valeurs("CLM_ROLE_CONTRAT")).thenReturn(List.of(new ReferencesClm.Valeur("SIGNATAIRE_1", "Premier signataire", null)));
        when(references.valeurs("CLM_CLAUSE_ATTENDUE")).thenReturn(List.of(new ReferencesClm.Valeur("LOI", "Loi applicable", "Lois du Québec")));
        assertNull(listes.exiger(ListesClm.Liste.ROLE_CONTRAT, null));
        assertNull(listes.exiger(ListesClm.Liste.ROLE_CONTRAT, " "));
        assertEquals("Ventes", listes.exiger(ListesClm.Liste.UNITE, " Ventes "));
        assertEquals("SIGNATAIRE_1", listes.exiger(ListesClm.Liste.ROLE_CONTRAT, "signataire_1"));
        IllegalArgumentException hors = assertThrows(IllegalArgumentException.class, () -> listes.exiger(ListesClm.Liste.ROLE_CONTRAT, "PARRAIN"));
        assertEquals("« PARRAIN » ne figure pas dans la liste « Rôle dans le contrat » de l'organisation.", hors.getMessage());
        assertEquals(Map.of("LOI", List.of("lois du quebec")), listes.motsCles(ListesClm.Liste.CLAUSE_ATTENDUE));
        assertEquals("CLM_CHAMP_SALESFORCE", ListesClm.Liste.CHAMP_SALESFORCE.code());
    }

    @Test
    void la_politique_publique_reprend_les_niveaux_par_risque() {
        assertEquals(new ClmDto.NiveauxIdentite("COURRIEL", "CODE_SMS", "FORTE"), ParametresClmService.niveaux(Identite.Politique.DEFAUT, true));
        assertEquals(new ClmDto.NiveauxIdentite("COURRIEL", "COURRIEL", "CODE_SMS"), ParametresClmService.niveaux(Identite.Politique.DEFAUT, false));
    }
}
