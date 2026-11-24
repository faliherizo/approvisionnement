package qc.levis.contrat.auth.application.dto;

import java.time.Instant;

/** Politique de session de l'organisation : propre (avec auteur et date) ou héritée de la plateforme. */
public record PolitiqueSessionVue(long inactiviteMinutes, long dureeMaxMinutes, boolean propre, String modifiePar, Instant modifieLe) {
}
