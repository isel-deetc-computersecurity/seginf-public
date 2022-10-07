import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class KeystoreDemo {
    public static void main(String[] args) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(
            new FileInputStream("file.pfx"),
            "changeit".toCharArray()
        );
        Enumeration<String> entries = ks.aliases();
        while(entries.hasMoreElements()) {
            String alias = entries.nextElement();
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            PublicKey publicKey = cert.getPublicKey();
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, "changeit".toCharArray());
            System.out.println("Alias: " + alias);
            System.out.println(privateKey);
            System.out.println(publicKey);
        }
    }
}