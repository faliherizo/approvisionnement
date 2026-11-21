package qc.levis.contrat.tenant.infrastructure.lecture;

import org.springframework.stereotype.Component;
import qc.levis.contrat.soutien.application.port.Organisations;
import qc.levis.contrat.tenant.application.repository.TenantRepository;

@Component
class OrganisationsJpa implements Organisations {

    private final TenantRepository tenants;

    OrganisationsJpa(TenantRepository tenants) { this.tenants = tenants; }

    @Override
    public boolean existe(Long tenantId) {
        return tenantId != null && tenants.existsById(tenantId);
    }
}
