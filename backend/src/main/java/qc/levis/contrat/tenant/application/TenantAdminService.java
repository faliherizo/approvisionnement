package qc.levis.contrat.tenant.application;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.tenant.application.dto.TenantPublic;
import qc.levis.contrat.tenant.application.dto.TenantResume;
import qc.levis.contrat.tenant.application.dto.Theme;
import qc.levis.contrat.tenant.application.dto.ThemeRequest;
import qc.levis.contrat.tenant.application.repository.TenantRepository;
import qc.levis.contrat.tenant.domain.Tenant;

/** Gestion des tenants et de leur administrateur (réservé au superadmin). */
@Service
public class TenantAdminService {

    private static final Logger log = LoggerFactory.getLogger(TenantAdminService.class);

    private final TenantRepository tenants;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public TenantAdminService(TenantRepository tenants, UserRepository users, PasswordEncoder encoder) {
        this.tenants = tenants;
        this.users = users;
        this.encoder = encoder;
    }

    public List<Tenant> lister() { return tenants.findAll(); }

    /** Résumés publics (actifs) pour le sélecteur de connexion. */
    public List<TenantResume> listerPublic() {
        return tenants.findAll().stream()
                .filter(Tenant::isActif)
                .map(t -> new TenantResume(t.getSlug(), t.getTitre()))
                .toList();
    }

    @Transactional
    public Tenant creer(String slug, String titre, String sousTitre) {
        if (tenants.existsBySlug(slug)) {
            log.warn("Création de tenant refusée : slug déjà utilisé ({})", slug);
            throw new IllegalArgumentException("Slug déjà utilisé: " + slug);
        }
        Tenant cree = tenants.save(new Tenant(slug, titre, sousTitre));
        log.info("Tenant créé : id={}, slug={}", cree.getId(), slug);
        return cree;
    }

    @Transactional
    public Tenant mettreAJour(Long id, String titre, String sousTitre, Boolean actif) {
        Tenant t = tenants.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tenant introuvable: " + id));
        if (titre != null) t.setTitre(titre);
        if (sousTitre != null) t.setSousTitre(sousTitre);
        if (actif != null) t.setActif(actif);
        Tenant sauve = tenants.save(t);
        log.info("Tenant {} mis à jour (actif={})", id, sauve.isActif());
        return sauve;
    }

    /** Crée l'administrateur (rôle ADMIN) d'un tenant donné. */
    @Transactional
    public User creerAdminTenant(Long tenantId, String email, String nom, String motDePasse) {
        Tenant t = tenants.findById(tenantId)
                .orElseThrow(() -> new NoSuchElementException("Tenant introuvable: " + tenantId));
        if (users.existsByEmail(email)) {
            throw new IllegalArgumentException("Courriel déjà utilisé: " + email);
        }
        User u = new User();
        u.setEmail(email);
        u.setNom(nom);
        u.setMotDePasseHash(encoder.encode(motDePasse));
        u.setRoles(Set.of(Enums.Role.ADMIN));
        u.setTenantId(t.getId()); // rattachement explicite au tenant cible
        return users.save(u);
    }

    @Transactional
    public Tenant mettreAJourTheme(Long id, ThemeRequest req) {
        Tenant t = tenants.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tenant introuvable: " + id));
        t.setCouleurPrimaire(req.couleurPrimaire());
        t.setCouleurAccent(req.couleurAccent());
        t.setCouleurFond(req.couleurFond());
        t.setCouleurTexte(req.couleurTexte());
        t.setLogoTexte(req.logoTexte());
        t.setLogoImage(req.logoImage());
        return tenants.save(t);
    }

    public TenantPublic publicParSlug(String slug) {
        Tenant t = tenants.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Tenant introuvable: " + slug));
        return new TenantPublic(t.getSlug(), t.getTitre(), t.getSousTitre(), theme(t));
    }

    /** Identité publique d'un tenant actif, pour les pages sans session qui le connaissent par son identifiant. */
    public Optional<TenantPublic> publicParId(Long id) {
        return tenants.findById(id).filter(Tenant::isActif)
                .map(t -> new TenantPublic(t.getSlug(), t.getTitre(), t.getSousTitre(), theme(t)));
    }

    /** Thème du tenant, avec valeurs par défaut VINA (logo/texte de notre entreprise). */
    private Theme theme(Tenant t) {
        return new Theme(t.getCouleurPrimaire(), t.getCouleurAccent(),
                t.getCouleurFond(), t.getCouleurTexte(),
                t.getLogoTexte() != null ? t.getLogoTexte() : "VINA IT Solutions inc.",
                t.getLogoImage());
    }
}
