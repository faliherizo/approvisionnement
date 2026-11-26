package qc.levis.contrat.clm.infrastructure;

import java.time.Clock;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.DepotsM365Service;
import qc.levis.contrat.clm.application.RappelService;
import qc.levis.contrat.clm.application.RappelsExternesService;
import qc.levis.contrat.clm.application.SalesforceService;
import qc.levis.contrat.clm.application.port.AlertesExploitation;

/** Câblage du module CLM : propriétés, horloge et planificateur des rappels, du relais Salesforce et des dépôts Microsoft 365. */
@Configuration
@EnableConfigurationProperties(ClmProprietes.class)
public class ClmConfiguration {

    /**
     * Les instants restent universels ; le fuseau ({@code app.clm.fuseau}) ne sert qu'à dire quel jour on est, pour les
     * échéances exprimées en date : une tâche due aujourd'hui n'est pas en retard à 20 h, heure du Québec.
     */
    @Bean
    @ConditionalOnMissingBean
    public Clock horloge(@Value("${app.clm.fuseau:America/Toronto}") String fuseau) {
        return Clock.system(ZoneId.of(fuseau));
    }

    /** Désactivable ({@code app.clm.planificateur.actif=false}) pour les tests et les instances secondaires. */
    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = "app.clm.planificateur.actif", havingValue = "true", matchIfMissing = true)
    public static class Planificateur {

        private static final Logger log = LoggerFactory.getLogger(Planificateur.class);

        private final RappelService rappels;
        private final SalesforceService salesforce;
        private final RappelsExternesService rappelsExternes;
        private final DepotsM365Service depotsM365;
        private final AlertesExploitation alertes;

        public Planificateur(RappelService rappels, SalesforceService salesforce, RappelsExternesService rappelsExternes,
                             DepotsM365Service depotsM365, AlertesExploitation alertes) {
            this.rappels = rappels;
            this.salesforce = salesforce;
            this.rappelsExternes = rappelsExternes;
            this.depotsM365 = depotsM365;
            this.alertes = alertes;
        }

        /** Chaque tâche est isolée : l'échec des rappels n'empêche pas le relais Salesforce, et chacun est signalé. */
        @Scheduled(fixedDelayString = "${app.clm.planificateur.delai-ms:60000}", initialDelayString = "${app.clm.planificateur.delai-ms:60000}")
        public void executer() {
            executer("rappels et échéances", rappels::executer);
            executer("relais Salesforce", salesforce::relayer);
            executer("rappels aux systèmes externes", rappelsExternes::relayer);
            executer("dépôts Microsoft 365", depotsM365::relayer);
        }

        private void executer(String tache, Runnable travail) {
            try {
                travail.run();
            } catch (RuntimeException e) {
                log.error("Planificateur CLM : échec de la tâche « {} »", tache, e);
                alertes.signaler(AlertesExploitation.Code.PLANIFICATEUR_ECHEC, null, tache, e.getClass().getSimpleName());
            }
        }
    }
}
