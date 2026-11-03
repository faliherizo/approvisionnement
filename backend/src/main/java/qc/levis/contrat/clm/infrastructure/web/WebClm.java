package qc.levis.contrat.clm.infrastructure.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import qc.levis.contrat.clm.application.ContexteClm;
import qc.levis.contrat.clm.application.JournalClm;
import qc.levis.contrat.clm.application.port.PortSalesforce;
import qc.levis.contrat.clm.domain.Habilitations;

/** Support web du module CLM : identité de l'acteur et traduction des erreurs propres au module. */
public final class WebClm {

    private WebClm() {
    }

    @Component
    public static class Acteurs {
        private final ContexteClm ctx;

        public Acteurs(ContexteClm ctx) {
            this.ctx = ctx;
        }

        /** Rôles lus du jeton vérifié côté serveur, jamais d'un paramètre transmis par le navigateur. */
        public Habilitations.Acteur de(Authentication auth) {
            List<String> roles = auth.getAuthorities().stream().map(a -> a.getAuthority().replaceFirst("^ROLE_", "")).toList();
            return ctx.acteur(auth.getName(), roles);
        }
    }

    @RestControllerAdvice(basePackages = "qc.levis.contrat.clm")
    public static class Erreurs {
        private final JournalClm journal;

        public Erreurs(JournalClm journal) {
            this.journal = journal;
        }

        /** Tout refus, décidé par l'annotation du contrôleur ou par la règle métier, laisse une trace d'audit (ST-02). */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<Map<String, String>> refus(AccessDeniedException e, HttpServletRequest requete) {
            Authentication qui = SecurityContextHolder.getContext().getAuthentication();
            journal.refus(qui == null ? null : qui.getName(), requete.getMethod() + " " + requete.getRequestURI(), e.getMessage());
            // Le message générique du cadriciel est en anglais ; les motifs métier sont déjà rédigés pour l'utilisateur.
            String message = e.getMessage() == null || "Access Denied".equals(e.getMessage()) ? "Accès refusé." : e.getMessage();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", message));
        }

        @ExceptionHandler(PortSalesforce.Indisponible.class)
        public ResponseEntity<Map<String, String>> salesforce(PortSalesforce.Indisponible e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", e.getMessage()));
        }

        @ExceptionHandler(OptimisticLockingFailureException.class)
        public ResponseEntity<Map<String, String>> concurrence(OptimisticLockingFailureException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Conflit de traitement : la trousse a été modifiée entre-temps, rechargez-la."));
        }
    }
}
