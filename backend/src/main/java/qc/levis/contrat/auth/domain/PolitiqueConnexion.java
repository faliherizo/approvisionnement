package qc.levis.contrat.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Politique de connexion locale d'une organisation (addenda 4, Q12) : réservée ou non aux comptes de secours. Quand elle
 * l'est, tout autre compte passe par l'authentification unique, et un compte de secours doit avoir un second facteur :
 * le secours ne contourne jamais les contrôles d'identité.
 */
@Entity
@Table(name = "auth_politique_connexion")
public class PolitiqueConnexion {

    /** Motif d'un refus de connexion locale ; nul si la connexion est admise. */
    public enum Refus { RESERVEE_AUX_SECOURS, SECOURS_SANS_SECOND_FACTEUR }

    @Id
    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "locale_reservee", nullable = false)
    private boolean localeReservee;

    @Column(name = "modifie_par", nullable = false, length = 200)
    private String modifiePar;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    protected PolitiqueConnexion() {
    }

    public PolitiqueConnexion(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void reserver(boolean reservee, String par, Instant le) {
        this.localeReservee = reservee;
        this.modifiePar = par;
        this.modifieLe = le;
    }

    /** Motif de refus d'une connexion locale pour ce compte, ou nul si elle est admise. */
    public Refus refus(boolean compteSecours, boolean secondFacteur) {
        if (!localeReservee) {
            return null;
        }
        if (!compteSecours) {
            return Refus.RESERVEE_AUX_SECOURS;
        }
        return secondFacteur ? null : Refus.SECOURS_SANS_SECOND_FACTEUR;
    }

    public Long getTenantId() { return tenantId; }
    public boolean isLocaleReservee() { return localeReservee; }
    public String getModifiePar() { return modifiePar; }
    public Instant getModifieLe() { return modifieLe; }
}
