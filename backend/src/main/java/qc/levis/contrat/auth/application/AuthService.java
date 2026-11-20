package qc.levis.contrat.auth.application;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.auth.application.repository.UserRepository;
import qc.levis.contrat.auth.domain.SecurityQuestion;
import qc.levis.contrat.auth.application.repository.PolitiqueConnexionRepository;
import qc.levis.contrat.auth.domain.PolitiqueConnexion;
import qc.levis.contrat.auth.domain.User;
import qc.levis.contrat.commun.domain.Enums;

/**
 * Orchestration de l'authentification : mot de passe + second facteur
 * (TOTP ou question secrète dynamique). Émet un jeton JWT en cas de succès.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Résultat d'une tentative d'authentification. */
    public record Outcome(Status status, User user, String token,
                          String methode, List<String> questions, String erreur) {
        public enum Status { SUCCES, DEFI_MFA, ECHEC }
    }

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final TotpService totp;
    private final AuditService audit;
    private final PolitiqueConnexionRepository politiques;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt,
                       TotpService totp, AuditService audit,
                       PolitiqueConnexionRepository politiques) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.totp = totp;
        this.audit = audit;
        this.politiques = politiques;
    }

    public Outcome authentifier(String email, String motDePasse,
                                String totpCode, List<String> reponses) {
        User user = users.findByEmail(email).orElse(null);
        if (user == null || !user.isActif() || !encoder.matches(motDePasse, user.getMotDePasseHash())) {
            audit.log(user != null ? user.getTenantId() : null, email, "CONNEXION_ECHEC", "User", user != null ? user.getId() : null, "Identifiants invalides");
            log.warn("Échec de connexion pour {} : identifiants invalides", masquer(email)); // jamais le mot de passe
            return new Outcome(Outcome.Status.ECHEC, null, null, null, null, "Identifiants invalides");
        }

        Outcome refus = refusLocal(user, email);
        if (refus != null) {
            return refus;
        }
        if (!user.isMfaActive() || user.getMfaMethode() == Enums.MfaMethode.NONE) {
            return succes(user);
        }

        if (user.getMfaMethode() == Enums.MfaMethode.TOTP) {
            if (estVide(totpCode)) {
                return new Outcome(Outcome.Status.DEFI_MFA, null, null, "totp", List.of(), null);
            }
            if (!totp.verifier(user.getTotpSecret(), totpCode)) {
                audit.log(user.getTenantId(), email, "CONNEXION_ECHEC", "User", user.getId(), "Code TOTP invalide");
                return new Outcome(Outcome.Status.ECHEC, null, null, null, null, "Code TOTP invalide");
            }
            return succes(user);
        }

        // Méthode QUESTION : sélection dynamique (change chaque jour) d'une question.
        SecurityQuestion q = questionDuJour(user);
        if (q == null) {
            return succes(user); // MFA question activée mais aucune question définie
        }
        if (reponses == null || reponses.isEmpty() || estVide(reponses.get(0))) {
            return new Outcome(Outcome.Status.DEFI_MFA, null, null, "question", List.of(q.getQuestion()), null);
        }
        if (!encoder.matches(normaliser(reponses.get(0)), q.getReponseHash())) {
            audit.log(user.getTenantId(), email, "CONNEXION_ECHEC", "User", user.getId(), "Réponse de sécurité invalide");
            return new Outcome(Outcome.Status.ECHEC, null, null, null, null, "Réponse de sécurité invalide");
        }
        return succes(user);
    }

    /** Journalise une déconnexion (exigence 9 : journaux d'accès complets), scopée au tenant du compte. */
    public void journaliserDeconnexion(String email) {
        User u = users.findByEmail(email).orElse(null);
        audit.log(u != null ? u.getTenantId() : null, email, "DECONNEXION", "User",
                u != null ? u.getId() : null, null);
        log.info("Déconnexion de l'utilisateur {} (tenant {})",
                u != null ? u.getId() : "inconnu", u != null ? u.getTenantId() : null);
    }

    /** Question secrète dynamique : dépend du jour, reproductible sur la journée. */
    public SecurityQuestion questionDuJour(User user) {
        List<SecurityQuestion> qs = user.getQuestionsSecurite();
        if (qs == null || qs.isEmpty()) return null;
        int index = LocalDate.now().getDayOfYear() % qs.size();
        return qs.get(index);
    }

    public String hacher(String valeur) {
        return encoder.encode(normaliser(valeur));
    }

    /**
     * Connexion locale réservée aux comptes de secours (addenda 4, Q12) : tout autre compte de l'organisation passe par
     * l'authentification unique ; un compte de secours sans second facteur est refusé. Chaque refus est journalisé.
     */
    private Outcome refusLocal(User user, String email) {
        PolitiqueConnexion.Refus refus = user.getTenantId() == null ? null : politiques.findById(user.getTenantId())
                .map(p -> p.refus(user.isCompteSecours(), user.isMfaActive() && user.getMfaMethode() != Enums.MfaMethode.NONE))
                .orElse(null);
        if (refus == null) {
            return null;
        }
        String motif = refus == PolitiqueConnexion.Refus.RESERVEE_AUX_SECOURS
                ? "Connexion locale réservée aux comptes de secours : utilisez l'authentification unique de l'organisation."
                : "Compte de secours sans second facteur : activez-le avant de vous connecter.";
        audit.log(user.getTenantId(), email, "CONNEXION_LOCALE_REFUSEE", "User", user.getId(), refus.name());
        log.warn("Connexion locale refusée pour {} : {}", masquer(email), refus);
        return new Outcome(Outcome.Status.ECHEC, null, null, null, null, motif);
    }

    private Outcome succes(User user) {
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();
        String token = jwt.genererToken(user.getEmail(), roles, user.getTenantId());
        audit.log(user.getTenantId(), user.getEmail(), user.isCompteSecours() ? "CONNEXION_SECOURS" : "LOGIN", "User", user.getId(), null);
        // On journalise l'identifiant technique et les rôles, jamais le jeton ni le mot de passe.
        log.info("Connexion réussie : utilisateur {} (tenant {}, rôles {})",
                user.getId(), user.getTenantId(), roles);
        return new Outcome(Outcome.Status.SUCCES, user, token, null, null, null);
    }

    private boolean estVide(String s) { return s == null || s.isBlank(); }

    /** Masque un courriel dans les journaux : « j***@domaine » (jamais l'adresse complète). */
    private String masquer(String email) {
        return qc.levis.contrat.commun.application.LogSafe.courriel(email);
    }

    /** Normalise une réponse secrète (insensible à la casse et aux espaces). */
    private String normaliser(String s) { return s == null ? "" : s.trim().toLowerCase(); }
}
