import java.io.*;
import java.security.*;

public class GenSign {

    public static void generate(String file) {

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);

            KeyPair pair = keyGen.generateKeyPair();
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
            };
            bufin.close();

            // Gera assinatura
            byte[] real_sig = dsa.sign();

            // Guarda a assinatura no ficheiro
            FileOutputStream sigfos = new FileOutputStream("sig");
            sigfos.write(real_sig);
            sigfos.close();

            // Guarda a chave publica no ficheiro
            byte[] key = pub.getEncoded();
            FileOutputStream keyfos = new FileOutputStream("pubkey");
            keyfos.write(key);
            keyfos.close();

        } catch (Exception e) {
            System.err.println("Erro:" + e.toString());
        }
    }
}
