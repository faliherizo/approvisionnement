package qc.levis.contrat.rapport.application.port;

import qc.levis.contrat.rapport.domain.RapportFiches;

/** Met en forme un rapport en PDF/A. */
public interface RenduRapport {
    byte[] pdf(RapportFiches rapport);
}
