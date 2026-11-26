package qc.levis.contrat.clm.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.repository.ModeleContratRepository;
import qc.levis.contrat.clm.domain.ClauseNegociee;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.ModeleContrat;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Clauses normatives d'une trousse (RST-55) : celles de la bibliothèque de son modèle, retenues par les conditions de
 * visibilité et fusionnées avec les valeurs de la trousse, exactement comme la génération du contrat les produit. À la
 * clôture, le contrat est réassemblé avec le corps du modèle et les clauses négociées.
 */
@Component
public class ClausesDuModele {

    private final ModeleContratRepository modeles;
    private final TrousseService trousses;
    private final ContexteClm ctx;

    public ClausesDuModele(ModeleContratRepository modeles, TrousseService trousses, ContexteClm ctx) {
        this.modeles = modeles;
        this.trousses = trousses;
        this.ctx = ctx;
    }

    public List<ClauseNegociee> de(Trousse t) {
        DocumentClmService.Evaluation e = DocumentClmService.evaluer(modele(t), valeurs(t), ctx);
        List<ClauseNegociee> clauses = e.clauses().stream().filter(c -> !e.clausesExclues().contains(c.cle()))
                .map(c -> new ClauseNegociee(c.cle(), c.titre(), Modele.fusionner(c.texte(), e.visibles()))).toList();
        if (clauses.isEmpty()) {
            throw new IllegalStateException("Aucune clause à négocier : le modèle de la trousse n'en retient aucune.");
        }
        return clauses;
    }

    /** Texte intégral du contrat négocié : corps du modèle, clauses négociées à la place des clauses de la bibliothèque. */
    public String contrat(Trousse t, List<ClauseNegociee> negociees) {
        ModeleContrat m = modele(t);
        DocumentClmService.Evaluation e = DocumentClmService.evaluer(m, valeurs(t), ctx);
        List<Modele.Clause> clauses = negociees.stream()
                .map(c -> new Modele.Clause(c.cle(), c.titre(), c.texte(), null, List.of())).toList();
        return Modele.assembler(m.getCorps(), clauses, e.visibles(), Set.of());
    }

    private ModeleContrat modele(Trousse t) {
        if (t.getModeleId() == null) {
            throw new IllegalStateException("Aucune clause à négocier : associez à la trousse un modèle de la bibliothèque.");
        }
        return modeles.findByIdAndTenantId(t.getModeleId(), t.getTenantId())
                .orElseThrow(() -> new NoSuchElementException("Modèle introuvable : " + t.getModeleId()));
    }

    private Map<String, Object> valeurs(Trousse t) {
        Map<String, Object> valeurs = new HashMap<>();
        trousses.valeurs(t).forEach((k, v) -> valeurs.put(k, v.valeur()));
        return valeurs;
    }
}
