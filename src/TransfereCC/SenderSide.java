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
    InetAddress svAddress;
    int port;
    StateTable stateTable;

    SenderSide(InetAddress ip, int port_number, String filename, AgenteUDP msgSender){
        super();
        svAddress = ip;
        port = port_number;
        stateTable = new StateTable();
        this.msgSender = msgSender;
        this.stateTable.setDestination(ip,port_number);
        this.stateTable.setFilename(filename);
    }

    public void run() {
            try {
                // INICIO DE CONEXAO - Verifica se existe o ficheiro pedido
                boolean connection_accepted = establishConnection(stateTable.file);

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

            msgSender.sendMissingFileFYN(stateTable.IPAddress, stateTable.port);
            return false;
        }

        msgSender.sendSYN(stateTable.IPAddress, stateTable.port);
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
            byte[] checksum = calculateChecksum(to_send.toByteArray());

            to_send.setChecksum(checksum);
            msgSender.sendPacket(stateTable.IPAddress, stateTable.port, to_send);

            waitSegment();
            received = getNextSegment();
            if(isACK(received))
                System.out.println("Recebi um ACK ao pacote enviado - "+ LocalTime.now());
        }

    }




    private void endConnection() throws InterruptedException {
        msgSender.sendFYN(stateTable.IPAddress, stateTable.port);

        waitSegment();
        MySegment received = getNextSegment();

        if(isACK(received))
            System.out.println("A terminar - "+ LocalTime.now());
    }
}

