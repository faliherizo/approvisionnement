package qc.levis.contrat.clm.infrastructure;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ContexteClm;
import qc.levis.contrat.clm.application.GroupesClmService;
import qc.levis.contrat.commun.application.AppartenancesAnnuaire;

/**
 * Reçoit les domaines d'affaires tirés de l'annuaire (addenda 3, Q44) et les reporte sur les groupes du module, dans la
 * portée de l'organisation concernée. Le module d'authentification n'accède jamais aux groupes du module.
 */
@Component
public class GroupesAnnuaireEcouteur {

    private final GroupesClmService groupes;
    private final ContexteClm ctx;

    public GroupesAnnuaireEcouteur(GroupesClmService groupes, ContexteClm ctx) {
        this.groupes = groupes;
        this.ctx = ctx;
    }

    @EventListener
    public void surAppartenances(AppartenancesAnnuaire e) {
        ctx.dansTenant(e.tenantId(), () -> {
            groupes.synchroniserAnnuaire(e.tenantId(), e.courriel(), e.groupes(), e.groupesGeres());
            return null;
        });
    }
}
