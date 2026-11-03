package qc.levis.contrat.notification.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qc.levis.contrat.notification.application.port.CanalNotification;

/** Canal SMS par défaut (simulation, ADR-002). Adaptateur réel (Twilio/Telnyx) via profil. */
@Component
public class SmsSimulation implements CanalNotification {
    private static final Logger log = LoggerFactory.getLogger(SmsSimulation.class);

    @Override public String canal() { return "SMS"; }

    @Override
    public String envoyer(String destinataire, String sujet, String corps) {
        log.info("[SIMULATION SMS] à={}", destinataire);
        return "SIMULE";
    }
}
