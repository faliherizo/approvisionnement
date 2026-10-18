package qc.levis.contrat.soumission.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Bordereau de prix d'un document (tableau de lignes tarifaires). */
@Entity
public class Bordereau {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long documentId;

    private String titre;

    @OneToMany(mappedBy = "bordereau", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("ordre ASC")
    private List<LignePrix> lignes = new ArrayList<>();

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public List<LignePrix> getLignes() { return lignes; }
    public void setLignes(List<LignePrix> lignes) { this.lignes = lignes; }

    public void addLigne(LignePrix l) {
        l.setBordereau(this);
        this.lignes.add(l);
    }
}
