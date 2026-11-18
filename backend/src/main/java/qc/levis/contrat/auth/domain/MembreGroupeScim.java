package qc.levis.contrat.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Appartenance d'un compte (identifiant SCIM de l'utilisateur) à un groupe SCIM. */
@Entity
@Table(name = "annuaire_membre_scim", uniqueConstraints = @UniqueConstraint(name = "uk_membre_scim", columnNames = {"groupe_id", "utilisateur_id"}))
public class MembreGroupeScim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "groupe_id", nullable = false, length = 36)
    private String groupeId;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    protected MembreGroupeScim() {
    }

    public MembreGroupeScim(Long tenantId, String groupeId, Long utilisateurId) {
        this.tenantId = tenantId;
        this.groupeId = groupeId;
        this.utilisateurId = utilisateurId;
    }

    public String getGroupeId() { return groupeId; }
    public Long getUtilisateurId() { return utilisateurId; }
}
