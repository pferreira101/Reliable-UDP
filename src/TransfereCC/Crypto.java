package TransfereCC;

import Common.Pair;

import javax.crypto.Cipher;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class Crypto {


    static KeyPair generateKeys() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        keyGen.initialize(1024, random);

        KeyPair pair = keyGen.generateKeyPair();

        return pair;
    }


    /******************* ASSINATURA DIGITAL *******************/

     static Pair<byte[], byte[]> generateSignature(String file, KeyPair pair) {

        try {
            PrivateKey priv = pair.getPrivate();
            PublicKey pub = pair.getPublic();

            Signature dsa = Signature.getInstance("SHA1withDSA", "SUN");

            dsa.initSign(priv);

            // Indicar o ficheiro para ser assinado
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bufin = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bufin.read(buffer)) >= 0) {
                dsa.update(buffer, 0, len);
            }
            bufin.close();

            // Gera assinatura
            byte[] real_sig = dsa.sign();
            byte[] key = pub.getEncoded();

            return new Pair<>(real_sig, key);
        }
        catch (Exception e) {
            System.err.println("Erro:" + e.toString());
        }

        return null;
    }


    static boolean verifySignature(String file, byte[] sig_to_verify, byte[] pubkey_bytes) {

        try {
            // Gera PublicKey a partir do byte[]
            PublicKey pub_key = KeyFactory.getInstance("DSA", "SUN").generatePublic(new X509EncodedKeySpec(pubkey_bytes));

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
            }
            
            bufin.close();

            try {
                return sig.verify(sig_to_verify);
            } catch (SignatureException e) {

                e.printStackTrace();
                return false;
            }
        }
        catch (Exception e){
            return false;
        }
    }

}
