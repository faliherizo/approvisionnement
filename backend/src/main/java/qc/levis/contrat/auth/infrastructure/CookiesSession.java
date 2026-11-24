package qc.levis.contrat.auth.infrastructure;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/** Cookie de session httpOnly, avec les mêmes attributs que celui posé à la connexion (Secure et SameSite réglables). */
@Component
public class CookiesSession {

    private final boolean secure;
    private final String sameSite;

    public CookiesSession(@Value("${app.cookie.secure}") boolean secure, @Value("${app.cookie.same-site}") String sameSite) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public ResponseCookie session(String jeton, Duration validite) {
        return ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, jeton)
                .httpOnly(true).secure(secure).sameSite(sameSite).path("/").maxAge(validite).build();
    }
}
