import java.io.*;
import java.net.*;

public class Server {

    private static boolean isSYN(byte[] receivedData){
        return (receivedData[0] == 1);
    }

    private static boolean isACK(byte[] receivedData){
        return (receivedData[0] == 3);
    }

    private static boolean isFYN(byte[] receivedData){
        return (receivedData[0] == 2);
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


    public static void main(String args[]) throws Exception
    {
        System.out.print("a correr");
        InetAddress svAddress = InetAddress.getByAddress(new byte[] {
                (byte)192, (byte)168, (byte)43, (byte)121});
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
            serverSocket.receive(receivePacket);
            analisaSegmento(receivePacket.getData());

            if(isFYN(receivePacket.getData())) break;

            String sentence = new String(receivePacket.getData());
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }

        sendData = buildACK();
        sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        serverSocket.send(sendPacket);

        System.out.println("A terminar");

    }
}
