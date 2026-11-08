package qc.levis.contrat.clm.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.clm.application.dto.ClmDto;
import qc.levis.contrat.clm.application.repository.ParametresClmRepository;
import qc.levis.contrat.clm.domain.FormatDocument;
import qc.levis.contrat.clm.domain.Identite;
import qc.levis.contrat.clm.domain.ParametresClm;
import qc.levis.contrat.clm.domain.Relances;

/**
 * Paramètres du module (UI-22) : ceux de la plateforme, remplacés par ceux que l'organisation enregistre. Chaque
 * changement est borné par le domaine et consigné au journal d'audit de l'organisation.
 */
@Service
public class ParametresClmService {

    private final ParametresClmRepository parametres;
    private final ClmProprietes proprietes;
    private final JournalClm journal;
    private final ContexteClm ctx;

    public ParametresClmService(ParametresClmRepository parametres, ClmProprietes proprietes, JournalClm journal, ContexteClm ctx) {
        this.parametres = parametres;
        this.proprietes = proprietes;
        this.journal = journal;
        this.ctx = ctx;
    }

    /** Valeurs de la plateforme : configuration du déploiement, puis valeurs initiales du domaine. */
    public ParametresClm.Reglages plateforme() {
        return new ParametresClm.Reglages(proprietes.documents().tailleMaxOctets(), proprietes.invitation().validiteJours(),
                proprietes.portailUrl(), ParametresClm.PREFIXE_PAR_DEFAUT, Relances.FREQUENCE_PAR_DEFAUT_JOURS,
                Relances.RAPPELS_PAR_DEFAUT, Identite.Politique.DEFAUT, ParametresClm.CHAMP_MONTANT_PAR_DEFAUT);
    }

    /** Paramètres effectifs d'une organisation. */
    public ParametresClm.Reglages reglages(Long tenantId) {
        return parametres.findByTenantId(tenantId).map(ParametresClm::reglages).orElseGet(this::plateforme);
    }

    public ClmDto.ParametresModule lire() {
        Optional<ParametresClm> propres = parametres.findByTenantId(ctx.tenant());
        return vue(propres.map(ParametresClm::reglages).orElseGet(this::plateforme), propres.orElse(null));
    }

    public ClmDto.ParametresPublics publics() {
        ParametresClm.Reglages r = reglages(ctx.tenant());
        return new ClmDto.ParametresPublics(r.tailleMaxOctets(), formats(), r.rappelFrequenceJours(), r.rappelsMax(),
                niveaux(r.identite(), true), niveaux(r.identite(), false));
    }

    @Transactional
    public ClmDto.ParametresModule modifier(ClmDto.ParametresModuleRequest req, String acteur) {
        Long tenant = ctx.tenant();
        ParametresClm.Reglages r = fusion(reglages(tenant), req);
        List<String> erreurs = new ArrayList<>(r.erreurs(proprietes.documents().plafondOctets()));
        if (proprietes.production() && !r.portailUrl().toLowerCase(Locale.ROOT).startsWith("https://")) {
            erreurs.add("Adresse du portail des signataires : https requis en production.");
        }
        if (!erreurs.isEmpty()) {
            throw new IllegalArgumentException(String.join(" ", erreurs));
        }
        ParametresClm p = parametres.findByTenantId(tenant).orElseGet(() -> new ParametresClm(tenant));
        p.appliquer(r, acteur, ctx.maintenant());
        p = parametres.save(p);
        journal.configuration(acteur, "PARAMETRES", resume(r));
        return vue(p.reglages(), p);
    }

    /** L'organisation revient aux paramètres de la plateforme. */
    @Transactional
    public ClmDto.ParametresModule retablir(String acteur) {
        parametres.findByTenantId(ctx.tenant()).ifPresent(p -> {
            parametres.delete(p);
            journal.configuration(acteur, "PARAMETRES", "paramètres de la plateforme rétablis");
        });
        return lire();
    }

    /** Formats de document acceptés : PDF et Word (NF-07), fixés par le contrôle de contenu de la plateforme. */
    public static List<String> formats() {
        return Arrays.stream(FormatDocument.Type.values()).map(FormatDocument.Type::mime).toList();
    }

