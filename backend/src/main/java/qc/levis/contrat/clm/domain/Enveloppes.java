package qc.levis.contrat.clm.domain;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Mesure de l'utilisation par enveloppe envoyée (addenda 3, Q39) : une enveloppe est une trousse dont une première
 * invitation à signer est partie, quel que soit le nombre de documents ou de signataires. Elle se compte une fois, au mois
 * de ce premier envoi. Une trousse annulée avant tout envoi ne compte pas ; une invitation renvoyée (rappel manuel,
 * signataire remplacé, nouveau cycle après correction) reste la même enveloppe et n'est pas facturée de nouveau.
 */
public final class Enveloppes {

    public static final String ENVOI = "INVITATION_ENVOYEE";

    public record Evenement(long trousseId, String type, Instant le) {
    }

    /**
     * @param envoyees            enveloppes dont le premier envoi tombe dans la période (facturées)
     * @param annuleesApresEnvoi  enveloppes annulées après envoi : facturées, puisque l'invitation est partie
     * @param annuleesAvantEnvoi  trousses annulées sans envoi : jamais facturées
     * @param renvois             invitations renvoyées sur une enveloppe déjà comptée : non facturées
     * @param parMois             enveloppes facturées, par mois du premier envoi
     */
    public record Releve(int envoyees, int terminees, int rejetees, int annuleesApresEnvoi, int annuleesAvantEnvoi,
                         int echeancesDepassees, int renvois, Map<YearMonth, Integer> parMois) {
    }

    private Enveloppes() {
    }

    /**
     * @param periode      événements de la période, toutes trousses de l'organisation
     * @param dejaEnvoyees trousses dont une invitation précède la période : leurs envois sont des renvois
     */
    public static Releve compter(List<Evenement> periode, Set<Long> dejaEnvoyees, ZoneId fuseau) {
        Set<Long> envoyees = new HashSet<>(dejaEnvoyees);
        Set<Long> echues = new HashSet<>();
        Map<YearMonth, Integer> parMois = new TreeMap<>();
        int nouvelles = 0;
        int renvois = 0;
        int terminees = 0;
        int rejetees = 0;
        int annuleesApres = 0;
        int annuleesAvant = 0;
        for (Evenement e : periode.stream().sorted(Comparator.comparing(Evenement::le)).toList()) {
            boolean envoyee = envoyees.contains(e.trousseId());
            switch (e.type()) {
                case ENVOI -> {
                    if (envoyee) {
                        renvois++;
                    } else {
                        envoyees.add(e.trousseId());
                        nouvelles++;
                        parMois.merge(YearMonth.from(e.le().atZone(fuseau)), 1, Integer::sum);
                    }
                }
                case "TROUSSE_TERMINEE" -> terminees += envoyee ? 1 : 0;
                case "TROUSSE_REJETEE" -> rejetees += envoyee ? 1 : 0;
                case "TROUSSE_ANNULEE" -> {
                    if (envoyee) {
                        annuleesApres++;
                    } else {
                        annuleesAvant++;
                    }
                }
                case "ECHEANCE_DEPASSEE" -> {
                    if (envoyee) {
                        echues.add(e.trousseId());
                    }
                }
                default -> {
                    // Les autres événements du journal ne changent pas la mesure.
                }
            }
        }
        return new Releve(nouvelles, terminees, rejetees, annuleesApres, annuleesAvant, echues.size(), renvois, parMois);
    }
}
