package Common;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TransfereCC {


    public static void main(String args[]) throws UnknownHostException {
        if(args.length == 1){
            try{
                System.out.println(InetAddress.getByName(args[0]).toString());
                System.out.println("Válido\n");
            }
            catch (Exception e){
                System.out.println("Inválido\n");
            }
        }
        else{
            System.out.println("Introduza um IP \n");
        }
    }
}
