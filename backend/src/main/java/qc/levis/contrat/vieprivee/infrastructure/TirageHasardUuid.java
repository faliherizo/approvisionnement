package qc.levis.contrat.vieprivee.infrastructure;

import java.util.UUID;
import org.springframework.stereotype.Component;
import qc.levis.contrat.vieprivee.application.port.TirageHasard;

@Component
class TirageHasardUuid implements TirageHasard {
    @Override
    public UUID tirer() {
        return UUID.randomUUID();
    }
}
