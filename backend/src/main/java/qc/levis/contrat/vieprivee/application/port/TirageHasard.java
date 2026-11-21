package qc.levis.contrat.vieprivee.application.port;

import java.util.UUID;

/** Source d'aléa injectée : le domaine ne tire jamais lui-même (tests déterministes). */
@FunctionalInterface
public interface TirageHasard {
    UUID tirer();
}
