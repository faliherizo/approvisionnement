package qc.levis.contrat.soutien.application.dto;

import java.time.LocalDate;
import java.util.List;
import qc.levis.contrat.soutien.domain.Mouvement;
import qc.levis.contrat.soutien.domain.Solde;

/** Banque d'heures avec son solde et, à la consultation, ses mouvements. */
public record BanqueVue(Long id, Long tenantId, String nom, LocalDate debut, LocalDate fin, Solde solde, List<Mouvement> mouvements) {}
