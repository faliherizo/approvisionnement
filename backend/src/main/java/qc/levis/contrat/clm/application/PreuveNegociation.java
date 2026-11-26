package qc.levis.contrat.clm.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import qc.levis.contrat.clm.domain.Negociation;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * RST-55 : chaque négociation de la trousse entre au dossier de preuve — versions et leurs empreintes, propositions,
 * suggestions et décisions humaines — dans un fichier JSON par cycle, scellé avec le reste de l'archive.
 */
@Component
public class PreuveNegociation implements PieceDePreuve {

    private final RegistreNegociation registre;
    private final VuesNegociation vues;
    private final ContexteClm ctx;

    public PreuveNegociation(RegistreNegociation registre, VuesNegociation vues, ContexteClm ctx) {
        this.registre = registre;
        this.vues = vues;
        this.ctx = ctx;
    }

    @Override
    public Map<String, byte[]> pieces(Trousse t) {
        Map<String, byte[]> pieces = new LinkedHashMap<>();
        for (Negociation n : registre.deLaTrousse(t)) {
            Map<String, Object> contenu = new LinkedHashMap<>();
            contenu.put("negociation", vues.interne(n, List.of()));
            registre.versions(n).forEach(v -> contenu.put("v" + v.getNumero(), registre.clauses(n, v.getNumero())));
            pieces.put("negociations/negociation-" + n.getId() + ".json", ctx.ecrireLisible(contenu));
        }
        return pieces;
    }
}
