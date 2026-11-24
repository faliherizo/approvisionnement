package qc.levis.contrat.redaction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import qc.levis.contrat.commun.domain.Enums;
import qc.levis.contrat.redaction.domain.Clause;
import qc.levis.contrat.redaction.domain.ClauseCondition;
import qc.levis.contrat.redaction.domain.CriteresRechercheClause;

class CriteresRechercheClauseTest {

    private static Clause clause(String code, String titre, String corps, String section, boolean normative,
                                 Enums.ClauseCategorie categorie, String... typesContrat) {
        Clause c = new Clause();
        c.setCode(code);
        c.setTitre(titre);
        c.setCorps(corps);
        c.setSection(section);
        c.setNormative(normative);
        c.setCategorie(categorie);
        for (String type : typesContrat) {
            ClauseCondition k = new ClauseCondition();
            k.setAttribut(Enums.AttributCondition.TYPE_CONTRAT);
            k.setValeur(type);
            c.getConditions().add(k);
        }
        return c;
    }

    private static final Clause ASSURANCE = clause("ASS-1", "Assurance responsabilité", "Le fournisseur s'assure",
            "Assurances", true, Enums.ClauseCategorie.STANDARD, "APPEL_OFFRES_PUBLIC");
    private static final Clause GENERALE = clause("GEN-1", "Objet", null, null, false, Enums.ClauseCategorie.OPTIONNELLE);

    @Test
    void sansCritere_retientTout() {
        var tous = new CriteresRechercheClause(null, null, null, null, false);
        assertThat(tous.terme()).isEmpty();
        assertThat(tous.thematique()).isEmpty();
        assertThat(tous.retient(ASSURANCE)).isTrue();
        assertThat(tous.retient(GENERALE)).isTrue();
    }

    @Test
    void terme_cherche_codeTitreEtCorps_sansCasse() {
        assertThat(new CriteresRechercheClause(" ass-1 ", null, null, null, false).retient(ASSURANCE)).isTrue();
        assertThat(new CriteresRechercheClause("RESPONSABILITÉ", null, null, null, false).retient(ASSURANCE)).isTrue();
        assertThat(new CriteresRechercheClause("fournisseur", null, null, null, false).retient(ASSURANCE)).isTrue();
        assertThat(new CriteresRechercheClause("fournisseur", null, null, null, false).retient(GENERALE)).isFalse();
    }

    @Test
    void portee_filtreParCategorie() {
        var standard = new CriteresRechercheClause("", Enums.ClauseCategorie.STANDARD, null, null, false);
        assertThat(standard.retient(ASSURANCE)).isTrue();
        assertThat(standard.retient(GENERALE)).isFalse();
    }

    @Test
    void thematique_filtreParSection_sansCasse_niEspaces() {
        var assurances = new CriteresRechercheClause("", null, null, "  ASSURANCES ", false);
        assertThat(assurances.retient(ASSURANCE)).isTrue();
        assertThat(assurances.retient(GENERALE)).isFalse();
    }

    @Test
    void normativesSeulement_exclutLesAutres() {
        var normatives = new CriteresRechercheClause("", null, null, null, true);
        assertThat(normatives.retient(ASSURANCE)).isTrue();
        assertThat(normatives.retient(GENERALE)).isFalse();
    }

    @Test
    void type_clauseSansConditionDeType_vautPourTous() {
        assertThat(new CriteresRechercheClause("", null, Enums.TypeContrat.GRE_A_GRE, null, false).retient(GENERALE)).isTrue();
    }

    @Test
    void type_clauseConditionnee_neVautQuePourSesTypes() {
        assertThat(new CriteresRechercheClause("", null, Enums.TypeContrat.APPEL_OFFRES_PUBLIC, null, false).retient(ASSURANCE)).isTrue();
        assertThat(new CriteresRechercheClause("", null, Enums.TypeContrat.GRE_A_GRE, null, false).retient(ASSURANCE)).isFalse();
    }

    @Test
    void type_ignoreLesConditionsDAutresAttributs() {
        Clause c = clause("V-1", "Valeur", "c", null, false, Enums.ClauseCategorie.STANDARD);
        ClauseCondition k = new ClauseCondition();
        k.setAttribut(Enums.AttributCondition.VALEUR_MIN);
        k.setValeur("100000");
        c.getConditions().add(k);
        assertThat(new CriteresRechercheClause("", null, Enums.TypeContrat.GRE_A_GRE, null, false).retient(c)).isTrue();
    }
}
