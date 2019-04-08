import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class AgenteUDP {


    /*
        - Passar logo file em vez do path?
        - Retornar uma lista de Packets já montados?
     */
    public void dividePacket(String path, int max) throws IOException {
        File file = new File(path);

        byte[] content = Files.readAllBytes(file.toPath());

        int frag = content.length/max;
        System.out.println("Frags = " + frag);

        ArrayList<byte[]> fragmentos = new ArrayList<>();
        for(int i = 0; i<frag; i++){

            fragmentos.add(Arrays.copyOfRange(content,i*max, i*max+max-1));

            System.out.println("Fragmento " + i + " completo.");
        }

        fragmentos.add(Arrays.copyOfRange(content,frag*max-1, content.length-1));

    }
}
