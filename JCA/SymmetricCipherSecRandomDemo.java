import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import java.security.SecureRandom;

/* Este programa apresenta uma demonstração de uso de cifra simétrica
 * com chave gerada aleatoriamente a partir do SecureRandom.
 * Ao final, é demonstrado o funcionamento da decifra utilizando a 
 * mesma chave.
 * */

public class SymmetricCipherSecRandomDemo {

	public static void main(String args[]) throws Exception{

		KeyGenerator keyGen = KeyGenerator.getInstance("AES");

		// Opcional, se não passar um SecureRandom ao método init de keyGen
		SecureRandom secRandom = new SecureRandom();
		
		// Opcional
		keyGen.init(secRandom);

		SecretKey key = keyGen.generateKey();

		// Gera o objeto da cifra simetrica
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");      

		// Associa a chave key a cifra
		cipher.init(Cipher.ENCRYPT_MODE, key);      

		// Mensagem a ser cifrada
		String msg = new String("Mensagem secreta!");

		// Mostra bytes da mensagem a ser cifrada
		prettyPrint(msg.getBytes());

		// Cifra mensagem com chave key
		byte[] bytes = cipher.doFinal(msg.getBytes());

		// Mostra os bytes em hexadecimal
		prettyPrint(bytes);

		/* ...
		 * Apenas para experiencia: */

		// Decifra com mesma chave
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] bytes2 = cipher.doFinal(bytes);

		// Mostra a mensagem original
		System.out.println(new String(bytes2));
	}

	// Imprime array de bytes em hexadecimal
	private static void prettyPrint(byte[] tag) {
		for (byte b: tag) {
			System.out.printf("%02x", b);
		}
		System.out.println();
	}
}
