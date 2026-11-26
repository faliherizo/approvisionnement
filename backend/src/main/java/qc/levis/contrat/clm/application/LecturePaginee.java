package qc.levis.contrat.clm.application;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Lecture des trousses à la volumétrie d'IQ (65 000 par an ; addenda 3, Q38) : jamais toute l'organisation en mémoire.
 * Qui voit tout (administration d'affaires, audit) reçoit une page tirée de la base, avec le total. Pour les autres, la
 * base présélectionne les candidates ({@link PerimetreVisible}) et la visibilité exacte se vérifie par tranches, sans total.
 */
@Component
public class LecturePaginee {

    static final Sort TRI = Sort.by(Sort.Order.desc("majLe"), Sort.Order.desc("id"));
    static final int TRANCHE = 200;

    /** Trousses d'une page ; {@code total} nul quand la visibilité se vérifie trousse par trousse. */
    public record Fenetre(List<Trousse> trousses, Long total, boolean suivant) {
    }

    private final TrousseRepository trousses;
    private final LectureTrousse lecture;

    public LecturePaginee(TrousseRepository trousses, LectureTrousse lecture) {
        this.trousses = trousses;
        this.lecture = lecture;
    }

    public static boolean voitTout(Habilitations.Acteur acteur) {
        return acteur.estAdmin() || acteur.estAuditeur();
    }

    public Fenetre page(Habilitations.Acteur acteur, Specification<Trousse> spec, int page, int taille) {
        if (voitTout(acteur)) {
            Page<Trousse> r = trousses.findAll(spec, PageRequest.of(page, taille, TRI));
            return new Fenetre(r.getContent(), r.getTotalElements(), r.hasNext());
        }
        List<Trousse> fenetre = new ArrayList<>();
        int[] aIgnorer = {page * taille};
        parcourir(acteur, spec, visibles -> {
            for (Trousse t : visibles) {
                if (aIgnorer[0] > 0) {
                    aIgnorer[0]--;
                } else if (fenetre.size() <= taille) {
                    fenetre.add(t);
                }
            }
        }, () -> fenetre.size() > taille);
        boolean suivant = fenetre.size() > taille;
        return new Fenetre(suivant ? fenetre.subList(0, taille) : fenetre, null, suivant);
    }

    /** Toutes les trousses visibles, tranche par tranche : pour un décompte ou un export, sans tout charger d'un coup. */
    public void parcourir(Habilitations.Acteur acteur, Specification<Trousse> spec, Consumer<List<Trousse>> tranche) {
        parcourir(acteur, spec, tranche, () -> false);
    }

    private void parcourir(Habilitations.Acteur acteur, Specification<Trousse> spec, Consumer<List<Trousse>> tranche,
                           java.util.function.BooleanSupplier assez) {
        boolean tout = voitTout(acteur);
        Specification<Trousse> retenues = tout ? spec : spec.and(PerimetreVisible.candidates(acteur));
        for (int k = 0; ; k++) {
            Page<Trousse> lot = trousses.findAll(retenues, PageRequest.of(k, TRANCHE, TRI));
            tranche.accept(tout ? lot.getContent() : lot.getContent().stream().filter(t -> lecture.visible(t, acteur)).toList());
            if (assez.getAsBoolean() || !lot.hasNext()) {
                return;
            }
        }
    }
}
