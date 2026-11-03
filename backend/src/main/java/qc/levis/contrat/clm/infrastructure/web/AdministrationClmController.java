package qc.levis.contrat.clm.infrastructure.web;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.AnalyseService;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.MessagerieClm;
import qc.levis.contrat.clm.application.SalesforceService;
import qc.levis.contrat.clm.application.SuiviService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.infrastructure.SalesforceSimulation;

/** Administration CLM : notifications (UI-17), intégrations (UI-21), paramètres (UI-22) et option IA (UI-24). */
public final class AdministrationClmController {

    private AdministrationClmController() {
    }

    @RestController
    @RequestMapping("/api/clm/admin")
    @PreAuthorize("@modules.actif('clm') and hasAnyRole('ADMIN','SUPERADMIN')")
    public static class Administration {

        /** UI-23 : auditeur et administrateur ; une annotation de méthode remplace celle de la classe, d'où le module répété. */
        private static final String JOURNAL = "@modules.actif('clm') and hasAnyRole('ADMIN','SUPERADMIN','AUDITEUR')";

        private final MessagerieClm messagerie;
        private final SalesforceService salesforce;
        private final SuiviService suivi;
        private final ClmProprietes proprietes;
        private final Optional<SalesforceSimulation> simulation;
        private final long sessionMs;
        private final boolean cookieSecurise;

        public Administration(MessagerieClm messagerie, SalesforceService salesforce, SuiviService suivi, ClmProprietes proprietes,
                              Optional<SalesforceSimulation> simulation, @Value("${app.jwt.expiration-ms:3600000}") long sessionMs,
                              @Value("${app.cookie.secure:false}") boolean cookieSecurise) {
            this.messagerie = messagerie;
            this.salesforce = salesforce;
            this.suivi = suivi;
            this.proprietes = proprietes;
            this.simulation = simulation;
            this.sessionMs = sessionMs;
            this.cookieSecurise = cookieSecurise;
        }

        @GetMapping("/messages")
        public List<ClmDto.MessageVue> messages() {
            return messagerie.lister();
        }

        @PutMapping("/messages")
        public ClmDto.MessageVue enregistrer(@RequestBody ClmDto.MessageRequest req) {
            return messagerie.enregistrer(req);
        }

        @PostMapping("/messages/{evenement}/test")
        public Map<String, String> tester(@PathVariable String evenement, @RequestBody ClmDto.TestMessageRequest req) {
            return Map.of("statut", messagerie.tester(evenement, req));
        }

        @GetMapping("/integrations/salesforce")
        public ClmDto.IntegrationVue integration() {
            return salesforce.etat();
        }

        @PostMapping("/integrations/salesforce/tester")
        public ClmDto.PortSalesforceEtat testerConnexion() {
            return salesforce.tester();
        }

        @PostMapping("/integrations/salesforce/evenements/{id}/relancer")
        public ClmDto.SortieVue relancer(@PathVariable Long id) {
            return salesforce.relancer(id);
        }

        @PostMapping("/integrations/salesforce/relayer")
        public Map<String, Integer> relayer() {
            return Map.of("envoyes", salesforce.relayer());
        }

        @PostMapping("/integrations/salesforce/trousses/{id}/relancer")
        public Map<String, Integer> relancerTrousse(@PathVariable Long id) {
            return Map.of("relances", salesforce.relancerTrousse(id));
        }

        @GetMapping("/journal")
        @PreAuthorize(JOURNAL)
        public List<ClmDto.JournalLigne> journal(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
                                                 @RequestParam(required = false) String acteur, @RequestParam(required = false) String action,
                                                 @RequestParam(required = false) String trousse, @RequestParam(required = false) String texte) {
            return suivi.journal(new SuiviService.FiltreJournal(du, au, acteur, action, trousse, texte));
        }

        @GetMapping("/journal/export")
        @PreAuthorize(JOURNAL)
        public ResponseEntity<byte[]> journalCsv(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
                                                 @RequestParam(required = false) String acteur, @RequestParam(required = false) String action,
                                                 @RequestParam(required = false) String trousse, @RequestParam(required = false) String texte) {
            String csv = suivi.journalCsv(new SuiviService.FiltreJournal(du, au, acteur, action, trousse, texte));
            return TrousseController.fichier(("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8), "text/csv;charset=UTF-8", "journal-clm.csv");
        }

        @PutMapping("/integrations/salesforce/simulation/panne")
        public ClmDto.PortSalesforceEtat panne(@RequestBody Map<String, Boolean> corps) {
            SalesforceSimulation sim = simulation.orElseThrow(() -> new NoSuchElementException("Mode simulation inactif."));
            sim.simulerPanne(Boolean.TRUE.equals(corps.get("panne")));
            return salesforce.tester();
        }

        /** Lecture seule : en production, la configuration se change par variables d'environnement et déploiement. */
        @GetMapping("/parametres")
        public ClmDto.ParametresVue parametres() {
            return new ClmDto.ParametresVue(proprietes.environnement(), proprietes.documents().tailleMaxOctets(),
                    proprietes.invitation().validiteJours(), proprietes.salesforce().mode(), proprietes.planificateur().actif(),
                    proprietes.planificateur().delaiMs(), !"PROD".equalsIgnoreCase(proprietes.environnement()),
                    java.util.Arrays.stream(FormatDocument.Type.values()).map(FormatDocument.Type::mime).toList(),
                    sessionMs / 60_000, cookieSecurise, Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()));
        }
    }

    @RestController
    @RequestMapping("/api/clm")
    @PreAuthorize("@modules.actif('clm_ia')")
    public static class OptionIa {

        private final AnalyseService analyses;
        private final WebClm.Acteurs acteurs;

        public OptionIa(AnalyseService analyses, WebClm.Acteurs acteurs) {
            this.analyses = analyses;
            this.acteurs = acteurs;
        }

        @PostMapping("/trousses/{id}/documents/{documentId}/analyse")
        public ClmDto.AnalyseVue lancer(@PathVariable Long id, @PathVariable Long documentId, Authentication auth) {
            return analyses.lancer(id, documentId, acteurs.de(auth));
        }

        @GetMapping("/trousses/{id}/analyses")
        public List<ClmDto.AnalyseVue> lister(@PathVariable Long id, Authentication auth) {
            return analyses.lister(id, acteurs.de(auth));
        }

        @GetMapping("/analyses")
        public List<ClmDto.AnalyseVue> recentes(Authentication auth) {
            return analyses.recentes(acteurs.de(auth));
        }

        @PostMapping("/analyses/{id}/revision")
        public ClmDto.AnalyseVue reviser(@PathVariable Long id, @RequestBody ClmDto.RevisionRequest req, Authentication auth) {
            return analyses.reviser(id, req, acteurs.de(auth));
        }
    }
}
