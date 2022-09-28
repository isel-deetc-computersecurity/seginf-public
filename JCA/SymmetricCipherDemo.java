import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/* Este programa apresenta uma demonstração de uso de cifra simétrica
 * com chave estática gerada a partir de um array de bytes.
 * Ao final, é demonstrado o funcionamento da decifra utilizando a 
 * mesma chave.
 * */

public class SymmetricCipherDemo {

	public static void main(String args[]) throws Exception{

		// Gera os bytes para o vetor de bytes correspondente a chave
		byte[] keyBytes = {0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef, 0x01, 0x23, 0x45, 0x67, (byte)0x89, (byte)0xab, (byte)0xcd, (byte)0xef};

		// Gera chave a partir do vetor de bytes (valor fixo, não aleatório)
		SecretKey key = new SecretKeySpec(keyBytes, "AES");
		System.out.println("key:" + key);

		// Gera o objeto da cifra simetrica
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");      

		// Associa a chave key a cifra
		cipher.init(cipher.ENCRYPT_MODE, key);      

		// Mensagem a ser cifrada
		String msg = new String("Essa mensagem e' secreta!");

		// Mostra bytes da mensagem original
		prettyPrint(msg.getBytes());

		// Cifra mensagem com chave key
		byte[] bytes = cipher.doFinal(msg.getBytes());

		// Mostra os bytes em hexadecimal
		prettyPrint(bytes);

		/* ...
		   Apenas para experiencia de decifra: */

		// Decifra com mesma chave
		cipher.init(cipher.DECRYPT_MODE, key);
		byte[] bytes2 = cipher.doFinal(bytes);

		// Mostra a mensagem original
		String msg2 = new String(bytes2);
		System.out.println(msg2);
	}

	// Imprime array de bytes em hexadecimal
	private static void prettyPrint(byte[] tag) {
		for (byte b: tag) {
			System.out.printf("%02x", b);
		}
		System.out.println();
	}
}
