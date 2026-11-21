package qc.levis.contrat.rapport.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.commun.infrastructure.TenantContext;
import qc.levis.contrat.rapport.application.dto.TableauBordRequest;
import qc.levis.contrat.rapport.application.dto.TableauBordVue;
import qc.levis.contrat.rapport.application.port.LectureFormulaires;
import qc.levis.contrat.rapport.application.repository.TableauBordRepository;
import qc.levis.contrat.rapport.domain.Agregation;
import qc.levis.contrat.rapport.domain.Indicateur;
import qc.levis.contrat.rapport.domain.TableauBord;
import qc.levis.contrat.saisie.application.repository.SaisieRepository;
import qc.levis.contrat.saisie.domain.StatutSaisie;

/**
 * Tableaux de bord configurables (CHU 6.04) : chacun voit les siens et ceux partagés de son organisation ; seul le
 * propriétaire modifie. Les indicateurs ne portent que sur des formulaires de l'organisation et n'exposent que des comptes.
 */
@Service
public class TableauBordService {

    private final TableauBordRepository tableaux;
    private final SaisieRepository fiches;
    private final LectureFormulaires formulaires;
    private final Clock horloge;
    private final ObjectMapper json = new ObjectMapper();

    public TableauBordService(TableauBordRepository tableaux, SaisieRepository fiches, LectureFormulaires formulaires, Clock horloge) {
        this.tableaux = tableaux;
        this.fiches = fiches;
        this.formulaires = formulaires;
        this.horloge = horloge;
    }

    public List<TableauBordVue> lister(String moi) {
        return tableaux.findByTenantIdOrderByNomAsc(TenantContext.get()).stream().filter(t -> t.estVisiblePar(moi))
                .map(t -> vue(t, moi, false)).toList();
    }

    public TableauBordVue consulter(Long id, String moi) {
        return vue(visible(id, moi), moi, true);
    }

    @Transactional
    public TableauBordVue creer(TableauBordRequest req, String moi) {
        TableauBord t = new TableauBord(TenantContext.get(), moi, horloge.instant());
        appliquer(t, req);
        return vue(tableaux.save(t), moi, true);
    }

    @Transactional
    public TableauBordVue modifier(Long id, TableauBordRequest req, String moi) {
        TableauBord t = proprietaire(id, moi);
        appliquer(t, req);
        return vue(tableaux.save(t), moi, true);
    }

    @Transactional
    public void supprimer(Long id, String moi) {
        tableaux.delete(proprietaire(id, moi));
    }

    private void appliquer(TableauBord t, TableauBordRequest req) {
        List<Indicateur> indicateurs = req.indicateurs().stream().map(TableauBordRequest.IndicateurRequest::versDomaine).toList();
        indicateurs.forEach(this::exigerValide);
        t.configurer(req.nom(), req.partage(), ecrire(indicateurs), horloge.instant());
    }

    /** Le formulaire doit être de l'organisation, la question en faire partie, les statuts exister. */
    private void exigerValide(Indicateur i) {
        LectureFormulaires.Description d = formulaires.decrire(TenantContext.get(), i.formulaireId())
                .orElseThrow(() -> new IllegalArgumentException("Formulaire inconnu pour l'indicateur « " + i.titre() + " »."));
        if (i.regroupement() == Indicateur.Regroupement.CHAMP && !d.libelles().containsKey(i.champ())) {
            throw new IllegalArgumentException("Question inconnue pour l'indicateur « " + i.titre() + " » : " + i.champ());
        }
        RapportPersonnaliseService.exigerConnus(i.statuts(), Arrays.stream(StatutSaisie.values()).map(Enum::name).toList(), "Statut inconnu");
    }

    private TableauBord visible(Long id, String moi) {
        return tableaux.findByIdAndTenantId(id, TenantContext.get()).filter(t -> t.estVisiblePar(moi))
                .orElseThrow(() -> new NoSuchElementException("Tableau de bord introuvable : " + id));
    }

    private TableauBord proprietaire(Long id, String moi) {
        TableauBord t = visible(id, moi);
        if (!t.estProprietaire(moi)) throw new AccessDeniedException("Seul le propriétaire modifie ce tableau de bord.");
        return t;
    }

    private TableauBordVue vue(TableauBord t, String moi, boolean avecValeurs) {
        List<TableauBordVue.IndicateurVue> indicateurs = lire(t.getIndicateursJson()).stream()
                .map(i -> new TableauBordVue.IndicateurVue(i, avecValeurs ? Agregation.calculer(i, donnees(i.formulaireId())) : List.of()))
                .toList();
        return new TableauBordVue(t.getId(), t.getNom(), t.getProprietaire(), t.isPartage(), t.estProprietaire(moi), t.getModifieLe(), indicateurs);
    }

    private List<Agregation.Fiche> donnees(Long formulaireId) {
        return fiches.findByTenantIdAndFormulaireIdOrderByMajLeDesc(TenantContext.get(), formulaireId).stream()
                .map(s -> new Agregation.Fiche(s.getStatut().name(), s.getSoumisLe(), reponses(s.getDonneesJson()))).toList();
    }

    private String ecrire(List<Indicateur> indicateurs) {
        try {
            return json.writeValueAsString(indicateurs);
        } catch (Exception e) {
            throw new IllegalStateException("Indicateurs illisibles.", e);
        }
    }

    List<Indicateur> lire(String indicateursJson) {
        try {
            return json.readValue(indicateursJson, new TypeReference<List<Indicateur>>() { });
        } catch (Exception illisible) {
            return List.of();
        }
    }

    private Map<String, Object> reponses(String donnees) {
        try {
            return donnees == null || donnees.isBlank() ? Map.of() : json.readValue(donnees, new TypeReference<Map<String, Object>>() { });
        } catch (Exception illisible) {
            return Map.of();
        }
    }
}
