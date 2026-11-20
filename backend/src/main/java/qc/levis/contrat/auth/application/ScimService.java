package qc.levis.contrat.auth.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.dto.JetonScimVue;
import qc.levis.contrat.auth.application.dto.ScimGroupe;
import qc.levis.contrat.auth.application.dto.ScimGroupeVue;
import qc.levis.contrat.auth.application.dto.ScimUtilisateur;
import qc.levis.contrat.auth.application.dto.ScimUtilisateurVue;
import qc.levis.contrat.auth.application.repository.GroupeScimRepository;
import qc.levis.contrat.auth.application.repository.JetonScimRepository;
import qc.levis.contrat.auth.application.repository.MembreGroupeScimRepository;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.AppartenanceAnnuaire;
import qc.levis.contrat.auth.domain.FaitAnnuaire;
import qc.levis.contrat.auth.domain.GroupeScim;
import qc.levis.contrat.auth.domain.JetonScim;
import qc.levis.contrat.auth.domain.MembreGroupeScim;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;

/**
 * Provisionnement et déprovisionnement des comptes par SCIM 2.0 (RFC 7643, 7644 ; addenda 3, Q51), poussés par Saviynt.
 * Un compte créé reçoit le rôle le plus restreint, puis ses rôles et domaines d'affaires suivent ses groupes par les
 * correspondances d'annuaire. Le déprovisionnement désactive le compte sans l'effacer : la piste d'audit reste entière.
 */
@Service
public class ScimService {

    /** Moindre privilège : le rôle d'un compte provisionné tant qu'aucune correspondance ne lui en donne un autre. */
    public static final String ROLE_INITIAL = Enums.Role.LECTEUR.name();
    public static final class Conflit extends RuntimeException {
        public Conflit(String message) {
            super(message);
        }
    }

    private final UserRepository users;
    private final GroupeScimRepository groupes;
    private final MembreGroupeScimRepository membres;
    private final JetonScimRepository jetons;
    private final AnnuaireService annuaire;
    private final qc.levis.contrat.auth.application.repository.AppartenanceAnnuaireRepository appartenances;
    private final PasswordEncoder encoder;
    private final AuditService audit;
    private final Clock horloge;
    private final SecureRandom alea = new SecureRandom();

    public ScimService(UserRepository users, GroupeScimRepository groupes, MembreGroupeScimRepository membres, JetonScimRepository jetons,
                       AnnuaireService annuaire, PasswordEncoder encoder, AuditService audit, Clock horloge,
                       qc.levis.contrat.auth.application.repository.AppartenanceAnnuaireRepository appartenances) {
        this.appartenances = appartenances;
        this.users = users;
        this.groupes = groupes;
        this.membres = membres;
        this.jetons = jetons;
        this.annuaire = annuaire;
        this.encoder = encoder;
        this.audit = audit;
        this.horloge = horloge;
    }

    // --- Jeton porteur ---

    /** Organisation du jeton présenté ; vide si le jeton est inconnu ou révoqué. */
    public Optional<Long> organisation(String jeton) {
        return jeton == null || jeton.isBlank() ? Optional.empty() : jetons.findByEmpreinte(empreinte(jeton)).map(JetonScim::getTenantId);
    }

    @Transactional
    public JetonScimVue creerJeton(Long tenant, String par, String point) {
        byte[] octets = new byte[32];
        alea.nextBytes(octets);
        String jeton = "scim_" + Base64.getUrlEncoder().withoutPadding().encodeToString(octets);
        Instant maintenant = Instant.now(horloge);
        JetonScim j = jetons.findById(tenant).orElseGet(() -> new JetonScim(tenant, empreinte(jeton), par, maintenant));
        j.remplacer(empreinte(jeton), par, maintenant);
        jetons.save(j);
        audit.log(tenant, par, "SCIM_JETON_CREE", "JetonScim", tenant, "jeton précédent révoqué");
        return new JetonScimVue(true, jeton, par, maintenant, point);
    }

    public JetonScimVue etatJeton(Long tenant, String point) {
        return jetons.findById(tenant).map(j -> new JetonScimVue(true, null, j.getCreePar(), j.getCreeLe(), point))
                .orElse(new JetonScimVue(false, null, null, null, point));
    }

    // --- Utilisateurs ---

    @Transactional
    public ScimUtilisateurVue creerUtilisateur(Long tenant, ScimUtilisateur in) {
        String courriel = courriel(in.userName());
        if (users.existsByEmail(courriel)) {
            throw new Conflit("userName déjà utilisé : " + courriel);
        }
        User u = new User();
        u.setEmail(courriel);
        u.setNom(in.nom() == null || in.nom().isBlank() ? courriel : in.nom().strip());
        u.setTenantId(tenant);
        u.setActif(in.actif() == null || in.actif());
        u.setRoles(Set.of(Enums.Role.valueOf(ROLE_INITIAL)));
        // Compte fédéré : aucun mot de passe local exploitable.
        u.setMotDePasseHash(encoder.encode(UUID.randomUUID().toString()));
        u = users.save(u);
        audit.log(tenant, "scim", "PROVISION_SCIM", "User", u.getId(), courriel);
        annuaire.synchroniser(u, AppartenanceAnnuaire.Origine.SCIM, List.of(), ROLE_INITIAL);
        attributs(u, in);
        return vue(u);
    }

