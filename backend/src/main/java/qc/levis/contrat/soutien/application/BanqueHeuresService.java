package qc.levis.contrat.soutien.application;

import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.audit.application.AuditService;
import qc.levis.contrat.soutien.application.dto.BanqueRequest;
import qc.levis.contrat.soutien.application.dto.BanqueVue;
import qc.levis.contrat.soutien.application.dto.MouvementRequest;
import qc.levis.contrat.soutien.application.port.Organisations;
import qc.levis.contrat.soutien.application.repository.BanqueHeuresRepository;
import qc.levis.contrat.soutien.application.repository.MouvementRepository;
import qc.levis.contrat.soutien.domain.BanqueHeures;
import qc.levis.contrat.soutien.domain.Mouvement;
import qc.levis.contrat.soutien.domain.Solde;

/**
 * Banque d'heures de services professionnels (CHU 9.05). Le fournisseur ouvre la banque d'une organisation, y inscrit
 * achats, consommations et corrections ; l'organisation consulte son solde et son relevé. L'organisation est toujours
 * donnée explicitement : le superadministrateur n'a pas d'organisation courante.
 */
@Service
public class BanqueHeuresService {

    private final BanqueHeuresRepository banques;
    private final MouvementRepository mouvements;
    private final Organisations organisations;
    private final AuditService audit;
    private final Clock horloge;

    public BanqueHeuresService(BanqueHeuresRepository banques, MouvementRepository mouvements, Organisations organisations,
                               AuditService audit, Clock horloge) {
        this.banques = banques;
        this.mouvements = mouvements;
        this.organisations = organisations;
        this.audit = audit;
        this.horloge = horloge;
    }

    public List<BanqueVue> lister(Long tenantId) {
        return banques.findByTenantIdOrderByDebutDesc(tenantId).stream().map(b -> vue(b, false)).toList();
    }

    public BanqueVue consulter(Long tenantId, Long banqueId) {
        return vue(exiger(tenantId, banqueId), true);
    }

    @Transactional
    public BanqueVue ouvrir(Long tenantId, BanqueRequest req, String par) {
        if (!organisations.existe(tenantId)) throw new NoSuchElementException("Organisation introuvable : " + tenantId);
        BanqueHeures b = banques.save(new BanqueHeures(tenantId, req.nom(), req.debut(), req.fin(), horloge.instant()));
        mouvements.save(Mouvement.achat(b.getId(), req.heures(), req.debut(), "Achat initial", par, horloge.instant()));
        audit.log(tenantId, par, "BANQUE_HEURES_OUVERTURE", "BanqueHeures", b.getId(), req.heures() + " h");
        return vue(b, true);
    }

    /** Inscrit un mouvement ; la banque est verrouillée pour que le solde contrôlé reste exact jusqu'à l'enregistrement. */
    @Transactional
    public BanqueVue inscrire(Long tenantId, Long banqueId, MouvementRequest req, String par) {
        BanqueHeures b = banques.verrouiller(banqueId, tenantId).orElseThrow(() -> new NoSuchElementException("Banque d'heures introuvable : " + banqueId));
        b.exigerDansPeriode(req.date());
        Solde solde = Solde.de(mouvements.findByBanqueIdOrderByDateAscIdAsc(banqueId));
        Mouvement m = switch (req.type()) {
            case ACHAT -> Mouvement.achat(banqueId, req.heures(), req.date(), req.description(), par, horloge.instant());
            case CONSOMMATION -> {
                solde.exigerDisponible(req.heures());
                yield Mouvement.consommation(banqueId, req.categorie(), req.heures(), req.date(), req.description(), req.intervenant(), par, horloge.instant());
            }
            case CORRECTION -> {
                Mouvement c = Mouvement.correction(banqueId, req.categorie(), req.heures(), req.date(), req.description(), par, horloge.instant());
                solde.exigerCorrigeable(req.categorie(), c.getHeures());
                yield c;
            }
        };
        mouvements.save(m);
        audit.log(tenantId, par, "BANQUE_HEURES_" + req.type(), "BanqueHeures", banqueId, m.getHeures() + " h");
        return vue(b, true);
    }

    private BanqueHeures exiger(Long tenantId, Long banqueId) {
        return banques.findByIdAndTenantId(banqueId, tenantId)
                .orElseThrow(() -> new NoSuchElementException("Banque d'heures introuvable : " + banqueId));
    }

    private BanqueVue vue(BanqueHeures b, boolean avecMouvements) {
        List<Mouvement> liste = mouvements.findByBanqueIdOrderByDateAscIdAsc(b.getId());
        return new BanqueVue(b.getId(), b.getTenantId(), b.getNom(), b.getDebut(), b.getFin(), Solde.de(liste), avecMouvements ? liste : List.of());
    }
}
