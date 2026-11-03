package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.io.ByteArrayOutputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.port.AlertesExploitation;
import qc.levis.contrat.clm.application.port.FournisseurSignature;
import qc.levis.contrat.clm.application.port.IdentiteOrganisation;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Identite;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.tenant.application.TenantAdminService;
import qc.levis.contrat.tenant.application.dto.TenantPublic;
import qc.levis.contrat.tenant.application.dto.Theme;

/** Adaptateurs CLM : scellement, attestation, aléa, antivirus, Salesforce simulé, rendu PDF/Word. */
class AdaptateursClmTest {

    private static final Instant T0 = Instant.parse("2026-11-02T10:00:00Z");
    private final ClmProprietes proprietes = new ClmProprietes(null, null, null, null, new ClmProprietes.Preuve("cle-test"), null, null);
    private final AdaptateursClm.ScelleurHmac scelleur = new AdaptateursClm.ScelleurHmac(proprietes);

    @Test
    void une_cle_de_preuve_vide_bloque_le_scellement_au_lieu_de_produire_un_sceau_faible() {
        AdaptateursClm.ScelleurHmac sansCle = new AdaptateursClm.ScelleurHmac(
                new ClmProprietes(null, null, null, null, new ClmProprietes.Preuve(""), null, null));
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> sansCle.sceller("manifeste".getBytes()));
        assertEquals("Scellement impossible", e.getMessage());
    }

    @Test
    void l_organisation_presentee_au_signataire_vient_du_theme_de_son_tenant() {
        TenantAdminService tenants = mock(TenantAdminService.class);
        when(tenants.publicParId(1L)).thenReturn(Optional.of(new TenantPublic("levis", "Ville de Lévis", null,
                new Theme(null, null, null, null, "Lévis", "data:image/png;base64,AAAA"))));
        when(tenants.publicParId(9L)).thenReturn(Optional.empty());
        AdaptateursClm.IdentiteTenant identite = new AdaptateursClm.IdentiteTenant(tenants);

        assertEquals(new IdentiteOrganisation.Organisation("Ville de Lévis", "data:image/png;base64,AAAA"), identite.de(1L));
        assertEquals(new IdentiteOrganisation.Organisation(null, null), identite.de(9L), "tenant inactif ou inconnu : identité neutre");
    }

    @Test
    void une_alerte_s_ecrit_sur_une_ligne_au_format_releve_par_la_supervision() {
        Logger journal = (Logger) LoggerFactory.getLogger("qc.levis.contrat.clm.ALERTES");
        ListAppender<ILoggingEvent> lignes = new ListAppender<>();
        lignes.start();
        journal.addAppender(lignes);
        try {
            AdaptateursClm.AlertesJournal alertes = new AdaptateursClm.AlertesJournal();
            alertes.signaler(AlertesExploitation.Code.INTEGRITE_ALTEREE, 3L, "TR-2026-00001", "1 document\r\nCLM_ALERTE forgée");
            alertes.signaler(AlertesExploitation.Code.PLANIFICATEUR_ECHEC, null, " ", null);
        } finally {
            journal.detachAppender(lignes);
        }
        assertEquals(List.of(
                "CLM_ALERTE code=INTEGRITE_ALTEREE tenant=3 ressource=TR-2026-00001 detail=1 document CLM_ALERTE forgée",
                "CLM_ALERTE code=PLANIFICATEUR_ECHEC tenant=- ressource=- detail=-"),
                lignes.list.stream().map(ILoggingEvent::getFormattedMessage).toList(), "un retour chariot ne forge pas une seconde ligne");
        assertEquals(Level.ERROR, lignes.list.get(0).getLevel());
    }

    @Test
    void scelleur_et_attestation_detectent_toute_modification() {
        String sceau = scelleur.sceller("manifeste".getBytes());
        assertTrue(scelleur.verifier("manifeste".getBytes(), sceau));
        assertFalse(scelleur.verifier("manifestE".getBytes(), sceau));
        assertFalse(scelleur.verifier("manifeste".getBytes(), null));

        SignatureProbanteLocale signature = new SignatureProbanteLocale(scelleur);
        FournisseurSignature.Demande d = new FournisseurSignature.Demande("abc", "Marie", "m@x.ca", Identite.Niveau.CODE_SMS, T0, "1.2.3.4");
        FournisseurSignature.Attestation a = signature.signer(d);
        assertEquals(T0, a.horodatage());
        assertTrue(a.identifiant().startsWith("LOC-"));
        assertTrue(signature.verifier(d, a));
        assertFalse(signature.verifier(new FournisseurSignature.Demande("abd", "Marie", "m@x.ca", Identite.Niveau.CODE_SMS, T0, null), a));
        assertFalse(signature.verifier(d, new FournisseurSignature.Attestation("autre", "x", T0, a.jetonHorodatage(), "x")));
        assertFalse(signature.verifier(d, null));
    }

    @Test
    void alea_antivirus_et_notificateur() {
        AdaptateursClm.SourceAleatoireSecurisee alea = new AdaptateursClm.SourceAleatoireSecurisee();
        assertEquals(43, alea.jeton().length());
        assertNotEquals(alea.jeton(), alea.jeton());
        assertTrue(alea.code().matches("\\d{6}"));
        assertEquals(36, alea.uuid().length());

        AdaptateursClm.AntivirusSignatures av = new AdaptateursClm.AntivirusSignatures();
        assertTrue(av.estSain("%PDF-1.7 contenu".getBytes()));
        assertFalse(av.estSain("X5O!P%@AP EICAR-STANDARD-ANTIVIRUS-TEST-FILE!".getBytes()));
        assertTrue(av.estSain(new byte[0]));

        qc.levis.contrat.notification.application.port.CanalNotification sms = new qc.levis.contrat.notification.application.port.CanalNotification() {
            public String canal() { return "SMS"; }
            public String envoyer(String d, String s, String c) { return "ENVOYE"; }
        };
        AdaptateursClm.NotificateurCanaux n = new AdaptateursClm.NotificateurCanaux(List.of(sms));
        assertEquals("ENVOYE", n.envoyer("SMS", "+15145550101", "s", "c"));
        assertEquals("ECHEC", n.envoyer("COURRIEL", "a@b.ca", "s", "c"));
        assertEquals("ECHEC", n.envoyer("SMS", " ", "s", "c"));
    }

    @Test
    void salesforce_simule_lecture_publication_idempotente_et_panne() {
        SalesforceSimulation sf = new SalesforceSimulation(Clock.fixed(T0, ZoneOffset.UTC));
        PortSalesforce.Dossier d = sf.lireDossier("dem-2026-0001").orElseThrow();
        assertEquals("a0B5e00000GRT001", d.id());
        assertEquals("250000.00", d.champs().get("Montant"));
        assertTrue(sf.lireDossier("a0B5e00000GRT002").isPresent());
        assertTrue(sf.lireDossier("inconnu").isEmpty());

        PortSalesforce.Statut s = new PortSalesforce.Statut(d.id(), "LANCEE", "c", "k1", Map.of());
        sf.publierStatut(s);
        sf.publierStatut(s);
        assertEquals(1, sf.statutsRecus().size(), "une même clé n'est publiée qu'une fois");
        assertThrows(IllegalArgumentException.class, () -> sf.publierStatut(new PortSalesforce.Statut("x", "E", "c", "k2", Map.of())));

        sf.simulerPanne(true);
        assertFalse(sf.tester().disponible());
        assertThrows(PortSalesforce.Indisponible.class, () -> sf.lireDossier("DEM-2026-0001"));
        sf.simulerPanne(false);
        assertTrue(sf.tester().disponible());
    }

    @Test
    void rendu_genere_lit_et_appose() throws Exception {
        RenduDocumentsPdf rendu = new RenduDocumentsPdf();
        byte[] pdf = rendu.genererPdf("Convention", "Montant : 250 000 $\n\nGarantie");
        assertEquals(FormatDocument.Type.PDF, FormatDocument.verifier("c.pdf", pdf, Long.MAX_VALUE));
        assertTrue(rendu.texte(FormatDocument.Type.PDF, pdf).contains("Garantie"));

        byte[] signe = rendu.apposer(pdf, List.of(
                new RenduDocuments.Apposition("Marie → Tremblay", T0.toString(), "TR-1", new Modele.Position(1, 10, 70, 40, 10)),
                new RenduDocuments.Apposition("Jean", T0.toString(), "TR-1", null)));
        try (PDDocument doc = PDDocument.load(signe)) {
            assertEquals(2, doc.getNumberOfPages(), "page de signatures ajoutée pour l'apposition sans zone");
        }
        assertTrue(rendu.texte(FormatDocument.Type.PDF, signe).contains("Marie ? Tremblay"));
        assertEquals("é?€", RenduDocumentsPdf.winAnsi("é→€"));
        assertThrows(IllegalStateException.class, () -> rendu.texte(FormatDocument.Type.PDF, "pas un pdf".getBytes()));
        assertThrows(IllegalStateException.class, () -> rendu.apposer("pas un pdf".getBytes(), List.of()));

        try (PDDocument vide = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            vide.addPage(new PDPage());
            vide.save(out);
            byte[] horsPage = rendu.apposer(out.toByteArray(), List.of(new RenduDocuments.Apposition("A", "t", "R", new Modele.Position(5, 0, 0, 10, 10))));
            try (PDDocument doc = PDDocument.load(horsPage)) {
                assertEquals(2, doc.getNumberOfPages(), "une zone sur une page absente bascule en page de signatures");
            }
        }

        try (org.apache.poi.xwpf.usermodel.XWPFDocument docx = new org.apache.poi.xwpf.usermodel.XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            docx.createParagraph().createRun().setText("Annexe Word");
            docx.write(out);
            assertTrue(rendu.texte(FormatDocument.Type.DOCX, out.toByteArray()).contains("Annexe Word"));
        }
    }
}