    public ScimUtilisateurVue utilisateur(Long tenant, String id) {
        return vue(exigerUtilisateur(tenant, id));
    }

    /** Liste, filtrée au besoin par {@code userName eq "…"}, le seul filtre qu'emploient Saviynt et Entra ID. */
    public List<ScimUtilisateurVue> utilisateurs(Long tenant, String userName) {
        if (userName != null) {
            return users.findByEmail(userName.strip().toLowerCase(Locale.ROOT)).filter(u -> tenant.equals(u.getTenantId()))
                    .map(u -> List.of(vue(u))).orElse(List.of());
        }
        return users.findByTenantIdOrderByEmailAsc(tenant).stream().map(this::vue).toList();
    }

    @Transactional
    public ScimUtilisateurVue modifierUtilisateur(Long tenant, String id, ScimUtilisateur in) {
        User u = exigerUtilisateur(tenant, id);
        if (in.nom() != null && !in.nom().isBlank()) {
            u.setNom(in.nom().strip());
        }
        if (in.actif() != null && in.actif() != u.isActif()) {
            u.setActif(in.actif());
            audit.log(tenant, "scim", in.actif() ? "REACTIVATION_SCIM" : "DEPROVISION_SCIM", "User", u.getId(), u.getEmail());
        }
        User sauve = users.save(u);
        attributs(sauve, in);
        return vue(sauve);
    }

    /** Déprovisionnement : compte désactivé, appartenances retirées, rôles et domaines recalculés ; rien n'est effacé. */
    @Transactional
    public void desactiverUtilisateur(Long tenant, String id) {
        User u = exigerUtilisateur(tenant, id);
        u.setActif(false);
        users.save(u);
        membres.findByTenantIdAndUtilisateurId(tenant, u.getId()).forEach(membres::delete);
        audit.log(tenant, "scim", "DEPROVISION_SCIM", "User", u.getId(), u.getEmail());
        annuaire.synchroniser(u, AppartenanceAnnuaire.Origine.SCIM, List.of(), ROLE_INITIAL);
    }

    /** Attributs SCIM déjà connus d'un compte, pour compléter un PATCH partiel. */
    public java.util.Map<String, String> attributs(Long tenant, String id) {
        User u = exigerUtilisateur(tenant, id);
        java.util.Map<String, String> m = new java.util.LinkedHashMap<>();
        appartenances.findByTenantIdAndCourriel(tenant, u.getEmail()).stream()
                .filter(a -> a.getOrigine() == AppartenanceAnnuaire.Origine.SCIM_ATTRIBUT)
                .map(AppartenanceAnnuaire::fait).forEach(f -> m.put(f.attribut(), f.valeur()));
        return m;
    }

    // --- Groupes ---

    @Transactional
    public ScimGroupeVue creerGroupe(Long tenant, ScimGroupe in) {
        if (in.nom() != null && groupes.existsByTenantIdAndNomIgnoreCase(tenant, in.nom().strip())) {
            throw new Conflit("displayName déjà utilisé : " + in.nom());
        }
        GroupeScim g = groupes.save(new GroupeScim(UUID.randomUUID().toString(), tenant, in.nom(), in.idExterne()));
        if (in.membres() != null) {
            remplacerMembres(g, in.membres());
        }
        return vue(g);
    }

    public ScimGroupeVue groupe(Long tenant, String id) {
        return vue(exigerGroupe(tenant, id));
    }

    public List<ScimGroupeVue> groupes(Long tenant, String nom) {
        return groupes.findByTenantIdOrderByNomAsc(tenant).stream().filter(g -> nom == null || g.getNom().equalsIgnoreCase(nom.strip()))
                .map(this::vue).toList();
    }

    /** PUT : nom et, si fournis, membres remplacés en entier. */
    @Transactional
    public ScimGroupeVue remplacerGroupe(Long tenant, String id, ScimGroupe in) {
        GroupeScim g = exigerGroupe(tenant, id);
        List<Long> touches = membresDe(g);
        g.renommer(in.nom() == null ? g.getNom() : in.nom(), in.idExterne() == null ? g.getIdExterne() : in.idExterne());
        groupes.save(g);
        if (in.membres() != null) {
            remplacerMembres(g, in.membres());
        } else {
            touches.forEach(u -> resynchroniser(tenant, u));
        }
        return vue(g);
    }

