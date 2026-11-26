package qc.levis.contrat.clm.application.port;

/** Aléa cryptographique injecté : clés de données et vecteurs d'initialisation du chiffrement au repos (ADR-037). */
public interface AleaCryptographique {

    byte[] octets(int longueur);
}
