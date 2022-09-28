import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/* Exemplo de autenticação com criptografia simétrica.
 * Gera uma marca MAC usando o algoritmo HmacSHA256.
 */

public class MacDemo {
    public static final String HMAC = "HmacSHA256";

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] someImportantMessage = "This is a very important message".getBytes();

        KeyGenerator secretKeyGenerator = KeyGenerator.getInstance(HMAC);
        SecretKey key = secretKeyGenerator.generateKey();
        Mac mac = Mac.getInstance(HMAC);
        mac.init(key);
        byte[] tag = mac.doFinal(someImportantMessage);
        prettyPrint(tag);
    }

    private static void prettyPrint(byte[] tag) {
        for (byte b: tag) {
            System.out.printf("%02x", b);
        }
    }
}
