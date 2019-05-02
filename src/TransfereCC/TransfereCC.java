package TransfereCC;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;

public class TransfereCC {


    public static void main(String[] args) throws Exception {
        System.out.println("--- Welcome to TransfereCC ---");
        System.out.println(InetAddress.getLocalHost().getHostAddress());

        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        boolean valid_port = false;
        int porta = 0;

        while(!valid_port){
            System.out.println("Insert port:");
            String input = buffer.readLine();
            try{
                porta = Integer.parseInt(input);
                if(porta < 65536 && porta > 0) valid_port = true;
            }
            catch (NumberFormatException e){
                System.out.println("Invalid port, please insert a new one.");
            }

        }

        Thread server = new Thread(new SenderSide(InetAddress.getLocalHost(), porta));
        server.start();
        System.out.println("Server started");

        while(true){
            String input = buffer.readLine();

            String[] inputs = input.split(" ");

            switch(inputs[0]){
                case "get":
                    if(inputs.length == 4){
                        try {
                            System.out.println(InetAddress.getByName(inputs[1]).toString());
                            System.out.println("Válido");

                            ReceiverSide c = new ReceiverSide();
                            InetAddress ip = InetAddress.getByName(inputs[1]);
                            String filename = inputs[2];
                            porta = Integer.parseInt(inputs[3]);
                            c.connect(ip, filename, porta);
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
