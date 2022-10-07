import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Base64Stream {
    public static void main(String[] args) throws IOException {
        FileOutputStream baseOut = new FileOutputStream("test_file.cif");
        Base64OutputStream out = new Base64OutputStream(baseOut);
        out.write(new byte[]{1,2,3});
        out.write(new byte[]{45,9,2,23,34,14,4,34,34,11});
        out.close();

        FileInputStream baseIn = new FileInputStream("test_file.cif");
        Base64InputStream in = new Base64InputStream(baseIn);
        int value;
        while ((value = in.read()) != -1) {
            System.out.println(value);
        }
        in.close();
    }
}
