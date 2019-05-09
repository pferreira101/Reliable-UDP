package TransfereCC;

import Common.Pair;

import java.io.File;
import java.net.InetAddress;
import java.time.LocalTime;
import java.util.*;




import static TransfereCC.AgenteUDP.*;
import static TransfereCC.ConnectionControl.*;
import static TransfereCC.Crypto.*;
import static TransfereCC.ErrorControl.*;
import static TransfereCC.CongestionControl.*;
import static TransfereCC.FlowControl.adjustFlowControlWindow;


public class SenderSide extends ConnectionHandler implements Runnable {
    TimeoutManager timer;
    boolean isTimerRunning;

    SenderSide(InetAddress ip, int port_number, MySegment first_segment, String filename, AgenteUDP msgSender){
        super();
        this.segmentsToProcess = new TreeSet<>((s1,s2) -> Integer.compare(s1.ack_number, s2.ack_number ));

        this.st = new StateTable();
        this.st.setDestination(ip,port_number);
        this.msg_sender = msgSender;

        this.st.windowSize = 1;
        this.st.threshold = 4;
        this.st.congestion_state = SS;

        if(first_segment == null) {
            this.st.opMode = 1;
            this.st.setFilename(filename);
        }
        else if (filename == null) {
            this.st.opMode = 0;
            this.st.setFilename(extractFileName(first_segment));
        }

        Pair<byte[], byte[]> assinatura = generateSignature(this.st.file, this.msg_sender.keys);
        this.st.setCrypto(assinatura.first, assinatura.second);
    }

    public void run() {
        System.out.println("Playing sender role");
            try {
                // INICIO DE CONEXAO - Verifica se existe o ficheiro pedido
                boolean connection_accepted = establishConnection();

                if(!connection_accepted){
                    this.msg_sender.removeConnection(this.st);
                    return;
                }

                transferFile();

                //TERMINO DE CONEXAO
                endConnection();

            }
            catch (Exception e) {
                e.printStackTrace();
            }
        System.out.println("Numero de pacotes por confirmar = " + this.st.unAckedSegments.size());
    }



    private boolean establishConnection() {
        MySegment received;

        if(this.st.opMode == 0) { // foi feito pedido get (thread criada devido à chegada de syn com pedido)
            if (!(new File(this.st.file).isFile())) {
                System.out.println("Não existe o ficheiro pedido");

                msg_sender.sendRejectedConnectionFYN(this.st);
                return false;
            }

            msg_sender.sendSYNACK(st);

            waitSegment();
            MySegment to_process = getNextSegment();

            if (isACK(to_process)) {
                processReceivedAck(to_process, this.st);
                System.out.println("Recebi um ACK ao SYNACK - " + LocalTime.now());
                return true;
            } else return false;
        }
        else {// foi feito um pedido de put, é preciso iniciar conexão
            msg_sender.sendSYNWithFilename(st);

            waitSegment();
            received = getNextSegment();

            if(isRejectedConnectionFYN(received)) {
                System.out.println("Já existe o ficheiro que se pretende dar UP");
                return false;
            }

            // Envia ACK
            if (isACK(received)) {
                processReceivedAck(received, this.st);
                System.out.println("Recebi ack ao pedido de conexao PUT - " + LocalTime.now());

                msg_sender.sendSYNACK(st);

                waitSegment();
                received = getNextSegment();
                if (isACK(received)) {
                    processReceivedAck(received, this.st);
                    return true;
                }
            }
        }

        return false;
    }

    private void transferFile() throws Exception {
        MySegment received;

        //TRANSFERENCIA DE FICHEIRO
        List<byte[]> data_packets = dividePacket(this.st.file, 1024);

        int i=0;
        while(i < data_packets.size()){
            int max_unacked_segs = (int) Math.min(st.windowSize, st.flow_windowsize);

            while(i < data_packets.size() && st.unAckedSegments.size() <  max_unacked_segs) {
                msg_sender.sendDataSegment(st, data_packets.get(i++));
                if(!isTimerRunning) initTimer();
            }

            waitSegment();
            received = getNextSegment();
            if(isACK(received)) {
                int re_send = processReceivedAck(received,st);

                adjustFlowControlWindow(received, st);

                if(re_send == -2) {
                    System.out.printf("Recebi um ACK (ACK : %d ) - " + LocalTime.now() + "\n", received.ack_number);
                    resetTimer();
                    recalculateWindowSize(st, NEWACK);
                }
                if(re_send == -1) {
                    System.out.printf("Recebi primeiro ack repetido (ACK : %d ) - " + LocalTime.now() + "\n", received.ack_number);
                    recalculateWindowSize(st, ACKDUP);
                }
                if(re_send > 0) {
                    System.out.printf("Recebi segundo ack repetido (ACK : %d ) - " + LocalTime.now() + "\n", re_send);
                    recalculateWindowSize(st, ACK3DUP);
                    reSend(re_send);
                }


            }
        }

        System.out.println("####### Supostamente todos os pacotes foram enviados. Pacotes:" + data_packets.size()+", enviados:" +i );
        while(st.unAckedSegments.size()>0) {
            waitSegment();
            received = getNextSegment();
            if (isACK(received)) {
                int re_send = processReceivedAck(received, st);
                if(re_send == -2) {
                    System.out.printf("Recebi um ACK (ACK : %d ) - " + LocalTime.now() + "\n", received.ack_number);
                    resetTimer();
            }
                if(re_send == -1) System.out.printf("Recebi primeiro ack repetido (ACK : %d ) - " + LocalTime.now() + "\n", received.ack_number);
                if(re_send > 0) {
                    System.out.printf("Recebi segundo ack repetido (ACK : %d ) - " + LocalTime.now() + "\n", re_send);
                    reSend(re_send);
                }
            }
        }

    }

    void reSend(int re_send) {
        MySegment to_send = this.st.unAckedSegments.first();
        System.out.printf("A reenviar (SEQ : %d ) - " + LocalTime.now() + "\n", to_send.seq_number);
        this.msg_sender.directSend(to_send, this.st);
    }

    void initTimer(){
        this.timer = new TimeoutManager(this, 1000);
        this.isTimerRunning = true;
    }

    void resetTimer(){
        this.timer.cancelTimer();
        initTimer();
    }

    private void endConnection() throws InterruptedException {
        this.msg_sender.sendFYN(this.st);

        while(st.unAckedSegments.size() != 0) {
            waitSegment();
            MySegment received = getNextSegment();

            if (isACK(received)) { // esta parte precisa de ser mudada
                int re_send = processReceivedAck(received, this.st);
                if (re_send == -2) {
                    System.out.printf("Recebi um ACK (ACK : %d ) - " + LocalTime.now() + "\n", received.ack_number);
                }

            }
        }
        System.out.println("A terminar - " + LocalTime.now());
        this.timer.cancelTimer();
        this.msg_sender.removeConnection(this.st);
    }
}

