package qc.levis.contrat.clm.domain;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Forme stockée d'un document chiffré au repos (Annexe 10, SEC-28 ; ADR-037) : un en-tête qui désigne l'organisation et la
 * version de sa clé de données, le vecteur d'initialisation, puis le chiffré AES-GCM. L'en-tête sert aussi de données
 * authentifiées : un document présenté sous une autre organisation ou une autre version est refusé au déchiffrement.
 * Un contenu sans la marque est un ancien document en clair, lisible tel quel jusqu'à son réenveloppement.
 */
public final class EnveloppeDocument {

    /** Marque de tête : commence par un octet nul, qu'aucun format admis (PDF, Word, image, texte) ne porte en tête. */
    static final byte[] MARQUE = {0x00, 'V', 'C', 'E', 0x01};
    public static final int LONGUEUR_IV = 12;
    static final int LONGUEUR_ENTETE = MARQUE.length + Long.BYTES + Integer.BYTES;

    private final long tenantId;
    private final int version;
    private final byte[] iv;
    private final byte[] chiffre;

    public EnveloppeDocument(long tenantId, int version, byte[] iv, byte[] chiffre) {
        if (version < 1 || iv.length != LONGUEUR_IV || chiffre.length == 0) {
            throw new IllegalArgumentException("Enveloppe de document invalide.");
        }
        this.tenantId = tenantId;
        this.version = version;
        this.iv = iv.clone();
        this.chiffre = chiffre.clone();
    }

    /** Vrai si le contenu stocké porte la marque d'une enveloppe ; faux pour un ancien contenu en clair ou effacé. */
    public static boolean estEnveloppe(byte[] stockage) {
        return stockage != null && stockage.length > LONGUEUR_ENTETE + LONGUEUR_IV
                && Arrays.equals(stockage, 0, MARQUE.length, MARQUE, 0, MARQUE.length);
    }

    public static EnveloppeDocument lire(byte[] stockage) {
        if (!estEnveloppe(stockage)) {
            throw new IllegalArgumentException("Contenu non chiffré.");
        }
        ByteBuffer b = ByteBuffer.wrap(stockage, MARQUE.length, stockage.length - MARQUE.length);
        long tenant = b.getLong();
        int version = b.getInt();
        byte[] iv = new byte[LONGUEUR_IV];
        b.get(iv);
        byte[] chiffre = new byte[b.remaining()];
        b.get(chiffre);
        return new EnveloppeDocument(tenant, version, iv, chiffre);
    }

    /** Données authentifiées du chiffrement : marque, organisation et version de clé. */
    public static byte[] entete(long tenantId, int version) {
        return ByteBuffer.allocate(LONGUEUR_ENTETE).put(MARQUE).putLong(tenantId).putInt(version).array();
    }

    public byte[] entete() {
        return entete(tenantId, version);
    }

    public byte[] octets() {
        return ByteBuffer.allocate(LONGUEUR_ENTETE + iv.length + chiffre.length).put(entete()).put(iv).put(chiffre).array();
    }

    public long tenantId() {
        return tenantId;
    }

    public int version() {
        return version;
    }

    public byte[] iv() {
        return iv.clone();
    }

    public byte[] chiffre() {
        return chiffre.clone();
    }
}
