package qc.levis.contrat.clm.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.ContexteClm;
import qc.levis.contrat.clm.application.repository.GroupeClmRepository;
import qc.levis.contrat.clm.application.repository.MembreGroupeClmRepository;
import qc.levis.contrat.clm.domain.GroupeClm;
import qc.levis.contrat.clm.domain.MembreGroupeClm;
import qc.levis.contrat.commun.application.DroitModule;
import qc.levis.contrat.commun.application.DroitsModule;

/**
 * Groupes du module CLM d'un compte, exposés en lecture à la découverte des droits (TI-21, RST-52). La lecture se fait dans
 * la portée de l'organisation demandée : un jeton SCIM d'une organisation ne voit jamais les groupes d'une autre.
 */
@Component
public class DroitsModuleClm implements DroitsModule {

    static final String MODULE = "clm";

    private final GroupeClmRepository groupes;
    private final MembreGroupeClmRepository membres;
    private final ContexteClm ctx;

    public DroitsModuleClm(GroupeClmRepository groupes, MembreGroupeClmRepository membres, ContexteClm ctx) {
        this.groupes = groupes;
        this.membres = membres;
        this.ctx = ctx;
    }

    @Override
    public List<DroitModule> droits(Long tenantId, String courriel) {
        return ctx.dansTenant(tenantId, () -> lire(tenantId, courriel));
    }

    private List<DroitModule> lire(Long tenantId, String courriel) {
        Map<String, String> libelles = groupes.findByTenantIdOrderByCodeAsc(tenantId).stream()
                .collect(Collectors.toMap(GroupeClm::getCode, GroupeClm::getLibelle, (a, b) -> a));
        return membres.findByTenantIdAndCourrielIgnoreCase(tenantId, courriel).stream().map(MembreGroupeClm::getGroupeCode)
                .distinct().sorted().map(code -> new DroitModule(MODULE, code, libelles.get(code))).toList();
    }
}
