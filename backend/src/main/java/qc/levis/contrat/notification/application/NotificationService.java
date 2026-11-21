package qc.levis.contrat.notification.application;

import java.util.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.module.application.ModuleService;
import qc.levis.contrat.notification.application.port.CanalNotification;
import qc.levis.contrat.notification.application.repository.*;
import qc.levis.contrat.notification.domain.NotificationEnvoi;
import qc.levis.contrat.notification.domain.NotificationModele;
import qc.levis.contrat.notification.domain.NotificationRegle;
import qc.levis.contrat.saisie.application.FicheEvent;

/**
 * Module de notification. Réagit aux événements de fiche (via {@link FicheEvent}) si le module est actif
 * pour le tenant, applique les règles de notification configurées, fusionne le gabarit et envoie par le
 * canal approprié (adaptateurs en simulation par défaut). Journalise chaque envoi.
 */
@Service
public class NotificationService {

    private final NotificationModeleRepository modeles;
    private final NotificationRegleRepository regles;
    private final NotificationEnvoiRepository journal;
    private final ModuleService modules;
    private final qc.levis.contrat.notification.application.port.NumerosFiches numeros;
    private final Map<String, CanalNotification> canaux = new HashMap<>();

    public NotificationService(NotificationModeleRepository modeles, NotificationRegleRepository regles,
                               NotificationEnvoiRepository journal, ModuleService modules,
                               List<CanalNotification> canauxDisponibles) {
        this(modeles, regles, journal, modules, canauxDisponibles, (tenant, fiche) -> Optional.empty());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public NotificationService(NotificationModeleRepository modeles, NotificationRegleRepository regles,
                               NotificationEnvoiRepository journal, ModuleService modules,
                               List<CanalNotification> canauxDisponibles,
                               qc.levis.contrat.notification.application.port.NumerosFiches numeros) {
        this.numeros = numeros;
        this.modeles = modeles;
        this.regles = regles;
        this.journal = journal;
        this.modules = modules;
        for (CanalNotification c : canauxDisponibles) canaux.put(c.canal(), c);
    }

    @EventListener
    @Transactional
    public void surFiche(FicheEvent e) {
        if (!modules.estActif(e.tenantId(), "notification")) return;
        Map<String, Object> valeurs = valeursFusionnables(e);
        for (NotificationRegle r : regles.findByTenantIdAndFormulaireIdAndDeclencheurAndActifTrue(
                e.tenantId(), e.formulaireId(), e.declencheur())) {
            NotificationModele m = modeles.findByTenantIdAndCode(e.tenantId(), r.getModeleCode()).orElse(null);
            if (m == null) continue;
            String sujet = GabaritTexte.fusionner(m.getSujet(), valeurs);
            String corps = GabaritTexte.fusionner(m.getCorps(), valeurs);
            CanalNotification canal = canaux.get(m.getCanal());
            for (String dest : destinataires(r.getDestinataires(), valeurs)) {
                String statut = canal == null ? "ECHEC" : canal.envoyer(dest, sujet, corps);
                journal.save(new NotificationEnvoi(e.tenantId(), m.getCanal(), dest, sujet, corps, statut));
            }
        }
    }

    /**
     * Réponses de la fiche, plus son numéro de suivi ({@code {{numero}}}) que le demandeur cite pour suivre sa demande.
     * Un champ du formulaire nommé « numero » prévaut.
     */
    private Map<String, Object> valeursFusionnables(FicheEvent e) {
        Map<String, Object> valeurs = new LinkedHashMap<>(e.valeurs() == null ? Map.of() : e.valeurs());
        numeros.numero(e.tenantId(), e.saisieId()).ifPresent(n -> valeurs.putIfAbsent("numero", n));
        return valeurs;
    }

    /** Résout les destinataires : chaque entrée est une adresse directe ou un marqueur {{champ}}. */
    private List<String> destinataires(String csv, Map<String, Object> valeurs) {
        if (csv == null || csv.isBlank()) return List.of();
        List<String> res = new ArrayList<>();
        for (String part : csv.split(",")) {
            String d = GabaritTexte.fusionner(part.trim(), valeurs);
            if (!d.isBlank()) res.add(d);
        }
        return res;
    }

    // --- CRUD (superadmin/admin du tenant) ---
    public List<NotificationModele> listerModeles(Long tenantId) { return modeles.findByTenantId(tenantId); }
    public NotificationModele enregistrerModele(NotificationModele m) { return modeles.save(m); }
    public List<NotificationRegle> listerRegles(Long tenantId) { return regles.findByTenantId(tenantId); }
    public NotificationRegle enregistrerRegle(NotificationRegle r) { return regles.save(r); }
    public List<NotificationEnvoi> journal(Long tenantId) { return journal.findTop100ByTenantIdOrderByEnvoyeLeDesc(tenantId); }
}
