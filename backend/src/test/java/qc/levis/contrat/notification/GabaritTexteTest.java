package qc.levis.contrat.notification;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.notification.application.GabaritTexte;

/** Tests purs de la fusion de gabarit. */
class GabaritTexteTest {

    @Test
    void remplace_les_marqueurs() {
        String r = GabaritTexte.fusionner("Bonjour {{nom}}, dossier {{no}}", Map.of("nom", "Alice", "no", "A-12"));
        assertEquals("Bonjour Alice, dossier A-12", r);
    }

    @Test
    void marqueur_absent_devient_vide() {
        assertEquals("X=", GabaritTexte.fusionner("X={{manquant}}", Map.of()));
    }

    @Test
    void gabarit_null_ou_sans_marqueur() {
        assertEquals("", GabaritTexte.fusionner(null, Map.of()));
        assertEquals("texte simple", GabaritTexte.fusionner("texte simple", Map.of("a", "b")));
    }
}
