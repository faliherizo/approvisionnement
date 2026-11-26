package qc.levis.contrat.clm.application;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.Participant;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Présélection en base des trousses qu'un acteur peut consulter, pour qui ne voit pas tout le tenant (initiateurs,
 * intervenants). Sans elle, la visibilité se vérifiait trousse par trousse sur toute l'organisation : à 500 utilisateurs
 * et quelques milliers de trousses (essai TI-12), chaque page du suivi relisait toutes les trousses et leurs étapes et
 * participants, des millions de requêtes qui saturaient la base.
 *
 * <p>Le critère est un sur-ensemble de {@link Habilitations#peutConsulter} : propriétaire, unité de l'acteur, participant
 * désigné par son courriel, ou étape dont les affectations citent l'acteur, l'un de ses groupes ou l'un de ses rôles (une
 * affectation de participant renvoie à un participant, couvert par le critère précédent). La décision exacte reste
 * {@link LectureTrousse#visible}, appliquée ensuite aux seules candidates : la règle d'accès ne vit qu'à un endroit.
 */
public final class PerimetreVisible {

    private static final char ECHAPPEMENT = '\\';

    private PerimetreVisible() {
    }

    public static Specification<Trousse> candidates(Habilitations.Acteur acteur) {
        return (r, q, cb) -> {
            String id = acteur.id().toLowerCase(Locale.ROOT);
            List<Predicate> ou = new ArrayList<>();
            // Sous-requêtes non corrélées : la base les évalue une fois (par index pour le propriétaire et le participant)
            // puis consulte leur résultat par hachage, sans calcul par trousse.
            ou.add(r.get("id").in(possedees(q, cb, id)));
            if (!acteur.groupes().isEmpty()) {
                ou.add(r.get("unite").in(acteur.groupes()));
            }
            ou.add(r.get("id").in(participantes(q, cb, id)));
            ou.add(r.get("id").in(affectees(q, cb, acteur)));
            return cb.or(ou.toArray(Predicate[]::new));
        };
    }

    private static Subquery<Long> possedees(CriteriaQuery<?> q, CriteriaBuilder cb, String id) {
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Trousse> t = sq.from(Trousse.class);
        return sq.select(t.get("id")).where(cb.equal(cb.lower(t.get("proprietaire")), id));
    }

    private static Subquery<Long> participantes(CriteriaQuery<?> q, CriteriaBuilder cb, String id) {
        Subquery<Long> sq = q.subquery(Long.class);
        Root<Participant> p = sq.from(Participant.class);
        return sq.select(p.get("trousseId")).where(cb.equal(cb.lower(p.get("courriel")), id));
    }

    private static Subquery<Long> affectees(CriteriaQuery<?> q, CriteriaBuilder cb, Habilitations.Acteur acteur) {
        Subquery<Long> sq = q.subquery(Long.class);
        Root<EtapeTrousse> e = sq.from(EtapeTrousse.class);
        Expression<String> json = e.get("affectationsJson");
        List<Predicate> cite = new ArrayList<>();
        // Nominative : sans égard à la casse, comme Habilitations.correspond. Le premier critère, sans calcul, réserve la
        // mise en minuscules aux seules étapes qui portent une affectation nominative.
        cite.add(cb.and(cb.like(json, motif(Parcours.TypeAffecte.UTILISATEUR.name()), ECHAPPEMENT),
                cb.like(cb.lower(json), motif(acteur.id().toLowerCase(Locale.ROOT)), ECHAPPEMENT)));
        // Groupe et rôle : à l'identique, comme Habilitations.correspond.
        acteur.groupes().forEach(g -> cite.add(cb.like(json, motif(g), ECHAPPEMENT)));
        acteur.roles().forEach(role -> cite.add(cb.like(json, motif(role), ECHAPPEMENT)));
        return sq.select(e.get("trousseId")).where(cb.or(cite.toArray(Predicate[]::new)));
    }

    /**
     * Valeur telle que l'écrit la sérialisation JSON des affectations (guillemets, échappements), avec les caractères
     * spéciaux de LIKE neutralisés : {@code "valeur"} n'importe où dans le texte.
     */
    static String motif(String valeur) {
        String json = new String(JsonStringEncoder.getInstance().quoteAsString(valeur));
        String litteral = json.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%\"" + litteral + "\"%";
    }
}
