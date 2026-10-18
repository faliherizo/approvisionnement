package qc.levis.contrat.commun.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Chiffrement symétrique des secrets applicatifs stockés en base (ex. le secret client Microsoft
 * 365 d'un tenant). AES-256 (Spring Security Crypto), clé et sel fournis par configuration/variables
 * d'environnement — jamais en dur ni versionnés. Un secret n'est ainsi jamais persisté en clair.
 */
@Component
public class SecretCipher {

    private final TextEncryptor encryptor;

    public SecretCipher(@Value("${app.crypto.cle:changez-moi-cle-de-chiffrement-des-secrets}") String cle,
                        @Value("${app.crypto.sel:5c0ff33a1b2c3d4e}") String selHex) {
        this.encryptor = Encryptors.text(cle, selHex);
    }

    /** Chiffre une valeur en clair (null/vide → null : rien à stocker). */
    public String chiffrer(String clair) {
        return (clair == null || clair.isBlank()) ? null : encryptor.encrypt(clair);
    }

    /** Déchiffre une valeur chiffrée (null/vide → null). */
    public String dechiffrer(String chiffre) {
        return (chiffre == null || chiffre.isBlank()) ? null : encryptor.decrypt(chiffre);
    }
}
