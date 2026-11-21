package qc.levis.contrat.saisie.application.port;

import qc.levis.contrat.saisie.domain.ContenuFiche;

/** Met en forme le document d'une fiche (PDF/A). */
public interface RenduFiche {
    byte[] pdf(ContenuFiche contenu);
}
