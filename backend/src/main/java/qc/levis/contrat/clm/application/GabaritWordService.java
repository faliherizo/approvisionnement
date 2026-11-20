package qc.levis.contrat.clm.application;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.RenduDocuments;
import qc.levis.contrat.clm.application.repository.GabaritWordRepository;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.GabaritWord;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.ModeleContrat;

/**
 * Gabarit Word d'un modèle (addenda 4, Q22) : déposé sur un brouillon par qui gère le modèle, refusé s'il emploie une
 * variable que le modèle ne déclare pas, téléchargeable par qui voit le modèle.
 */
@Service
public class GabaritWordService {

    private final ModeleService modeles;
    private final GabaritWordRepository gabarits;
    private final RenduDocuments rendu;
    private final ContexteClm ctx;

    public GabaritWordService(ModeleService modeles, GabaritWordRepository gabarits, RenduDocuments rendu, ContexteClm ctx) {
        this.modeles = modeles;
        this.gabarits = gabarits;
        this.rendu = rendu;
        this.ctx = ctx;
    }

    @Transactional
    public ClmDto.ModeleVue deposer(Long modeleId, String nom, byte[] contenu, Habilitations.Acteur acteur) {
        ModeleContrat m = modeles.exigerGestion(modeleId, acteur);
        FormatDocument.verifier(nom, contenu, GabaritWord.TAILLE_MAX);
        Set<String> variables = Modele.variables(rendu.texte(FormatDocument.Type.DOCX, contenu));
        GabaritWord g = new GabaritWord(m, new GabaritWord.Depot(nom, contenu, variables), acteur.id(), ctx.maintenant());
        var inconnues = g.inconnues(cles(m));
        if (!inconnues.isEmpty()) {
            throw new IllegalArgumentException("Variables du gabarit absentes du modèle : " + String.join(", ", inconnues) + ".");
        }
        gabarits.deleteByModeleIdAndTenantId(m.getId(), m.getTenantId());
        gabarits.flush();
        gabarits.save(g);
        return modeles.lire(modeleId, acteur);
    }

    public GabaritWord lire(Long modeleId, Habilitations.Acteur acteur) {
        ModeleContrat m = modeles.exigerVisible(modeleId, acteur);
        return gabarits.findByModeleIdAndTenantId(m.getId(), m.getTenantId())
                .orElseThrow(() -> new NoSuchElementException("Aucun gabarit Word pour ce modèle."));
    }

    @Transactional
    public ClmDto.ModeleVue retirer(Long modeleId, Habilitations.Acteur acteur) {
        ModeleContrat m = modeles.exigerGestion(modeleId, acteur);
        m.exigerBrouillon();
        gabarits.deleteByModeleIdAndTenantId(m.getId(), m.getTenantId());
        return modeles.lire(modeleId, acteur);
    }

    private Set<String> cles(ModeleContrat m) {
        return modeles.champs(m).stream().map(Modele.Champ::cle).collect(Collectors.toSet());
    }
}
