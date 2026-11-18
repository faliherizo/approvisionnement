package qc.levis.contrat.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Groupe poussé par le fournisseur d'identités (Saviynt) au moyen de SCIM 2.0 (addenda 3, Q51). Ses membres deviennent
 * des faits d'annuaire de type groupe (nom et identifiant externe), traduits en rôles et en domaines d'affaires par les
 * correspondances de l'organisation.
 */
@Entity
@Table(name = "annuaire_groupe_scim", uniqueConstraints = @UniqueConstraint(name = "uk_groupe_scim_nom", columnNames = {"tenant_id", "nom"}))
public class GroupeScim {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 255)
    private String nom;

    @Column(name = "id_externe", length = 255)
    private String idExterne;

    protected GroupeScim() {
    }

    public GroupeScim(String id, Long tenantId, String nom, String idExterne) {
        this.id = id;
        this.tenantId = tenantId;
        renommer(nom, idExterne);
    }

    public final void renommer(String nom, String idExterne) {
        if (nom == null || nom.isBlank() || nom.length() > 255) {
            throw new IllegalArgumentException("displayName requis (255 caractères au plus).");
        }
        this.nom = nom.strip();
        this.idExterne = idExterne == null || idExterne.isBlank() ? null : idExterne.strip();
    }

    /** Faits que porte l'appartenance à ce groupe : son nom, et son identifiant externe s'il en a un. */
    public java.util.List<FaitAnnuaire> faits() {
        return idExterne == null ? java.util.List.of(FaitAnnuaire.groupe(nom))
                : java.util.List.of(FaitAnnuaire.groupe(nom), FaitAnnuaire.groupe(idExterne));
    }

    public String getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getNom() { return nom; }
    public String getIdExterne() { return idExterne; }
}
