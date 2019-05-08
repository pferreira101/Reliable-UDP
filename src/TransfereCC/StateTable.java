package TransfereCC;

import java.net.InetAddress;
import java.util.*;

class StateTable {
    public int congestion_state;
    int windowSize;
    int threshold;
    int curr_seq_num;
    int last_ack_value;
    InetAddress IPAddress;
    int port;
    String file;
    int connection_state; // 0 sem conexão -- 1 em inicio de conexao -- 2 em transferencia -- 3 em termino de conexao
    TreeSet<MySegment> unAckedSegments;
    Map<Integer,Integer> dupACKs;
    byte[] assinatura;
    byte[] public_key;

    StateTable(){
        connection_state=0;
        curr_seq_num=0;
        windowSize = 4; // começa a 1?
        unAckedSegments = new TreeSet<>((s1,s2) -> Integer.compare(s1.seq_number, s2.seq_number) ); // ordenar por seq number
        dupACKs = new HashMap<>();
        threshold = 1; // mudar

    }


    void setDestination(InetAddress IPAddress, int port){
        this.IPAddress = IPAddress;
        this.port = port;
    }

    void setFilename(String filename){
        this.file = filename;
    }

    void setCrypto(byte[] assinatura, byte[] public_key){
        this.assinatura = assinatura;
        this.public_key = public_key;
    }


}
