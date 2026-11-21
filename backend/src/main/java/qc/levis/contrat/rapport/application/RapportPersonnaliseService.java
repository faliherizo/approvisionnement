package qc.levis.contrat.rapport.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.rapport.application.port.LectureFormulaires;
import qc.levis.contrat.rapport.application.port.RenduRapport;
import qc.levis.contrat.rapport.domain.RapportFiches;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.Saisie;
import qc.levis.contrat.saisie.domain.StatutSaisie;

/**
 * Rapport PDF personnalisé (CHU 6.03) : la personne choisit les questions à montrer et les statuts à retenir. Les questions
 * et statuts inconnus sont refusés plutôt qu'ignorés, pour qu'un rapport ne paraisse jamais complet alors qu'il ne l'est pas.
 */
@Service
public class RapportPersonnaliseService {

    public static final int COLONNES_MAX = 8;
    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("America/Toronto"));

    private final SaisieRepository fiches;
    private final LectureFormulaires formulaires;
    private final RenduRapport rendu;
    private final ObjectMapper json = new ObjectMapper();

    public RapportPersonnaliseService(SaisieRepository fiches, LectureFormulaires formulaires, RenduRapport rendu) {
        this.fiches = fiches;
        this.formulaires = formulaires;
        this.rendu = rendu;
    }

    public byte[] pdf(Long formulaireId, String titre, List<String> champs, List<String> statuts) {
        return rendu.pdf(rapport(formulaireId, titre, champs, statuts));
    }

    RapportFiches rapport(Long formulaireId, String titre, List<String> champs, List<String> statuts) {
        LectureFormulaires.Description d = formulaires.decrire(TenantContext.get(), formulaireId)
                .orElseThrow(() -> new NoSuchElementException("Formulaire introuvable : " + formulaireId));
        List<String> retenus = exigerConnus(champs, d.libelles().keySet(), "Question inconnue");
        if (retenus.size() > COLONNES_MAX) throw new IllegalArgumentException("Un rapport montre au plus " + COLONNES_MAX + " questions.");
        List<String> filtre = exigerConnus(statuts, Arrays.stream(StatutSaisie.values()).map(Enum::name).toList(), "Statut inconnu");
        List<Saisie> choisies = fiches.findByTenantIdAndFormulaireIdOrderByMajLeDesc(TenantContext.get(), formulaireId).stream()
                .filter(s -> filtre.isEmpty() || filtre.contains(s.getStatut().name())).toList();
        List<String> colonnes = new ArrayList<>(List.of("Fiche", "Statut", "Soumise le"));
        retenus.forEach(c -> colonnes.add(d.libelles().get(c)));
        Map<String, Long> parStatut = choisies.stream().collect(Collectors.groupingBy(s -> s.getStatut().name(), Collectors.counting()));
        String intitule = titre == null || titre.isBlank() ? "Rapport — " + d.nom() : titre.strip();
        return new RapportFiches(intitule, d.nom(), filtre, parStatut, colonnes, choisies.stream().map(ligne(retenus)).toList());
    }

    private Function<Saisie, List<String>> ligne(List<String> retenus) {
        return s -> {
            Map<String, Object> reponses = reponses(s.getDonneesJson());
            List<String> l = new ArrayList<>(List.of(s.getNumero() == null ? "" : s.getNumero(), s.getStatut().name(),
                    s.getSoumisLe() == null ? "" : DATE.format(s.getSoumisLe())));
            retenus.forEach(c -> l.add(texte(reponses.get(c))));
            return l;
        };
    }

    static List<String> exigerConnus(List<String> demandes, Collection<String> connus, String erreur) {
        List<String> nettoyes = demandes == null ? List.of() : demandes.stream().map(String::strip).filter(x -> !x.isEmpty()).distinct().toList();
        for (String x : nettoyes) if (!connus.contains(x)) throw new IllegalArgumentException(erreur + " : « " + x + " ».");
        return nettoyes;
    }

    static String texte(Object v) {
        if (v == null) return "";
        if (v instanceof Collection<?> plusieurs) return plusieurs.stream().map(String::valueOf).collect(Collectors.joining(", "));
        if (v instanceof Boolean b) return b ? "Oui" : "Non";
        return String.valueOf(v);
    }

    private Map<String, Object> reponses(String donnees) {
        try {
            return donnees == null || donnees.isBlank() ? Map.of() : json.readValue(donnees, new TypeReference<Map<String, Object>>() { });
        } catch (Exception illisible) {
            return Map.of();
        }
    }
}
