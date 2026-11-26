package qc.levis.contrat.clm.infrastructure;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.application.port.ConseillerNegociation;
import qc.levis.contrat.clm.domain.PositionNegociation;
import qc.levis.contrat.ia.application.AiTraceabiliteService;
import qc.levis.contrat.ia.application.dto.AiRedactionRequest;
import qc.levis.contrat.ia.application.dto.AiRedactionResponse;
import qc.levis.contrat.ia.application.port.AssistantRedactionPort;

/**
 * Adaptateur du conseil de négociation (RST-55, ADR-038) sur l'IA locale de la plateforme (Ollama) : aucune donnée ne quitte
 * l'infrastructure. Une IA désactivée pour l'organisation (art. 21), injoignable ou muette rend un conseil vide : le cas
 * d'utilisation applique alors les règles déterministes.
 */
@Component
public class ConseillerNegociationOllama implements ConseillerNegociation {

    private static final Logger log = LoggerFactory.getLogger(ConseillerNegociationOllama.class);

    static final String INSTRUCTION = """
            Tu assistes une organisation publique qui négocie un contrat avec un tiers. Compare la proposition du tiers au texte
            normatif de la clause. En trois phrases au plus, résume les écarts et leur risque pour l'organisation. Termine par
            une ligne « POSITION : ACCEPTER », « POSITION : CONTRE_PROPOSER » ou « POSITION : REFUSER ». Tu ne décides pas :
            une personne de l'organisation décidera.""";

    private final AssistantRedactionPort assistant;
    private final AiTraceabiliteService encadrement;

    public ConseillerNegociationOllama(AssistantRedactionPort assistant, AiTraceabiliteService encadrement) {
        this.assistant = assistant;
        this.encadrement = encadrement;
    }

    @Override
    public Optional<Avis> conseiller(Demande d) {
        if (!encadrement.estActivee()) {
            return Optional.empty();
        }
        try {
            AiRedactionResponse r = assistant.rediger(new AiRedactionRequest(INSTRUCTION, contexte(d), d.textePropose()));
            String texte = r == null || r.proposition() == null ? "" : r.proposition().strip();
            return texte.isEmpty() ? Optional.empty() : Optional.of(new Avis(texte, PositionNegociation.lire(texte), r.modele()));
        } catch (RuntimeException e) {
            // Panne de l'IA : ni erreur ni attente pour la personne, le repli déterministe prend le relais.
            log.warn("IA locale indisponible pour la négociation assistée, repli déterministe : {}", e.getClass().getSimpleName());
            return Optional.empty();
        }
    }

    static String contexte(Demande d) {
        return "Clause : " + d.clause() + "\nTexte normatif (bibliothèque) :\n" + d.texteNormatif() + "\nTexte en vigueur :\n"
                + d.texteActuel() + (d.commentaire() == null ? "" : "\nCommentaire du tiers : " + d.commentaire());
    }
}
