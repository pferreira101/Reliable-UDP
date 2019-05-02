import java.io.*;
import java.security.*;
import java.security.spec.*;

public class VerSign {

    public static boolean verify(String file, String sig_file, String pubkey_file) {

        try {
            FileInputStream keyfis = new FileInputStream(pubkey_file);
            byte[] enc_key = new byte[keyfis.available()];
            keyfis.read(enc_key);

            keyfis.close();

            X509EncodedKeySpec pub_key_spec = new X509EncodedKeySpec(enc_key);

            KeyFactory key_factory = KeyFactory.getInstance("DSA", "SUN");

            PublicKey pub_key = key_factory.generatePublic(pub_key_spec);

            FileInputStream sigfis = new FileInputStream(sig_file);
            byte[] sig_to_verify = new byte[sigfis.available()];
            sigfis.read(sig_to_verify);
            sigfis.close();

            // Initialize the Signature Object for Verification
            Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(pub_key);

            // Supply the Signature Object With the Data to be Verified
            FileInputStream datafis = new FileInputStream(file);
            BufferedInputStream bufin = new BufferedInputStream(datafis);

            byte[] buffer = new byte[1024];
            int len;
            while (bufin.available() != 0) {
                len = bufin.read(buffer);
                sig.update(buffer, 0, len);
            };

            bufin.close();

            return sig.verify(sig_to_verify);

        } catch (Exception e) {
            return false;
        }
    }
}
