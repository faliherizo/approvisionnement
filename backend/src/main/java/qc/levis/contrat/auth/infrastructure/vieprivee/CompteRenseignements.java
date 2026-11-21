package qc.levis.contrat.auth.infrastructure.vieprivee;

import java.util.Objects;
import java.util.Optional;
import java.util.List;
import org.springframework.stereotype.Component;
import qc.levis.contrat.vieprivee.application.port.SourceRenseignementsPersonnels;
import qc.levis.contrat.vieprivee.domain.BilanEffacement;
import qc.levis.contrat.vieprivee.domain.ExtraitRenseignements;
import qc.levis.contrat.vieprivee.domain.Pseudonyme;
import qc.levis.contrat.vieprivee.domain.Renseignement;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.vieprivee.domain.EffacementRefuse;

/** Compte de la personne : identité, rôles et moyens d'authentification (jamais les secrets eux-mêmes). */
@Component
class CompteRenseignements implements SourceRenseignementsPersonnels {

    /** Aucune empreinte BCrypt ne vaut ce texte : plus aucun mot de passe n'ouvre le compte. */
    static final String EMPREINTE_INUTILISABLE = "!compte-efface";

    private final UserRepository comptes;

    CompteRenseignements(UserRepository comptes) { this.comptes = comptes; }

    @Override
    public int ordre() { return 100; }

    private Optional<User> compte(Long tenantId, String courriel) {
        return comptes.findByEmail(courriel).filter(u -> Objects.equals(u.getTenantId(), tenantId));
    }

    @Override
    public ExtraitRenseignements extraire(Long tenantId, String courriel) {
        List<Renseignement> elements = compte(tenantId, courriel).stream()
                .map(u -> Renseignement.de("compte/" + u.getId(), null, "courriel", u.getEmail(), "nom", u.getNom(),
                        "direction", u.getDirection(), "roles", u.getRoles().toString(), "actif", String.valueOf(u.isActif()),
                        "second facteur", u.getMfaMethode().name()))
                .toList();
        return new ExtraitRenseignements("Compte", "Identité et droits d'accès à la plateforme.",
                "Le compte est désactivé, sans moyen de connexion, et ne porte plus ni nom ni courriel.", elements);
    }

    @Override
    public BilanEffacement effacer(Long tenantId, String courriel, Pseudonyme pseudonyme) {
        Optional<User> u = compte(tenantId, courriel);
        if (u.isEmpty()) return BilanEffacement.complet("Compte", 0);
        exigerUnAutreAdministrateur(tenantId, u.get());
        u.get().effacer(pseudonyme.courriel(), EMPREINTE_INUTILISABLE);
        comptes.save(u.get());
        return BilanEffacement.complet("Compte", 1);
    }

    private void exigerUnAutreAdministrateur(Long tenantId, User u) {
        if (!u.getRoles().contains(Enums.Role.ADMIN)) return;
        boolean autre = comptes.findByTenantIdAndActifTrueOrderByNomAsc(tenantId).stream()
                .anyMatch(x -> !x.getId().equals(u.getId()) && x.getRoles().contains(Enums.Role.ADMIN));
        if (!autre) throw new EffacementRefuse("Effacement refusé : l'organisation resterait sans administrateur actif.");
    }
}
