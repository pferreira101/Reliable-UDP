package ServerSide;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.List;
import Common.AgenteUDP;

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

                System.out.println("ESPERAR PRIMEIRO PACOTE");
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                System.out.println("RECEBEU PRIMEIRO PACOTE");
                InetAddress IPAddress = receivePacket.getAddress();
                int port = receivePacket.getPort();

                // INICIO DE CONEXAO
                if(isSYN(receivePacket.getData())){
                    sendData = buildACK(); //seria SYNACK
                    sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                    System.out.println("Enviei synack - "+ LocalTime.now());
                    serverSocket.send(sendPacket);
                }

                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);


                if(isACK(receivePacket.getData())) {
                    System.out.println("Recebi um ACK - "+ LocalTime.now());
                    //TRANSFERENCIA DE FICHEIRO
                    List<byte[]> teste = dividePacket("connect.txt", 1024);
                    System.out.println("TAMANHO = " + teste.size());

                    for (byte[] b : teste) {
                        sendPacket = new DatagramPacket(b, b.length, IPAddress, port);
                        serverSocket.send(sendPacket);

                        receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        System.out.println("Recebi um ACK ao pacote enviado - "+ LocalTime.now());
                    }

                }
                //TERMINO DE CONEXAO
                sendData = buildFYN();
                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                System.out.println("Enviei FYN - "+ LocalTime.now());
                serverSocket.send(sendPacket);

                receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                if(isACK(receivePacket.getData()))
                    System.out.println("A terminar - "+ LocalTime.now());
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
