package TransfereCC;

import java.net.InetAddress;
import java.util.TreeSet;

class StateTable {
    int curr_seq_num;
    int last_correct_seq;
    InetAddress IPAddress;
    int port;
    String file;
    int connection_state; // 0 sem conexão -- 1 em inicio de conexao -- 2 em transferencia -- 3 em termino de conexao
    TreeSet<MySegment> unAckedSegments;
    byte[] assinatura;
    byte[] public_key;

    StateTable(){
        connection_state=0;
        curr_seq_num=0;
        unAckedSegments = new TreeSet<>((s1,s2) -> Integer.compare(s1.seq_number, s2.seq_number) ); // ordenar por seq number
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
