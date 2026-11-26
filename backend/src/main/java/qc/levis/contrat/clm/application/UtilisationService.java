package qc.levis.contrat.clm.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.DocumentTrousseRepository;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.domain.ComptageSignes;
import qc.levis.contrat.clm.domain.Enveloppes;
import qc.levis.contrat.clm.domain.EvenementTrousse;

/**
 * Relevé d'utilisation par enveloppe envoyée (addenda 3, Q39), tiré du journal scellé : la mesure qui fonde la facture
 * est vérifiable événement par événement. S'y ajoutent les documents et les pages signés (Annexe 10, AF-49).
 */
@Service
public class UtilisationService {

    private final EvenementTrousseRepository evenements;
    private final DocumentTrousseRepository documents;
    private final ContexteClm ctx;

    public UtilisationService(EvenementTrousseRepository evenements, DocumentTrousseRepository documents, ContexteClm ctx) {
        this.evenements = evenements;
        this.documents = documents;
        this.ctx = ctx;
    }

    public ClmDto.UtilisationVue releve(LocalDate du, LocalDate au) {
        if (du == null || au == null || au.isBefore(du)) {
            throw new IllegalArgumentException("Période invalide : « du » et « au » requis, « au » postérieur à « du ».");
        }
        Long tenant = ctx.tenant();
        Instant debut = du.atStartOfDay(ctx.fuseau()).toInstant();
        Instant fin = au.plusDays(1).atStartOfDay(ctx.fuseau()).toInstant().minusMillis(1);
        List<EvenementTrousse> periode = evenements.findByTenantIdAndHorodatageBetweenOrderByHorodatageDesc(tenant, debut, fin);
        Set<Long> dejaEnvoyees = evenements.trousseIdsParTenantIdTypeAvant(tenant, Enveloppes.ENVOI, debut);
        Enveloppes.Releve r = Enveloppes.compter(periode.stream()
                .map(e -> new Enveloppes.Evenement(e.getTrousseId(), e.getType(), e.getHorodatage())).toList(), dejaEnvoyees, ctx.fuseau());
        Map<String, Integer> parMois = new LinkedHashMap<>();
        r.parMois().forEach((mois, n) -> parMois.put(mois.toString(), n));
        return new ClmDto.UtilisationVue(du, au, r.envoyees(), r.terminees(), r.rejetees(), r.annuleesApresEnvoi(),
                r.annuleesAvantEnvoi(), r.echeancesDepassees(), r.renvois(), parMois, signes(tenant, debut, fin));
    }

    /** Documents et pages signés des trousses terminées dans la période, au total, par unité et par mois (AF-49). */
    private ClmDto.SignesVue signes(Long tenant, Instant debut, Instant fin) {
        ComptageSignes.Releve r = ComptageSignes.compter(documents.signesDansPeriode(tenant, debut, fin), ctx.fuseau());
        return new ClmDto.SignesVue(vue("", r.total()),
                r.parUnite().entrySet().stream().map(e -> vue(e.getKey(), e.getValue())).toList(),
                r.parMois().entrySet().stream().map(e -> vue(e.getKey().toString(), e.getValue())).toList());
    }

    private static ClmDto.ComptageVue vue(String cle, ComptageSignes.Compte c) {
        return new ClmDto.ComptageVue(cle, c.documents(), c.pages(), c.sansPagination());
    }
}
