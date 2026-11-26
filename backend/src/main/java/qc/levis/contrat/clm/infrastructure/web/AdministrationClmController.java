package qc.levis.contrat.clm.infrastructure.web;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import qc.levis.contrat.clm.application.AnalyseService;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.ConnexionsSalesforce;
import qc.levis.contrat.clm.application.MessagerieClm;
import qc.levis.contrat.clm.application.ParametresClmService;
import qc.levis.contrat.clm.application.SalesforceService;
import qc.levis.contrat.clm.application.SuiviService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.domain.ConnexionSalesforce;
import qc.levis.contrat.clm.infrastructure.SalesforceSimulation;

/** Administration CLM : notifications (UI-17), intégrations (UI-21), paramètres (UI-22) et option IA (UI-24). */
public final class AdministrationClmController {

    private AdministrationClmController() {
    }

    @RestController
    @RequestMapping("/api/clm/admin")
    @PreAuthorize("@modules.actif('clm') and hasAnyRole('ADMIN_AFFAIRES','ADMIN_TECHNIQUE')")
    public static class Administration {

        /** Administration d'affaires : messages et paramètres du module (addenda 4, Q10). */
        private static final String AFFAIRES = "@modules.actif('clm') and hasAnyRole('ADMIN_AFFAIRES')";
        /** Administration technique : intégrations (addenda 4, Q10). */
        private static final String TECHNIQUE = "@modules.actif('clm') and hasAnyRole('ADMIN_TECHNIQUE')";

        /** UI-23 : auditeur et administrateur ; une annotation de méthode remplace celle de la classe, d'où le module répété. */
        private static final String JOURNAL = "@modules.actif('clm') and hasAnyRole('ADMIN_AFFAIRES','AUDITEUR')";

        private final MessagerieClm messagerie;
        private final SalesforceService salesforce;
        private final SuiviService suivi;
        private final ClmProprietes proprietes;
        private final ParametresClmService parametres;
        private final ConnexionsSalesforce connexions;
        private final SalesforceSimulation simulation;
        private final long sessionMs;
        private final boolean cookieSecurise;

        public Administration(MessagerieClm messagerie, SalesforceService salesforce, SuiviService suivi, ClmProprietes proprietes,
                              ParametresClmService parametres, ConnexionsSalesforce connexions, SalesforceSimulation simulation,
                              @Value("${app.jwt.expiration-ms:3600000}") long sessionMs,
                              @Value("${app.cookie.secure:false}") boolean cookieSecurise) {
            this.messagerie = messagerie;
            this.salesforce = salesforce;
            this.suivi = suivi;
            this.proprietes = proprietes;
            this.parametres = parametres;
            this.connexions = connexions;
            this.simulation = simulation;
            this.sessionMs = sessionMs;
            this.cookieSecurise = cookieSecurise;
        }

        @GetMapping("/messages")
        @PreAuthorize(AFFAIRES)
        public List<ClmDto.MessageVue> messages() {
            return messagerie.lister();
        }

        @PutMapping("/messages")
        @PreAuthorize(AFFAIRES)
        public ClmDto.MessageVue enregistrer(@RequestBody ClmDto.MessageRequest req) {
            return messagerie.enregistrer(req);
        }

        @PostMapping("/messages/{evenement}/test")
        @PreAuthorize(AFFAIRES)
        public Map<String, String> tester(@PathVariable String evenement, @RequestBody ClmDto.TestMessageRequest req) {
            return Map.of("statut", messagerie.tester(evenement, req));
        }

        @GetMapping("/integrations/salesforce")
        @PreAuthorize(TECHNIQUE)
        public ClmDto.IntegrationVue integration() {
            return salesforce.etat();
        }

        @PostMapping("/integrations/salesforce/tester")
        @PreAuthorize(TECHNIQUE)
        public ClmDto.PortSalesforceEtat testerConnexion() {
            return salesforce.tester();
        }

        /** UI-21 : connexion propre à l'organisation ; le secret n'est jamais renvoyé. */
        @PutMapping("/integrations/salesforce/configuration")
        @PreAuthorize(TECHNIQUE)
        public ClmDto.ConfigIntegration configurer(@RequestBody ClmDto.ConnexionRequest req, Authentication auth) {
            return connexions.modifier(req, auth.getName());
        }

        @DeleteMapping("/integrations/salesforce/configuration")
        @PreAuthorize(TECHNIQUE)
        public ClmDto.ConfigIntegration retablirConnexion(Authentication auth) {
            return connexions.retablir(auth.getName());
        }

        @PostMapping("/integrations/salesforce/evenements/{id}/relancer")
        @PreAuthorize(TECHNIQUE)
        public ClmDto.SortieVue relancer(@PathVariable Long id) {
            return salesforce.relancer(id);
        }

        @PostMapping("/integrations/salesforce/relayer")
        @PreAuthorize(TECHNIQUE)
        public Map<String, Integer> relayer() {
            return Map.of("envoyes", salesforce.relayer());
        }

        @PostMapping("/integrations/salesforce/trousses/{id}/relancer")
        @PreAuthorize(TECHNIQUE)
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

        /** La panne simulée n'a de sens que pour une organisation reliée à la simulation. */
        @PutMapping("/integrations/salesforce/simulation/panne")
        @PreAuthorize(TECHNIQUE)
        public ClmDto.PortSalesforceEtat panne(@RequestBody Map<String, Boolean> corps) {
            if (connexions.courante().mode() != ConnexionSalesforce.Mode.SIMULATION) {
                throw new NoSuchElementException("Mode simulation inactif.");
            }
            simulation.simulerPanne(Boolean.TRUE.equals(corps.get("panne")));
            return salesforce.tester();
        }

        /** UI-22 : réglages de la plateforme en lecture seule et paramètres du module de l'organisation. */
        @GetMapping("/parametres")
        @PreAuthorize(AFFAIRES)
        public ClmDto.ParametresVue parametres() {
            return new ClmDto.ParametresVue(proprietes.environnement(), connexions.configuration().mode(),
                    proprietes.planificateur().actif(), proprietes.planificateur().delaiMs(), ParametresClmService.formats(),
                    sessionMs / 60_000, cookieSecurise,
                    Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime()), parametres.lire());
        }

        @PutMapping("/parametres")
        @PreAuthorize(AFFAIRES)
        public ClmDto.ParametresModule modifierParametres(@RequestBody ClmDto.ParametresModuleRequest req, Authentication auth) {
            return parametres.modifier(req, auth.getName());
        }

        @DeleteMapping("/parametres")
        @PreAuthorize(AFFAIRES)
        public ClmDto.ParametresModule retablirParametres(Authentication auth) {
            return parametres.retablir(auth.getName());
        }
    }

    /** Paramètres du module dont tout utilisateur a besoin (création de trousse, participants, dépôt). */
    @RestController
    @RequestMapping("/api/clm")
    @PreAuthorize("@modules.actif('clm')")
    public static class Consultation {

        private final ParametresClmService parametres;

        public Consultation(ParametresClmService parametres) {
            this.parametres = parametres;
        }

        @GetMapping("/parametres")
        public ClmDto.ParametresPublics parametres() {
            return parametres.publics();
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
