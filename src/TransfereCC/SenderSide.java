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

    SenderSide(InetAddress ip, int port_number, String filename, AgenteUDP msgSender){
        super();
        this.st = new StateTable();

        this.st.setDestination(ip,port_number);
        this.st.setFilename(filename);

        this.msgSender = msgSender;
    }

    public void run() {
        System.out.println("Playing sender role");
            try {
                // INICIO DE CONEXAO - Verifica se existe o ficheiro pedido
                boolean connection_accepted = establishConnection(st.file);

                if (connection_accepted) {
                    waitSegment();
                    MySegment to_process = getNextSegment();

                    if (isACK(to_process))
                        transferFile();

                    //TERMINO DE CONEXAO
                    endConnection();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

    }



    private boolean establishConnection(String wanted_file) {
        if(!(new File(wanted_file).isFile())){
            System.out.println("Não existe o ficheiro pedido");

            msgSender.sendMissingFileFYN(st);
            return false;
        }

        msgSender.sendSYNACK(st);
        return true;
    }

    private void transferFile() throws Exception {
        MySegment to_send, received;
        System.out.println("Recebi um ACK - "+ LocalTime.now());
        //TRANSFERENCIA DE FICHEIRO

        List<byte[]> bytes_pacotes = new ArrayList<>();
        try {
            bytes_pacotes = dividePacket(st.file, 1024);
        }
        catch (Exception e){
            e.printStackTrace(); // NUNCA ENTRA AQUI PORQUE JÁ VERIFICA ANTES SE EXISTE
        }

        for (byte[] b : bytes_pacotes) {
            msgSender.sendDataSegment(st, b);

            waitSegment();
            received = getNextSegment();
            if(isACK(received))
                System.out.println("Recebi um ACK ao pacote enviado - "+ LocalTime.now());
        }

    }




    private void endConnection() throws InterruptedException {
        msgSender.sendFYN(st);

        waitSegment();
        MySegment received = getNextSegment();

        if(isACK(received))
            System.out.println("A terminar - "+ LocalTime.now());
    }
}

