package qc.levis.contrat.saisie.infrastructure;

import java.util.Optional;
import org.springframework.stereotype.Component;
import qc.levis.contrat.notification.application.port.NumerosFiches;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.Saisie;

/** Lecture du numéro d'une fiche de l'organisation, pour les notifications. */
@Component
class NumerosFichesJpa implements NumerosFiches {

    private final SaisieRepository fiches;

    NumerosFichesJpa(SaisieRepository fiches) { this.fiches = fiches; }

    @Override
    public Optional<String> numero(Long tenantId, Long ficheId) {
        return fiches.findByIdAndTenantId(ficheId, tenantId).map(Saisie::getNumero);
    }
}
