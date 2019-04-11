package Common;

public class ConnectionControl {

     public static boolean isSYN(MySegment received){
        return (received.flag == 1);
    }

    public static boolean isFYN(MySegment received){
        return (received.flag == 2);
    }

    public static boolean isACK(MySegment received){
        return (received.flag == 3);
    }

    public static boolean isSYNErrorFile(MySegment received){
        return (received.flag == 4);
    }

     public static void buildSYN(MySegment to_send){
         to_send.flag=1;
     }

     public static void buildFYN(MySegment to_send){
         to_send.flag=2;
     }

    public static void buildACK(MySegment to_send){
        to_send.flag=3;
    }

     public static void buildSYNWithFileName(MySegment to_send, String filename){
         to_send.flag = 1;
         to_send.fileData = filename.getBytes();
     }

     public static void buildErrorFileSYN(MySegment to_send){
         to_send.flag = 4;
     }


}
