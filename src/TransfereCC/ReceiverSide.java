package TransfereCC;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.Arrays;
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

            byte read_assinatura[] = new byte[4];
            byte read_pubkey[] = new byte[4];

            System.arraycopy(received.fileData, 0, read_assinatura, 0, 4);
            System.arraycopy(received.fileData, 4, read_pubkey, 0, 4);

            int assinatura_size = fromByteArray(read_assinatura);
            int pubkey_size = fromByteArray(read_pubkey);

            byte assinatura[] = new byte[assinatura_size];
            byte public_key[] = new byte[pubkey_size];

            System.arraycopy(received.fileData, 8, assinatura, 0, assinatura_size);
            System.arraycopy(received.fileData, 8+assinatura_size, public_key, 0, pubkey_size);

            this.st.setCrypto(assinatura, public_key);

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

        FileOutputStream fos = new FileOutputStream("downloads/" + st.file);
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

        // Verifica se o ficheiro é o mesmo
        boolean check_file = Crypto.verifySign("downloads/"+st.file, st.assinatura, st.public_key);
        System.out.println("VERIFICA FICHEIRO C/ ASSINATURA DIGITAL = " + check_file);
    }


    /****************** FUNÇÕES AUXILIARES ********************/

    int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
}
