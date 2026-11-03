package qc.levis.contrat.clm.application.port;

/** Scellement (HMAC) des dossiers de preuve : la clé ne quitte jamais le serveur. */
public interface Scelleur {
    String sceller(byte[] contenu);

    boolean verifier(byte[] contenu, String sceau);
}
