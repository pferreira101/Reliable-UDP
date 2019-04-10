package ClientSide;

import Common.MySegment;

import java.io.*;
import java.net.*;
import java.time.LocalTime;


import static Common.ConnectionControl.*;


public class Client {



    public void connect(InetAddress IPAddress, int porta) throws Exception {
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData;
        byte[] receiveData = new byte[4096];
        MySegment to_send;
        MySegment received;
        DatagramPacket sendPacket;



        //INICIO DE CONEXAO
        to_send = new MySegment();
        buildSYN(to_send);
        sendData = to_send.toByteArray();
        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, porta);
        System.out.println("Enviei SYN inicial - "+ LocalTime.now());
        clientSocket.send(sendPacket);

        // Espera resposta SYN
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        received = MySegment.fromByteArray(receivePacket.getData());

        // Envia ACK
        if(isSYN(received)){ // DEVIA ESTAR A ESPERA DE UM SYNACK
            to_send = new MySegment();
            buildACK(to_send);
            sendData = to_send.toByteArray();
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, porta);
            System.out.println("Enviei ACK ao SYNACK - "+ LocalTime.now());
            clientSocket.send(sendPacket);
        }

        //TRANSFERENCIA DE FICHEIRO
        int count=0;
        FileOutputStream fos = new FileOutputStream("output.png");
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        while(true) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            received = MySegment.fromByteArray(receivePacket.getData());

            if(isFYN(received)){ System.out.println("Recebi FYN - "+ LocalTime.now()); break;}

            System.out.printf("Recebi o %d fragmento -" + LocalTime.now() +"\n" ,++count);
            byte[] data = receivePacket.getData();
            received = MySegment.fromByteArray(receivePacket.getData());
            bos.write(received.fileData, 0,received.fileData.length);
            System.out.println("TAMANHO RECEBIDO: " + data.length);

            to_send = new MySegment();
            buildACK(to_send);
            sendData = to_send.toByteArray();
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, porta);
            System.out.println("Enviei ACK ao Segmento "+count  );
            clientSocket.send(sendPacket);
        }
        bos.flush();

        //TERMINO DE CONEXAO
        to_send = new MySegment();
        buildACK(to_send);
        sendData = to_send.toByteArray();
        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, porta);
        System.out.println("Enviei ACK ao FYN");
        clientSocket.send(sendPacket);

    }
}