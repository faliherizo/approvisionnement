package qc.levis.contrat.soutien.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/** Banque d'heures de services professionnels d'une organisation (CHU 9.05), valable sur une période. */
@Entity
@Table(name = "soutien_banque_heures")
public class BanqueHeures {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, updatable = false) private Long tenantId;
    @Column(nullable = false, length = 150) private String nom;
    @Column(nullable = false) private LocalDate debut;
    @Column(nullable = false) private LocalDate fin;
    @Column(nullable = false, updatable = false) private Instant creeLe;

    protected BanqueHeures() { }

    public BanqueHeures(Long tenantId, String nom, LocalDate debut, LocalDate fin, Instant le) {
        if (nom == null || nom.isBlank()) throw new IllegalArgumentException("Le nom de la banque d'heures est requis.");
        if (debut == null || fin == null || fin.isBefore(debut)) throw new IllegalArgumentException("La période de la banque d'heures est invalide.");
        this.tenantId = tenantId;
        this.nom = nom.strip();
        this.debut = debut;
        this.fin = fin;
        this.creeLe = le;
    }

    /** Une heure ne s'impute que pendant la période de la banque. */
    public void exigerDansPeriode(LocalDate date) {
        if (date.isBefore(debut) || date.isAfter(fin)) {
            throw new IllegalArgumentException("Le " + date + " est hors de la période de la banque (" + debut + " au " + fin + ").");
        }
    }

    public Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public String getNom() { return nom; }
    public LocalDate getDebut() { return debut; }
    public LocalDate getFin() { return fin; }
    public Instant getCreeLe() { return creeLe; }
}
