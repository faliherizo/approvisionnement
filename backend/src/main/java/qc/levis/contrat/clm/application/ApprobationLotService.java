package qc.levis.contrat.clm.application;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.ElementLotRepository;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.LotSignatureRepository;
import qc.levis.contrat.clm.domain.ElementLot;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.LotSignature;
import qc.levis.contrat.clm.domain.Parcours;

/**
 * Approbation globale d'un lot avant l'envoi en signature (addenda 4, Q36). L'approbateur décide en une action toutes les
 * étapes de validation ou d'approbation actives du lot qui lui reviennent ; chaque décision passe par le parcours normal
 * (droits, séparation des responsabilités, journal scellé de chaque trousse) : la traçabilité reste celle de chaque
 * dossier. Une décision refusée n'empêche pas les autres, chacune ayant sa propre transaction.
 */
@Service
public class ApprobationLotService {

    /** Motifs d'écart rapportés au plus, pour un lot de plusieurs centaines de dossiers. */
    static final int MOTIFS_MAX = 20;

    private final LotSignatureRepository lots;
    private final ElementLotRepository elements;
    private final EtapeTrousseRepository etapes;
    private final ParcoursService parcours;
    private final ContexteClm ctx;

    public ApprobationLotService(LotSignatureRepository lots, ElementLotRepository elements, EtapeTrousseRepository etapes,
                                 ParcoursService parcours, ContexteClm ctx) {
        this.lots = lots;
        this.elements = elements;
        this.etapes = etapes;
        this.parcours = parcours;
        this.ctx = ctx;
    }

    public ClmDto.ApprobationLotVue approuver(Long lotId, Habilitations.Acteur acteur, String commentaire) {
        Long tenant = ctx.tenant();
        LotSignature lot = lots.findByIdAndTenantId(lotId, tenant).orElseThrow(() -> new NoSuchElementException("Lot introuvable."));
        List<Long> trousses = elements.findByTenantIdAndLotIdOrderByRangAsc(tenant, lotId).stream()
                .map(ElementLot::getTrousseId).filter(Objects::nonNull).toList();
        List<EtapeTrousse> aDecider = trousses.isEmpty() ? List.of()
                : etapes.findByTenantIdAndTrousseIdInAndStatut(tenant, trousses, Parcours.StatutEtape.ACTIVE).stream()
                .filter(e -> e.getType() != Parcours.TypeEtape.SIGNATURE).toList();
        String motif = "Approbation globale du lot « " + lot.getLibelle() + " »"
                + (commentaire == null || commentaire.isBlank() ? "" : " : " + commentaire.strip());
        int approuvees = 0;
        List<String> ecarts = new ArrayList<>();
        for (EtapeTrousse e : aDecider) {
            try {
                parcours.decider(e.getId(), acteur, new ClmDto.DecisionRequest("APPROUVE", motif, null));
                approuvees++;
            } catch (IllegalStateException | IllegalArgumentException | AccessDeniedException ex) {
                ecarts.add("#" + e.getTrousseId() + " " + e.getLibelle() + " : " + ex.getMessage());
            }
        }
        return new ClmDto.ApprobationLotVue(lotId, trousses.size(), approuvees, ecarts.size(),
                ecarts.stream().limit(MOTIFS_MAX).toList());
    }
}
