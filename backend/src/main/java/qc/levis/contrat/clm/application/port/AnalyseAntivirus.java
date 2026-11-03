package qc.levis.contrat.clm.application.port;

/** Analyse antivirus d'un fichier téléversé (UI-05) ; l'adaptateur de production délègue à l'antivirus d'IQ. */
public interface AnalyseAntivirus {
    boolean estSain(byte[] contenu);
}
