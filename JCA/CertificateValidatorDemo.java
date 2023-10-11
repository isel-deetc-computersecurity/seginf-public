import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathParameters;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class CertificateValidatorDemo {

    private static final String[] CERT_CHAIN = {
        "certificates-keys/end-entities/Alice_1.cer",
        "certificates-keys/intermediates/CA1-int.cer",
        "certificates-keys/trust-anchors/CA1.cer"
    };

    static private X509Certificate getCertificateFromFile(String fileName) throws IOException, CertificateException {

        // Instancia uma factory de certificados X.509
        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        // Extrai o certificado do ficheiro
        FileInputStream fis;
        fis = new FileInputStream(fileName);
        X509Certificate cert = (X509Certificate) factory.generateCertificate(fis);
        fis.close();

        return (cert);
    }

    public static void main(String args[]) throws CertificateException, IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        // Gera lista de certificados da cadeia, na ordem da folha para a raiz
        ArrayList<X509Certificate> certList = new ArrayList<>();
            
        // Adiciona os certificados
        certList.add(0, getCertificateFromFile(CERT_CHAIN[0]));
        certList.add(1, getCertificateFromFile(CERT_CHAIN[1]));
        certList.add(2, getCertificateFromFile(CERT_CHAIN[2]));

        // Gera a cadeia de certificados a partir da coleção (lista)
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        CertPath certPath = factory.generateCertPath(certList);

        // Também é necessário um conjunto de trust anchors
        Set<TrustAnchor> trustAnchorSet = new HashSet<>();

        // Adiciona apenas o certificado raiz (trust anchor)
        trustAnchorSet.add(new TrustAnchor(certList.get(2), null));

        // Cria os parâmetros do PKIX a partir dos trust anchors
        CertPathParameters params = (CertPathParameters) new PKIXParameters(trustAnchorSet);

        // Neste exemplo, desabilitar a revogação pois não se tem esta lista
        ((PKIXParameters) params).setRevocationEnabled(false);

        // Obtém um validador de cadeia de certificados
        CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");

        try {

            // Realiza a validação em si
            certPathValidator.validate(certPath, params);
            System.out.println("Certificate is valid");

            // Mosta a chave pública
            Key publicKey = certList.get(0).getPublicKey();
            System.out.println("Here is the public key: " + publicKey);

        } catch (CertPathValidatorException | InvalidAlgorithmParameterException e) {
            System.out.println("Certificate is invalid");
            System.out.println(e);
        }   
    }

}