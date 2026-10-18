package qc.levis.contrat.addenda.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import qc.levis.contrat.commun.domain.Enums;

/** Addenda rattaché à un document, modifiant une ou plusieurs clauses. */
@Entity
public class Addenda {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long documentParentId;

    @Column(nullable = false)
    private int numero;

    @Column(length = 500)
    private String objet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Enums.StatutDocument statut = Enums.StatutDocument.BROUILLON;

    private Instant emisLe;

    @OneToMany(mappedBy = "addenda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AddendaChange> changements = new ArrayList<>();

    public Long getId() { return id; }
    public Long getDocumentParentId() { return documentParentId; }
    public void setDocumentParentId(Long d) { this.documentParentId = d; }
    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }
    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }
    public Enums.StatutDocument getStatut() { return statut; }
    public void setStatut(Enums.StatutDocument s) { this.statut = s; }
    public Instant getEmisLe() { return emisLe; }
    public void setEmisLe(Instant emisLe) { this.emisLe = emisLe; }
    public List<AddendaChange> getChangements() { return changements; }
    public void setChangements(List<AddendaChange> c) { this.changements = c; }

    public void addChangement(AddendaChange c) {
        c.setAddenda(this);
        this.changements.add(c);
    }
}
