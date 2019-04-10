package ClientSide;

import java.io.*;
import java.net.*;
import java.time.LocalTime;


import static Common.ConnectionControl.*;


public class Client {



    public static void main(String args[]) throws Exception {
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByAddress(new byte[] {
                (byte)127, (byte)0, (byte)0, (byte)1});


        byte[] sendData;
        byte[] receiveData = new byte[1024];



        //INICIO DE CONEXAO
        sendData = buildSYN();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        System.out.println("Enviei SYN inicial - "+ LocalTime.now());
        clientSocket.send(sendPacket);

        // Espera resposta SYN
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);

        // Envia ACK
        if(isACK(receivePacket.getData())){ // DEVIA ESTAR A ESPERA DE UM SYNACK
            sendData = buildACK();
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            System.out.println("Enviei ACK ao SYNACK - "+ LocalTime.now());
            clientSocket.send(sendPacket);
        }

        //TRANSFERENCIA DE FICHEIRO
        int count=0;
        FileOutputStream fos = new FileOutputStream("received.png");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        while(true) {
            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            if(isFYN(receivePacket.getData())){ System.out.println("Recebi FYN - "+ LocalTime.now()); break;}

            System.out.printf("Recebi o %d fragmento -" + LocalTime.now() +"\n" ,++count);
            byte[] data = receivePacket.getData();
            bos.write(data, 0,data.length);

            sendData = buildACK();
            System.out.println("Enviei ACK ao Segmento "+count  );
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            clientSocket.send(sendPacket);
        }
        bos.flush();

        //TERMINO DE CONEXAO
        sendData = buildACK();
        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        System.out.println("Enviei ACK ao FYN");
        clientSocket.send(sendPacket);




    }
}