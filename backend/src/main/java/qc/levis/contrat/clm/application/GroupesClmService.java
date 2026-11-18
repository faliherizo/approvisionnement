package qc.levis.contrat.clm.application;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.port.AnnuaireOrganisation;
import qc.levis.contrat.clm.application.port.ReferencesClm;
import qc.levis.contrat.clm.application.repository.GroupeClmRepository;
import qc.levis.contrat.clm.application.repository.MembreGroupeClmRepository;
import qc.levis.contrat.clm.domain.GroupeClm;
import qc.levis.contrat.clm.domain.MembreGroupeClm;

/**
 * Groupes d'utilisateurs du module, administrés par l'organisation (Cahier § 1.4.7 : attribution par groupes, partage
 * des modèles par groupe d'usagers ; § 1.4.12 : accès par groupes et unités administratives). Un groupe dont le code est
 * celui d'une unité de la liste de l'organisation ouvre à ses membres les trousses de cette unité.
 */
@Service
public class GroupesClmService {

    private final GroupeClmRepository groupes;
    private final MembreGroupeClmRepository membres;
    private final AnnuaireOrganisation annuaire;
    private final ListesClm listes;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public GroupesClmService(GroupeClmRepository groupes, MembreGroupeClmRepository membres, AnnuaireOrganisation annuaire,
                             ListesClm listes, JournalClm journal, ContexteClm ctx) {
        this.groupes = groupes;
        this.membres = membres;
        this.annuaire = annuaire;
        this.listes = listes;
        this.journal = journal;
        this.ctx = ctx;
    }

    public List<ClmDto.GroupeVue> lister() {
        Set<String> unites = unites();
        return groupes.findByTenantIdOrderByCodeAsc(ctx.tenant()).stream().map(g -> vue(g, unites)).toList();
    }

    @Transactional
    public ClmDto.GroupeVue creer(ClmDto.GroupeRequest req, String acteur) {
        GroupeClm g = GroupeClm.creer(ctx.tenant(), req.code(), req.libelle(), acteur, ctx.maintenant());
        if (groupes.existsByTenantIdAndCode(g.getTenantId(), g.getCode())) {
            throw new IllegalStateException("Un groupe porte déjà le code " + g.getCode() + ".");
        }
        g = groupes.save(g);
        journal.configuration(acteur, "GROUPE", "créé : " + g.getCode() + " (" + g.getLibelle() + ")");
        return vue(g, unites());
    }

    @Transactional
    public void supprimer(Long id, String acteur) {
        GroupeClm g = exiger(id);
        membres.deleteByTenantIdAndGroupeId(g.getTenantId(), g.getId());
        groupes.delete(g);
        journal.configuration(acteur, "GROUPE", "supprimé : " + g.getCode());
    }

    @Transactional
    public ClmDto.GroupeVue ajouterMembre(Long id, ClmDto.MembreRequest req, String acteur) {
        GroupeClm g = exiger(id);
        String courriel = req.courriel() == null ? "" : req.courriel().strip().toLowerCase(Locale.ROOT);
        if (!annuaire.estMembre(g.getTenantId(), courriel)) {
            throw new IllegalArgumentException("« " + courriel + " » n'est pas un compte actif de l'organisation.");
        }
        if (membres.existsByTenantIdAndGroupeIdAndCourrielIgnoreCase(g.getTenantId(), g.getId(), courriel)) {
            throw new IllegalStateException("Déjà membre du groupe " + g.getCode() + ".");
        }
        membres.save(new MembreGroupeClm(g, courriel));
        journal.configuration(acteur, "GROUPE", g.getCode() + " : membre ajouté " + courriel);
        return vue(g, unites());
    }

    @Transactional
    public ClmDto.GroupeVue retirerMembre(Long id, String courriel, String acteur) {
        GroupeClm g = exiger(id);
        if (membres.deleteByTenantIdAndGroupeIdAndCourrielIgnoreCase(g.getTenantId(), g.getId(), courriel) == 0) {
            throw new NoSuchElementException("« " + courriel + " » n'est pas membre du groupe " + g.getCode() + ".");
        }
        journal.configuration(acteur, "GROUPE", g.getCode() + " : membre retiré " + courriel.toLowerCase(Locale.ROOT));
        return vue(g, unites());
    }

    /**
     * Domaines d'affaires tirés de l'annuaire (addenda 3, Q44) : pour chaque groupe piloté par une correspondance, le compte
     * y est ajouté ou retiré selon ses faits d'annuaire ; un groupe piloté absent est créé. Les groupes manuels ne bougent pas.
     */
    @Transactional
    public void synchroniserAnnuaire(Long tenantId, String courriel, Set<String> groupesDuCompte, Set<String> groupesGeres) {
        String c = courriel.strip().toLowerCase(Locale.ROOT);
        for (String code : groupesGeres) {
            GroupeClm g = groupes.findByTenantIdAndCode(tenantId, code).orElseGet(() -> {
                GroupeClm cree = groupes.save(GroupeClm.creer(tenantId, code, code, "annuaire", ctx.maintenant()));
                journal.configuration("annuaire", "GROUPE", "créé depuis l'annuaire : " + code);
                return cree;
            });
            boolean membre = membres.existsByTenantIdAndGroupeIdAndCourrielIgnoreCase(tenantId, g.getId(), c);
            if (groupesDuCompte.contains(code) && !membre) {
                membres.save(new MembreGroupeClm(g, c));
                journal.configuration("annuaire", "GROUPE", code + " : membre ajouté " + c);
            } else if (!groupesDuCompte.contains(code) && membre) {
                membres.deleteByTenantIdAndGroupeIdAndCourrielIgnoreCase(tenantId, g.getId(), c);
                journal.configuration("annuaire", "GROUPE", code + " : membre retiré " + c);
            }
        }
    }

    /** Codes des groupes d'un compte : lus à chaque requête pour composer son périmètre. */
    public Set<String> codesDe(Long tenantId, String courriel) {
        return membres.findByTenantIdAndCourrielIgnoreCase(tenantId, courriel).stream()
                .map(MembreGroupeClm::getGroupeCode).collect(Collectors.toSet());
    }

    private GroupeClm exiger(Long id) {
        return groupes.findByIdAndTenantId(id, ctx.tenant()).orElseThrow(() -> new NoSuchElementException("Groupe introuvable."));
    }

    private Set<String> unites() {
        return listes.valeurs(ListesClm.Liste.UNITE).stream().map(ReferencesClm.Valeur::code).collect(Collectors.toSet());
    }

    private ClmDto.GroupeVue vue(GroupeClm g, Set<String> unites) {
        List<String> courriels = membres.findByTenantIdAndGroupeIdOrderByCourrielAsc(g.getTenantId(), g.getId()).stream()
                .map(MembreGroupeClm::getCourriel).toList();
        return new ClmDto.GroupeVue(g.getId(), g.getCode(), g.getLibelle(), courriels, unites.contains(g.getCode()));
    }
}
