package TransfereCC;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.*;




import static TransfereCC.AgenteUDP.*;
import static TransfereCC.ConnectionControl.*;
import static TransfereCC.ErrorControl.*;


public class SenderSide extends ConnectionHandler implements Runnable {
    AgenteUDP msgSender;
    StateTable st;

    SenderSide(InetAddress ip, int port_number, String filename, int isn, AgenteUDP msgSender){
        super();
        this.st = new StateTable();

        this.st.setDestination(ip,port_number);
        this.st.setFilename(filename);
        this.st.last_correct_seq = isn;

        this.msgSender = msgSender;
    }

    public void run() {
        System.out.println("Playing sender role");
            try {
                // INICIO DE CONEXAO - Verifica se existe o ficheiro pedido
                boolean connection_accepted = establishConnection(this.st.file);

                if (connection_accepted) {
                    transferFile();

                    //TERMINO DE CONEXAO
                    endConnection();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        System.out.println("Numero de pacotes por confirmar = " + this.st.unAckedSegments.size());
    }



    private boolean establishConnection(String wanted_file) {
        if(!(new File(wanted_file).isFile())){
            System.out.println("Não existe o ficheiro pedido");

            msgSender.sendMissingFileFYN(this.st);
            return false;
        }

        msgSender.sendSYNACK(st);

        waitSegment();
        MySegment to_process = getNextSegment();

        if(isACK(to_process)) {
            processReceivedAck(to_process,this.st);
            System.out.println("Recebi um ACK ao SYNACK - "+ LocalTime.now());
            return true;
        }
        else return false;
    }

    private void transferFile() throws Exception {
        MySegment received;

        //TRANSFERENCIA DE FICHEIRO
        List<byte[]> bytes_pacotes = dividePacket(this.st.file, 1024);

        for (byte[] b : bytes_pacotes) {
            msgSender.sendDataSegment(this.st, b);

            waitSegment();
            received = getNextSegment();
            if(isACK(received)) {
                processReceivedAck(received,this.st);
                System.out.printf("Recebi um ACK (ACK : %d ) - " + LocalTime.now() + "\n", received.ack_number);
            }
        }

    }




    private void endConnection() throws InterruptedException {
        this.msgSender.sendFYN(this.st);

        waitSegment();
        MySegment received = getNextSegment();

        if(isACK(received)) {
            processReceivedAck(received,this.st);
            System.out.println("A terminar - " + LocalTime.now());
        }
    }
}

