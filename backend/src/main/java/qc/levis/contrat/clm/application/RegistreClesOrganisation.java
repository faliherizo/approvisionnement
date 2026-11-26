package qc.levis.contrat.clm.application;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.port.AleaCryptographique;
import qc.levis.contrat.clm.application.port.GestionnaireCles;
import qc.levis.contrat.clm.application.repository.CleOrganisationRepository;
import qc.levis.contrat.clm.domain.CleOrganisation;

/**
 * Registre des clés de données par organisation (Annexe 10, SEC-28 ; ADR-037). Les lectures et la création de la première
 * clé se font dans une transaction propre : elles sont appelées pendant l'écriture ou le chargement d'un document, et ne
 * doivent ni dépendre de la transaction en cours ni l'altérer.
 */
@Service
public class RegistreClesOrganisation {

    static final int OCTETS_CLE = 32;

    private final CleOrganisationRepository cles;
    private final GestionnaireCles kms;
    private final AleaCryptographique alea;
    private final ContexteClm ctx;

    public RegistreClesOrganisation(CleOrganisationRepository cles, GestionnaireCles kms, AleaCryptographique alea, ContexteClm ctx) {
        this.cles = cles;
        this.kms = kms;
        this.alea = alea;
        this.ctx = ctx;
    }

    /** Version active de l'organisation ; la première est créée à la première écriture d'un document. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CleOrganisation active(Long tenantId) {
        return cles.findByTenantIdAndStatut(tenantId, CleOrganisation.Statut.ACTIVE).orElseGet(() -> creer(tenantId, 1));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public Optional<CleOrganisation> version(Long tenantId, int version) {
        return cles.findByTenantIdAndVersion(tenantId, version);
    }

    @Transactional(readOnly = true)
    public List<CleOrganisation> lister(Long tenantId) {
        return cles.findByTenantIdOrderByVersionDesc(tenantId);
    }

    /** Rotation : la version active est retirée, une nouvelle clé de données devient active. */
    @Transactional
    public CleOrganisation pivoter(Long tenantId) {
        Optional<CleOrganisation> active = cles.findByTenantIdAndStatut(tenantId, CleOrganisation.Statut.ACTIVE);
        int suivante = cles.findByTenantIdOrderByVersionDesc(tenantId).stream().mapToInt(CleOrganisation::getVersion).max().orElse(0) + 1;
        active.ifPresent(c -> {
            c.retirer(ctx.maintenant());
            cles.saveAndFlush(c);
        });
        return creer(tenantId, suivante);
    }

    public String cleMaitresseActive() {
        return kms.cleMaitresseActive();
    }

    /** Rotation de la clé maîtresse : les clés de données enveloppées par une autre clé maîtresse le sont par l'active. */
    @Transactional
    public int reenvelopper(Long tenantId) {
        String maitresse = kms.cleMaitresseActive();
        List<CleOrganisation> anciennes = cles.findByTenantIdOrderByVersionDesc(tenantId).stream()
                .filter(c -> !maitresse.equals(c.getCleMaitresse())).toList();
        anciennes.forEach(c -> {
            c.reenvelopper(kms.envelopper(kms.desenvelopper(c.enveloppe())));
            cles.save(c);
        });
        return anciennes.size();
    }

    private CleOrganisation creer(Long tenantId, int version) {
        byte[] cle = alea.octets(OCTETS_CLE);
        return cles.saveAndFlush(CleOrganisation.creer(tenantId, version, kms.envelopper(cle), ctx.maintenant()));
    }

}
