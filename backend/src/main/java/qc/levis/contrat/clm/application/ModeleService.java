package qc.levis.contrat.clm.application;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.SourceAleatoire;
import qc.levis.contrat.clm.application.repository.ModeleContratRepository;
import qc.levis.contrat.clm.application.repository.TrousseRepository;
import qc.levis.contrat.clm.domain.Habilitations;
import qc.levis.contrat.clm.domain.Modele;
import qc.levis.contrat.clm.domain.ModeleContrat;
import qc.levis.contrat.clm.domain.Parcours;

/**
 * Catalogue et cycle de vie des modèles (ST-05, UI-07 à UI-09) : brouillon → révision → publié → retiré.
 * L'auteur soumet, l'administration publie ; une seule version publiée par modèle à la fois.
 */
@Service
public class ModeleService {

    static final TypeReference<List<Modele.Clause>> CLAUSES = new TypeReference<>() {
    };

    private final ModeleContratRepository modeles;
    private final TrousseRepository trousses;
    private final SourceAleatoire alea;
    private final ContexteClm ctx;

    public ModeleService(ModeleContratRepository modeles, TrousseRepository trousses, SourceAleatoire alea, ContexteClm ctx) {
        this.modeles = modeles;
        this.trousses = trousses;
        this.alea = alea;
        this.ctx = ctx;
    }

    public List<ClmDto.ModeleVue> lister(Habilitations.Acteur acteur, String statut) {
        return modeles.findByTenantIdOrderByNomAscNumeroDesc(ctx.tenant()).stream()
                .filter(m -> visible(m, acteur))
                .filter(m -> statut == null || statut.isBlank() || m.getStatut().name().equals(statut))
                .map(this::vue).toList();
    }

    public ClmDto.ModeleVue lire(Long id, Habilitations.Acteur acteur) {
        return vue(exigerVisible(id, acteur));
    }

    @Transactional
    public ClmDto.ModeleVue creer(ClmDto.ModeleRequest req, Habilitations.Acteur acteur) {
        ModeleContrat m = new ModeleContrat(ctx.tenant(), alea.uuid(), 1, exigerNom(req), acteur.id(), ctx.maintenant());
        appliquer(m, req);
        return vue(modeles.save(m));
    }

    @Transactional
    public ClmDto.ModeleVue modifier(Long id, ClmDto.ModeleRequest req, Habilitations.Acteur acteur) {
        ModeleContrat m = exigerGestion(id, acteur);
        m.exigerBrouillon();
        if (req.verrou() != null && req.verrou() != m.getVerrou()) {
            throw new IllegalStateException("Conflit de version : le modèle a été modifié entre-temps.");
        }
        appliquer(m, req);
        return vue(modeles.save(m));
    }

    private void appliquer(ModeleContrat m, ClmDto.ModeleRequest req) {
        m.modifier(exigerNom(req), vide(req.categorie()), vide(req.groupes()), req.corps(), new ModeleContrat.Contenu(
                ctx.ecrire(req.champs() == null ? List.of() : req.champs()),
                ctx.ecrire(req.clauses() == null ? List.of() : req.clauses()),
                ctx.ecrire(req.parcours() == null ? List.of() : req.parcours())), ctx.maintenant());
    }

    public ClmDto.Validation valider(Long id, Habilitations.Acteur acteur) {
        return validation(exigerVisible(id, acteur));
    }

    ClmDto.Validation validation(ModeleContrat m) {
        List<String> erreurs = new ArrayList<>(Modele.valider(m.getCorps(), champs(m), clauses(m)));
        List<ClmDto.EtapeRequest> parcours = ctx.lire(m.getParcoursJson(), TrousseService.PARCOURS, List.of());
        if (!parcours.isEmpty()) {
            try {
                erreurs.addAll(Parcours.valider(TrousseService.definitions(parcours)));
            } catch (IllegalArgumentException e) {
                erreurs.add(e.getMessage());
            }
        }
        return new ClmDto.Validation(erreurs.isEmpty(), erreurs);
    }

    /** « Tester règle » : aperçu avec des valeurs d'essai, sans rien enregistrer. */
    public ClmDto.ApercuModele apercu(Long id, Map<String, Object> valeurs, Habilitations.Acteur acteur) {
        ModeleContrat m = exigerVisible(id, acteur);
        ClmDto.ApercuModele a = DocumentClmService.assembler(m, valeurs == null ? Map.of() : valeurs, ctx);
        return new ClmDto.ApercuModele(a.texte(), a.clausesRetenues(), a.champsMasques(), validation(m).erreurs());
    }

    @Transactional
    public ClmDto.ModeleVue soumettre(Long id, Habilitations.Acteur acteur) {
        ModeleContrat m = exigerGestion(id, acteur);
        ClmDto.Validation v = validation(m);
        if (!v.valide()) {
            throw new IllegalArgumentException(String.join(" ", v.erreurs()));
        }
        m.changerStatut(Modele.Statut.EN_REVISION, ctx.maintenant());
        return vue(modeles.save(m));
    }

