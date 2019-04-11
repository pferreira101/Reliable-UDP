package ServerSide;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import ClientSide.ClientErrorControl;
import Common.AgenteUDP;
import Common.MySegment;

import static Common.AgenteUDP.dividePacket;
import static Common.ConnectionControl.*;


public class Server extends Thread{
    private InetAddress svAddress;
    private int port;

    public Server(InetAddress ip, int port_number){
        svAddress = ip;
        port = port_number;
    }

    public void run() {
        DatagramSocket serverSocket = null;
        byte[] receiveData = new byte[1024];
        byte[] sendData;

        try{
            serverSocket = new DatagramSocket(port, svAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while(true){
            try {

                DatagramPacket sendPacket;
                MySegment to_send;
                MySegment received;

                System.out.println("ESPERAR PRIMEIRO PACOTE");
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                received = MySegment.fromByteArray(receivePacket.getData());
                System.out.println("RECEBEU PRIMEIRO PACOTE");

                //####### meter na tabela de estado
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();
                //#######

                // INICIO DE CONEXAO
                if(isSYN(received)){
                    to_send = new MySegment(); //seria SYNACK
                    buildSYN(to_send);
                    AgenteUDP.sendPacket(serverSocket, IPAddress, port, to_send);
                }

                received = AgenteUDP.receivePacket(serverSocket);


                if(isACK(received)) {
                    System.out.println("Recebi um ACK - "+ LocalTime.now());
                    //TRANSFERENCIA DE FICHEIRO
                    String filename = new String(received.fileData);
                    List<byte[]> teste = new ArrayList<>();
                    try {
                        teste = dividePacket(filename, 1024);
                    }
                    catch (IOException e){
                        System.out.println("Ficheiro Inexistente"); // AVISAR CLIENTE QUE FICHEIRO NÃO EXISTE
                    }

                    for (byte[] b : teste) {
                        to_send = new MySegment();
                        to_send.setFileData(b);
                        to_send.setChecksum(ClientErrorControl.calculateChecksum(to_send.toByteArray()));
                        AgenteUDP.sendPacket(serverSocket, IPAddress, port, to_send);

                        received = AgenteUDP.receivePacket(serverSocket);
                        System.out.println("Recebi um ACK ao pacote enviado - "+ LocalTime.now());
                    }

                }
                //TERMINO DE CONEXAO
                to_send = new MySegment();
                buildFYN(to_send);
                AgenteUDP.sendPacket(serverSocket, IPAddress, port, to_send);


                received = AgenteUDP.receivePacket(serverSocket);

                if(isACK(received))
                    System.out.println("A terminar - "+ LocalTime.now());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
