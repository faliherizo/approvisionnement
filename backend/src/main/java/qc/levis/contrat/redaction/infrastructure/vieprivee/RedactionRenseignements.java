package qc.levis.contrat.redaction.infrastructure.vieprivee;

import java.util.stream.Stream;
import java.util.List;
import org.springframework.stereotype.Component;
import qc.levis.contrat.vieprivee.application.port.SourceRenseignementsPersonnels;
import qc.levis.contrat.vieprivee.domain.BilanEffacement;
import qc.levis.contrat.vieprivee.domain.ExtraitRenseignements;
import qc.levis.contrat.vieprivee.domain.Pseudonyme;
import qc.levis.contrat.vieprivee.domain.Renseignement;
import qc.levis.contrat.redaction.application.repository.ClauseVersionRepository;
import qc.levis.contrat.redaction.application.repository.CommentRepository;
import qc.levis.contrat.redaction.domain.ClauseVersion;
import qc.levis.contrat.redaction.domain.Comment;

/** Rédaction : commentaires écrits ou résolus par la personne, versions de clauses dont elle est l'auteur. */
@Component
class RedactionRenseignements implements SourceRenseignementsPersonnels {

    private final CommentRepository commentaires;
    private final ClauseVersionRepository versions;

    RedactionRenseignements(CommentRepository commentaires, ClauseVersionRepository versions) {
        this.commentaires = commentaires;
        this.versions = versions;
    }

    @Override
    public ExtraitRenseignements extraire(Long tenantId, String courriel) {
        Stream<Renseignement> notes = commentaires.deLaPersonne(tenantId, courriel).stream()
                .map(c -> Renseignement.de("commentaire/" + c.getId(), c.getCreeLe(), "document", String.valueOf(c.getDocumentId()), "texte", c.getCorps()));
        Stream<Renseignement> clauses = versions.findByTenantIdAndAuteurIgnoreCase(tenantId, courriel).stream()
                .map(v -> Renseignement.de("clause/" + v.getClauseId() + "/version/" + v.getNumero(), v.getCreeLe(), "clause", v.getTitre(), "motif", v.getMotif().name()));
        return new ExtraitRenseignements("Rédaction", "Commentaires de révision et versions de clauses attribués à la personne.",
                "Le texte reste (il porte sur les contrats) ; l'attribution passe au pseudonyme.", Stream.concat(notes, clauses).toList());
    }

    @Override
    public BilanEffacement effacer(Long tenantId, String courriel, Pseudonyme pseudonyme) {
        List<Comment> notes = commentaires.deLaPersonne(tenantId, courriel);
        notes.forEach(c -> c.pseudonymiser(courriel, pseudonyme.valeur()));
        commentaires.saveAll(notes);
        List<ClauseVersion> attribuees = versions.findByTenantIdAndAuteurIgnoreCase(tenantId, courriel);
        attribuees.forEach(v -> v.pseudonymiserAuteur(pseudonyme.valeur()));
        versions.saveAll(attribuees);
        return BilanEffacement.complet("Rédaction", notes.size() + attribuees.size());
    }
}
