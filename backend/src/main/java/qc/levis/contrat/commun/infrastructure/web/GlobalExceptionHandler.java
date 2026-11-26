package qc.levis.contrat.commun.infrastructure.web;

import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import qc.levis.contrat.commun.module.application.ModuleNonSouscrit;

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

    /**
     * Fichier au-delà du plafond de transport : 413 et message clair. Tomcat le signale par une IllegalStateException
     * dont la cause est une FileSizeLimitExceededException ; son message technique n'est jamais renvoyé au client.
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> tropVolumineux(Exception e) {
        log.warn("Fichier refusé (413) : {}", e.getMessage());
        return corps(HttpStatus.PAYLOAD_TOO_LARGE, "Fichier trop volumineux : il dépasse le plafond autorisé par la plateforme.");
    }

    private static boolean depassementDeTaille(Throwable e) {
        for (Throwable c = e; c != null; c = c.getCause()) {
            if (c instanceof org.apache.tomcat.util.http.fileupload.impl.SizeException) {
                return true;
            }
        }
        return false;
    }

    /** L'analyse antivirus n'a pas pu être menée : le fichier n'est pas accepté, l'utilisateur réessaiera plus tard. */
    @ExceptionHandler(qc.levis.contrat.commun.antivirus.application.Antivirus.AntivirusIndisponible.class)
    public ResponseEntity<Map<String, String>> antivirusIndisponible(RuntimeException e) {
        log.error("Antivirus indisponible : {}", e.getMessage());
        return corps(HttpStatus.SERVICE_UNAVAILABLE, "Analyse antivirus momentanément indisponible : réessayez plus tard.");
    }

    /** Règle métier violée (ex. clause normative verrouillée). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflit(IllegalStateException e) {
        if (depassementDeTaille(e)) {
            return tropVolumineux(e);
        }
        log.warn("Conflit métier (409) : {}", e.getMessage());
        return corps(HttpStatus.CONFLICT, e.getMessage());
    }

    /** Ressource d'un module que l'organisation n'a pas souscrit : refus, quel que soit le rôle. */
    @ExceptionHandler(ModuleNonSouscrit.class)
    public ResponseEntity<Map<String, String>> nonSouscrit(ModuleNonSouscrit e) {
        log.warn("Accès refusé (403) : {}", e.getMessage());
        return corps(HttpStatus.FORBIDDEN, e.getMessage());
    }

    /** Requête de fichier mal formée (pas multipart, partie manquante) : erreur du client, jamais 500. */
    @ExceptionHandler({org.springframework.web.multipart.MultipartException.class,
            org.springframework.web.multipart.support.MissingServletRequestPartException.class})
    public ResponseEntity<Map<String, String>> requeteFichierInvalide(Exception e) {
        log.debug("Requête de fichier invalide (400) : {}", e.getMessage());
        return corps(HttpStatus.BAD_REQUEST, "Requête de fichier invalide : envoyez le fichier en multipart/form-data.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> requeteInvalide(IllegalArgumentException e) {
        log.debug("Requête invalide (400) : {}", e.getMessage());
        return corps(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /**
     * Contrainte de la base violée (SEC-35, relevé par le balayage actif de ZAP) : valeur plus longue que la colonne,
     * doublon, référence absente. C'est une erreur du client, jamais une erreur interne ; le message ne reprend ni la
     * valeur ni la contrainte (aucune fuite du schéma), le journal ne garde que le code SQLSTATE.
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> contrainteViolee(org.springframework.dao.DataIntegrityViolationException e) {
        String etat = etatSql(e);
        log.warn("Contrainte de données violée (SQLSTATE {})", etat);
        if (DOUBLON.equals(etat)) {
            return corps(HttpStatus.CONFLICT, "Un enregistrement porte déjà cet identifiant.");
        }
        return corps(HttpStatus.BAD_REQUEST, "Valeur refusée : trop longue ou incompatible avec les données attendues.");
    }

    private static final String DOUBLON = "23505";

    static String etatSql(Throwable e) {
        for (Throwable c = e; c != null; c = c.getCause()) {
            if (c instanceof java.sql.SQLException sql && sql.getSQLState() != null) {
                return sql.getSQLState();
            }
        }
        return "inconnu";
    }

    private ResponseEntity<Map<String, String>> corps(HttpStatus statut, String message) {
        return ResponseEntity.status(statut).body(Map.of("message", message != null ? message : statut.getReasonPhrase()));
    }
}
