package qc.levis.contrat.auth.application.dto;

import java.util.List;

/**
* Réponse d'authentification. Sur succès : ok=true + profil (le jeton est posé
* dans un cookie httpOnly). Si un second facteur est requis : mfaRequired=true.
*/
public record AuthResponse(
            boolean ok,
            boolean mfaRequired,
            String securityMethod,          // "totp" | "question"
            List<String> securityQuestions, // questions dynamiques à présenter
            String email,
            String nom,
            List<String> roles,
            String error) {

        public static AuthResponse succes(String email, String nom, List<String> roles) {
            return new AuthResponse(true, false, null, null, email, nom, roles, null);
        }
        public static AuthResponse defiMfa(String methode, List<String> questions) {
            return new AuthResponse(false, true, methode, questions, null, null, null, null);
        }
        public static AuthResponse echec(String message) {
            return new AuthResponse(false, false, null, null, null, null, null, message);
        }
    }
