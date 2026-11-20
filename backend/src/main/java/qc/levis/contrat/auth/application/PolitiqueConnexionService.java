package qc.levis.contrat.auth.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.dto.CompteLocalVue;
import qc.levis.contrat.auth.application.dto.PolitiqueConnexionVue;
import qc.levis.contrat.auth.application.repository.PolitiqueConnexionRepository;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.PolitiqueConnexion;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;

/**
 * Comptes locaux de secours (addenda 4, Q12) : l'administration technique désigne les comptes de secours et peut réserver
 * la connexion locale à eux seuls. Pour ne jamais verrouiller l'organisation, la réservation exige au moins un compte de
 * secours actif muni d'un second facteur, et le dernier ne peut pas être retiré tant qu'elle est en vigueur.
 */
@Service
public class PolitiqueConnexionService {

    private final PolitiqueConnexionRepository politiques;
    private final UserRepository users;
    private final AuditService audit;
    private final Clock horloge;

    public PolitiqueConnexionService(PolitiqueConnexionRepository politiques, UserRepository users, AuditService audit, Clock horloge) {
        this.politiques = politiques;
        this.users = users;
        this.audit = audit;
        this.horloge = horloge;
    }

    public PolitiqueConnexionVue etat(Long tenant) {
        PolitiqueConnexion p = politiques.findById(tenant).orElse(null);
        List<CompteLocalVue> comptes = users.findByTenantIdAndActifTrueOrderByNomAsc(tenant).stream()
                .map(u -> new CompteLocalVue(u.getId(), u.getEmail(), u.getNom(), secondFacteur(u), u.isCompteSecours())).toList();
        return new PolitiqueConnexionVue(p != null && p.isLocaleReservee(), p == null ? null : p.getModifiePar(),
                p == null ? null : p.getModifieLe(), comptes);
    }

    @Transactional
    public PolitiqueConnexionVue reserver(Long tenant, boolean reservee, String par) {
        if (reservee && secoursValides(tenant) == 0) {
            throw new IllegalStateException("Désignez d'abord un compte de secours actif muni d'un second facteur : sinon personne ne pourrait "
                    + "se connecter si l'authentification unique tombait.");
        }
        PolitiqueConnexion p = politiques.findById(tenant).orElseGet(() -> new PolitiqueConnexion(tenant));
        p.reserver(reservee, par, Instant.now(horloge));
        politiques.save(p);
        audit.log(tenant, par, "POLITIQUE_CONNEXION_LOCALE", "PolitiqueConnexion", tenant,
                reservee ? "réservée aux comptes de secours" : "ouverte à tous les comptes");
        return etat(tenant);
    }

    @Transactional
    public PolitiqueConnexionVue designer(Long tenant, Long utilisateur, boolean secours, String par) {
        User u = users.findById(utilisateur).filter(x -> tenant.equals(x.getTenantId()))
                .orElseThrow(() -> new NoSuchElementException("Compte introuvable."));
        boolean reservee = politiques.findById(tenant).map(PolitiqueConnexion::isLocaleReservee).orElse(false);
        if (!secours && reservee && u.isCompteSecours() && secondFacteur(u) && secoursValides(tenant) <= 1) {
            throw new IllegalStateException("Dernier compte de secours valide : levez d'abord la réservation de la connexion locale.");
        }
        u.setCompteSecours(secours);
        users.save(u);
        audit.log(tenant, par, secours ? "COMPTE_SECOURS_DESIGNE" : "COMPTE_SECOURS_RETIRE", "User", u.getId(), u.getEmail());
        return etat(tenant);
    }

    private long secoursValides(Long tenant) {
        return users.findByTenantIdAndActifTrueOrderByNomAsc(tenant).stream().filter(u -> u.isCompteSecours() && secondFacteur(u)).count();
    }

    private static boolean secondFacteur(User u) {
        return u.isMfaActive() && u.getMfaMethode() != Enums.MfaMethode.NONE;
    }
}
