package Common;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Common.ConnectionControl.*;

public class AgenteUDP {


    public static  void sendPacket(DatagramSocket datagramSocket, InetAddress IPAddress, int porta, MySegment to_send){
        DatagramPacket sendPacket;
        try {
            byte[] data = to_send.toByteArray();
            sendPacket = new DatagramPacket(data, data.length, IPAddress, porta);
            datagramSocket.send(sendPacket);
        }
        catch(Exception e){
            System.out.println("Error sending packet");
        }
    }

    public static MySegment receivePacket(DatagramSocket datagramSocket){
        MySegment to_return=null;
        byte[] receiveData = new byte[4096];
        byte[] sendData;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try{
            datagramSocket.receive(receivePacket);
            to_return = MySegment.fromByteArray(receivePacket.getData());
        }
        catch(IOException e){
            System.out.println("Error ocurred during receive method");
        }
        catch(ClassNotFoundException e){
            System.out.println("Can't convert to MySegment");
        }
        finally {
            return to_return;
        }

    }

    public static List<byte[]> dividePacket(String path, int max) throws IOException {
        File file = new File(path);

        byte[] content = Files.readAllBytes(file.toPath());

        int to_consume= content.length;
        int frag = content.length/max ;

        ArrayList<byte[]> fragmentos = new ArrayList<>();
        for(int i = 0; i<frag; i++) {
            fragmentos.add(Arrays.copyOfRange(content, i * max, i * max + max ));
            to_consume -= max;
        }
        //add last frag
        if(to_consume > 0) fragmentos.add(Arrays.copyOfRange(content,frag*max, content.length));

        return fragmentos;
    }

    public static void sendMissingFileFYN(DatagramSocket serverSocket, InetAddress ipAddress, int port) {
        MySegment to_send = new MySegment();
        buildErrorFileFYN(to_send);
        sendPacket(serverSocket, ipAddress, port, to_send);
    }

    public static void sendSYN(DatagramSocket serverSocket, InetAddress ipAddress, int port) {
        MySegment to_send = new MySegment();
        buildSYN(to_send);
        sendPacket(serverSocket, ipAddress, port, to_send);
    }

    public static void sendFYN(DatagramSocket serverSocket, InetAddress ipAddress, int port) {
        MySegment to_send = new MySegment();
        buildFYN(to_send);
        sendPacket(serverSocket, ipAddress, port, to_send);
    }
}
