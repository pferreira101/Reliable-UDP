package ClientSide;

import java.io.*;
import java.net.*;


import static Common.ConnectionControl.*;


public class Client {



    public static void main(String args[]) throws Exception {
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        FileOutputStream fos = new FileOutputStream("abc.txt");
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByAddress(new byte[] {
                (byte)127, (byte)0, (byte)0, (byte)1});


        byte[] sendData;
        byte[] receiveData = new byte[1024];


        // Envia syn
        sendData = buildSYN();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        clientSocket.send(sendPacket);
        System.out.println("Enviei SYN inicial");

        // Espera resposta SYN
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        analisaSegmento(receivePacket.getData());

        // Envia ACK
        if(isSYN(receivePacket.getData())){
            sendData = buildACK();
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            clientSocket.send(sendPacket);
            System.out.println("Enviei ACK ao SYN");


        }

        // Espera por um sinal para começar a falar
        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String modifiedSentence = new String(receivePacket.getData());
        System.out.println("FROM SERVER:" + modifiedSentence);

        while(true) {

            //String sentence = inFromUser.readLine();

            //if(sentence.equals("end")) break;

            //sendData = sentence.getBytes();
            //sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            //clientSocket.send(sendPacket);

            //receivePacket = new DatagramPacket(receiveData, receiveData.length);
            //lientSocket.receive(receivePacket);
            //modifiedSentence = new String(receivePacket.getData());
            //System.out.println("FROM SERVER:" + modifiedSentence);
            /************************************************************/

            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            if(isFYN(receivePacket.getData())) break;

            clientSocket.receive(receivePacket);
            byte[] data = receivePacket.getData();
            fos.write(data);


        }

        fos.flush();
        fos.close();
        sendData = buildFYN();
        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        clientSocket.send(sendPacket);

        receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        analisaSegmento(receivePacket.getData());
        if(isACK(receivePacket.getData()))
            System.out.println("a terminar");

    }
}