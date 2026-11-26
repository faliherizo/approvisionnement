package qc.levis.contrat.clm;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.ContexteClm;
import qc.levis.contrat.clm.application.LectureTrousse;
import qc.levis.contrat.clm.application.PerimetreVisible;
import qc.levis.contrat.clm.application.repository.EtapeTrousseRepository;
import qc.levis.contrat.clm.application.repository.ParticipantRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.EtapeTrousse;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Identite;
import qc.levis.contrat.clm.domain.Parcours;
import qc.levis.contrat.clm.domain.Participant;
import qc.levis.contrat.clm.domain.Trousse;

/**
 * Présélection en base des trousses visibles (essai TI-12) : sur la vraie base, le critère retient chaque trousse que la
 * règle d'accès ouvre (propriété, unité, participation, affectation nominative, de groupe ou de rôle) et écarte les
 * autres ; il reste un sur-ensemble exact de {@link LectureTrousse#visible}.
 */
@SpringBootTest
@Transactional
@TestPropertySource(properties = "app.clm.planificateur.actif=false")
class PerimetreVisibleIntegrationTest {

    private static final long TENANT = 990_001L;
    private static final String MOI = "Lea.Martin@iq.example";
    private static final String AUTRE = "autre@iq.example";

    @Autowired TrousseRepository trousses;
    @Autowired EtapeTrousseRepository etapes;
    @Autowired ParticipantRepository participants;
    @Autowired LectureTrousse lecture;
    @Autowired ContexteClm ctx;

    private final Habilitations.Acteur acteur =
            new Habilitations.Acteur(MOI.toLowerCase(), Set.of("JURIDIQUE"), Set.of("FINANCEMENT"));

    @BeforeEach
    void peupler() {
        trousse("PROPRIETE", MOI.toUpperCase());
        trousse("UNITE", AUTRE).setUnite("FINANCEMENT");
        participant(trousse("PARTICIPANT", AUTRE), MOI);
        etape(trousse("NOMINATIVE", AUTRE), Parcours.TypeAffecte.UTILISATEUR, MOI);
        etape(trousse("GROUPE", AUTRE), Parcours.TypeAffecte.GROUPE, "FINANCEMENT");
        etape(trousse("ROLE", AUTRE), Parcours.TypeAffecte.ROLE, "JURIDIQUE");
        trousse("ETRANGERE", AUTRE).setUnite("AFFAIRES_JURIDIQUES");
        etape(trousse("AUTRE_GROUPE", AUTRE), Parcours.TypeAffecte.GROUPE, "FINANCEMENT_PME");
        etape(trousse("AUTRE_PERSONNE", AUTRE), Parcours.TypeAffecte.UTILISATEUR, "lea.martin@iq.example.org");
        participant(trousse("AUTRE_PARTICIPANT", AUTRE), "x" + MOI);
    }

    @Test
    void retient_chaque_voie_d_acces_et_ecarte_les_trousses_d_autrui() {
        Set<String> retenues = trousses.findAll(duTenant().and(PerimetreVisible.candidates(acteur))).stream()
                .map(Trousse::getTitre).collect(Collectors.toSet());

        assertEquals(Set.of("PROPRIETE", "UNITE", "PARTICIPANT", "NOMINATIVE", "GROUPE", "ROLE"), retenues);
    }

    @Test
    void est_un_sur_ensemble_de_la_regle_d_acces() {
        List<Trousse> toutes = trousses.findAll(duTenant());
        Set<Long> retenues = trousses.findAll(duTenant().and(PerimetreVisible.candidates(acteur))).stream()
                .map(Trousse::getId).collect(Collectors.toSet());

        for (Trousse t : toutes) {
            assertEquals(lecture.visible(t, acteur), retenues.contains(t.getId()), t.getTitre());
        }
    }

    @Test
    void un_acteur_sans_groupe_ne_retient_que_ses_propres_trousses() {
        Habilitations.Acteur seul = new Habilitations.Acteur(AUTRE, Set.of(), Set.of());

        Set<String> retenues = trousses.findAll(duTenant().and(PerimetreVisible.candidates(seul))).stream()
                .map(Trousse::getTitre).collect(Collectors.toSet());

        assertEquals(9, retenues.size(), "toutes les trousses du jeu sauf celle de " + MOI);
        assertFalse(retenues.contains("PROPRIETE"));
    }

    private Specification<Trousse> duTenant() {
        return (r, q, cb) -> cb.equal(r.get("tenantId"), TENANT);
    }

    private Trousse trousse(String titre, String proprietaire) {
        return trousses.save(new Trousse(TENANT, "PV-" + titre, titre, proprietaire, Instant.EPOCH));
    }

    private void participant(Trousse t, String courriel) {
        participants.save(new Participant(TENANT, t.getId(), "Personne", courriel, null, "SIGNATAIRE", true,
                Identite.Niveau.COURRIEL));
    }

    private void etape(Trousse t, Parcours.TypeAffecte type, String valeur) {
        List<Parcours.Affectation> affectations = List.of(new Parcours.Affectation(type, valeur));
        Parcours.DefinitionEtape d = new Parcours.DefinitionEtape("validation", "Validation", 1, Parcours.TypeEtape.VALIDATION,
                affectations, 1, Parcours.PolitiqueRejet.UN_REFUS, 5, List.of());
        etapes.save(new EtapeTrousse(TENANT, t.getId(), d, ctx.ecrire(affectations)));
    }
}
