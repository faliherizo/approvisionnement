package qc.levis.contrat.clm.application;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AnnuaireOrganisation;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.TacheAssigneeRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.StatutTrousse;
import qc.levis.contrat.clm.domain.TacheAssignee;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Départ d'un employé (P3) : l'administration d'affaires voit ce qu'il détient encore, puis le confie à une personne
 * active en une opération. Les trousses ouvertes changent de propriétaire ; les étapes non décidées qui le désignent
 * nommément et ses tâches ouvertes passent à la personne désignée. Chaque changement est consigné dans la trousse.
 * Les trousses closes gardent leur propriétaire, qui fait partie de leur preuve.
 */
@Service
public class DepartEmployeService {

    private static final Set<StatutTrousse> OUVERTES = EnumSet.of(StatutTrousse.BROUILLON, StatutTrousse.EN_TRAITEMENT, StatutTrousse.REJETEE);
    private static final Set<Parcours.StatutEtape> NON_DECIDEES = EnumSet.of(Parcours.StatutEtape.EN_ATTENTE, Parcours.StatutEtape.ACTIVE);
    private static final Set<TacheAssignee.Statut> TACHES_OUVERTES = EnumSet.of(TacheAssignee.Statut.A_FAIRE, TacheAssignee.Statut.EN_COURS);

    private final TrousseRepository trousses;
    private final EtapeTrousseRepository etapes;
    private final TacheAssigneeRepository taches;
    private final ParcoursService parcours;
    private final TachesAssigneesService tachesService;
    private final AnnuaireOrganisation annuaire;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public DepartEmployeService(TrousseRepository trousses, EtapeTrousseRepository etapes, TacheAssigneeRepository taches,
                                ParcoursService parcours, TachesAssigneesService tachesService, AnnuaireOrganisation annuaire,
                                JournalClm journal, ContexteClm ctx) {
        this.trousses = trousses;
        this.etapes = etapes;
        this.taches = taches;
        this.parcours = parcours;
        this.tachesService = tachesService;
        this.annuaire = annuaire;
        this.journal = journal;
        this.ctx = ctx;
    }

    public ClmDto.InventaireDepart inventaire(String courriel, Habilitations.Acteur acteur) {
        exigerAdmin(acteur);
        String qui = exigerCourriel(courriel);
        List<Trousse> possedees = trousses.findByTenantIdAndProprietaireIgnoreCaseAndStatutInOrderByIdAsc(ctx.tenant(), qui, OUVERTES);
        return new ClmDto.InventaireDepart(qui, possedees.stream().map(Trousse::getReference).toList(),
                etapesDe(qui).size(), tachesDe(qui).size());
    }

    @Transactional
    public ClmDto.BilanDepart transferer(ClmDto.DepartRequest req, Habilitations.Acteur acteur) {
        exigerAdmin(acteur);
        String de = exigerCourriel(req == null ? null : req.de());
        String vers = exigerCourriel(req.vers());
        if (de.equalsIgnoreCase(vers)) {
            throw new IllegalArgumentException("La personne qui reprend doit être différente de celle qui part.");
        }
        if (!annuaire.estMembre(ctx.tenant(), vers)) {
            throw new IllegalArgumentException("« " + vers + " » n'est pas une personne active de l'organisation.");
        }
        String motif = req.motif() == null || req.motif().isBlank() ? "Départ de " + de : req.motif().trim();
        int nbTrousses = transfererTrousses(de, vers, motif, acteur);
        int nbEtapes = reassignerEtapes(de, vers, motif, acteur);
        List<TacheAssignee> ouvertes = tachesDe(de);
        ouvertes.forEach(t -> tachesService.transferer(t, vers, acteur, motif));
        return new ClmDto.BilanDepart(de, vers, nbTrousses, nbEtapes, ouvertes.size());
    }

    private int transfererTrousses(String de, String vers, String motif, Habilitations.Acteur acteur) {
        List<Trousse> possedees = trousses.findByTenantIdAndProprietaireIgnoreCaseAndStatutInOrderByIdAsc(ctx.tenant(), de, OUVERTES);
        for (Trousse t : possedees) {
            String ancien = t.transfererA(vers, ctx.maintenant());
            trousses.save(t);
            journal.consigner(t, "TRANSFERT_PROPRIETE", acteur.id(), ancien + " → " + t.getProprietaire() + " · " + motif);
        }
        return possedees.size();
    }

    /** Par la réassignation ordinaire (ST-08) : application à chaud, invitations, trace et quorum restent cohérents. */
    private int reassignerEtapes(String de, String vers, String motif, Habilitations.Acteur acteur) {
        List<EtapeTrousse> visees = etapesDe(de);
        for (EtapeTrousse e : visees) {
            List<Parcours.Affectation> nouvelles = Parcours.remplacer(ctx.affectations(e.getAffectationsJson()), de, vers);
            List<ClmDto.AffectationDto> dtos = nouvelles.stream().map(a -> new ClmDto.AffectationDto(a.type().name(), a.valeur())).toList();
            parcours.modifierAffectations(e.getId(), acteur, new ClmDto.AffectationsRequest(dtos,
                    Math.min(e.getQuorum(), nouvelles.size()), motif));
        }
        return visees.size();
    }

    private List<EtapeTrousse> etapesDe(String courriel) {
        List<EtapeTrousse> visees = new ArrayList<>();
        for (Parcours.StatutEtape s : NON_DECIDEES) {
            etapes.findByTenantIdAndStatut(ctx.tenant(), s).stream()
                    .filter(e -> Parcours.designe(ctx.affectations(e.getAffectationsJson()), courriel))
                    .forEach(visees::add);
        }
        return visees;
    }

    private List<TacheAssignee> tachesDe(String courriel) {
        return taches.findByTenantIdAndAssigneAIgnoreCaseAndStatutInOrderByEcheanceAscIdAsc(ctx.tenant(), courriel, TACHES_OUVERTES);
    }

    private static void exigerAdmin(Habilitations.Acteur acteur) {
        if (!acteur.estAdmin()) {
            throw new AccessDeniedException("Réservé à l'administration d'affaires.");
        }
    }

    private static String exigerCourriel(String courriel) {
        if (courriel == null || courriel.isBlank()) {
            throw new IllegalArgumentException("Courriel requis.");
        }
        return courriel.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
