package TransfereCC;

import java.io.*;
import java.net.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static TransfereCC.ConnectionControl.*;
import static TransfereCC.ErrorControl.*;

class ReceiverSide extends ConnectionHandler {
     LocalTime begin;



    public ReceiverSide(InetAddress ip, int port, MySegment first_segment, String filename, AgenteUDP sender) {
        super();
        this.segmentsToProcess = new TreeSet<>((s1, s2) -> Integer.compare(s1.seq_number, s2.seq_number) );
        this.st = new StateTable();
        this.st.setDestination(ip, port);
        this.msg_sender = sender;

        if(filename == null) {
            this.st.opMode = 1;
            this.st.setFilename(extractFileName(first_segment));
            this.st.last_ack_value = first_segment.seq_number;
        }
        else if (first_segment == null) {
            this.st.file = filename;
            this.st.opMode = 0;
        }
    }

    public void run()  {
        System.out.println("Playing receiver role");
        begin =  LocalTime.now();

        boolean connection_accepted =  establishConnection();

        if(!connection_accepted){
            this.msg_sender.removeConnection(this.st);
            return;
        }

        try {
            receiveFile();
        } catch (Exception e) {
            System.out.println("Error saving file");
        }
        System.out.println("Numero de pacotes por confirmar = " + this.st.unAckedSegments.size());
    }

    boolean establishConnection(){
        MySegment received = null;

        if(this.st.opMode == 0) {
            //INICIO DE CONEXAO
            int tries = 0;
            while(tries < 3) {
                msg_sender.sendSYNWithFilename(st);

                waitResponse();
                received = getNextSegment();
                if(received != null) break;
                else ++tries;
            }
            if(tries == 3){
                System.out.println("Couldn't establish connection");
                return false;
            }

            if(tries > 0){
                st.unAckedSegments.clear();
            }

            // Verifica se a file existe
            if (isRejectedConnectionFYN(received)) {
                System.out.println("File not found. Closing...");
                return false;
            }

            // Envia ACK
            if (isSYNACK(received)) {
                processReceivedSYNAck(received, this.st);
                msg_sender.sendACK(st);
                return true;
            }
        }
        else if(this.st.opMode == 1){

            if ((new File(this.st.file).isFile())) {
                //msg_sender.sendRejectedConnectionFYN(this.st);
                //return false;
            }

            msg_sender.sendACK(st);


            // Espera  SYNACK
            waitSegment();
            received = getNextSegment();

            if(isSYNACK(received)) {
                processReceivedSYNAck(received, this.st);
                msg_sender.sendACK(st);
                return true;
            }
        }

        return false;
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
                }
                else {
                    this.readyToProcess = false;
                    msg_sender.requestRepeat(this.st, this.st.last_ack_value);
                    break;
                }

                if (isFYN(received)) {
                    msg_sender.sendACK(st);
                    break;
                }
                msg_sender.sendACK(st);

                bos.write(received.fileData, 0, received.fileData.length);

            }
            this.l.unlock();
            if(isFYN(received))break;
        }

        bos.flush();
        bos.close();

        // Verifica se o ficheiro é o mesmo
        boolean check_file = Crypto.verifySignature("downloads/"+this.st.file, this.st.assinatura, this.st.public_key);
        if(check_file == true ) System.out.println("Received file is correct and safe.");

        this.msg_sender.removeConnection(this.st);
        LocalTime end =  LocalTime.now();
        System.out.println("Transfer time: " +  Duration.between(begin, end).toMillis() + " ms");
    }

}