    @Transactional
    public ClmDto.ModeleVue renvoyer(Long id, Habilitations.Acteur acteur) {
        ModeleContrat m = exigerAdmin(id, acteur);
        m.changerStatut(Modele.Statut.BROUILLON, ctx.maintenant());
        return vue(modeles.save(m));
    }

    @Transactional
    public ClmDto.ModeleVue publier(Long id, Habilitations.Acteur acteur) {
        ModeleContrat m = exigerAdmin(id, acteur);
        if (m.getProprietaire().equalsIgnoreCase(acteur.id()) && !acteur.roles().contains("SUPERADMIN")) {
            throw new IllegalStateException("Séparation des responsabilités : l'auteur ne publie pas son propre modèle.");
        }
        for (ModeleContrat ancien : modeles.findByTenantIdAndCleModeleAndStatut(m.getTenantId(), m.getCleModele(), Modele.Statut.PUBLIE)) {
            ancien.changerStatut(Modele.Statut.RETIRE, ctx.maintenant());
            modeles.save(ancien);
        }
        m.changerStatut(Modele.Statut.PUBLIE, ctx.maintenant());
        return vue(modeles.save(m));
    }

    @Transactional
    public ClmDto.ModeleVue retirer(Long id, Habilitations.Acteur acteur) {
        ModeleContrat m = exigerAdmin(id, acteur);
        m.changerStatut(Modele.Statut.RETIRE, ctx.maintenant());
        return vue(modeles.save(m));
    }

    /** Nouvelle version brouillon copiée de la plus récente ; la version publiée reste en service jusqu'à remplacement. */
    @Transactional
    public ClmDto.ModeleVue nouvelleVersion(Long id, Habilitations.Acteur acteur) {
        ModeleContrat source = exigerVisible(id, acteur);
        List<ModeleContrat> versions = modeles.findByTenantIdAndCleModeleOrderByNumeroDesc(source.getTenantId(), source.getCleModele());
        if (versions.stream().anyMatch(v -> v.getStatut() == Modele.Statut.BROUILLON || v.getStatut() == Modele.Statut.EN_REVISION)) {
            throw new IllegalStateException("Conflit de version : un brouillon ou une révision existe déjà.");
        }
        ModeleContrat m = new ModeleContrat(source.getTenantId(), source.getCleModele(), versions.get(0).getNumero() + 1,
                source.getNom(), acteur.id(), ctx.maintenant());
        m.modifier(source.getNom(), source.getCategorie(), source.getGroupes(), source.getCorps(),
                new ModeleContrat.Contenu(source.getChampsJson(), source.getClausesJson(), source.getParcoursJson()), ctx.maintenant());
        return vue(modeles.save(m));
    }

    private boolean visible(ModeleContrat m, Habilitations.Acteur acteur) {
        if (acteur.estAdmin() || m.getProprietaire().equalsIgnoreCase(acteur.id()) || m.getGroupes() == null || m.getGroupes().isBlank()) {
            return true;
        }
        return Arrays.stream(m.getGroupes().split(",")).map(String::trim).anyMatch(acteur.groupes()::contains);
    }

    private ModeleContrat exiger(Long id) {
        return modeles.findByIdAndTenantId(id, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Modèle introuvable : " + id));
    }

    private ModeleContrat exigerVisible(Long id, Habilitations.Acteur acteur) {
        ModeleContrat m = exiger(id);
        if (!visible(m, acteur)) {
            throw new NoSuchElementException("Modèle introuvable : " + id);
        }
        return m;
    }

    private ModeleContrat exigerGestion(Long id, Habilitations.Acteur acteur) {
        ModeleContrat m = exigerVisible(id, acteur);
        if (!acteur.estAdmin() && !m.getProprietaire().equalsIgnoreCase(acteur.id())) {
            throw new AccessDeniedException("Droits insuffisants sur ce modèle.");
        }
        return m;
    }

    private ModeleContrat exigerAdmin(Long id, Habilitations.Acteur acteur) {
        if (!acteur.estAdmin()) {
            throw new AccessDeniedException("Droits insuffisants : publication réservée à l'administration.");
        }
        return exigerVisible(id, acteur);
    }

    List<Modele.Champ> champs(ModeleContrat m) {
        return ctx.lire(m.getChampsJson(), TrousseService.CHAMPS, List.of());
    }

    List<Modele.Clause> clauses(ModeleContrat m) {
        return ctx.lire(m.getClausesJson(), CLAUSES, List.of());
    }

    private ClmDto.ModeleVue vue(ModeleContrat m) {
        return new ClmDto.ModeleVue(m.getId(), m.getCleModele(), m.getNumero(), m.getNom(), m.getCategorie(), m.getStatut().name(),
                m.getProprietaire(), m.getGroupes(), m.getCorps(), champs(m), clauses(m),
                ctx.lire(m.getParcoursJson(), TrousseService.PARCOURS, List.of()), m.getMajLe(), m.getVerrou(),
                trousses.countByTenantIdAndModeleId(m.getTenantId(), m.getId()));
    }

    private static String exigerNom(ClmDto.ModeleRequest req) {
        if (req.nom() == null || req.nom().isBlank()) {
            throw new IllegalArgumentException("Nom du modèle requis.");
        }
        return req.nom().trim();
    }

    private static String vide(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
