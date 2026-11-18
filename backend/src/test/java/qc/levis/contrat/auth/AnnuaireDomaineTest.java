package qc.levis.contrat.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.auth.domain.AppartenanceAnnuaire;
import qc.levis.contrat.auth.domain.CorrespondanceAnnuaire;
import qc.levis.contrat.auth.domain.FaitAnnuaire;
import qc.levis.contrat.auth.domain.GroupeScim;
import qc.levis.contrat.auth.domain.JetonScim;
import qc.levis.contrat.auth.domain.MembreGroupeScim;
import qc.levis.contrat.auth.domain.ReglesAnnuaire;

/** Annuaire d'entreprise (addenda 3, Q43, Q44, Q51) : faits, correspondances, règles et objets SCIM. */
class AnnuaireDomaineTest {

    private static final Instant T0 = Instant.parse("2026-11-17T12:00:00Z");

    private static CorrespondanceAnnuaire groupe(String valeur, String role, String domaine) {
        return CorrespondanceAnnuaire.definir(3L, FaitAnnuaire.Source.GROUPE, "ignoré", valeur, role, domaine, "admin", T0);
    }

    @Test
    void un_fait_se_compare_sans_egard_a_la_casse_et_un_groupe_n_a_pas_d_attribut() {
        FaitAnnuaire g = new FaitAnnuaire(FaitAnnuaire.Source.GROUPE, "x", " Juristes ");
        assertNull(g.attribut());
        assertEquals("Juristes", g.valeur());
        assertTrue(g.correspond(FaitAnnuaire.Source.GROUPE, null, "JURISTES"));
        assertFalse(g.correspond(FaitAnnuaire.Source.ATTRIBUT, null, "Juristes"));
        FaitAnnuaire a = FaitAnnuaire.attribut(" department ", "Finance");
        assertTrue(a.correspond(FaitAnnuaire.Source.ATTRIBUT, "Department", "finance"));
        assertFalse(a.correspond(FaitAnnuaire.Source.ATTRIBUT, "jobTitle", "finance"));
        assertFalse(new FaitAnnuaire(FaitAnnuaire.Source.ATTRIBUT, null, "v").correspond(FaitAnnuaire.Source.ATTRIBUT, "a", "v"));
        assertEquals("ATTRIBUT|department|finance", a.cle());
        assertEquals("GROUPE||juristes", g.cle());
        assertThrows(IllegalArgumentException.class, () -> new FaitAnnuaire(null, null, "v"));
        assertThrows(IllegalArgumentException.class, () -> FaitAnnuaire.groupe(null));
        assertThrows(IllegalArgumentException.class, () -> FaitAnnuaire.groupe(" "));
    }

    @Test
    void une_correspondance_exige_une_valeur_un_attribut_valide_et_au_moins_un_effet() {
        CorrespondanceAnnuaire c = CorrespondanceAnnuaire.definir(3L, FaitAnnuaire.Source.ATTRIBUT, " department ", " Finance ", " ", " finance_ent ",
                "admin@iq.example", T0);
        assertEquals("department", c.getAttribut());
        assertEquals("Finance", c.getValeur());
        assertNull(c.getRole());
        assertEquals("FINANCE_ENT", c.getGroupe());
        assertEquals(3L, c.getTenantId());
        assertNull(c.getId());
        assertEquals("admin@iq.example", c.getModifiePar());
        assertEquals(T0, c.getModifieLe());
        assertEquals(FaitAnnuaire.Source.ATTRIBUT, c.getSource());
        assertTrue(c.satisfaitePar(FaitAnnuaire.attribut("department", "finance")));
        assertNull(groupe("Juristes", "AUDITEUR", null).getAttribut(), "un groupe n'a pas d'attribut");

        assertThrows(IllegalArgumentException.class, () -> CorrespondanceAnnuaire.definir(3L, null, null, "v", "LECTEUR", null, "a", T0));
        assertThrows(IllegalArgumentException.class, () -> groupe(null, "LECTEUR", null));
        assertThrows(IllegalArgumentException.class, () -> groupe("x".repeat(256), "LECTEUR", null));
        assertThrows(IllegalArgumentException.class, () -> CorrespondanceAnnuaire.definir(3L, FaitAnnuaire.Source.ATTRIBUT, null, "v", "LECTEUR", null, "a", T0));
        assertThrows(IllegalArgumentException.class, () -> CorrespondanceAnnuaire.definir(3L, FaitAnnuaire.Source.ATTRIBUT, "1dept", "v", "LECTEUR", null, "a", T0));
        assertThrows(IllegalArgumentException.class, () -> groupe("v", null, null));
        assertThrows(IllegalArgumentException.class, () -> groupe("v", null, "9X"));
    }

