package ServerSide;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.List;
import Common.AgenteUDP;
import Common.MySegment;

import static Common.AgenteUDP.dividePacket;
import static Common.ConnectionControl.*;


public class Server extends Thread{
    private InetAddress svAddress;

    public Server(InetAddress ip){
        svAddress = ip;
    }

    public void run() {
        DatagramSocket serverSocket = null;
        try{

            serverSocket = new DatagramSocket(9876,svAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        while(true){
            try {
                byte[] receiveData = new byte[1024];
                byte[] sendData;

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
                    sendData = to_send.toByteArray();
                    sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    System.out.println("Enviei synack - "+ LocalTime.now());
                    serverSocket.send(sendPacket);
                }

                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                received = MySegment.fromByteArray(receivePacket.getData());


                if(isACK(received)) {
                    System.out.println("Recebi um ACK - "+ LocalTime.now());
                    //TRANSFERENCIA DE FICHEIRO
                    List<byte[]> teste = dividePacket("connect.txt", 1024);
                    System.out.println("TAMANHO = " + teste.size());

                    for (byte[] b : teste) {
                        to_send = new MySegment();
                        to_send.setFileData(b);
                        sendData = to_send.toByteArray();
                        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        serverSocket.send(sendPacket);

                        receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        System.out.println("Recebi um ACK ao pacote enviado - "+ LocalTime.now());
                    }

                }
                //TERMINO DE CONEXAO
                to_send = new MySegment();
                buildFYN(to_send);
                sendData = to_send.toByteArray();
                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                System.out.println("Enviei FYN - "+ LocalTime.now());
                serverSocket.send(sendPacket);


                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                received = MySegment.fromByteArray(receivePacket.getData());

                if(isACK(received))
                    System.out.println("A terminar - "+ LocalTime.now());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
