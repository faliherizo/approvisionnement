package qc.levis.contrat.clm.application;

import qc.levis.contrat.clm.application.port.ContratPorteeTenant;
import qc.levis.contrat.clm.application.port.PorteeTenant;

/** Le double de test respecte le même contrat que l'adaptateur de production. */
class PorteeTenantMemoireContratTest extends ContratPorteeTenant {

    @Override
    protected PorteeTenant portee() {
        return new PorteeTenantMemoire();
    }
}
