package qc.levis.contrat.clm.application;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Paramètres de plateforme du module CLM ({@code app.clm.*}). Ceux qu'une organisation peut changer n'y sont que des
 * valeurs par défaut : chaque organisation les remplace par les siens (UI-21, UI-22).
 */
@ConfigurationProperties("app.clm")
public record ClmProprietes(String environnement, String portailUrl, Documents documents, Invitations invitation,
                            Preuve preuve, Planificateur planificateur, Salesforce salesforce) {

    /** {@code plafondOctets} : taille qu'accepte le transport des fichiers, au-delà de laquelle aucune organisation ne va. */
    public record Documents(long tailleMaxOctets, long plafondOctets) {
        public Documents {
            plafondOctets = plafondOctets <= 0 ? 30L * 1024 * 1024 : plafondOctets;
        }
    }

    public record Invitations(int validiteJours) {
    }

    public record Preuve(String cle) {
    }

    public record Planificateur(boolean actif, long delaiMs) {
    }

    /** {@code hotesAutorises} : domaines qu'une organisation peut désigner comme instance ou serveur de jetons. */
    public record Salesforce(String mode, String instanceUrl, String tokenUrl, String clientId, String clientSecret,
                             String objet, String versionApi, List<String> hotesAutorises) {
        public Salesforce {
            hotesAutorises = hotesAutorises == null || hotesAutorises.isEmpty() ? List.of("salesforce.com", "force.com")
                    : List.copyOf(hotesAutorises);
        }
    }

    public ClmProprietes {
        environnement = environnement == null ? "DEV" : environnement;
        portailUrl = portailUrl == null ? "http://localhost:5173/portail" : portailUrl;
        documents = documents == null ? new Documents(25L * 1024 * 1024, 0) : documents;
        invitation = invitation == null ? new Invitations(14) : invitation;
        preuve = preuve == null ? new Preuve("cle-de-developpement") : preuve;
        planificateur = planificateur == null ? new Planificateur(true, 60_000) : planificateur;
        salesforce = salesforce == null ? new Salesforce("simulation", null, null, null, null, "FundingRequest", "v60.0", null) : salesforce;
    }

    public boolean production() {
        return "PROD".equalsIgnoreCase(environnement);
    }
}
