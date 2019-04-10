package ClientSide;

import Common.AgenteUDP;
import Common.MySegment;

import java.io.*;
import java.net.*;
import java.time.LocalTime;


import static Common.ConnectionControl.*;


public class Client {



    public void connect(InetAddress ip, String filename, int porta) throws Exception {
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData;
        byte[] receiveData = new byte[4096];
        MySegment to_send;
        MySegment received;
        DatagramPacket sendPacket;



        //INICIO DE CONEXAO
        to_send = new MySegment();
        buildSYN(to_send);
        AgenteUDP.sendPacket(clientSocket, ip, porta, to_send);

        // Espera resposta SYN
        received = AgenteUDP.receivePacket(clientSocket);

        // Envia ACK
        if(isSYN(received)){ // DEVIA ESTAR A ESPERA DE UM SYNACK
            to_send = new MySegment();
            buildACKWithFilename(to_send, filename);
            AgenteUDP.sendPacket(clientSocket, ip, porta, to_send);
        }

        //TRANSFERENCIA DE FICHEIRO
        int count = 0;
        new File("downloads/").mkdirs();
        FileOutputStream fos = new FileOutputStream("downloads/" + filename);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        while(true) {
            received = AgenteUDP.receivePacket(clientSocket);

            if(isFYN(received)){ System.out.println("Recebi FYN - "+ LocalTime.now()); break;}

            System.out.printf("Recebi o %d fragmento -" + LocalTime.now() +"\n" ,++count);
            bos.write(received.fileData, 0,received.fileData.length);


            to_send = new MySegment();
            buildACK(to_send);
            AgenteUDP.sendPacket(clientSocket, ip, porta, to_send);
        }
        bos.flush();
        bos.close();

        //TERMINO DE CONEXAO
        to_send = new MySegment();
        buildACK(to_send);
        AgenteUDP.sendPacket(clientSocket, ip, porta, to_send);

    }
}