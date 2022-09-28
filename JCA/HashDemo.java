import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/* Exemplo para gerar um valor de hash com o algorithmo SHA-256.
 * O valor de hash pode ser calculado chamando 1 vez o digest ou N vezes o update e 1 vez o digest.
 */

public class HashDemo {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] msg = {1,2,3,4,5,6};
        md.update(msg);
        md.update(new byte[]{7,8,9,10});
        byte[] h = md.digest();
        prettyPrint(h);
        MessageDigest mdagain = MessageDigest.getInstance("SHA-256");
        byte[] hcopy = mdagain.digest(new byte[] {1,2,3,4,5,6,7,8,9,10});
        prettyPrint(hcopy);
    }

    private static void prettyPrint(byte[] h) {
        for (byte b : h) {
            System.out.printf("%02x", b);
        }
        System.out.println();
    }
}