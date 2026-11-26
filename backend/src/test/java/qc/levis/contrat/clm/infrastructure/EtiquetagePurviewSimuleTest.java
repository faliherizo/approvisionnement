package qc.levis.contrat.clm.infrastructure;

import qc.levis.contrat.clm.application.port.EtiquetagePurview;

/** Purview simulé (défaut, ADR-036) contre le contrat du port. */
class EtiquetagePurviewSimuleTest extends EtiquetagePurviewContrat {

    private final EtiquetagePurviewSimule sujet = new EtiquetagePurviewSimule();

    @Override
    EtiquetagePurview sujet() {
        return sujet;
    }

    @Override
    String fichierDepose() {
        return "sim://sharepoint/Contrats/c.pdf";
    }
}
