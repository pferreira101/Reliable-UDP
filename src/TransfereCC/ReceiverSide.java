package TransfereCC;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.util.*;

import static TransfereCC.ConnectionControl.*;
import static TransfereCC.ErrorControl.*;

class ReceiverSide extends ConnectionHandler {



    public ReceiverSide(InetAddress ip, int port, String filename, AgenteUDP sender) {
        super();
        this.segmentsToProcess = new TreeSet<>((s1, s2) -> Integer.compare(s1.seq_number, s2.seq_number) );
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
        if(isSYNACK(received)){
            processReceivedSYNAck(received);
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
        MySegment received=null;

        new File("downloads/").mkdirs();

        FileOutputStream fos = new FileOutputStream("downloads/" + st.file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        int count=0;
        while(true) {
            waitSegment();
            this.l.lock();
            // only wakes up when in order, error free, segment was received
            // there is still need to check for gaps between segments in queue
            while(this.segmentsToProcess.size() > 0) {
                if (isInOrder(st, this.segmentsToProcess.first())){
                    received = getNextSegment();
                    System.out.println("A processar um pacote  (SEQ:" + received.seq_number + ") - " + LocalTime.now());
                }
                else {
                    System.out.println("Pedir reenvio do pacote em falta" + LocalTime.now());
                    this.readyToProcess = false;
                    msg_sender.requestRepeat(this.st, this.st.last_ack_value);
                    break;
                }

                if (isFYN(received)) {
                    System.out.println("Recebi FYN - " + LocalTime.now());
                    msg_sender.sendACK(st);
                    break;
                }
                msg_sender.sendACK(st);

                //System.out.printf("Recebi o %d fragmento - (seq : %d) " + LocalTime.now() + "\n", ++count, received.seq_number);
                bos.write(received.fileData, 0, received.fileData.length);

            }
            this.l.unlock();
            if(isFYN(received))break;
        }

        bos.flush();
        bos.close();

        // Verifica se o ficheiro é o mesmo
        boolean check_file = Crypto.verifySignature("downloads/"+this.st.file, this.st.assinatura, this.st.public_key);
        System.out.println("VERIFICA FICHEIRO C/ ASSINATURA DIGITAL = " + check_file);
    }


    /****************** FUNÇÕES AUXILIARES ********************/

    private void processReceivedSYNAck(MySegment received) {
        processReceivedAck(received, this.st);

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
    }

    int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
}
