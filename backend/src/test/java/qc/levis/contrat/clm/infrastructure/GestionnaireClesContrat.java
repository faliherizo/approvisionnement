package qc.levis.contrat.clm.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import qc.levis.contrat.clm.application.port.GestionnaireCles;
import qc.levis.contrat.clm.domain.CleEnveloppee;

/**
 * Contrat du port {@link GestionnaireCles} (ADR-037), exécuté contre chaque adaptateur : une clé de données enveloppée
 * s'ouvre à l'identique, deux enveloppes d'une même clé diffèrent, et toute enveloppe altérée ou d'une clé maîtresse
 * inconnue lève {@link GestionnaireCles.CleIllisible} — jamais une autre exception, jamais une clé fausse.
 */
abstract class GestionnaireClesContrat {

    static final byte[] CLE = "0123456789abcdef0123456789abcdef".getBytes();

    abstract GestionnaireCles sujet();

    /** Identifiant de clé maîtresse que l'adaptateur ne connaît pas. */
    abstract String cleMaitresseInconnue();

    /** Forme Base64 propre à l'adaptateur (standard ou URL), pour altérer une enveloppe sans la rendre illisible au décodage. */
    abstract String depuisUrl(String base64Url);

    static String versUrl(String base64) {
        return base64.replace('+', '-').replace('/', '_').replace("=", "");
    }

    @Test
    void une_cle_enveloppee_s_ouvre_a_l_identique_par_la_cle_maitresse_active() {
        CleEnveloppee e = sujet().envelopper(CLE);
        assertEquals(sujet().cleMaitresseActive(), e.cleMaitresse());
        assertArrayEquals(CLE, sujet().desenvelopper(e));
        assertFalse(e.valeur().contains(Base64.getEncoder().encodeToString(CLE)), "jamais la clé en clair");
    }

    @Test
    void deux_enveloppes_d_une_meme_cle_different() {
        assertNotEquals(sujet().envelopper(CLE).valeur(), sujet().envelopper(CLE).valeur());
    }

    @Test
    void une_enveloppe_alteree_est_illisible() {
        CleEnveloppee e = sujet().envelopper(CLE);
        byte[] octets = Base64.getUrlDecoder().decode(versUrl(e.valeur()));
        octets[octets.length - 1] ^= 1;
        CleEnveloppee alteree = new CleEnveloppee(e.cleMaitresse(), depuisUrl(Base64.getUrlEncoder().withoutPadding().encodeToString(octets)));
        assertThrows(GestionnaireCles.CleIllisible.class, () -> sujet().desenvelopper(alteree));
    }

    @Test
    void une_cle_maitresse_inconnue_est_illisible() {
        CleEnveloppee e = sujet().envelopper(CLE);
        assertThrows(GestionnaireCles.CleIllisible.class, () -> sujet().desenvelopper(new CleEnveloppee(cleMaitresseInconnue(), e.valeur())));
    }

    @Test
    void une_valeur_qui_n_est_pas_une_enveloppe_est_illisible() {
        assertThrows(GestionnaireCles.CleIllisible.class, () -> sujet().desenvelopper(new CleEnveloppee(sujet().cleMaitresseActive(), "@@@")));
        assertThrows(GestionnaireCles.CleIllisible.class, () -> sujet().desenvelopper(new CleEnveloppee(sujet().cleMaitresseActive(), "AAAA")));
    }
}
