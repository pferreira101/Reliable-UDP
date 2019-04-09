package ServerSide;

import java.io.*;
import java.net.*;
import java.util.List;
import Common.AgenteUDP;

import static Common.AgenteUDP.dividePacket;
import static Common.ConnectionControl.*;

public class Server {




    public static void main(String args[]) throws Exception
    {
        System.out.print("a correr");
        InetAddress svAddress = InetAddress.getByAddress(new byte[] {
                (byte)127, (byte)0, (byte)0, (byte)1});
        DatagramSocket serverSocket = new DatagramSocket(9876,svAddress);
        byte[] receiveData = new byte[1024];
        byte[] sendData;

        DatagramPacket sendPacket;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        analisaSegmento(receivePacket.getData());

        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();

        if(isSYN(receivePacket.getData())){
            sendData = buildSYN();
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
            System.out.println("enviei syn de confirmacao");
        }

        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        analisaSegmento(receivePacket.getData());

        if(isACK(receivePacket.getData())){
            String confirm = new String("ola");
            sendData = confirm.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
            System.out.println("enviei mensagem inicial");
        }

        while(true){
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            //serverSocket.receive(receivePacket);
            //analisaSegmento(receivePacket.getData());

            if(isFYN(receivePacket.getData())) break;

            List<byte[]> teste = dividePacket("teste.txt", 10);
            System.out.println("TAMANHO = " + teste.size());
            for(byte[] b : teste){
                sendPacket = new DatagramPacket(b, b.length, IPAddress, port);
                serverSocket.send(sendPacket);
            }
            /*
            String sentence = new String(receivePacket.getData());
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            */
            sendData = buildFYN();
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);

        }

        sendData = buildACK();
        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        serverSocket.send(sendPacket);

        System.out.println("A terminar");

    }
}
