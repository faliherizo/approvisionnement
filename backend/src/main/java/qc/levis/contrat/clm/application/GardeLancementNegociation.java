package qc.levis.contrat.clm.application;

import java.util.EnumSet;
import java.util.Optional;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.repository.NegociationRepository;
import qc.levis.contrat.clm.domain.EtatNegociation;
import qc.levis.contrat.clm.domain.Trousse;

/** RST-55 : une trousse ne part pas en validation ou en signature tant qu'une négociation reste ouverte. */
@Component
public class GardeLancementNegociation implements ConditionLancement {

    private static final EnumSet<EtatNegociation> OUVERTS = EnumSet.of(EtatNegociation.ATTENTE_CONTREPARTIE,
            EtatNegociation.ATTENTE_INTERNE, EtatNegociation.ENTENTE);

    private final NegociationRepository negociations;

    public GardeLancementNegociation(NegociationRepository negociations) {
        this.negociations = negociations;
    }

    @Override
    public Optional<String> empechement(Trousse t) {
        return negociations.existsByTenantIdAndTrousseIdAndEtatIn(t.getTenantId(), t.getId(), OUVERTS)
                ? Optional.of("Négociation en cours : clôturez-la ou annulez-la avant de lancer la trousse.")
                : Optional.empty();
    }
}
