package qc.levis.contrat.clm.application;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.repository.NegociationRepository;
import qc.levis.contrat.clm.application.repository.PropositionNegociationRepository;
import qc.levis.contrat.clm.application.repository.VersionNegociationRepository;
import qc.levis.contrat.clm.domain.ClauseNegociee;
import qc.levis.contrat.clm.domain.Negociation;
import qc.levis.contrat.clm.domain.PropositionNegociation;
import qc.levis.contrat.clm.domain.Trousse;
import qc.levis.contrat.clm.domain.VersionNegociation;

/**
 * Persistance d'une négociation (RST-55) partagée par l'organisation et le portail : lecture bornée au tenant, versions
 * relues depuis leurs clauses figées, et enregistrement d'une version, dont l'empreinte entre au journal scellé.
 */
@Component
public class RegistreNegociation {

    static final TypeReference<List<ClauseNegociee>> CLAUSES = new TypeReference<>() {
    };

    private final NegociationRepository negociations;
    private final VersionNegociationRepository versions;
    private final PropositionNegociationRepository propositions;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public RegistreNegociation(NegociationRepository negociations, VersionNegociationRepository versions,
                               PropositionNegociationRepository propositions, JournalClm journal, ContexteClm ctx) {
        this.negociations = negociations;
        this.versions = versions;
        this.propositions = propositions;
        this.journal = journal;
        this.ctx = ctx;
    }

    public Negociation exiger(Long id) {
        return negociations.findByIdAndTenantId(id, ctx.tenant())
                .orElseThrow(() -> new NoSuchElementException("Négociation introuvable : " + id));
    }

    public List<Negociation> deLaTrousse(Trousse t) {
        return negociations.findByTenantIdAndTrousseIdOrderByIdDesc(t.getTenantId(), t.getId());
    }

    public Negociation enregistrer(Negociation n) {
        return negociations.save(n);
    }

    public List<ClauseNegociee> clauses(Negociation n, int numero) {
        VersionNegociation v = versions.findByTenantIdAndNegociationIdAndNumero(n.getTenantId(), n.getId(), numero)
                .orElseThrow(() -> new NoSuchElementException("Version introuvable : " + numero));
        return ctx.lire(v.getClausesJson(), CLAUSES, List.of());
    }

    public List<ClauseNegociee> courantes(Negociation n) {
        return clauses(n, n.getVersionCourante());
    }

    public List<VersionNegociation> versions(Negociation n) {
        return versions.findByTenantIdAndNegociationIdOrderByNumeroAsc(n.getTenantId(), n.getId());
    }

    public List<PropositionNegociation> propositions(Negociation n) {
        return propositions.findByTenantIdAndNegociationIdOrderByIdAsc(n.getTenantId(), n.getId());
    }

    public List<PropositionNegociation> duTour(Negociation n) {
        return propositions.findByTenantIdAndNegociationIdAndTourOrderByIdAsc(n.getTenantId(), n.getId(), n.getTour());
    }

    public PropositionNegociation enregistrer(PropositionNegociation p) {
        return propositions.save(p);
    }

    /** Fige les clauses sous le numéro courant de la négociation et scelle leur empreinte au journal de la trousse. */
    public VersionNegociation figer(Trousse t, Negociation n, List<ClauseNegociee> clauses, String motif, String auteur) {
        VersionNegociation.Contenu contenu = new VersionNegociation.Contenu(n.getVersionCourante(), ctx.ecrire(clauses), motif);
        VersionNegociation v = versions.save(new VersionNegociation(n, contenu, auteur, ctx.maintenant()));
        journal.consigner(t, "NEGOCIATION_VERSION", auteur, "v" + v.getNumero() + " · " + clauses.size() + " clause(s) · "
                + v.getEmpreinte().substring(0, 16) + " · " + motif);
        return v;
    }
}
