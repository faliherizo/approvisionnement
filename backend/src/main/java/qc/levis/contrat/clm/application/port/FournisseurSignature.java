package qc.levis.contrat.clm.application.port;

import java.time.Instant;
import qc.levis.contrat.clm.domain.Identite;

/**
 * Fournisseur de signature à valeur probante (ST-09). L'adaptateur retenu après la décision V1 (fournisseur
 * certifié, horodatage qualifié RFC 3161) remplace l'adaptateur local sans toucher aux cas d'utilisation.
 */
public interface FournisseurSignature {

    record Demande(String empreinteDocuments, String signataire, String courriel, Identite.Niveau niveau,
                   Instant consentementLe, String ip) {
    }

    record Attestation(String fournisseur, String identifiant, Instant horodatage, String jetonHorodatage, String algorithme) {
    }

    Attestation signer(Demande demande);

    boolean verifier(Demande demande, Attestation attestation);
}
