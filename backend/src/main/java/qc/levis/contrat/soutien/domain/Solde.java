package qc.levis.contrat.soutien.domain;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Solde d'une banque d'heures : heures achetées, consommées (nettes des corrections), restantes, et répartition par catégorie. */
public record Solde(BigDecimal achetees, BigDecimal consommees, BigDecimal restantes, Map<CategorieService, BigDecimal> parCategorie) {

    public Solde {
        parCategorie = Map.copyOf(parCategorie);
    }

    public static Solde de(List<Mouvement> mouvements) {
        BigDecimal achetees = BigDecimal.ZERO.setScale(2);
        Map<CategorieService, BigDecimal> parCategorie = new EnumMap<>(CategorieService.class);
        for (CategorieService c : CategorieService.values()) parCategorie.put(c, BigDecimal.ZERO.setScale(2));
        for (Mouvement m : mouvements) {
            switch (m.getType()) {
                case ACHAT -> achetees = achetees.add(m.getHeures());
                case CONSOMMATION -> parCategorie.merge(m.getCategorie(), m.getHeures(), BigDecimal::add);
                case CORRECTION -> parCategorie.merge(m.getCategorie(), m.getHeures().negate(), BigDecimal::add);
            }
        }
        BigDecimal consommees = parCategorie.values().stream().reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        return new Solde(achetees, consommees, achetees.subtract(consommees), parCategorie);
    }

    /** Une consommation qui dépasserait les heures restantes est refusée : il faut d'abord acheter des heures. */
    public void exigerDisponible(BigDecimal heures) {
        if (heures.compareTo(restantes) > 0) {
            throw new SoldeInsuffisant("Solde insuffisant : " + restantes + " h restantes, " + heures + " h demandées.");
        }
    }

    /** Une correction ne rend pas plus d'heures que la catégorie n'en a consommé. */
    public void exigerCorrigeable(CategorieService categorie, BigDecimal heures) {
        if (heures.compareTo(parCategorie.get(categorie)) > 0) {
            throw new IllegalArgumentException("La correction dépasse les heures consommées en " + categorie + ".");
        }
    }

    public static class SoldeInsuffisant extends IllegalStateException {
        public SoldeInsuffisant(String message) {
            super(message);
        }
    }
}
