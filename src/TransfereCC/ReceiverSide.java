package TransfereCC;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static TransfereCC.ConnectionControl.*;
import static TransfereCC.ErrorControl.*;

class ReceiverSide extends ConnectionHandler {

    InetAddress ip;
    int port;
    String filename;
    AgenteUDP msg_sender;


    public ReceiverSide(InetAddress ip, int port, String filename, AgenteUDP sender) {
        this.ip = ip;
        this.port = port;
        this.filename = filename;
        this.msg_sender = sender;
    }

    public void run()  {
        MySegment to_send;
        MySegment received;

        //INICIO DE CONEXAO
        to_send = new MySegment();
        buildSYNWithFileName(to_send, filename);
        msg_sender.sendPacket(ip, port, to_send);

        // Espera resposta SYN
        waitSegment();
        received = getNextSegment();

        // Verifica se a file existe
        if(isSYNErrorFile(received)){
            System.out.println("File not found. Closing...");
            return;
        }

        // Envia ACK
        if(isSYN(received)){ // DEVIA ESTAR A ESPERA DE UM SYNACK
            to_send = new MySegment();
            buildACK(to_send);
            msg_sender.sendPacket(ip, port, to_send);
        }

        try {
            receiveFile();
        } catch (Exception e) {
            System.out.println("Error saving file");
        }

        //TERMINO DE CONEXAO
        to_send = new MySegment();
        buildACK(to_send);
        msg_sender.sendPacket(ip, port, to_send);

    }


    void receiveFile() throws Exception {
        MySegment received, to_send;

        new File("downloads/").mkdirs();

        FileOutputStream fos = new FileOutputStream("downloads/" + filename);;
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        int count=0;
        while(true) {
            waitSegment();
            received = getNextSegment();
            boolean isOk = verificaChecksum(received);
            System.out.println("Checksum is " + isOk);

            if(isFYN(received)){ System.out.println("Recebi FYN - "+ LocalTime.now()); break;}

            System.out.printf("Recebi o %d fragmento -" + LocalTime.now() +"\n" ,++count);
            bos.write(received.fileData, 0,received.fileData.length);

            to_send = new MySegment();
            buildACK(to_send);
            msg_sender.sendPacket(ip, port, to_send);
        }

        bos.flush();
        bos.close();
    }
}
