package qc.levis.contrat.clm.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Paramètres du module CLM ({@code app.clm.*}), exposés en lecture seule dans UI-22. */
@ConfigurationProperties("app.clm")
public record ClmProprietes(String environnement, String portailUrl, Documents documents, Invitations invitation,
                            Preuve preuve, Planificateur planificateur, Salesforce salesforce) {

    public record Documents(long tailleMaxOctets) {
    }

    public record Invitations(int validiteJours) {
    }

    public record Preuve(String cle) {
    }

    public record Planificateur(boolean actif, long delaiMs) {
    }

    public record Salesforce(String mode, String instanceUrl, String tokenUrl, String clientId, String clientSecret,
                             String objet, String versionApi) {
    }

    public ClmProprietes {
        environnement = environnement == null ? "DEV" : environnement;
        portailUrl = portailUrl == null ? "http://localhost:5173/portail" : portailUrl;
        documents = documents == null ? new Documents(25L * 1024 * 1024) : documents;
        invitation = invitation == null ? new Invitations(14) : invitation;
        preuve = preuve == null ? new Preuve("cle-de-developpement") : preuve;
        planificateur = planificateur == null ? new Planificateur(true, 60_000) : planificateur;
        salesforce = salesforce == null ? new Salesforce("simulation", null, null, null, null, "FundingRequest", "v60.0") : salesforce;
    }
}
