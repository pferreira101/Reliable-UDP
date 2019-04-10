package ServerSide;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.List;
import Common.AgenteUDP;

import static Common.AgenteUDP.dividePacket;
import static Common.ConnectionControl.*;

public class Server {




    public static void main(String args[]) throws Exception
    {
        InetAddress svAddress = InetAddress.getByAddress(new byte[] {
                (byte)127, (byte)0, (byte)0, (byte)1});
        DatagramSocket serverSocket = new DatagramSocket(9876,svAddress);
        byte[] receiveData = new byte[1024];
        byte[] sendData;

        DatagramPacket sendPacket;

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);

        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();

        //INICIO DE CONEXAO
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
            List<byte[]> teste = dividePacket("testepng.png", 1024);
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
}
