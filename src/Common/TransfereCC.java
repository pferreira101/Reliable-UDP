package Common;

import ClientSide.Client;
import ServerSide.Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class TransfereCC {


    public static void main(String args[]) throws Exception {
        System.out.println("--- Welcome to TransfereCC ---");
        System.out.println(InetAddress.getLocalHost().getHostAddress());

        Thread server = new Thread(new Server(InetAddress.getLocalHost()));
        server.start();
        System.out.println("Server started");

        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));

        while(true){
            String input = buffer.readLine();

            String inputs[] = input.split(" ");

            switch(inputs[0]){
                case "connect":
                    if(inputs.length == 3){

                            System.out.println(InetAddress.getByName(inputs[1]).toString());
                            System.out.println("Válido");

                            Client c = new Client();
                            int porta = Integer.parseInt(inputs[2]);
                            c.connect(InetAddress.getByName(inputs[1]), porta);


                    }
                    else{
                        System.out.println("Inserir um IP.");
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
