package TransfereCC;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.*;


public class AgenteUDP {

    private Map<AbstractMap.SimpleEntry, ConnectionHandler> connections;

    public void addNewConnection(AbstractMap.SimpleEntry<InetAddress,Integer> key, ConnectionHandler value){
        this.connections.put(key,value);
    }

    static  void sendPacket(DatagramSocket datagramSocket, InetAddress IPAddress, int porta, MySegment to_send){
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

    static MySegment receivePacket(DatagramSocket datagramSocket){
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

    static List<byte[]> dividePacket(String path, int max) throws IOException {
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

    static void sendMissingFileFYN(DatagramSocket serverSocket, InetAddress ipAddress, int port) {
        MySegment to_send = new MySegment();
        ConnectionControl.buildErrorFileFYN(to_send);
        sendPacket(serverSocket, ipAddress, port, to_send);
    }

    static void sendSYN(DatagramSocket serverSocket, InetAddress ipAddress, int port) {
        MySegment to_send = new MySegment();
        ConnectionControl.buildSYN(to_send);
        sendPacket(serverSocket, ipAddress, port, to_send);
    }

    static void sendFYN(DatagramSocket serverSocket, InetAddress ipAddress, int port) {
        MySegment to_send = new MySegment();
        ConnectionControl.buildFYN(to_send);
        sendPacket(serverSocket, ipAddress, port, to_send);
    }
}
