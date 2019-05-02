package ServerSide;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import ClientSide.ClientErrorControl;
import Common.*;

import static Common.AgenteUDP.*;
import static Common.ConnectionControl.*;


public class Server extends Thread{
    private InetAddress svAddress;
    private int port;
    private StateTable stateTable;
    private DatagramSocket serverSocket;

    public Server(InetAddress ip, int port_number){
        svAddress = ip;
        port = port_number;
        stateTable = new StateTable();
        serverSocket = null;
    }

    public void run() {

        try{
            serverSocket = new DatagramSocket(port, svAddress);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        while(true){
            try {
                MySegment received = receive1stPacket();

                // INICIO DE CONEXAO - Verifica se existe o ficheiro pedido
                if(isFileRequest(received)){
                    boolean connection_accepted = establishConnection(stateTable.file);
                    if (!connection_accepted) break;
                }
                else break;

                received = receivePacket(serverSocket);

                if(isACK(received))
                    transferFile();

                //TERMINO DE CONEXAO
                endConnection();

            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    private MySegment receive1stPacket() throws Exception{
        byte[] receiveData = new byte[1024];
        System.out.println("ESPERAR PRIMEIRO PACOTE");
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        MySegment received = MySegment.fromByteArray(receivePacket.getData());
        System.out.println("RECEBEU PRIMEIRO PACOTE");

        this.stateTable.setDestination(receivePacket.getAddress(),receivePacket.getPort());
        this.stateTable.setFilename(new String(received.fileData));

        return received;
    }


    private boolean establishConnection(String wanted_file) {
        if(!(new File(wanted_file).isFile())){
            System.out.println("Não existe o ficheiro pedido");

            sendMissingFileFYN(serverSocket, stateTable.IPAddress, stateTable.port);
            return false;
        }

        sendSYN(serverSocket, stateTable.IPAddress, stateTable.port);
        return true;
    }

    private void transferFile() throws Exception {
        MySegment to_send, received;
        System.out.println("Recebi um ACK - "+ LocalTime.now());
        //TRANSFERENCIA DE FICHEIRO

        List<byte[]> bytes_pacotes = new ArrayList<>();
        try {
            bytes_pacotes = dividePacket(stateTable.file, 1024);
        }
        catch (Exception e){
            e.printStackTrace(); // NUNCA ENTRA AQUI PORQUE JÁ VERIFICA ANTES SE EXISTE
        }

        for (byte[] b : bytes_pacotes) {
            to_send = new MySegment();
            to_send.setFileData(b);
            to_send.setChecksum(ClientErrorControl.calculateChecksum(to_send.toByteArray()));
            AgenteUDP.sendPacket(serverSocket, stateTable.IPAddress, stateTable.port, to_send);

            received = AgenteUDP.receivePacket(serverSocket);
            System.out.println("Recebi um ACK ao pacote enviado - "+ LocalTime.now());
        }

    }




    private void endConnection() {
        sendFYN(serverSocket, stateTable.IPAddress, stateTable.port);

        MySegment received = receivePacket(serverSocket);

        if(isACK(received))
            System.out.println("A terminar - "+ LocalTime.now());
    }
}
