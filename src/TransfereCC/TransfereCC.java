package TransfereCC;


import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

public class TransfereCC {

    private static DatagramSocket beginSocket(BufferedReader buffer) {
        DatagramSocket serverSocket = null;
        boolean valid_port = false;
        int port = 0;

        while(!valid_port){
            System.out.println("Insert port:");

            try{
                String input = buffer.readLine();
                port = Integer.parseInt(input);
                if(port < 65536 && port > 0) {
                    serverSocket = new DatagramSocket(port);
                };
                valid_port = true;
            }
            catch (NumberFormatException e){
                System.out.println("Invalid port, please insert a new one.");
            }
            catch (IOException e){
                System.out.println("Port already in use, please insert a new one.");
            }
        }


        return serverSocket;
    }

    public static void main(String[] args)  throws Exception{
        System.out.println("--- Welcome to TransfereCC ---");
        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
        }

        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket socket = beginSocket(buffer);

        AgenteUDP connectionManager = new AgenteUDP(socket);

        //Process client's request for files
        while(true){
            String input = buffer.readLine();
            String[] inputs = input.split(" ");

            switch(inputs[0]){
                case "get":
                    if(inputs.length == 4){
                        try {
                            System.out.println(InetAddress.getByName(inputs[1]).toString());
                            System.out.println("Válido");

                            connectionManager.addReceiverRoleConnection(InetAddress.getByName(inputs[1]),Integer.parseInt(inputs[3]),null ,inputs[2]);
                        }
                        catch (IOException e){
                            System.out.println("Inválido");
                        }
                    }
                    else if (inputs.length > 4 && inputs.length%2==0){
                        int total_senders = (inputs.length-2)/2;
                        for (int i = 0; i<total_senders; i++){
                            InetAddress ip = InetAddress.getByName(inputs[i*2+1]);
                            int port = Integer.parseInt(inputs[i*2+2]);

                            System.err.println("Conexão " + i + ": IP = " + ip.toString() + "; Porta = " + port);
                            /* ..... */

                        }
                    }
                    else{
                        System.out.println("Faltam argumentos");
                    }
                    break;

                case "put":
                    if(inputs.length == 4){
                        try {
                            System.out.println(InetAddress.getByName(inputs[1]).toString());
                            System.out.println("Válido");

                            connectionManager.addSenderRoleConnection(InetAddress.getByName(inputs[1]),Integer.parseInt(inputs[3]),null, inputs[2]);
                        }
                        catch (IOException e){
                            System.out.println("Inválido");
                        }
                    }
                    else{
                        System.out.println("Faltam argumentos");
                    }
                    break;

                case "close":
                    System.out.println("--- TransfereCC closed ---");
                    return;

                default:
                    System.out.println("Introduza um comando válido");
                    break;
            }
        }



    }
}
