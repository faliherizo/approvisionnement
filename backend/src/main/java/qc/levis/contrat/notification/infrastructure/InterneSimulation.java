package qc.levis.contrat.notification.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qc.levis.contrat.notification.application.port.CanalNotification;

/** Canal messagerie INTERNE par défaut (simulation : journalisé). */
@Component
public class InterneSimulation implements CanalNotification {
    private static final Logger log = LoggerFactory.getLogger(InterneSimulation.class);

    @Override public String canal() { return "INTERNE"; }

    @Override
    public String envoyer(String destinataire, String sujet, String corps) {
        log.info("[MESSAGERIE INTERNE] à={} sujet={}", destinataire, sujet);
        return "SIMULE";
    }
}
