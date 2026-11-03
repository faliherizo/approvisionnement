package qc.levis.contrat.saisie.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Règles d'ouverture d'un formulaire (fenêtre d'activation), PURES et testables.
 * Un formulaire est ouvert si l'on est dans la fenêtre de dates et sous la capacité maximale.
 */
public final class Activation {

    private Activation() {}

    /**
     * Configuration d'activation (issue de la structure JSON du formulaire). {@code plagesReseau} = liste de
     * préfixes d'adresses autorisés (ex. « 192.168. », « 10.0.0. ») ; vide = aucune restriction réseau.
     */
    public record Config(Instant dateDebut, Instant dateFin, Integer maxRepondants, Boolean captchaRequis,
                         List<String> plagesReseau) {}

    /** Retourne la raison de fermeture, ou vide si le formulaire est ouvert. */
    public static Optional<String> raisonFermeture(Config cfg, Instant maintenant, long nbSoumissions) {
        if (cfg == null) return Optional.empty();
        if (cfg.dateDebut() != null && maintenant.isBefore(cfg.dateDebut()))
            return Optional.of("Le formulaire n'est pas encore ouvert.");
        if (cfg.dateFin() != null && maintenant.isAfter(cfg.dateFin()))
            return Optional.of("Le formulaire est fermé.");
        if (cfg.maxRepondants() != null && nbSoumissions >= cfg.maxRepondants())
            return Optional.of("La capacité maximale est atteinte.");
        return Optional.empty();
    }

    public static boolean captchaRequis(Config cfg) {
        return cfg != null && Boolean.TRUE.equals(cfg.captchaRequis());
    }

    /**
     * L'adresse IP est-elle autorisée à soumettre ? Vraie si aucune plage n'est configurée, ou si l'IP
     * commence par l'un des préfixes autorisés (correspondance par préfixe, format §Q12 à confirmer).
     */
    public static boolean ipAutorisee(Config cfg, String ip) {
        if (cfg == null || cfg.plagesReseau() == null || cfg.plagesReseau().isEmpty()) return true;
        if (ip == null || ip.isBlank()) return false;
        return cfg.plagesReseau().stream().filter(p -> p != null && !p.isBlank())
                .anyMatch(p -> ip.startsWith(p.trim()));
    }
}
