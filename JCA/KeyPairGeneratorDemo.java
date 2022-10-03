import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairGeneratorDemo {
	public static void main(String args[]) throws Exception{
		// Cria objeto KeyPair
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");

		// Inicia o tamanho da chave
		keyPairGen.initialize(2048);

		// Gera o par de chaves
		KeyPair pair = keyPairGen.generateKeyPair();

		// Obtém a chave privada
		PrivateKey privKey = pair.getPrivate();   

		// Obtém a chave pública
		PublicKey publicKey = pair.getPublic();

		System.out.println("A chave pública em hexadecimal:");
		prettyPrint(publicKey.getEncoded());
	}

	// Imprime array de bytes em hexadecimal
	private static void prettyPrint(byte[] tag) {
		for (byte b: tag) {
			System.out.printf("%02x", b);
		}
		System.out.println();
	}
}
