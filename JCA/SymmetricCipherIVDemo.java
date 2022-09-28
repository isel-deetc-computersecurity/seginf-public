import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

/* Código parcial para usar um Cipher para cifra simétrica com IV 
 * 
*/

public class SymmetricCipherIVDemo {

	public static void main(String args[]) throws Exception{

		KeyGenerator keyGen = KeyGenerator.getInstance("AES");

		// Opcional, se não passar um SecureRandom ao método init de keyGen
		SecureRandom secRandom = new SecureRandom();
		
		// Opcional
		keyGen.init(secRandom);

		SecretKey key = keyGen.generateKey();

		// Gera o objeto da cifra simetrica com um modo de operação que precisa de IV
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

		// Associa a chave key a cifra
		cipher.init(cipher.ENCRYPT_MODE, key);

		// Obtém o IV gerado aleatoriamente durante o init()
		byte[] iv = cipher.getIV();

		/* ...
		 *
		 * restantes operações de cifra */

		// Decifra com mesma chave e iv usado na cifra
		cipher.init(cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

		/* ...
		 *
		 * restantes operações de decifra */
	}

}
