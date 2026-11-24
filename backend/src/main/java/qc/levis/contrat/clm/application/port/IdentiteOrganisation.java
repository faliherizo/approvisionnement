package qc.levis.contrat.clm.application.port;

/** Identité publique de l'organisation qui invite à signer (F-12) : le signataire externe sait qui s'adresse à lui. */
public interface IdentiteOrganisation {

    /** {@code logo} : image en URL de données, absente si l'organisation n'en a pas déposé. */
    record Organisation(String nom, String logo) {
    }

    Organisation de(Long tenantId);

    /** Organisation désignée par son identifiant court (sous-domaine), si elle existe. */
    java.util.Optional<Long> parIdentifiant(String identifiant);
}
