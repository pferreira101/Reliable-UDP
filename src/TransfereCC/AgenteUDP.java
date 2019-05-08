package TransfereCC;


import javax.swing.plaf.nimbus.State;
import Common.Pair;
import javax.xml.crypto.Data;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.rmi.UnknownHostException;

import java.time.LocalTime;
import java.security.KeyPair;

import java.util.*;

import static TransfereCC.ConnectionControl.*;
import static TransfereCC.ErrorControl.*;


public class AgenteUDP {
    DatagramPacket desordenado;
    Map<AbstractMap.SimpleEntry, ConnectionHandler> connections;
    DatagramSocket serverSocket;
    Thread listener;

    KeyPair keys;

    AgenteUDP(DatagramSocket serverSocket) {

        connections = new HashMap<>();
        this.serverSocket = serverSocket;
        listener = new Thread(new SocketListener(this));
        listener.start();

        try{
            keys = Crypto.generateKeys();
        }
        catch (Exception e){
            System.out.println("Ocorreu um erro na geração da chave: ");
            e.printStackTrace();
        }

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
        ConnectionHandler sender = new SenderSide(ip, port, new String(first_packet.fileData), first_packet.seq_number,this);
        this.connections.put(new AbstractMap.SimpleEntry(ip,port),sender);

        Thread t_sender = new Thread(sender);
        t_sender.start();
    }

    /**************************************
     *     Methods for sending packets    *
     *************************************/
    void directSend(MySegment to_send, StateTable st) {
        DatagramPacket sendPacket;

        byte[] data = to_send.toByteArray();
        sendPacket = new DatagramPacket(data, data.length, st.IPAddress, st.port);

        try {
            serverSocket.send(sendPacket);
        } catch (IOException e) {
            System.out.println("Error sending packet");
        }
    }

    private void sendSegment(MySegment to_send, StateTable st){
        DatagramPacket sendPacket;

        if(!isACK(to_send)){
            setSegmentSeqNumber(st, to_send);
            st.unAckedSegments.add(to_send);
        }

        byte[] checksum = calculateChecksum(to_send.toByteArray());
        to_send.setChecksum(checksum);

        // criar gap de 3 pacotes
        //if(to_send.fileData != null && (to_send.seq_number > 2 && to_send.seq_number < 6)) return ;

        byte[] data = to_send.toByteArray();
        sendPacket = new DatagramPacket(data, data.length, st.IPAddress, st.port);

        // mandar pacotes fora de ordem
        if(to_send.fileData != null && to_send.seq_number==3) {this.desordenado = sendPacket; return ;}

        try {
            serverSocket.send(sendPacket);
            //mandar o pacote travado anteriormente
            if(to_send.fileData != null && to_send.seq_number==6) serverSocket.send(desordenado);
        } catch (IOException e) {
            System.out.println("Error sending packet");
        }
    }

    void sendDataSegment(StateTable st, byte[] seg_data){
        MySegment to_send = new MySegment();

        to_send.setFileData(seg_data);
        /* Debug */ System.out.printf("A enviar dados (SEQ : %d) - "+ LocalTime.now()+"\n",st.curr_seq_num);
        sendSegment(to_send, st);
    }

    void sendMissingFileFYN(StateTable st) {
        MySegment to_send = new MySegment();

        buildErrorFileFYN(to_send);
        /* Debug */ System.out.printf("A enviar missingfilefyn (SEQ : %d) - "+ LocalTime.now()+"\n",st.curr_seq_num);
        sendSegment(to_send, st);
    }

    void sendACK(StateTable st) {
        MySegment to_send = new MySegment();

        buildACK(to_send);
        setAckNumber(st,to_send);
        /* Debug */ System.out.printf("A enviar ack (ACK = %d)- "+ LocalTime.now()+"\n", to_send.ack_number);
        sendSegment(to_send, st);
    }

    void sendSpecificACK(StateTable st, int ack_num) {
        MySegment to_send = new MySegment();

        buildACK(to_send);
        to_send.ack_number = ack_num;
        /* Debug */ System.out.printf("A enviar ack especifico (ACK = %d)- "+ LocalTime.now()+"\n", to_send.ack_number);
        sendSegment(to_send, st);
    }


    void sendSYNACK(StateTable st) {
        MySegment to_send = new MySegment();
        buildSYNACK(to_send, st.assinatura, st.public_key);
        setAckNumber(st,to_send);
        /* Debug */ System.out.printf("A enviar synack (SEQ : %d) (ACK = %d)- "+ LocalTime.now()+"\n",st.curr_seq_num, to_send.ack_number);
        sendSegment(to_send, st);
    }


    void sendFYN(StateTable st) {
        MySegment to_send = new MySegment();

        buildFYN(to_send);
        /* Debug */ System.out.printf("A enviar fyn (SEQ : %d) - "+ LocalTime.now()+"\n",st.curr_seq_num);
        sendSegment(to_send, st);
    }


    void sendSYNWithFilename(StateTable st) {
        MySegment to_send = new MySegment();

        buildSYNWithFileName(to_send, st.file);
        /* Debug */ System.out.printf("A enviar synwithfilename (SEQ : %d) - "+ LocalTime.now()+"\n",st.curr_seq_num);
        sendSegment(to_send, st);
    }

    void requestRepeat(StateTable st, int ack_value) {
        MySegment to_send = new MySegment();

        buildACK(to_send);
        to_send.ack_number = ack_value;

        /* Debug */ System.out.printf("A enviar DUPack (ACK = %d)- "+ LocalTime.now()+"\n", to_send.ack_number);
        sendSegment(to_send, st);
        directSend(to_send, st);// para nao voltar a alterar valor do checksum
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
