package qc.levis.contrat.commun.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

/** SEC-35 : une contrainte de la base violée par le client répond 400 ou 409, jamais 500, sans divulguer le schéma. */
class ContraintesDonneesTest {

    private final GlobalExceptionHandler gestionnaire = new GlobalExceptionHandler();

    private static DataIntegrityViolationException violation(String etat, String detail) {
        return new DataIntegrityViolationException("could not execute statement",
                new RuntimeException("enveloppe", new SQLException(detail, etat)));
    }

    @Test
    void une_valeur_trop_longue_repond_400_sans_reprendre_la_valeur_ni_la_colonne() {
        var r = gestionnaire.contrainteViolee(violation("22001", "Value too long for column \"CODE VARCHAR(80)\": '${@print(...)}'"));
        assertEquals(400, r.getStatusCode().value());
        assertFalse(r.getBody().get("message").contains("CODE"));
        assertFalse(r.getBody().get("message").contains("print"));
    }

    @Test
    void un_doublon_repond_409() {
        var r = gestionnaire.contrainteViolee(violation("23505", "Unique index or primary key violation: UQ_NOTIF_MODELE"));
        assertEquals(409, r.getStatusCode().value());
        assertFalse(r.getBody().get("message").contains("UQ_"));
    }

    @Test
    void sans_code_sqlstate_la_violation_reste_une_erreur_du_client() {
        assertEquals(400, gestionnaire.contrainteViolee(new DataIntegrityViolationException("sans cause")).getStatusCode().value());
        assertEquals("inconnu", GlobalExceptionHandler.etatSql(new RuntimeException(new SQLException("sans état"))));
    }
}
