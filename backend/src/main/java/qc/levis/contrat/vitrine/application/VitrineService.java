package qc.levis.contrat.vitrine.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qc.levis.contrat.tenant.application.TenantAdminService;
import qc.levis.contrat.tenant.application.dto.TenantPublic;
import qc.levis.contrat.vitrine.application.dto.VitrineDto;
import qc.levis.contrat.vitrine.application.port.MediaStore;
import qc.levis.contrat.vitrine.application.repository.VitrineTenantRepository;
import qc.levis.contrat.vitrine.domain.VitrineTenant;

/**
 * Sites vitrines des organisations : lecture publique (vitrine publiée seulement) et administration par le
 * superadministrateur (contenu, vidéo choisie dans la médiathèque ou téléversée).
 */
@Service
public class VitrineService {

    /** Vidéos acceptées au téléversement : lues par tous les navigateurs modernes. */
    private static final Set<String> FORMATS_VIDEO = Set.of("mp4", "webm");

    private final VitrineTenantRepository vitrines;
    private final TenantAdminService tenants;
    private final MediaStore medias;
    private final Clock horloge;

    public VitrineService(VitrineTenantRepository vitrines, TenantAdminService tenants, MediaStore medias, Clock horloge) {
        this.vitrines = vitrines;
        this.tenants = tenants;
        this.medias = medias;
        this.horloge = horloge;
    }

    /** Vitrine publiée d'une organisation active ; vide sinon (le frontend montre alors la vitrine de la plateforme). */
    @Transactional(readOnly = true)
    public Optional<VitrineDto.Publique> publique(String organisation) {
        return tenants.idParSlug(organisation).flatMap(vitrines::findByTenantId).filter(VitrineTenant::isPubliee)
                .map(v -> new VitrineDto.Publique(organisation, v.getTitre(), v.getAccroche(), v.getFonctionnalites(),
                        v.getVideo(), v.getSousTitres(), v.getAffiche()));
    }

    /** Vitrine d'une organisation, à éditer ; une organisation sans vitrine reçoit un brouillon à son nom, non publié. */
    @Transactional(readOnly = true)
    public VitrineDto.Edition lire(Long tenantId) {
        TenantPublic t = organisation(tenantId);
        return edition(t, vitrines.findByTenantId(tenantId).orElseGet(() -> VitrineTenant.nouvelle(tenantId, t.titre())));
    }

    @Transactional
    public VitrineDto.Edition modifier(Long tenantId, VitrineDto.Requete req, String acteur) {
        TenantPublic t = organisation(tenantId);
        VitrineTenant v = vitrines.findByTenantId(tenantId).orElseGet(() -> VitrineTenant.nouvelle(tenantId, t.titre()));
        exigerMedia(req.video());
        exigerMedia(req.sousTitres());
        exigerMedia(req.affiche());
        v.modifier(new VitrineTenant.Contenu(req.titre(), req.accroche(), req.fonctionnalites(), req.video(), req.sousTitres(),
                req.affiche(), req.publiee()), acteur, Instant.now(horloge));
        return edition(t, vitrines.save(v));
    }

    /** Vidéo téléversée pour une organisation : rangée sous vitrines/<organisation>/, puis à choisir dans l'éditeur. */
    public VitrineDto.Media deposerVideo(Long tenantId, String nomFichier, byte[] contenu) {
        TenantPublic t = organisation(tenantId);
        String extension = nomFichier == null ? "" : nomFichier.substring(nomFichier.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!FORMATS_VIDEO.contains(extension) || contenu == null || contenu.length == 0) {
            throw new IllegalArgumentException("Vidéo attendue au format MP4 ou WebM.");
        }
        String chemin = VitrineTenant.chemin("vitrines/" + t.slug() + "/televersee-" + horloge.millis() + "." + extension);
        medias.enregistrer(chemin, contenu);
        return new VitrineDto.Media(chemin, contenu.length);
    }

    public List<VitrineDto.Media> mediatheque(String prefixe) {
        return medias.lister(prefixe == null ? "" : prefixe).stream().map(m -> new VitrineDto.Media(m.chemin(), m.taille())).toList();
    }

    /** Crée la vitrine d'une organisation si elle n'en a pas (amorçage) ; ne remplace jamais une vitrine existante. */
    @Transactional
    public boolean creerSiAbsente(String organisation, VitrineDto.Requete req, String acteur) {
        Optional<Long> id = tenants.idParSlug(organisation);
        if (id.isEmpty() || vitrines.findByTenantId(id.get()).isPresent()) {
            return false;
        }
        VitrineTenant v = VitrineTenant.nouvelle(id.get(), req.titre());
        v.modifier(new VitrineTenant.Contenu(req.titre(), req.accroche(), req.fonctionnalites(), req.video(), req.sousTitres(),
                req.affiche(), req.publiee()), acteur, Instant.now(horloge));
        vitrines.save(v);
        return true;
    }

    private void exigerMedia(String chemin) {
        String c = VitrineTenant.chemin(chemin);
        if (c != null && medias.fichier(c).isEmpty()) {
            throw new IllegalArgumentException("Média absent de la médiathèque : " + c);
        }
    }

    private TenantPublic organisation(Long tenantId) {
        return tenants.publicParId(tenantId).orElseThrow(() -> new NoSuchElementException("Organisation introuvable : " + tenantId));
    }

    private static VitrineDto.Edition edition(TenantPublic t, VitrineTenant v) {
        return new VitrineDto.Edition(v.getTenantId(), t.slug(), v.getTitre(), v.getAccroche(), v.getFonctionnalites(), v.getVideo(),
                v.getSousTitres(), v.getAffiche(), v.isPubliee(), v.getMajPar(), v.getMajLe());
    }
}
