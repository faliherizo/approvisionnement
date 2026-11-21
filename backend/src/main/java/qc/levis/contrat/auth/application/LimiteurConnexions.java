package qc.levis.contrat.auth.application;

import java.time.Clock;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import qc.levis.contrat.audit.application.AuditService;

/**
 * Frein à la force brute (OWASP A07) : au-delà d'un nombre d'échecs de connexion pour un même courriel dans une fenêtre
 * glissante, toute tentative est suspendue jusqu'à ce que la fenêtre s'écoule, même avec le bon mot de passe. Les échecs
 * sont lus dans le journal d'audit : le compte est commun à toutes les instances du serveur. Un courriel inconnu est
 * freiné comme un compte existant, pour ne pas révéler quels comptes existent.
 */
@Component
public class LimiteurConnexions {

    static final String ECHEC = "CONNEXION_ECHEC";

    private final AuditService audit;
    private final Clock horloge;
    private final int echecsMax;
    private final Duration fenetre;

    public LimiteurConnexions(AuditService audit, Clock horloge,
                              @Value("${app.securite.connexion.echecs-max:10}") int echecsMax,
                              @Value("${app.securite.connexion.fenetre-minutes:15}") long fenetreMinutes) {
        this.audit = audit;
        this.horloge = horloge;
        this.echecsMax = echecsMax;
        this.fenetre = Duration.ofMinutes(fenetreMinutes);
    }

    public boolean estSuspendu(String courriel) {
        if (courriel == null || courriel.isBlank()) return false;
        return audit.compter(courriel.strip(), ECHEC, horloge.instant().minus(fenetre)) >= echecsMax;
    }

    public long fenetreMinutes() {
        return fenetre.toMinutes();
    }
}
