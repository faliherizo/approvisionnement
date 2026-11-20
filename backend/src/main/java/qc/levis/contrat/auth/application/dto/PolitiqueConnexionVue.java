package qc.levis.contrat.auth.application.dto;

import java.time.Instant;
import java.util.List;

/** Politique de connexion locale de l'organisation et comptes actifs (addenda 4, Q12). */
public record PolitiqueConnexionVue(boolean reservee, String modifiePar, Instant modifieLe, List<CompteLocalVue> comptes) {
}
