package qc.levis.contrat.notification.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import qc.levis.contrat.notification.application.port.CanalNotification;

/** Canal COURRIEL par défaut (simulation, ADR-002). Adaptateur réel (ACS/SMTP) via profil. */
@Component
@ConditionalOnProperty(name = "app.notification.courriel.mode", havingValue = "simulation", matchIfMissing = true)
public class CourrielSimulation implements CanalNotification {
    private static final Logger log = LoggerFactory.getLogger(CourrielSimulation.class);

    @Override public String canal() { return "COURRIEL"; }

    @Override
    public String envoyer(String destinataire, String sujet, String corps) {
        log.info("[SIMULATION COURRIEL] à={} sujet={}", destinataire, sujet);
        return "SIMULE";
    }
}
