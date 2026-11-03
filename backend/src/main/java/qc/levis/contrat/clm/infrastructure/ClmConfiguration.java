package qc.levis.contrat.clm.infrastructure;

import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.RappelService;
import qc.levis.contrat.clm.application.SalesforceService;

/** Câblage du module CLM : propriétés, horloge et planificateur des rappels et du relais Salesforce. */
@Configuration
@EnableConfigurationProperties(ClmProprietes.class)
public class ClmConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock horloge() {
        return Clock.systemUTC();
    }

    /** Désactivable ({@code app.clm.planificateur.actif=false}) pour les tests et les instances secondaires. */
    @Configuration
    @EnableScheduling
    @ConditionalOnProperty(name = "app.clm.planificateur.actif", havingValue = "true", matchIfMissing = true)
    public static class Planificateur {

        private final RappelService rappels;
        private final SalesforceService salesforce;

        public Planificateur(RappelService rappels, SalesforceService salesforce) {
            this.rappels = rappels;
            this.salesforce = salesforce;
        }

        @Scheduled(fixedDelayString = "${app.clm.planificateur.delai-ms:60000}", initialDelayString = "${app.clm.planificateur.delai-ms:60000}")
        public void executer() {
            rappels.executer();
            salesforce.relayer();
        }
    }
}