    @Test
    void les_regles_donnent_roles_et_domaines_avec_le_role_par_defaut_en_dernier_recours() {
        List<CorrespondanceAnnuaire> regles = List.of(groupe("Juristes", "JURIDIQUE", "JURIDIQUE_IQ"), groupe("Admins", "ADMIN", null),
                CorrespondanceAnnuaire.definir(3L, FaitAnnuaire.Source.ATTRIBUT, "department", "Finance", null, "FINANCE", "a", T0));
        ReglesAnnuaire.Resultat juriste = ReglesAnnuaire.appliquer(regles,
                List.of(FaitAnnuaire.groupe("juristes"), FaitAnnuaire.attribut("department", "Finance")), "LECTEUR");
        assertTrue(juriste.rolesGeres());
        assertEquals(Set.of("JURIDIQUE"), juriste.roles());
        assertEquals(Set.of("JURIDIQUE_IQ", "FINANCE"), juriste.groupes());
        assertEquals(Set.of("JURIDIQUE_IQ", "FINANCE"), juriste.groupesGeres());

        assertEquals(Set.of("LECTEUR"), ReglesAnnuaire.appliquer(regles, List.of(), "LECTEUR").roles(), "moindre privilège");
        assertEquals(Set.of(), ReglesAnnuaire.appliquer(regles, List.of(), null).roles());

        ReglesAnnuaire.Resultat sansRole = ReglesAnnuaire.appliquer(List.of(regles.get(2)), List.of(FaitAnnuaire.groupe("Admins")), "LECTEUR");
        assertFalse(sansRole.rolesGeres(), "aucune correspondance de rôle : les rôles en place sont conservés");
        assertEquals(Set.of(), sansRole.roles());
    }

    @Test
    void les_objets_scim_et_les_faits_conserves() {
        GroupeScim g = new GroupeScim("id-1", 3L, " Juristes ", " ");
        assertEquals("Juristes", g.getNom());
        assertNull(g.getIdExterne());
        assertEquals(List.of(FaitAnnuaire.groupe("Juristes")), g.faits());
        g.renommer("Juristes IQ", "aad-123");
        assertEquals(List.of(FaitAnnuaire.groupe("Juristes IQ"), FaitAnnuaire.groupe("aad-123")), g.faits());
        assertEquals("id-1", g.getId());
        assertEquals(3L, g.getTenantId());
        assertEquals("aad-123", g.getIdExterne());
        g.renommer("Sans externe", null);
        assertNull(g.getIdExterne());
        assertThrows(IllegalArgumentException.class, () -> g.renommer(null, null));
        assertThrows(IllegalArgumentException.class, () -> g.renommer(" ", null));
        assertThrows(IllegalArgumentException.class, () -> g.renommer("x".repeat(256), null));

        MembreGroupeScim m = new MembreGroupeScim(3L, "id-1", 9L);
        assertEquals("id-1", m.getGroupeId());
        assertEquals(9L, m.getUtilisateurId());

        JetonScim j = new JetonScim(3L, "empreinte", "admin", T0);
        j.remplacer("autre", "admin2", T0.plusSeconds(1));
        assertEquals(3L, j.getTenantId());
        assertEquals("autre", j.getEmpreinte());
        assertEquals("admin2", j.getCreePar());
        assertEquals(T0.plusSeconds(1), j.getCreeLe());

        AppartenanceAnnuaire a = new AppartenanceAnnuaire(3L, "a@iq.example", AppartenanceAnnuaire.Origine.SSO,
                FaitAnnuaire.attribut("department", "x".repeat(300)));
        assertEquals(255, a.fait().valeur().length(), "valeur bornée à la colonne");
        assertEquals("a@iq.example", a.getCourriel());
        assertEquals(AppartenanceAnnuaire.Origine.SSO, a.getOrigine());
        assertNull(a.getId());
        assertEquals("v", new AppartenanceAnnuaire(3L, "a", AppartenanceAnnuaire.Origine.SCIM, FaitAnnuaire.groupe("v")).fait().valeur());
    }
}
