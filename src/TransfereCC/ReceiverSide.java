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
    StateTable st;
    AgenteUDP msg_sender;


    public ReceiverSide(InetAddress ip, int port, String filename, AgenteUDP sender) {
        super();

        this.st = new StateTable();
        this.st.setDestination(ip, port);
        this.st.setFilename(filename);

        this.msg_sender = sender;
    }

    public void run()  {
        System.out.println("Playing receiver role");
        MySegment received;

        //INICIO DE CONEXAO
        msg_sender.sendSYNWithFilename(st);

        // Espera resposta SYN
        waitSegment();
        received = getNextSegment();

        // Verifica se a file existe
        if(isFYNErrorFile(received)){
            System.out.println("File not found. Closing...");
            return;
        }

        // Envia ACK
        if(isSYNACK(received)){ // DEVIA ESTAR A ESPERA DE UM SYNACK;
            processReceivedAck(received,st);
            System.out.println("Recebi synack ao pedido de conexao - "+ LocalTime.now());
            msg_sender.sendACK(st);
        }

        try {
            receiveFile();
        } catch (Exception e) {
            System.out.println("Error saving file");
        }
        System.out.println("Numero de pacotes por confirmar = " + this.st.unAckedSegments.size());
    }


    void receiveFile() throws Exception {
        MySegment received;

        new File("downloads/").mkdirs();

        FileOutputStream fos = new FileOutputStream("downloads/" + st.file);;
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        int count=0;
        while(true) {
            waitSegment();
            received = getNextSegment();
            boolean isOk = verificaChecksum(received);

            if(isFYN(received)){
                System.out.println("Recebi FYN - "+ LocalTime.now());
                msg_sender.sendACK(st);
                break;
            }

            System.out.printf("Recebi o %d fragmento - (seq : %d) " + LocalTime.now() +"\n" ,++count, received.seq_number);
            bos.write(received.fileData, 0,received.fileData.length);

            msg_sender.sendACK(st);
        }

        bos.flush();
        bos.close();
    }
}
