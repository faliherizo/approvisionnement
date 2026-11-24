package qc.levis.contrat.interop.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.interop.application.dto.EchangeDto;
import qc.levis.contrat.interop.application.port.FichesEchange;
import qc.levis.contrat.interop.application.port.FormulairesEchange;
import qc.levis.contrat.interop.application.repository.EchangeHeriteRepository;
import qc.levis.contrat.interop.domain.BilanReprise;
import qc.levis.contrat.interop.domain.CorrespondanceEchange;
import qc.levis.contrat.interop.domain.EchangeHerite;

/**
 * Maintien des échanges de la plateforme précédente (CHU-8.04, ADR-021) : un système externe garde ses noms de champs,
 * dans les deux sens, et l'historique exporté se reprend après un essai qui n'écrit rien.
 */
@Service
public class EchangesHeritesService {

    /** Au-delà, l'export se reprend en plusieurs envois : un bilan reste lisible et une requête reste bornée. */
    public static final int REPRISE_MAX = 5000;

    private final EchangeHeriteRepository echanges;
    private final FormulairesEchange formulaires;
    private final FichesEchange fiches;
    private final AuditService audit;
    private final Clock horloge;

    @Autowired
    public EchangesHeritesService(EchangeHeriteRepository echanges, FormulairesEchange formulaires, FichesEchange fiches,
                                  AuditService audit) {
        this(echanges, formulaires, fiches, audit, Clock.systemUTC());
    }

    EchangesHeritesService(EchangeHeriteRepository echanges, FormulairesEchange formulaires, FichesEchange fiches,
                           AuditService audit, Clock horloge) {
        this.echanges = echanges;
        this.formulaires = formulaires;
        this.fiches = fiches;
        this.audit = audit;
        this.horloge = horloge;
    }

    public List<EchangeDto.Vue> lister(Long tenantId) {
        return echanges.findByTenantIdOrderByCodeAsc(tenantId).stream().map(e -> vue(tenantId, e)).toList();
    }

    @Transactional
    public EchangeDto.Vue declarer(Long tenantId, EchangeDto.Declaration d, String par) {
        FormulairesEchange.FormulaireCible f = formulaires.parCode(tenantId, d.formulaireCode())
                .orElseThrow(() -> new CorrespondanceEchange.CorrespondanceInvalide("Formulaire inconnu : " + d.formulaireCode()));
        EchangeHerite e = EchangeHerite.declarer(tenantId, d.code(), f.id(), CorrespondanceEchange.de(d.correspondances(), f.cles()),
                par, Instant.now(horloge));
        if (echanges.existsByTenantIdAndCode(tenantId, e.getCode())) {
            throw new IllegalStateException("Un échange porte déjà le code « " + e.getCode() + " ».");
        }
        EchangeHerite cree = echanges.save(e);
        audit.log(tenantId, par, "ECHANGE_HERITE_DECLARE", "EchangeHerite", cree.getId(), cree.getCode() + " → " + f.code());
        return vue(tenantId, cree);
    }

    @Transactional
    public EchangeDto.Vue desactiver(Long tenantId, Long id, String par) {
        EchangeHerite e = echanges.findByIdAndTenantId(id, tenantId).orElseThrow(() -> new NoSuchElementException("Échange introuvable."));
        e.desactiver();
        audit.log(tenantId, par, "ECHANGE_HERITE_DESACTIVE", "EchangeHerite", id, e.getCode());
        return vue(tenantId, echanges.save(e));
    }

    /** Fiche reçue d'un système externe sous ses anciens noms, soumise comme par l'interface. */
    public FichesEchange.FicheDeposee deposer(Long tenantId, String code, String par, Map<String, Object> ancien) {
        EchangeHerite e = actif(tenantId, code);
        return fiches.deposer(tenantId, e.getFormulaireId(), par, e.correspondance().versFormulaire(ancien));
    }

    /** Fiches soumises, rendues sous les anciens noms. */
    public List<EchangeDto.FicheHeritee> lire(Long tenantId, String code, Instant depuis) {
        EchangeHerite e = actif(tenantId, code);
        CorrespondanceEchange c = e.correspondance();
        return fiches.soumises(tenantId, e.getFormulaireId(), depuis).stream()
                .map(f -> new EchangeDto.FicheHeritee(f.numero(), f.statut(), f.soumisLe(), c.versAncien(f.donnees()))).toList();
    }

    /** Reprise de l'export de la plateforme précédente ; en essai, chaque enregistrement est validé et rien n'est écrit. */
    public BilanReprise reprendre(Long tenantId, Long id, List<Map<String, Object>> export, boolean essai, String par) {
        if (export == null || export.isEmpty() || export.size() > REPRISE_MAX) {
            throw new IllegalArgumentException("Export : 1 à " + REPRISE_MAX + " enregistrements par envoi.");
        }
        EchangeHerite e = echanges.findByIdAndTenantId(id, tenantId).filter(EchangeHerite::isActif)
                .orElseThrow(() -> new NoSuchElementException("Échange introuvable."));
        List<BilanReprise.Ligne> lignes = new ArrayList<>();
        for (int i = 0; i < export.size(); i++) {
            lignes.add(reprendreUn(tenantId, e, i + 1, export.get(i), essai, par));
        }
        BilanReprise bilan = BilanReprise.de(essai, lignes);
        audit.log(tenantId, par, essai ? "REPRISE_ESSAI" : "REPRISE", "EchangeHerite", id,
                bilan.acceptes() + " acceptée(s), " + bilan.rejetes() + " rejetée(s) sur " + bilan.total());
        return bilan;
    }

    private BilanReprise.Ligne reprendreUn(Long tenantId, EchangeHerite e, int rang, Map<String, Object> ancien, boolean essai,
                                          String par) {
        try {
            Map<String, Object> donnees = e.correspondance().versFormulaire(ancien == null ? Map.of() : ancien);
            if (essai) {
                Optional<String> refus = fiches.verifier(tenantId, e.getFormulaireId(), donnees);
                return refus.map(m -> new BilanReprise.Ligne(rang, BilanReprise.Statut.REJETEE, null, m))
                        .orElseGet(() -> new BilanReprise.Ligne(rang, BilanReprise.Statut.VALIDE, null, null));
            }
            FichesEchange.FicheDeposee f = fiches.deposer(tenantId, e.getFormulaireId(), "reprise:" + e.getCode(), donnees);
            return new BilanReprise.Ligne(rang, BilanReprise.Statut.REPRISE, f.numero(), null);
        } catch (IllegalArgumentException | IllegalStateException refus) {
            return new BilanReprise.Ligne(rang, BilanReprise.Statut.REJETEE, null, refus.getMessage());
        }
    }

    private EchangeHerite actif(Long tenantId, String code) {
        return echanges.findByTenantIdAndCodeAndActifTrue(tenantId, code)
                .orElseThrow(() -> new NoSuchElementException("Échange inconnu : " + code));
    }

    private EchangeDto.Vue vue(Long tenantId, EchangeHerite e) {
        Optional<FormulairesEchange.FormulaireCible> f = formulaires.parId(tenantId, e.getFormulaireId());
        return new EchangeDto.Vue(e.getId(), e.getCode(), e.getFormulaireId(), f.map(FormulairesEchange.FormulaireCible::code).orElse(null),
                f.map(FormulairesEchange.FormulaireCible::nom).orElse(null), e.correspondance().table(), e.isActif(),
                e.getCreePar(), e.getCreeLe());
    }
}
