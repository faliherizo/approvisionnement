package qc.levis.contrat.auth.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.dto.CorrespondanceRequest;
import qc.levis.contrat.auth.application.dto.CorrespondanceVue;
import qc.levis.contrat.auth.application.repository.AppartenanceAnnuaireRepository;
import qc.levis.contrat.auth.application.repository.CorrespondanceAnnuaireRepository;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.AppartenanceAnnuaire;
import qc.levis.contrat.auth.domain.CorrespondanceAnnuaire;
import qc.levis.contrat.auth.domain.FaitAnnuaire;
import qc.levis.contrat.auth.domain.ReglesAnnuaire;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.application.AppartenancesAnnuaire;
import qc.levis.contrat.commun.domain.Enums;

/**
 * Annuaire d'entreprise (addenda 3, Q43-Q44) : correspondances configurables des groupes et attributs (Microsoft Entra ID,
 * Saviynt) vers les rôles et les domaines d'affaires, appliquées à chaque connexion unique et à chaque poussée SCIM.
 * Tout changement de rôle est consigné au journal d'audit avec l'avant et l'après.
 */
@Service
public class AnnuaireService {

    /** Rôles qu'une correspondance peut donner : jamais celui de superadministrateur, hors organisation. */
    private static final Set<Enums.Role> ASSIGNABLES = EnumSet.complementOf(EnumSet.of(Enums.Role.SUPERADMIN));

    private final CorrespondanceAnnuaireRepository correspondances;
    private final AppartenanceAnnuaireRepository appartenances;
    private final UserRepository users;
    private final AuditService audit;
    private final ApplicationEventPublisher evenements;
    private final Clock horloge;

    public AnnuaireService(CorrespondanceAnnuaireRepository correspondances, AppartenanceAnnuaireRepository appartenances,
                           UserRepository users, AuditService audit, ApplicationEventPublisher evenements, Clock horloge) {
        this.correspondances = correspondances;
        this.appartenances = appartenances;
        this.users = users;
        this.audit = audit;
        this.evenements = evenements;
        this.horloge = horloge;
    }

    // --- Correspondances (administration de l'organisation) ---

    public List<CorrespondanceVue> correspondances(Long tenant) {
        return correspondances.findByTenantIdOrderByIdAsc(tenant).stream().map(AnnuaireService::vue).toList();
    }

    @Transactional
    public CorrespondanceVue ajouter(Long tenant, CorrespondanceRequest req, String par) {
        CorrespondanceAnnuaire c = CorrespondanceAnnuaire.definir(tenant, source(req.source()), req.attribut(), req.valeur(),
                role(req.role()), req.groupe(), par, Instant.now(horloge));
        c = correspondances.save(c);
        audit.log(tenant, par, "ANNUAIRE_CORRESPONDANCE_AJOUTEE", "CorrespondanceAnnuaire", c.getId(), resume(c));
        return vue(c);
    }

    @Transactional
    public void supprimer(Long tenant, Long id, String par) {
        CorrespondanceAnnuaire c = correspondances.findByIdAndTenantId(id, tenant)
                .orElseThrow(() -> new NoSuchElementException("Correspondance introuvable."));
        correspondances.delete(c);
        audit.log(tenant, par, "ANNUAIRE_CORRESPONDANCE_RETIREE", "CorrespondanceAnnuaire", id, resume(c));
    }

    // --- Application aux comptes ---

    /**
     * Remplace les faits d'une origine pour le compte, puis recalcule rôles et domaines d'affaires. Pour la connexion unique,
     * seuls les groupes et les attributs cités par une correspondance sont conservés.
     */
    @Transactional
    public void synchroniser(User u, AppartenanceAnnuaire.Origine origine, Collection<FaitAnnuaire> faits, String roleParDefaut) {
        Long tenant = u.getTenantId();
        List<CorrespondanceAnnuaire> regles = correspondances.findByTenantIdOrderByIdAsc(tenant);
        Set<String> attributsCites = regles.stream().filter(r -> r.getAttribut() != null)
                .map(r -> r.getAttribut().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
        appartenances.deleteByTenantIdAndCourrielAndOrigine(tenant, u.getEmail(), origine);
        faits.stream()
                .filter(f -> f.source() == FaitAnnuaire.Source.GROUPE || attributsCites.contains(f.attribut().toLowerCase(Locale.ROOT)))
                .collect(Collectors.toMap(FaitAnnuaire::cle, f -> f, (a, b) -> a)).values()
                .forEach(f -> appartenances.save(new AppartenanceAnnuaire(tenant, u.getEmail(), origine, f)));
        appliquer(u, regles, roleParDefaut);
    }

    private void appliquer(User u, List<CorrespondanceAnnuaire> regles, String roleParDefaut) {
        List<FaitAnnuaire> faits = appartenances.findByTenantIdAndCourriel(u.getTenantId(), u.getEmail()).stream()
                .map(AppartenanceAnnuaire::fait).toList();
        ReglesAnnuaire.Resultat r = ReglesAnnuaire.appliquer(regles, faits, roleParDefaut);
        if (r.rolesGeres()) {
            Set<Enums.Role> roles = r.roles().stream().map(Enums.Role::valueOf).collect(Collectors.toCollection(() -> EnumSet.noneOf(Enums.Role.class)));
            if (!roles.equals(u.getRoles())) {
                String avant = u.getRoles().stream().map(Enum::name).sorted().collect(Collectors.joining(","));
                u.setRoles(roles);
                users.save(u);
                audit.log(u.getTenantId(), "annuaire", "ROLES_ANNUAIRE", "User", u.getId(),
                        u.getEmail() + " : " + avant + " → " + String.join(",", r.roles()));
            }
        }
        if (!r.groupesGeres().isEmpty()) {
            evenements.publishEvent(new AppartenancesAnnuaire(u.getTenantId(), u.getEmail(), r.groupes(), r.groupesGeres()));
        }
    }

    private static FaitAnnuaire.Source source(String saisie) {
        try {
            return FaitAnnuaire.Source.valueOf(saisie == null ? "" : saisie.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Source : GROUPE ou ATTRIBUT.", e);
        }
    }

    private static String role(String saisie) {
        if (saisie == null || saisie.isBlank()) {
            return null;
        }
        String code = saisie.strip().toUpperCase(Locale.ROOT);
        if (ASSIGNABLES.stream().noneMatch(r -> r.name().equals(code))) {
            throw new IllegalArgumentException("Rôle inconnu ou non attribuable : " + code + ".");
        }
        return code;
    }

    private static String resume(CorrespondanceAnnuaire c) {
        return c.getSource() + (c.getAttribut() == null ? "" : " " + c.getAttribut()) + " = " + c.getValeur() + " → "
                + (c.getRole() == null ? "" : "rôle " + c.getRole() + " ") + (c.getGroupe() == null ? "" : "domaine " + c.getGroupe());
    }

    private static CorrespondanceVue vue(CorrespondanceAnnuaire c) {
        return new CorrespondanceVue(c.getId(), c.getSource().name(), c.getAttribut(), c.getValeur(), c.getRole(), c.getGroupe(),
                c.getModifiePar(), c.getModifieLe());
    }
}
