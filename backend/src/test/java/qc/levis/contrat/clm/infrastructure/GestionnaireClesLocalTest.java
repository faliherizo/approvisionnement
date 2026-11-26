package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import qc.levis.contrat.clm.application.ClmProprietes;
import qc.levis.contrat.clm.application.port.AleaCryptographique;
import qc.levis.contrat.clm.application.port.GestionnaireCles;
import qc.levis.contrat.clm.domain.CleEnveloppee;
import org.junit.jupiter.api.Test;

/** Service de clés local (ADR-037) : contrat du port, rotation de la clé maîtresse et refus en production sans secret. */
class GestionnaireClesLocalTest extends GestionnaireClesContrat {

    static final String K1 = Base64.getEncoder().encodeToString(new byte[32]);
    static final String K2 = Base64.getEncoder().encodeToString("abcdefghijklmnopqrstuvwxyz012345".getBytes());
    static final AleaCryptographique ALEA = n -> {
        byte[] o = new byte[n];
        new SecureRandom().nextBytes(o);
        return o;
    };

    private final GestionnaireCles sujet = local(K1, "k1", Map.of(), "DEV");

    @Override
    GestionnaireCles sujet() {
        return sujet;
    }

    @Override
    String cleMaitresseInconnue() {
        return "inconnue";
    }

    @Override
    String depuisUrl(String base64Url) {
        return Base64.getEncoder().encodeToString(Base64.getUrlDecoder().decode(base64Url));
    }

    static GestionnaireClesLocal local(String cle, String id, Map<String, String> anciennes, String environnement) {
        ClmProprietes clm = new ClmProprietes(environnement, null, null, null, null, null, null);
        return new GestionnaireClesLocal(new ChiffrementProprietes(cle, id, anciennes, null), clm, ALEA);
    }

    @Test
    void la_rotation_de_la_cle_maitresse_ouvre_encore_les_anciennes_enveloppes() {
        CleEnveloppee ancienne = sujet.envelopper(CLE);
        GestionnaireCles apres = local(K2, "k2", Map.of("k1", K1), "DEV");
        assertEquals("k2", apres.cleMaitresseActive());
        assertArrayEquals(CLE, apres.desenvelopper(ancienne), "l'ancienne clé maîtresse reste déclarée");
        assertEquals("k2", apres.envelopper(CLE).cleMaitresse());
        assertThrows(GestionnaireCles.CleIllisible.class, () -> local(K2, "k2", Map.of(), "DEV").desenvelopper(ancienne));
    }

    @Test
    void l_identifiant_authentifie_l_enveloppe() {
        CleEnveloppee e = sujet.envelopper(CLE);
        GestionnaireCles meme = local(K1, "autre-id", Map.of("k1", K1), "DEV");
        assertThrows(GestionnaireCles.CleIllisible.class, () -> meme.desenvelopper(new CleEnveloppee("autre-id", e.valeur())),
                "la même clé sous un autre identifiant est refusée");
    }

    @Test
    void sans_secret_la_cle_de_developpement_est_employee_mais_refusee_en_production() {
        GestionnaireCles dev = local("", null, Map.of(), "DEV");
        assertEquals(GestionnaireClesLocal.ID_DEVELOPPEMENT, dev.cleMaitresseActive());
        assertArrayEquals(CLE, dev.desenvelopper(dev.envelopper(CLE)));
        assertEquals(GestionnaireClesLocal.ID_DEVELOPPEMENT, local(null, null, null, "DEV").cleMaitresseActive());
        assertThrows(IllegalStateException.class, () -> local(null, null, Map.of(), "PROD"));
        assertEquals("locale-1", local(K1, " ", Map.of(), "PROD").cleMaitresseActive(), "identifiant par défaut");
    }

    @Test
    void une_cle_maitresse_doit_compter_32_octets() {
        assertThrows(IllegalArgumentException.class, () -> local(Base64.getEncoder().encodeToString(new byte[16]), "k", Map.of(), "DEV"));
    }

    @Test
    void les_proprietes_ont_des_valeurs_par_defaut_sans_secret() {
        ChiffrementProprietes p = new ChiffrementProprietes(null, null, null, null);
        assertEquals("locale-1", p.cleMaitresseId());
        assertTrue(p.anciennesCles().isEmpty());
        assertEquals("https://login.microsoftonline.com", p.azure().loginUrl());
        assertEquals("https://x", new ChiffrementProprietes.Azure(null, null, null, null, null, "https://x").loginUrl());
    }
}
