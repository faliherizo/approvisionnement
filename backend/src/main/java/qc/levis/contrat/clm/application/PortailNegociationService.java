package qc.levis.contrat.clm.application;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.NegociationDto;
import qc.levis.contrat.clm.application.port.IdentiteOrganisation;
import qc.levis.contrat.clm.application.repository.NegociationRepository;
import qc.levis.contrat.clm.domain.ChaineScellee;
import qc.levis.contrat.clm.domain.ClauseNegociee;
import qc.levis.contrat.clm.domain.Negociation;
import qc.levis.contrat.clm.domain.PropositionNegociation;
import qc.levis.contrat.clm.domain.Trousse;
import qc.levis.contrat.clm.domain.VersionNegociation;

/**
 * Négociation côté contrepartie (RST-55), par le portail externe : accès par le seul jeton du lien, sans compte. Elle lit la
 * version courante, compare deux versions, propose des modifications clause par clause ou accepte la version telle quelle.
 */
@Service
public class PortailNegociationService {

    private static final String INTROUVABLE = "Négociation introuvable ou lien invalide.";

    private final NegociationRepository negociations;
    private final RegistreNegociation registre;
    private final VuesNegociation vues;
    private final LectureTrousse lecture;
    private final IdentiteOrganisation identite;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public PortailNegociationService(NegociationRepository negociations, RegistreNegociation registre, VuesNegociation vues,
                                     LectureTrousse lecture, IdentiteOrganisation identite, JournalClm journal, ContexteClm ctx) {
        this.negociations = negociations;
        this.registre = registre;
        this.vues = vues;
        this.lecture = lecture;
        this.identite = identite;
        this.journal = journal;
        this.ctx = ctx;
    }

    private record Session(Negociation negociation, Trousse trousse) {
    }

    /** Organisation qui négocie : le contrôleur y vérifie l'option souscrite avant tout échange. */
    @Transactional(readOnly = true)
    public Long organisation(String jeton) {
        return trouver(jeton).getTenantId();
    }

    @Transactional(readOnly = true)
    public NegociationDto.PortailVue consulter(String jeton) {
        return dansLeTenant(jeton, this::vue);
    }

    @Transactional(readOnly = true)
    public NegociationDto.ComparaisonVue comparer(String jeton, int de, int a) {
        return dansLeTenant(jeton, s -> vues.comparer(s.negociation(), de, a));
    }

    @Transactional
    public NegociationDto.PortailVue proposer(String jeton, NegociationDto.PropositionsRequest req) {
        return dansLeTenant(jeton, s -> {
            List<NegociationDto.PropositionRequest> saisies = req == null || req.propositions() == null ? List.of() : req.propositions();
            if (saisies.stream().map(NegociationDto.PropositionRequest::cle).distinct().count() < saisies.size()) {
                throw new IllegalArgumentException("Une seule proposition par clause et par tour.");
            }
            s.negociation().recevoirPropositions(saisies.size());
            consigner(s, saisies);
            registre.enregistrer(s.negociation());
            return vue(s);
        });
    }

    private void consigner(Session s, List<NegociationDto.PropositionRequest> saisies) {
        Negociation n = s.negociation();
        Map<String, ClauseNegociee> courantes = registre.courantes(n).stream()
                .collect(Collectors.toMap(ClauseNegociee::cle, Function.identity()));
        for (NegociationDto.PropositionRequest r : saisies) {
            PropositionNegociation p = registre.enregistrer(new PropositionNegociation(n, clause(courantes, r.cle()),
                    new PropositionNegociation.Saisie(r.texte(), r.commentaire()), ctx.maintenant()));
            journal.consigner(s.trousse(), "NEGOCIATION_PROPOSITION", n.getContrepartieCourriel(), p.getTitreClause()
                    + (p.getCommentaire() == null ? "" : " · " + p.getCommentaire()));
        }
    }

    @Transactional
    public NegociationDto.PortailVue accepter(String jeton) {
        return dansLeTenant(jeton, s -> {
            s.negociation().accepterVersion();
            registre.enregistrer(s.negociation());
            journal.consigner(s.trousse(), "NEGOCIATION_ACCEPTEE", s.negociation().getContrepartieCourriel(),
                    "v" + s.negociation().getVersionCourante() + " acceptée par la contrepartie");
            return vue(s);
        });
    }

    private static ClauseNegociee clause(Map<String, ClauseNegociee> courantes, String cle) {
        ClauseNegociee c = cle == null ? null : courantes.get(cle);
        if (c == null) {
            throw new IllegalArgumentException("Clause inconnue dans la version courante : " + cle);
        }
        return c;
    }

    private Negociation trouver(String jeton) {
        if (jeton == null || jeton.length() < 20 || jeton.length() > 200) {
            throw new NoSuchElementException(INTROUVABLE);
        }
        return negociations.findByJetonHash(ChaineScellee.sha256(jeton.getBytes(StandardCharsets.UTF_8)))
                .orElseThrow(() -> new NoSuchElementException(INTROUVABLE));
    }

    private <T> T dansLeTenant(String jeton, Function<Session, T> action) {
        Negociation n = trouver(jeton);
        return ctx.dansTenant(n.getTenantId(), () -> {
            n.exigerAccessible(ctx.maintenant());
            return action.apply(new Session(n, lecture.exiger(n.getTrousseId())));
        });
    }

    private NegociationDto.PortailVue vue(Session s) {
        Negociation n = s.negociation();
        IdentiteOrganisation.Organisation o = identite.de(n.getTenantId());
        return new NegociationDto.PortailVue(s.trousse().getReference(), s.trousse().getTitre(), o.nom(), o.logo(), n.getContrepartieNom(),
                n.getLangue().code(), n.getEtat().name(), n.getVersionCourante(), n.getExpireLe(),
                VuesNegociation.clauses(registre.courantes(n)), vues.propositionsExternes(n),
                registre.versions(n).stream().map(VersionNegociation::getNumero).toList());
    }
}
