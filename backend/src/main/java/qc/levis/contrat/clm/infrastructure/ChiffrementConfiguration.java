package qc.levis.contrat.clm.infrastructure;

import java.security.SecureRandom;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import qc.levis.contrat.clm.application.port.AleaCryptographique;

/** Câblage du chiffrement au repos (ADR-037) : propriétés et aléa cryptographique. */
@Configuration
@EnableConfigurationProperties(ChiffrementProprietes.class)
public class ChiffrementConfiguration {

    @Bean
    public AleaCryptographique aleaCryptographique() {
        SecureRandom alea = new SecureRandom();
        return longueur -> {
            byte[] octets = new byte[longueur];
            alea.nextBytes(octets);
            return octets;
        };
    }
}
