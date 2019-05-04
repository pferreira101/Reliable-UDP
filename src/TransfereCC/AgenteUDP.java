package TransfereCC;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.rmi.UnknownHostException;
import java.util.*;

import static TransfereCC.ConnectionControl.*;
import static TransfereCC.ErrorControl.calculateChecksum;


public class AgenteUDP {

    Map<AbstractMap.SimpleEntry, ConnectionHandler> connections;
    DatagramSocket serverSocket;
    Thread listener;


    AgenteUDP(DatagramSocket serverSocket) {
        connections = new HashMap<>();
        this.serverSocket = serverSocket;
        listener = new Thread(new SocketListener(this));
        listener.start();
    }

    /**************************************
     * Methods for adding new connections *
     *************************************/

    void addReceiverRoleConnection(InetAddress ip, int port, String filename){
        ConnectionHandler receiver = new ReceiverSide(ip, port, filename, this);
        this.connections.put(new AbstractMap.SimpleEntry(ip,port),receiver);

        Thread t_receiver = new Thread(receiver);
        t_receiver.start();
    }

    void addSenderRoleConnection(InetAddress ip, int port, MySegment first_packet){
        ConnectionHandler sender = new SenderSide(ip, port, new String(first_packet.fileData), this);
        this.connections.put(new AbstractMap.SimpleEntry(ip,port),sender);

        Thread t_sender = new Thread(sender);
        t_sender.start();
    }

    /**************************************
     *     Methods for sending packets    *
     *************************************/
    private void sendSegment(MySegment to_send, InetAddress ip, int port){
        DatagramPacket sendPacket;

        byte[] checksum = calculateChecksum(to_send.toByteArray());
        to_send.setChecksum(checksum);

        byte[] data = to_send.toByteArray();
        sendPacket = new DatagramPacket(data, data.length, ip, port);

        try {
            System.out.println("a enviar pacote");
            serverSocket.send(sendPacket);
        } catch (IOException e) {
            System.out.println("Error sending packet");
        }
    }

    void sendDataSegment(StateTable st, byte[] seg_data){
        MySegment to_send = new MySegment();

        to_send.setFileData(seg_data);

        sendSegment(to_send, st.IPAddress, st.port);
    }

    void sendMissingFileFYN(StateTable st) {
        MySegment to_send = new MySegment();

        buildErrorFileFYN(to_send);

        sendSegment(to_send, st.IPAddress, st.port);
    }

    void sendACK(StateTable st) {
        MySegment to_send = new MySegment();

        buildACK(to_send);

        sendSegment(to_send, st.IPAddress, st.port);
    }

    void sendSYNACK(StateTable st) {
        MySegment to_send = new MySegment();

        buildSYNACK(to_send);

        sendSegment(to_send, st.IPAddress, st.port);
    }

    void sendFYN(StateTable st) {
        MySegment to_send = new MySegment();

        buildFYN(to_send);

        sendSegment(to_send, st.IPAddress, st.port);
    }


    void sendSYNWithFilename(StateTable st) {
        MySegment to_send = new MySegment();

        buildSYNWithFileName(to_send, st.file);

        sendSegment(to_send, st.IPAddress, st.port);
    }


    /**************************************
     *         Auxiliary functions        *
     *************************************/

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

}
