package Common;

import java.net.InetAddress;

public class StateTable {
    public  InetAddress IPAddress;
    public int port;
    public String file;
    int connection_state; // 0 sem conexão -- 1 em inicio de conexao -- 2 em transferencia -- 3 em termino de conexao

    public  StateTable(){connection_state=0;}

    public void setDestination(InetAddress IPAddress, int port){
        this.IPAddress = IPAddress;
        this.port = port;
    }

    public void setFilename(String filename){
        this.file = filename;
    }


}
