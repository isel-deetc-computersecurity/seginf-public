import java.util.Base64;

public class Base64URLDemo
{

    public static void main( String[] args ) {

        // Mensagem a ser codificada em Base64 URL
        String mensagem = "Um exemplo de mensagem";

        // Faz o encode
        String msgEncodeBase64URL = Base64.getUrlEncoder().withoutPadding().encodeToString(mensagem.getBytes());

        System.out.println("Mensagem em Base64 URL: " + msgEncodeBase64URL);

        // Faz o decode
        byte[] msgDecodeBase64URL = Base64.getUrlDecoder().decode(msgEncodeBase64URL);

        System.out.println("Mensagem descodificada: " + new String(msgDecodeBase64URL));
    }
}
