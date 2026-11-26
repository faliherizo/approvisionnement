package qc.levis.contrat.clm.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.domain.CleOrganisation;
import qc.levis.contrat.clm.domain.DocumentTrousse;

/**
 * Administration du chiffrement au repos de l'organisation (Annexe 10, SEC-28 ; ADR-037) : état des versions de sa clé,
 * rotation et réenveloppement. La rotation ne bloque rien : les documents restent lisibles par leur version, puis le
 * réenveloppement, par lots, les porte à la version active (anciens contenus en clair compris).
 */
@Service
public class ChiffrementClmService {

    static final int LOT = 200;

    private final RegistreClesOrganisation registre;
    private final CoffreDocuments coffre;
    private final DocumentTrousseRepository documents;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public ChiffrementClmService(RegistreClesOrganisation registre, CoffreDocuments coffre, DocumentTrousseRepository documents,
                                 JournalClm journal, ContexteClm ctx) {
        this.registre = registre;
        this.coffre = coffre;
        this.documents = documents;
        this.journal = journal;
        this.ctx = ctx;
    }

    @Transactional(readOnly = true)
    public ClmDto.ChiffrementVue etat() {
        Long tenant = ctx.tenant();
        String maitresse = registre.cleMaitresseActive();
        List<ClmDto.CleOrganisationVue> versions = registre.lister(tenant).stream().map(c -> vue(c, tenant, maitresse)).toList();
        return new ClmDto.ChiffrementVue(maitresse, documents.countByTenantIdAndCleVersionIsNull(tenant), versions);
    }

    @Transactional
    public ClmDto.ChiffrementVue pivoter(String acteur) {
        Long tenant = ctx.tenant();
        CleOrganisation nouvelle = registre.pivoter(tenant);
        coffre.oublier(tenant);
        journal.configuration(acteur, "CHIFFREMENT", "rotation de la clé de l'organisation : version " + nouvelle.getVersion() + " active");
        return etat();
    }

    /** Un lot : clés de données réenveloppées par la clé maîtresse active, puis documents portés à la version active. */
    @Transactional
    public ClmDto.ReenveloppementVue reenvelopper(String acteur) {
        Long tenant = ctx.tenant();
        int cles = registre.reenvelopper(tenant);
        coffre.oublier(tenant);
        int active = registre.active(tenant).getVersion();
        List<Long> ids = documents.findIdsByTenantIdHorsVersion(tenant, active);
        List<Long> lot = ids.subList(0, Math.min(LOT, ids.size()));
        lot.forEach(id -> documents.findByIdAndTenantId(id, tenant).ifPresent(d -> reenvelopper(tenant, d)));
        journal.configuration(acteur, "CHIFFREMENT", "réenveloppement : " + cles + " clé(s), " + lot.size() + " document(s) en version " + active);
        return new ClmDto.ReenveloppementVue(cles, lot.size(), ids.size() - lot.size());
    }

    private void reenvelopper(Long tenant, DocumentTrousse d) {
        CoffreDocuments.Chiffre c = coffre.chiffrer(tenant, d.getContenu());
        d.stockerChiffre(c.octets(), c.version());
        documents.save(d);
    }

    private ClmDto.CleOrganisationVue vue(CleOrganisation c, Long tenant, String maitresse) {
        return new ClmDto.CleOrganisationVue(c.getVersion(), c.getStatut().name(), c.getCleMaitresse(), c.getCreeLe(), c.getRetireeLe(),
                documents.countByTenantIdAndCleVersion(tenant, c.getVersion()), !maitresse.equals(c.getCleMaitresse()));
    }
}
