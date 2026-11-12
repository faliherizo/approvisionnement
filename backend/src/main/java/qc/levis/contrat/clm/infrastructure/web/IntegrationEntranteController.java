package qc.levis.contrat.clm.infrastructure.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import qc.levis.contrat.clm.application.ClientsIntegrationService;
import qc.levis.contrat.clm.application.DemandesSignatureService;
import qc.levis.contrat.clm.application.DocumentClmService;
import qc.levis.contrat.clm.application.ModeleService;
import qc.levis.contrat.clm.application.PreuveService;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.domain.ClientIntegration;
import qc.levis.contrat.clm.domain.DocumentTrousse;
import qc.levis.contrat.commun.module.application.ModuleService;

/**
 * API d'intégration entrante du module (addenda 1) : un système autorisé — Workday (TI-22), Salesforce Grantmaking
 * (AF-47) — obtient un jeton OAuth 2.0 (client credentials), crée des trousses ou des lots, lit leur statut et récupère
 * le document signé et le dossier de preuve. Aucune session utilisateur : le jeton désigne l'organisation.
 */
@RestController
@RequestMapping("/api/clm/integration")
public class IntegrationEntranteController {

    private final ClientsIntegrationService clients;
    private final DemandesSignatureService demandes;
    private final DocumentClmService documents;
    private final PreuveService preuves;
    private final ModuleService modules;
    private final ModeleService modeles;

    public IntegrationEntranteController(ClientsIntegrationService clients, DemandesSignatureService demandes,
                                         DocumentClmService documents, PreuveService preuves, ModuleService modules, ModeleService modeles) {
        this.clients = clients;
        this.demandes = demandes;
        this.documents = documents;
        this.preuves = preuves;
        this.modules = modules;
        this.modeles = modeles;
    }

    /** Modèles publiés et leurs champs (DAO § 3.3.5) : le système externe y choisit le {@code modeleId} et les clés de valeurs. */
    @GetMapping("/v1/modeles")
    public java.util.List<ClmDto.ModeleVue> modeles(@RequestHeader(value = "Authorization", required = false) String authz) {
        ClientIntegration c = client(authz);
        return demandes.dansOrganisation(c, () -> modeles.lister(demandes.acteurCatalogue(c), "PUBLIE"));
    }

    @PostMapping(value = "/oauth/token", consumes = "application/x-www-form-urlencoded")
    public ClmDto.JetonAcces jeton(@RequestParam("grant_type") String type, @RequestParam("client_id") String clientId,
                                   @RequestParam("client_secret") String secret) {
        if (!"client_credentials".equals(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported_grant_type");
        }
        return clients.emettreJeton(clientId, secret);
    }

    @PostMapping("/v1/trousses")
    @ResponseStatus(HttpStatus.CREATED)
    public ClmDto.TrousseExterneVue creer(@RequestHeader(value = "Authorization", required = false) String authz,
                                          @RequestBody ClmDto.DemandeTrousse demande) {
        return demandes.creerPourClient(client(authz), demande);
    }

    @GetMapping("/v1/trousses/{id}")
    public ClmDto.TrousseExterneVue trousse(@RequestHeader(value = "Authorization", required = false) String authz, @PathVariable Long id) {
        return demandes.trousseDuClient(client(authz), id);
    }

    /** Document signé (ou toute version) d'une trousse créée par ce client, pour dépôt dans le système appelant. */
    @GetMapping("/v1/trousses/{id}/documents/{documentId}/contenu")
    public ResponseEntity<byte[]> document(@RequestHeader(value = "Authorization", required = false) String authz,
                                           @PathVariable Long id, @PathVariable Long documentId) {
        ClientIntegration c = client(authz);
        DocumentTrousse d = demandes.dansOrganisation(c, () -> documents.telecharger(id, documentId, demandes.acteurDuClient(c, id)));
        return TrousseController.fichier(d.getContenu(), d.getType().mime(), d.getNom());
    }

    @GetMapping("/v1/trousses/{id}/preuve")
    public ResponseEntity<byte[]> preuve(@RequestHeader(value = "Authorization", required = false) String authz, @PathVariable Long id) {
        ClientIntegration c = client(authz);
        byte[] zip = demandes.dansOrganisation(c, () -> preuves.exporter(id, demandes.acteurDuClient(c, id)));
        return TrousseController.fichier(zip, "application/zip", "preuve-trousse-" + id + ".zip");
    }

    @PostMapping("/v1/lots")
    @ResponseStatus(HttpStatus.CREATED)
    public ClmDto.LotVue lot(@RequestHeader(value = "Authorization", required = false) String authz, @RequestBody ClmDto.DemandeLot lot) {
        return demandes.creerLotPourClient(client(authz), lot);
    }

    @GetMapping("/v1/lots/{id}")
    public ClmDto.LotVue lireLot(@RequestHeader(value = "Authorization", required = false) String authz, @PathVariable Long id) {
        return demandes.lotDuClient(client(authz), id);
    }

    @ExceptionHandler(ClientsIntegrationService.AccesRefuse.class)
    public ResponseEntity<Map<String, String>> refuse(ClientsIntegrationService.AccesRefuse e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("WWW-Authenticate", "Bearer")
                .body(Map.of("error", "invalid_client", "message", e.getMessage()));
    }

    /** Jeton valide d'un client actif, dont l'organisation a le module souscrit. */
    private ClientIntegration client(String authz) {
        String jeton = authz != null && authz.startsWith("Bearer ") ? authz.substring(7) : null;
        ClientIntegration c = clients.authentifier(jeton);
        if (!modules.estActif(c.getTenantId(), "clm")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Module Contrats et signature non souscrit.");
        }
        return c;
    }
}
