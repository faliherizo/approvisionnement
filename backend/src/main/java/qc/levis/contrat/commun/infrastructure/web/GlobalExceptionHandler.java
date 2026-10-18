package qc.levis.contrat.commun.infrastructure.web;

import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions métier en réponses JSON {@code { "message": ... }} avec un
 * statut HTTP explicite, pour que le frontend affiche un message clair (SRP).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> introuvable(NoSuchElementException e) {
        log.debug("Ressource introuvable (404) : {}", e.getMessage());
        return corps(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /** Règle métier violée (ex. clause normative verrouillée). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflit(IllegalStateException e) {
        log.warn("Conflit métier (409) : {}", e.getMessage());
        return corps(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> requeteInvalide(IllegalArgumentException e) {
        log.debug("Requête invalide (400) : {}", e.getMessage());
        return corps(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private ResponseEntity<Map<String, String>> corps(HttpStatus statut, String message) {
        return ResponseEntity.status(statut).body(Map.of("message", message != null ? message : statut.getReasonPhrase()));
    }
}
