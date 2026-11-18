package qc.levis.contrat.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Correspondance d'annuaire configurable par l'organisation (addenda 3, Q43-Q44) : un groupe ou un attribut de l'annuaire
 * donne un rôle de la plateforme, un domaine d'affaires (groupe du module, ex. unité administrative), ou les deux.
 */
@Entity
@Table(name = "annuaire_correspondance")
public class CorrespondanceAnnuaire {

    private static final Pattern ATTRIBUT = Pattern.compile("[A-Za-z][\\w.:/-]{0,199}");
    private static final Pattern CODE_GROUPE = Pattern.compile("[A-Z][A-Z0-9_]{0,59}");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FaitAnnuaire.Source source;

    @Column(length = 200)
    private String attribut;

    @Column(nullable = false, length = 255)
    private String valeur;

    @Column(length = 30)
    private String role;

    @Column(name = "groupe", length = 60)
    private String groupe;

    @Column(name = "modifie_par", nullable = false, length = 200)
    private String modifiePar;

    @Column(name = "modifie_le", nullable = false)
    private Instant modifieLe;

    protected CorrespondanceAnnuaire() {
    }

    /**
     * @param role   code de rôle déjà validé par l'application ; nul si la correspondance ne donne qu'un domaine d'affaires
     * @param groupe code du groupe (domaine d'affaires) ; nul si la correspondance ne donne qu'un rôle
     */
    public static CorrespondanceAnnuaire definir(Long tenantId, FaitAnnuaire.Source source, String attribut, String valeur,
                                                 String role, String groupe, String par, Instant le) {
        CorrespondanceAnnuaire c = new CorrespondanceAnnuaire();
        c.tenantId = tenantId;
        c.source = source;
        c.attribut = source == FaitAnnuaire.Source.ATTRIBUT ? vide(attribut) : null;
        c.valeur = vide(valeur);
        c.role = vide(role);
        c.groupe = vide(groupe) == null ? null : groupe.strip().toUpperCase(Locale.ROOT);
        c.modifiePar = par;
        c.modifieLe = le;
        c.valider();
        return c;
    }

    private void valider() {
        if (source == null || valeur == null || valeur.length() > 255) {
            throw new IllegalArgumentException("Correspondance : source et valeur (255 caractères au plus) requises.");
        }
        if (source == FaitAnnuaire.Source.ATTRIBUT && (attribut == null || !ATTRIBUT.matcher(attribut).matches())) {
            throw new IllegalArgumentException("Correspondance par attribut : nom d'attribut requis (lettres, chiffres, . : / - _).");
        }
        if (role == null && groupe == null) {
            throw new IllegalArgumentException("Correspondance : un rôle, un domaine d'affaires, ou les deux.");
        }
        if (groupe != null && !CODE_GROUPE.matcher(groupe).matches()) {
            throw new IllegalArgumentException("Domaine d'affaires : code en majuscules (A-Z, 0-9, _), 60 caractères au plus.");
        }
    }

    public boolean satisfaitePar(FaitAnnuaire f) {
        return f.correspond(source, attribut, valeur);
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public FaitAnnuaire.Source getSource() { return source; }
    public String getAttribut() { return attribut; }
    public String getValeur() { return valeur; }
    public String getRole() { return role; }
    public String getGroupe() { return groupe; }
    public String getModifiePar() { return modifiePar; }
    public Instant getModifieLe() { return modifieLe; }
}
