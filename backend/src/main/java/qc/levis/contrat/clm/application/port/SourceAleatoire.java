package qc.levis.contrat.clm.application.port;

/** Aléa injecté : jetons d'invitation, codes de vérification et identifiants de corrélation. */
public interface SourceAleatoire {
    String jeton();

    String code();

    String uuid();
}
