package qc.levis.contrat.clm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Un dossier d'un lot de signature : la trousse créée, ou le motif de l'échec. */
@Entity
@Table(name = "clm_lot_element")
public class ElementLot {

    public enum Statut { CREEE, LANCEE, ECHEC }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "lot_id", nullable = false)
    private Long lotId;

    @Column(nullable = false)
    private int rang;

    @Column(name = "reference_externe", length = 120)
    private String referenceExterne;

    @Column(name = "trousse_id")
    private Long trousseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Statut statut;

    @Column(length = 500)
    private String erreur;

    protected ElementLot() {
    }

    public static ElementLot reussi(LotSignature lot, int rang, String referenceExterne, Long trousseId, boolean lancee) {
        ElementLot e = base(lot, rang, referenceExterne);
        e.trousseId = trousseId;
        e.statut = lancee ? Statut.LANCEE : Statut.CREEE;
        return e;
    }

    public static ElementLot echoue(LotSignature lot, int rang, String referenceExterne, String erreur) {
        ElementLot e = base(lot, rang, referenceExterne);
        e.statut = Statut.ECHEC;
        String motif = erreur == null || erreur.isBlank() ? "Erreur inconnue." : erreur;
        e.erreur = motif.substring(0, Math.min(motif.length(), 500));
        return e;
    }

    private static ElementLot base(LotSignature lot, int rang, String referenceExterne) {
        ElementLot e = new ElementLot();
        e.tenantId = lot.getTenantId();
        e.lotId = lot.getId();
        e.rang = rang;
        e.referenceExterne = referenceExterne;
        return e;
    }

    public Long getId() { return id; }
    public Long getLotId() { return lotId; }
    public int getRang() { return rang; }
    public String getReferenceExterne() { return referenceExterne; }
    public Long getTrousseId() { return trousseId; }
    public Statut getStatut() { return statut; }
    public String getErreur() { return erreur; }
}
