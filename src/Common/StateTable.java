package Common;

public class StateTable {
    int port;
    String file;

    int connection_state; // 0 sem conexão -- 1 em inicio de conexao -- 2 em transferencia -- 3 em termino de conexao

    StateTable(){connection_state=0;}
}
