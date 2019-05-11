package TransfereCC;

import Common.Pair;

import java.io.File;
import java.net.InetAddress;
import java.time.Duration;
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
    LocalTime begin;

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
            Pair<byte[], byte[]> assinatura = generateSignature(this.st.file, this.msg_sender.keys);
            this.st.setCrypto(assinatura.first, assinatura.second);
        }
        else if (filename == null) {
            this.st.opMode = 0;
            this.st.setFilename(extractFileName(first_segment));
        }

    }

    public void run() {
        System.out.println("Playing sender role");
        begin = LocalTime.now();

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
        MySegment received = null;

        if(this.st.opMode == 0) { // foi feito pedido get (thread criada devido à chegada de syn com pedido)
            if (!(new File(this.st.file).isFile())) {
                msg_sender.sendRejectedConnectionFYN(this.st);
                return false;
            }
            else{
                Pair<byte[], byte[]> assinatura = generateSignature(this.st.file, this.msg_sender.keys);
                this.st.setCrypto(assinatura.first, assinatura.second);
            }

            msg_sender.sendSYNACK(st);
            initTimer();

            waitSegment();
            MySegment to_process = getNextSegment();

            if (isACK(to_process)) {
                processReceivedAck(to_process, this.st);
                return true;
            } else return false;
        }
        else {// foi feito um pedido de put, é preciso iniciar conexão
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

            if(isRejectedConnectionFYN(received)) {
                System.out.println("Connection was rejected");
                return false;
            }

            // Envia ACK
            if (isACK(received)) {
                processReceivedAck(received, this.st);

                msg_sender.sendSYNACK(st);
                initTimer();

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
                    resetTimer();
                    recalculateWindowSize(st, NEWACK);
                }
                if(re_send == -1) {
                    recalculateWindowSize(st, ACKDUP);
                }
                if(re_send > 0) {
                    recalculateWindowSize(st, ACK3DUP);
                    reSend(re_send);
                }


            }
        }

        while(st.unAckedSegments.size()>0) {
            waitSegment();
            received = getNextSegment();
            if (isACK(received)) {
                int re_send = processReceivedAck(received, st);
                if(re_send == -2) {
                    resetTimer();
                }
                if(re_send > 0) {
                    reSend(re_send);
                }
            }
        }

    }


    private void endConnection() throws InterruptedException {
        this.msg_sender.sendFYN(this.st);

        while(st.unAckedSegments.size() != 0) {
            waitSegment();
            MySegment received = getNextSegment();

            if (isACK(received)) {
                int re_send = processReceivedAck(received, this.st);
                if (re_send > 0) {
                    reSend(re_send);
                }
            }
        }
        l.lock();
        this.timer.cancelTimer();
        l.unlock();
        this.msg_sender.removeConnection(this.st);
        LocalTime end =  LocalTime.now();
        System.out.println("Transfer time: " +  Duration.between(begin, end).toMillis() + " ms");
    }

    void reSend(int re_send) {
        MySegment to_send = this.st.unAckedSegments.first();
        if(to_send == null) return ;

        this.msg_sender.directSend(to_send, this.st);
    }

    void initTimer(){
        l.lock();
        this.timer = new TimeoutManager(this, 1000);
        this.isTimerRunning = true;
        l.unlock();
    }

    void resetTimer(){
        l.lock();
        if(this.timer != null) this.timer.cancelTimer();
        initTimer();
        l.unlock();
    }
}

