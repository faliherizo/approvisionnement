package qc.levis.contrat.clm.infrastructure;

import org.junit.jupiter.api.AfterEach;
import qc.levis.contrat.clm.application.port.ContratPorteeTenant;
import qc.levis.contrat.clm.application.port.PorteeTenant;
import qc.levis.contrat.commun.infrastructure.TenantContext;

/** Adaptateur de production du port : le tenant est celui du fil d'exécution (TenantContext). */
class PorteeTenantFilContratTest extends ContratPorteeTenant {

    @AfterEach
    void nettoyer() {
        TenantContext.clear();
    }

    @Override
    protected PorteeTenant portee() {
        TenantContext.clear();
        return new AdaptateursClm.PorteeTenantFil();
    }
}
