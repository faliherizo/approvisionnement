package qc.levis.contrat.vitrine.application.dto;

import java.time.Instant;
import java.util.List;
import qc.levis.contrat.vitrine.domain.VitrineTenant;

/** Échanges du site vitrine d'une organisation. */
public final class VitrineDto {

    private VitrineDto() {
    }

    /** Ce que voit le public sur https://<organisation>.contrat.vinaitsolution.com/accueil. */
    public record Publique(String organisation, String titre, String accroche, List<VitrineTenant.Fonctionnalite> fonctionnalites,
                           String video, String sousTitres, String affiche) {
    }

    /** Ce que lit et modifie le superadministrateur. */
    public record Edition(Long tenantId, String organisation, String titre, String accroche,
                          List<VitrineTenant.Fonctionnalite> fonctionnalites, String video, String sousTitres, String affiche,
                          boolean publiee, String majPar, Instant majLe) {
    }

    public record Requete(String titre, String accroche, List<VitrineTenant.Fonctionnalite> fonctionnalites, String video,
                          String sousTitres, String affiche, boolean publiee) {
    }

    public record Media(String chemin, long taille) {
    }
}
