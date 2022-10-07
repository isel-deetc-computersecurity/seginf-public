import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAPublicKeySpec;
import java.security.PublicKey;

public class X509CertificateDemo {

    public static void main(String args[]) throws FileNotFoundException, CertificateException {

        // Assume que ficheiro cert.cer está na diretoria de execução.
        FileInputStream in = new FileInputStream("cert.cer");

        // Gera objeto para certificados X.509.
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Gera o certificado a partir do ficheiro.
        X509Certificate certificate = (X509Certificate) cf.generateCertificate(in);

        // Obtém a chave pública do certificado.
        PublicKey pk = certificate.getPublicKey();

        // Converte, via casting, o objeto pk para RSAPublicKey.
        RSAPublicKeySpec pkRSA = (RSAPublicKeySpec) pk;

        // Mostra informações da chave pública:
        System.out.println("Algoritmo da chave pública: " + pk.getAlgorithm());
        System.out.print("Chave pública: ");
        prettyPrint(pk.getEncoded());
        System.out.println("Expoente (BigInt): " + pkRSA.getPublicExponent());
        System.out.println("Modulus (BigInt): " + pkRSA.getModulus());

        // Alguns exemplos de acesso aos campos do certificado:
        System.out.println("Tipo: " + certificate.getType());
        System.out.println("Versão: " + certificate.getVersion());
        System.out.println("Algoritmo de assinatura: " + certificate.getSigAlgName());
        System.out.println("Período: " + certificate.getNotBefore() + " a " + certificate.getNotAfter());
        System.out.print("Assinatura: ");
        prettyPrint(certificate.getSignature());

        // Verifica a validade do período do certificado.
        certificate.checkValidity();
        System.out.println("Certificado válido (período de validade)");
    }

  	// Imprime array de bytes em hexadecimal
	private static void prettyPrint(byte[] tag) {
		for (byte b: tag) {
			System.out.printf("%02x", b);
		}
		System.out.println();
	}
}