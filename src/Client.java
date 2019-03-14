import java.io.*;
import java.net.*;

public class Client {


    private static boolean isSYN(byte[] receivedData){
        return (receivedData[0] == 1);
    }

    private static boolean isACK(byte[] receivedData){
        return (receivedData[0] == 3);
    }

    private static void analisaSegmento(byte[] receivedData){
        if(receivedData[0]==1) {
            System.out.println("got a syn");
        }
        if(receivedData[0]==2) {
            System.out.println("got a fyn");
        }
        if(receivedData[0]==3) {
            System.out.println("got an ack");
        }
    }


    private static byte[] buildSYN(){
        byte[] sendData = new byte[1024];
        sendData[0]=1;
        return sendData;
    }

    private static byte[] buildFYN(){
        byte[] sendData = new byte[1024];
        sendData[0]=2;
        return sendData;
    }

    private static byte[] buildACK(){
        byte[] sendData = new byte[1024];
        sendData[0]=3;
        return sendData;
    }

    public static void main(String args[]) throws Exception {
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByAddress(new byte[] {
                (byte)192, (byte)168, (byte)42, (byte)121});


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

            String sentence = inFromUser.readLine();

            if(sentence.equals("end")) break;

            sendData = sentence.getBytes();
            sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
            clientSocket.send(sendPacket);

            receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            modifiedSentence = new String(receivePacket.getData());
            System.out.println("FROM SERVER:" + modifiedSentence);
        }

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