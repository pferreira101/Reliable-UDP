package Common;

public class ConnectionControl {

     public static boolean isSYN(byte[] receivedData){
        return (receivedData[0] == 1);
    }

     public static boolean isACK(byte[] receivedData){
        return (receivedData[0] == 3);
    }

     public static boolean isFYN(byte[] receivedData){
        return (receivedData[0] == 2);
    }

     public static void analisaSegmento(byte[] receivedData){
        if(receivedData[0]==1) {
            System.out.println("got a syn");
        }
        if(receivedData[0]==2) {
            System.out.println("got a fyn");
        }
        if(receivedData[0]==3) {
            System.out.println("got an ack");
        }
     }


     public static byte[] buildSYN(){
        byte[] sendData = new byte[1024];
        sendData[0]=1;
        return sendData;
     }

     public static byte[] buildFYN(){
         byte[] sendData = new byte[1024];
        sendData[0]=2;
        return sendData;
     }

     public static byte[] buildACK(){
        byte[] sendData = new byte[1024];
        sendData[0]=3;
        return sendData;
     }


}
