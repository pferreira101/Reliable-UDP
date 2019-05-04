package TransfereCC;

import java.net.InetAddress;

class StateTable {
    int curr_seq_num;
    InetAddress IPAddress;
    int port;
    String file;
    int connection_state; // 0 sem conexão -- 1 em inicio de conexao -- 2 em transferencia -- 3 em termino de conexao

    StateTable(){connection_state=0; curr_seq_num=0;}

    void setDestination(InetAddress IPAddress, int port){
        this.IPAddress = IPAddress;
        this.port = port;
    }

    void setFilename(String filename){
        this.file = filename;
    }


}
