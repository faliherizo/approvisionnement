package qc.levis.contrat.clm.application;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.NegociationDto;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.domain.ChaineScellee;
import qc.levis.contrat.clm.domain.ClauseNegociee;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Langue;
import qc.levis.contrat.clm.domain.ModeleMessage.Evenement;
import qc.levis.contrat.clm.domain.Negociation;
import qc.levis.contrat.clm.domain.ParametresClm;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Ouverture d'un cycle de négociation avec une contrepartie externe (RST-55, IA-05) : la version 1 reprend les clauses
 * normatives du modèle de la trousse, puis la contrepartie reçoit un lien vers le portail externe. Le jeton en clair ne vit
 * que dans le courriel ; seule son empreinte est conservée.
 */
@Service
public class OuvertureNegociationService {

    private final RegistreNegociation registre;
    private final VuesNegociation vues;
    private final LectureTrousse lecture;
    private final ClausesDuModele clausesDuModele;
    private final MessagerieClm messagerie;
    private final SourceAleatoire alea;
    private final ParametresClmService parametres;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public OuvertureNegociationService(RegistreNegociation registre, VuesNegociation vues, LectureTrousse lecture,
                                       ClausesDuModele clausesDuModele, MessagerieClm messagerie, SourceAleatoire alea,
                                       ParametresClmService parametres, JournalClm journal, ContexteClm ctx) {
        this.registre = registre;
        this.vues = vues;
        this.lecture = lecture;
        this.clausesDuModele = clausesDuModele;
        this.messagerie = messagerie;
        this.alea = alea;
        this.parametres = parametres;
        this.journal = journal;
        this.ctx = ctx;
    }

    @Transactional
    public NegociationDto.NegociationVue ouvrir(Long trousseId, NegociationDto.OuvertureRequest req, Habilitations.Acteur acteur) {
        Trousse t = lecture.exigerGestion(trousseId, acteur);
        t.exigerModifiable();
        Negociation.exigerAucuneOuverte(registre.deLaTrousse(t));
        Negociation.Contrepartie c = contrepartie(req);
        List<ClauseNegociee> clauses = clausesDuModele.de(t);
        ParametresClm.Reglages reglages = parametres.reglages(t.getTenantId());
        String jeton = alea.jeton();
        Negociation n = registre.enregistrer(new Negociation(new Negociation.Ouverture(t.getTenantId(), t.getId(), c, empreinte(jeton),
                ctx.maintenant().plus(Duration.ofDays(reglages.invitationValiditeJours())), acteur.id(), ctx.maintenant())));
        journal.consigner(t, "NEGOCIATION_OUVERTE", acteur.id(), c.nom() + (c.entreprise() == null ? "" : " · " + c.entreprise()));
        registre.figer(t, n, clauses, "Clauses normatives du modèle", acteur.id());
        inviter(t, n, jeton, reglages);
        return vues.interne(n, VuesNegociation.actions(n, true));
    }

    private static Negociation.Contrepartie contrepartie(NegociationDto.OuvertureRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("Contrepartie requise.");
        }
        return new Negociation.Contrepartie(req.nom(), req.courriel(), req.entreprise(), Langue.de(req.langue()));
    }

    /** Lien du portail externe existant, sous « negociation/ », dans la langue de la contrepartie. */
    private void inviter(Trousse t, Negociation n, String jeton, ParametresClm.Reglages reglages) {
        boolean anglais = n.getLangue() == Langue.EN;
        Map<String, Object> vars = new HashMap<>();
        vars.put("reference", t.getReference());
        vars.put("titre", t.getTitre());
        vars.put("destinataire", n.getContrepartieNom());
        vars.put("lien", ParcoursService.lienPortail(reglages, "negociation/" + jeton, n.getLangue()));
        vars.put("echeance", n.getExpireLe().toString().substring(0, 10));
        vars.put("objet", anglais ? "Contract to negotiate" : "Contrat à négocier");
        vars.put("action", anglais ? "negotiate the clauses of" : "négocier les clauses de");
        messagerie.envoyer(t.getTenantId(), Evenement.INVITATION, "COURRIEL", n.getContrepartieCourriel(), vars, List.of(), n.getLangue());
        journal.consigner(t, "NEGOCIATION_INVITATION", "systeme", n.getContrepartieNom() + " · " + n.getLangue());
    }

    private static String empreinte(String jeton) {
        return ChaineScellee.sha256(jeton.getBytes(StandardCharsets.UTF_8));
    }
}
