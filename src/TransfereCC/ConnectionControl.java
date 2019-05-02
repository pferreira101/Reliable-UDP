package TransfereCC;

class ConnectionControl {

    static boolean isSYN(MySegment received){
        return (received.flag == 1);
    }

    static boolean isFileRequest(MySegment received){
        return (received.flag == 1) && (received.fileData != null);
    }

    static boolean isFYN(MySegment received){
        return (received.flag == 2);
    }

    static boolean isACK(MySegment received){
        return (received.flag == 3);
    }

    static boolean isSYNErrorFile(MySegment received){
        return (received.flag == 4);
    }

    static void buildSYN(MySegment to_send){
         to_send.flag=1;
    }

    static void buildFYN(MySegment to_send){
         to_send.flag=2;
    }

    static void buildACK(MySegment to_send){
        to_send.flag=3;
    }

    static void buildSYNWithFileName(MySegment to_send, String filename){
         to_send.flag = 1;
         to_send.fileData = filename.getBytes();
    }

    static void buildErrorFileFYN(MySegment to_send){
         to_send.flag = 4;
    }


}