    /** PATCH : ajout et retrait de membres, renommage. */
    @Transactional
    public ScimGroupeVue modifierMembres(Long tenant, String id, List<String> ajouts, List<String> retraits, String nouveauNom) {
        GroupeScim g = exigerGroupe(tenant, id);
        Set<Long> touches = new java.util.HashSet<>();
        if (nouveauNom != null) {
            g.renommer(nouveauNom, g.getIdExterne());
            groupes.save(g);
            touches.addAll(membresDe(g));
        }
        for (String m : ajouts) {
            User u = exigerUtilisateur(tenant, m);
            if (!membres.existsByTenantIdAndGroupeIdAndUtilisateurId(tenant, g.getId(), u.getId())) {
                membres.save(new MembreGroupeScim(tenant, g.getId(), u.getId()));
            }
            touches.add(u.getId());
        }
        for (String m : retraits) {
            User u = exigerUtilisateur(tenant, m);
            membres.deleteByTenantIdAndGroupeIdAndUtilisateurId(tenant, g.getId(), u.getId());
            touches.add(u.getId());
        }
        touches.forEach(u -> resynchroniser(tenant, u));
        return vue(g);
    }

    @Transactional
    public void supprimerGroupe(Long tenant, String id) {
        GroupeScim g = exigerGroupe(tenant, id);
        List<Long> touches = membresDe(g);
        membres.deleteByTenantIdAndGroupeId(tenant, g.getId());
        groupes.delete(g);
        touches.forEach(u -> resynchroniser(tenant, u));
    }

    private void remplacerMembres(GroupeScim g, List<String> ids) {
        Set<Long> avant = new java.util.HashSet<>(membresDe(g));
        membres.deleteByTenantIdAndGroupeId(g.getTenantId(), g.getId());
        for (String id : ids) {
            User u = exigerUtilisateur(g.getTenantId(), id);
            membres.save(new MembreGroupeScim(g.getTenantId(), g.getId(), u.getId()));
            avant.add(u.getId());
        }
        avant.forEach(u -> resynchroniser(g.getTenantId(), u));
    }

    /**
     * Attributs poussés (département, titre, division…) : faits d'annuaire de l'origine SCIM_ATTRIBUT, que les
     * correspondances traduisent en rôles et domaines d'affaires comme les attributs de la connexion unique (addenda 4, Q31).
     */
    private void attributs(User u, ScimUtilisateur in) {
        if (in.attributs() == null) {
            return;
        }
        List<FaitAnnuaire> faits = in.attributs().entrySet().stream().filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .map(e -> FaitAnnuaire.attribut(e.getKey(), e.getValue())).toList();
        annuaire.synchroniser(u, AppartenanceAnnuaire.Origine.SCIM_ATTRIBUT, faits, ROLE_INITIAL);
    }

    /** Faits SCIM d'un compte : les groupes dont il est membre ; recalcule ses rôles et domaines d'affaires. */
    private void resynchroniser(Long tenant, Long utilisateurId) {
        User u = users.findById(utilisateurId).orElseThrow();
        List<String> ids = membres.findByTenantIdAndUtilisateurId(tenant, utilisateurId).stream().map(MembreGroupeScim::getGroupeId).toList();
        List<FaitAnnuaire> faits = new ArrayList<>();
        groupes.findByTenantIdAndIdIn(tenant, ids).forEach(g -> faits.addAll(g.faits()));
        annuaire.synchroniser(u, AppartenanceAnnuaire.Origine.SCIM, faits, ROLE_INITIAL);
    }

    private List<Long> membresDe(GroupeScim g) {
        return membres.findByTenantIdAndGroupeId(g.getTenantId(), g.getId()).stream().map(MembreGroupeScim::getUtilisateurId).toList();
    }

    private User exigerUtilisateur(Long tenant, String id) {
        try {
            return users.findById(Long.parseLong(id)).filter(u -> tenant.equals(u.getTenantId()))
                    .orElseThrow(() -> new NoSuchElementException("Utilisateur SCIM introuvable : " + id));
        } catch (NumberFormatException e) {
            throw new NoSuchElementException("Utilisateur SCIM introuvable : " + id);
        }
    }

    private GroupeScim exigerGroupe(Long tenant, String id) {
        return groupes.findByIdAndTenantId(id, tenant).orElseThrow(() -> new NoSuchElementException("Groupe SCIM introuvable : " + id));
    }

    private ScimUtilisateurVue vue(User u) {
        List<String> ids = membres.findByTenantIdAndUtilisateurId(u.getTenantId(), u.getId()).stream().map(MembreGroupeScim::getGroupeId).toList();
        return new ScimUtilisateurVue(String.valueOf(u.getId()), u.getEmail(), u.getNom(), u.isActif(), ids);
    }

    private ScimGroupeVue vue(GroupeScim g) {
        return new ScimGroupeVue(g.getId(), g.getNom(), g.getIdExterne(), membresDe(g).stream().map(String::valueOf).toList());
    }

    private static String courriel(String userName) {
        String c = userName == null ? "" : userName.strip().toLowerCase(Locale.ROOT);
        if (!c.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("userName : adresse courriel requise.");
        }
        return c;
    }

    static String empreinte(String jeton) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(jeton.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
