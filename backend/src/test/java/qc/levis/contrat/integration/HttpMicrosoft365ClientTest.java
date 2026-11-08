package qc.levis.contrat.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.integration.application.port.Microsoft365Client;
import qc.levis.contrat.integration.application.port.PublicationCiblePort;
import qc.levis.contrat.integration.infrastructure.HttpMicrosoft365Client;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation de bout en bout du client Microsoft Graph réel contre le {@link FakeMicrosoft365Server}
 * (simulateur en mémoire). Vérifie l'acquisition du jeton, les URL/chemins par cible, l'en-tête
 * d'autorisation, le corps téléversé, le parsing de la réponse et les chemins d'échec — sans dépendre
 * d'un vrai tenant Microsoft 365.
 */
class HttpMicrosoft365ClientTest {

    private FakeMicrosoft365Server serveur;
    private HttpMicrosoft365Client client;

    private final Microsoft365Client.ConfigM365 config = new Microsoft365Client.ConfigM365(
            "tenant-1", "client-1", "secret-1", "site-1", "drive-1", "team-1", "chan-1", "exp@ville.ca");

    @BeforeEach
    void setUp() {
        serveur = new FakeMicrosoft365Server();
        serveur.demarrer();
        client = new HttpMicrosoft365Client(new ObjectMapper(), serveur.graphBaseUrl(), serveur.loginBaseUrl());
    }

    @AfterEach
    void tearDown() { serveur.arreter(); }

    private PublicationCiblePort.ContenuPublie contenu() {
        return new PublicationCiblePort.ContenuPublie(12L, "Contrat X", "contrat-12.pdf",
                "application/pdf", new byte[]{1, 2, 3, 4, 5}, "Contrats/2027");
    }

    @Test
    void tester_obtientUnJeton() {
        PublicationCiblePort.ResultatPublication r = client.tester(config);
        assertTrue(r.succes());
        assertTrue(serveur.dernierChemin.endsWith("/tenant-1/oauth2/v2.0/token"));
    }

    @Test
    void tester_jetonRefuse_echoue() {
        serveur.echecJeton = true;
        assertFalse(client.tester(config).succes());
    }

    @Test
    void deposer_sharepoint_construitLaBonneUrl_etTeleverseLesOctets() {
        PublicationCiblePort.ResultatPublication r =
                client.deposer(config, Enums.CiblePublication.SHAREPOINT, contenu());

        assertTrue(r.succes());
        assertEquals("PUT", serveur.derniereMethode);
        assertEquals("/v1.0/sites/site-1/drive/root:/Contrats/2027/contrat-12.pdf:/content", serveur.dernierChemin);
        assertEquals("Bearer FAKE_TOKEN", serveur.derniereAutorisation);
        assertEquals("application/pdf", serveur.dernierContentType);
        assertEquals(5, serveur.dernierCorps.length());       // 5 octets téléversés
        assertTrue(r.url().contains("/sites/site-1/drive/root:/Contrats/2027/contrat-12.pdf"));
    }

    @Test
    void deposer_onedrive_viseLeDrive() {
        client.deposer(config, Enums.CiblePublication.ONEDRIVE, contenu());
        assertEquals("/v1.0/drives/drive-1/root:/Contrats/2027/contrat-12.pdf:/content", serveur.dernierChemin);
    }

    @Test
    void deposer_teamsAvecCanal_deposeDansLeDossierDuCanal() {
        // config a un teamsChannelId ("chan-1") → on résout le dossier du canal puis on y dépose,
        // pour que le fichier apparaisse dans l'onglet Fichiers du canal Teams.
        PublicationCiblePort.ResultatPublication r =
                client.deposer(config, Enums.CiblePublication.TEAMS, contenu());
        assertTrue(r.succes());
        assertEquals("/v1.0/drives/CHANDRIVE/items/CHANFOLDER:/Contrats/2027/contrat-12.pdf:/content",
                serveur.dernierDepot);
        // Le lien renvoyé ouvre le canal dans Teams ; nom du canal indisponible (404) → « canal ».
        assertEquals("https://teams.microsoft.com/l/channel/chan-1/canal?groupId=team-1&tenantId=tenant-1", r.url());
    }

    @Test
    void deposer_teamsSansCanal_viseLaRacineDeLEquipe() {
        Microsoft365Client.ConfigM365 sansCanal = new Microsoft365Client.ConfigM365(
                "tenant-1", "client-1", "secret-1", "site-1", "drive-1", "team-1", null, "exp@ville.ca");
        client.deposer(sansCanal, Enums.CiblePublication.TEAMS, contenu());
        assertEquals("/v1.0/groups/team-1/drive/root:/Contrats/2027/contrat-12.pdf:/content", serveur.dernierChemin);
    }

    @Test
    void deposer_outlook_envoieUnCourriel() {
        PublicationCiblePort.ResultatPublication r =
                client.deposer(config, Enums.CiblePublication.OUTLOOK, contenu());
        assertTrue(r.succes());
        assertEquals("POST", serveur.derniereMethode);
        assertEquals("/v1.0/users/exp@ville.ca/sendMail", serveur.dernierChemin);
        assertTrue(serveur.dernierCorps.contains("fileAttachment"));
    }

    @Test
    void deposer_echecServeur_leveUneErreur() {
        serveur.echecDepot = true;
        assertThrows(IllegalStateException.class,
                () -> client.deposer(config, Enums.CiblePublication.SHAREPOINT, contenu()));
    }

    @Test
    void deposer_jetonSansAccessToken_leveUneErreur() {
        serveur.jetonSansAccessToken = true;
        assertThrows(IllegalStateException.class,
                () -> client.deposer(config, Enums.CiblePublication.SHAREPOINT, contenu()));
    }

    @Test
    void tester_reponseTokenNonJson_echoue() {
        serveur.jetonCorpsInvalide = true;
        assertFalse(client.tester(config).succes());   // JSON illisible → jeton absent → échec propre
    }

    @Test
    void tester_serveurInjoignable_echoue() {
        HttpMicrosoft365Client injoignable = new HttpMicrosoft365Client(
                new ObjectMapper(), "http://127.0.0.1:1/v1.0", "http://127.0.0.1:1");
        assertFalse(injoignable.tester(config).succes());   // erreur réseau capturée
    }
}