    private ClmDto.ParametresModule vue(ParametresClm.Reglages r, ParametresClm propres) {
        Identite.Politique p = r.identite();
        return new ClmDto.ParametresModule(r.tailleMaxOctets(), proprietes.documents().plafondOctets(), r.invitationValiditeJours(),
                r.portailUrl(), r.prefixeReference(), r.rappelFrequenceJours(), r.rappelsMax(), niveaux(p, true), niveaux(p, false),
                p.tentativesMax(), p.codeValiditeMinutes(), r.champMontant(), propres != null,
                propres == null ? null : propres.getModifiePar(), propres == null ? null : propres.getModifieLe());
    }

    static ClmDto.NiveauxIdentite niveaux(Identite.Politique p, boolean externe) {
        return new ClmDto.NiveauxIdentite(p.minimumRequis(Identite.Risque.FAIBLE, externe).name(),
                p.minimumRequis(Identite.Risque.MOYEN, externe).name(), p.minimumRequis(Identite.Risque.ELEVE, externe).name());
    }

    private static ParametresClm.Reglages fusion(ParametresClm.Reglages a, ClmDto.ParametresModuleRequest req) {
        Identite.Politique p = a.identite();
        Identite.Politique identite = new Identite.Politique(niveaux(req.identiteExterne(), p, true), niveaux(req.identiteInterne(), p, false),
                choix(req.tentativesIdentite(), p.tentativesMax()), choix(req.codeValiditeMinutes(), p.codeValiditeMinutes()));
        return new ParametresClm.Reglages(choix(req.tailleMaxOctets(), a.tailleMaxOctets()),
                choix(req.invitationValiditeJours(), a.invitationValiditeJours()),
                texte(req.portailUrl(), a.portailUrl(), Function.identity()),
                texte(req.prefixeReference(), a.prefixeReference(), s -> s.toUpperCase(Locale.ROOT)),
                choix(req.rappelFrequenceJours(), a.rappelFrequenceJours()), choix(req.rappelsMax(), a.rappelsMax()), identite,
                texte(req.champMontant(), a.champMontant(), Function.identity()));
    }

    private static Map<Identite.Risque, Identite.Niveau> niveaux(ClmDto.NiveauxIdentite saisis, Identite.Politique actuelle,
                                                                 boolean externe) {
        Map<Identite.Risque, Identite.Niveau> m = new EnumMap<>(Identite.Risque.class);
        for (Identite.Risque r : Identite.Risque.values()) {
            String saisi = saisis == null ? null : switch (r) {
                case FAIBLE -> saisis.faible();
                case MOYEN -> saisis.moyen();
                case ELEVE -> saisis.eleve();
            };
            m.put(r, saisi == null || saisi.isBlank() ? actuelle.minimumRequis(r, externe) : niveau(saisi));
        }
        return m;
    }

    private static Identite.Niveau niveau(String saisi) {
        try {
            return Identite.Niveau.valueOf(saisi.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Niveau d'identité inconnu : " + saisi + ".", e);
        }
    }

    private static long choix(Long saisi, long actuel) {
        return saisi == null ? actuel : saisi;
    }

    private static int choix(Integer saisi, int actuel) {
        return saisi == null ? actuel : saisi;
    }

    private static String texte(String saisi, String actuel, Function<String, String> forme) {
        return saisi == null ? actuel : forme.apply(saisi.trim());
    }

    /** Trace d'audit : la configuration complète retenue, chaque valeur nommée. */
    private static String resume(ParametresClm.Reglages r) {
        Identite.Politique p = r.identite();
        return "taille max " + r.tailleMaxOctets() + " o · invitation " + r.invitationValiditeJours() + " j · portail " + r.portailUrl()
                + " · préfixe " + r.prefixeReference() + " · rappels " + r.rappelFrequenceJours() + " j × " + r.rappelsMax()
                + " · identité externes " + niveaux(p, true) + " internes " + niveaux(p, false) + " · tentatives " + p.tentativesMax()
                + " · code " + p.codeValiditeMinutes() + " min · champ montant " + r.champMontant();
    }
}
