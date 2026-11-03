package qc.levis.contrat.clm.domain;

import java.util.Locale;

/**
 * Formats acceptés pour les documents d'une trousse (RST-16, NF-07) : PDF et Word (.docx). Le type est déduit
 * des octets et non de l'extension seule, pour qu'un fichier renommé ne passe pas la frontière.
 */
public final class FormatDocument {

    public enum Type {
        PDF("application/pdf"),
        DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        private final String mime;

        Type(String mime) {
            this.mime = mime;
        }

        public String mime() {
            return mime;
        }
    }

    private FormatDocument() {
    }

    public static Type verifier(String nomFichier, byte[] contenu, long tailleMax) {
        if (contenu == null || contenu.length == 0) {
            throw new IllegalArgumentException("Fichier vide.");
        }
        if (contenu.length > tailleMax) {
            throw new IllegalArgumentException(String.format(java.util.Locale.CANADA_FRENCH,
                    "Taille dépassée : %.1f Mo pour une limite configurée de %.1f Mo.", contenu.length / 1_048_576.0, tailleMax / 1_048_576.0));
        }
        String nom = nomFichier == null ? "" : nomFichier.toLowerCase(Locale.ROOT);
        if (commencePar(contenu, new byte[]{'%', 'P', 'D', 'F', '-'}) && nom.endsWith(".pdf")) {
            return Type.PDF;
        }
        if (commencePar(contenu, new byte[]{'P', 'K', 3, 4}) && nom.endsWith(".docx")) {
            return Type.DOCX;
        }
        throw new IllegalArgumentException("Format non pris en charge : seuls PDF et Word (.docx) sont acceptés.");
    }

    /** Nom de fichier sûr pour le stockage et l'en-tête Content-Disposition. */
    public static String nomSur(String nomFichier) {
        String nom = nomFichier == null ? "document" : nomFichier.replace('\\', '/');
        nom = nom.substring(nom.lastIndexOf('/') + 1).replaceAll("[^\\p{L}\\p{N}._ -]", "_").trim();
        if (nom.isEmpty() || nom.startsWith(".")) {
            nom = "document" + nom;
        }
        return nom.length() > 200 ? nom.substring(nom.length() - 200) : nom;
    }

    private static boolean commencePar(byte[] contenu, byte[] prefixe) {
        if (contenu.length < prefixe.length) {
            return false;
        }
        for (int i = 0; i < prefixe.length; i++) {
            if (contenu[i] != prefixe[i]) {
                return false;
            }
        }
        return true;
    }
}
