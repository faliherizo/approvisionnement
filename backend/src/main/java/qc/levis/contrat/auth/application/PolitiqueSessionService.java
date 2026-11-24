package qc.levis.contrat.auth.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.dto.PolitiqueSessionVue;
import qc.levis.contrat.auth.application.repository.PolitiqueSessionRepository;
import qc.levis.contrat.auth.domain.LimitesSession;
import qc.levis.contrat.auth.domain.PolitiqueSession;

/**
 * Politique de session (ST-01) : l'administration technique fixe le délai d'inactivité et la durée maximale de son
 * organisation ; à défaut, ceux de la plateforme s'appliquent. Lue à chaque requête authentifiée, elle est gardée en
 * mémoire une minute au plus : un changement prend effet dans la minute.
 */
@Service
public class PolitiqueSessionService {

    static final Duration FRAICHEUR = Duration.ofMinutes(1);
    /** Clé du cache pour la plateforme (superadministration, sans organisation). */
    private static final long PLATEFORME = Long.MIN_VALUE;

    private record EnCache(LimitesSession limites, Instant lue) { }

    private final PolitiqueSessionRepository politiques;
    private final AuditService audit;
    private final Clock horloge;
    private final LimitesSession parDefaut;
    private final Map<Long, EnCache> cache = new ConcurrentHashMap<>();

    public PolitiqueSessionService(PolitiqueSessionRepository politiques, AuditService audit, Clock horloge,
                                   @Value("${app.session.inactivite-minutes:60}") long inactiviteMinutes,
                                   @Value("${app.session.duree-max-minutes:480}") long dureeMaxMinutes) {
        this.politiques = politiques;
        this.audit = audit;
        this.horloge = horloge;
        this.parDefaut = new LimitesSession(Duration.ofMinutes(inactiviteMinutes), Duration.ofMinutes(dureeMaxMinutes));
    }

    /** Limites en vigueur pour l'organisation (nulle : plateforme). */
    public LimitesSession limites(Long tenant) {
        if (tenant == null) {
            return parDefaut;
        }
        Instant maintenant = horloge.instant();
        EnCache c = cache.get(tenant);
        if (c != null && maintenant.isBefore(c.lue().plus(FRAICHEUR))) {
            return c.limites();
        }
        LimitesSession lues = politiques.findById(tenant).map(PolitiqueSession::limites).orElse(parDefaut);
        cache.put(tenant, new EnCache(lues, maintenant));
        return lues;
    }

    public PolitiqueSessionVue etat(Long tenant) {
        Optional<PolitiqueSession> p = politiques.findById(tenant);
        LimitesSession l = p.map(PolitiqueSession::limites).orElse(parDefaut);
        return new PolitiqueSessionVue(l.inactivite().toMinutes(), l.dureeMax().toMinutes(), p.isPresent(),
                p.map(PolitiqueSession::getModifiePar).orElse(null), p.map(PolitiqueSession::getModifieLe).orElse(null));
    }

    @Transactional
    public PolitiqueSessionVue definir(Long tenant, long inactiviteMinutes, long dureeMaxMinutes, String par) {
        LimitesSession limites = new LimitesSession(Duration.ofMinutes(inactiviteMinutes), Duration.ofMinutes(dureeMaxMinutes));
        PolitiqueSession p = politiques.findById(tenant).orElseGet(() -> new PolitiqueSession(tenant));
        p.definir(limites, par, horloge.instant());
        politiques.save(p);
        cache.remove(tenant);
        audit.log(tenant, par, "POLITIQUE_SESSION", "Tenant", tenant,
                "inactivité " + inactiviteMinutes + " min, durée maximale " + dureeMaxMinutes + " min");
        return etat(tenant);
    }
}
