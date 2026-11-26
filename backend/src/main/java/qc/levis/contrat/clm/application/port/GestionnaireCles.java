package qc.levis.contrat.clm.application.port;

import qc.levis.contrat.clm.domain.CleEnveloppee;

/**
 * Service de gestion des clés (KMS) qui garde la clé maîtresse (Annexe 10, SEC-28 ; ADR-037). Il enveloppe et ouvre les
 * clés de données des organisations sans jamais livrer la clé maîtresse : adaptateur local par défaut (clé maîtresse issue
 * d'un secret de déploiement), Azure Key Vault sous le profil {@code azure-kms}. Tous les adaptateurs respectent le même
 * contrat, éprouvé par une suite commune : une enveloppe altérée, ou d'une clé maîtresse inconnue, lève {@link CleIllisible}.
 */
public interface GestionnaireCles {

    /** Enveloppe une clé de données par la clé maîtresse active. */
    CleEnveloppee envelopper(byte[] cleDonnees);

    /** Ouvre une clé de données ; {@link CleIllisible} si l'enveloppe est altérée ou sa clé maîtresse inconnue. */
    byte[] desenvelopper(CleEnveloppee cle);

    /** Identifiant de la clé maîtresse active : une clé de données enveloppée par une autre est à réenvelopper. */
    String cleMaitresseActive();

    /** Enveloppe impossible à ouvrir : altérée, d'une autre clé maîtresse, ou service de clés indisponible. */
    final class CleIllisible extends RuntimeException {
        public CleIllisible(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
