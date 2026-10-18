package qc.levis.contrat.integration.application.port;

import java.math.BigDecimal;

/**
 * Port d'enregistrement d'une référence financière de dossier dans un système externe (SAP —
 * art. 9). Distinct de la publication de document : on transmet des <em>métadonnées</em> (n° AO,
 * objet, montant total). L'implémentation réelle (RFC/OData SAP) est un adaptateur d'infrastructure
 * branché sur ce port (ADR-002) ; un adaptateur de simulation sert de repli.
 */
public interface ReferenceFinancierePort {

    PublicationCiblePort.ResultatPublication deposer(ReferenceFinanciere reference);

    /** Référence financière agrégée d'un dossier d'approvisionnement. */
    record ReferenceFinanciere(Long dossierId, String numeroAO, String objet,
                               int nombreDocuments, BigDecimal montantTotal) {}
}
