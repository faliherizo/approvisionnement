package qc.levis.contrat.auth.infrastructure;

import java.util.Objects;
import org.springframework.stereotype.Component;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.messagerie.application.port.AnnuaireMembres;

/** Membres de l'organisation, d'après les comptes : un compte inactif ou d'une autre organisation n'en est pas un. */
@Component
class AnnuaireMembresComptes implements AnnuaireMembres {

    private final UserRepository comptes;

    AnnuaireMembresComptes(UserRepository comptes) { this.comptes = comptes; }

    @Override
    public boolean estMembreActif(Long tenantId, String courriel) {
        return comptes.findByEmail(courriel).filter(u -> u.isActif() && Objects.equals(u.getTenantId(), tenantId)).isPresent();
    }
}
