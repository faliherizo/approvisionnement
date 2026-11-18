package qc.levis.contrat.clm.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.EvenementTrousseRepository;
import qc.levis.contrat.clm.domain.Enveloppes;
import qc.levis.contrat.clm.domain.EvenementTrousse;

/**
 * Relevé d'utilisation par enveloppe envoyée (addenda 3, Q39), tiré du journal scellé : la mesure qui fonde la facture
 * est vérifiable événement par événement.
 */
@Service
public class UtilisationService {

    private final EvenementTrousseRepository evenements;
    private final ContexteClm ctx;

    public UtilisationService(EvenementTrousseRepository evenements, ContexteClm ctx) {
        this.evenements = evenements;
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
                r.annuleesAvantEnvoi(), r.echeancesDepassees(), r.renvois(), parMois);
    }
}
