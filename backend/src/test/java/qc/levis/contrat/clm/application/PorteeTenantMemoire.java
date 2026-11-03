package qc.levis.contrat.clm.application;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import qc.levis.contrat.clm.application.port.PorteeTenant;

/** Double de test du port : tenant tenu en mémoire, même contrat que l'adaptateur de production. */
final class PorteeTenantMemoire implements PorteeTenant {

    private Long courant;
    private final List<Long> traverses = new ArrayList<>();

    @Override
    public Long courant() {
        return courant;
    }

    @Override
    public <T> T executerDans(Long tenantId, Supplier<T> action) {
        Long precedent = courant;
        courant = tenantId;
        traverses.add(tenantId);
        try {
            return action.get();
        } finally {
            courant = precedent;
        }
    }

    /** Tenants dans lesquels un traitement a été exécuté, dans l'ordre. */
    List<Long> traverses() {
        return traverses;
    }
}
