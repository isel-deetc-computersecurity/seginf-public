import java.security.KeyPairGenerator;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.SignatureException;

public class AsymmetricKeysAndSignatureDemo {
    public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        KeyPairGenerator pairGen = KeyPairGenerator.getInstance("RSA");
        KeyPair pair = pairGen.generateKeyPair();
        PrivateKey ks = pair.getPrivate();
        PublicKey kv = pair.getPublic();
        byte[] msg = {'a','u','l','a','.'};
        byte[] sig = sign(msg, ks);
        System.out.print("Signature length: " + sig.length + " ");
        prettyPrint(sig);
        // corrupt signature
        //sig[0]='x';
        //msg[0] = 0;
        System.out.println(verify(msg, sig, kv));
    }

    public static byte[] sign(byte[] msg, PrivateKey ks) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature sig = Signature.getInstance("SHA512withRSA");
        sig.initSign(ks);
        sig.update(msg);
        byte[] result = sig.sign();
        return result;
    }

    public static boolean verify(byte[] msg, byte[] s, PublicKey kv) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA512withRSA");
        sig.initVerify(kv);
        sig.update(msg);
        return sig.verify(s);
    }

	private static void prettyPrint(byte[] tag) {
		for (byte b: tag) {
			System.out.printf("%02x", b);
		}
		System.out.println();
	}

}
