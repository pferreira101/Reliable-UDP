package TransfereCC;

class ConnectionControl {

    static final int SYN = 1;
    static final int FYN = 2;
    static final int ACK = 3;
    static final int FYNERRORFILE = 4;
    static final int SYNACK = 5;

    static boolean isSYN(MySegment received){
        return (received.flag == SYN);
    }

    static boolean isFileRequest(MySegment received){
        return (received.flag == SYN) && (received.fileData != null);
    }

    static boolean isFYN(MySegment received){
        return (received.flag == FYN);
    }

    static boolean isACK(MySegment received){
        return (received.flag == ACK);
    }

    static boolean isSYNACK(MySegment received){
        return (received.flag == SYNACK);
    }

    static boolean isFYNErrorFile(MySegment received){
        return (received.flag == FYNERRORFILE);
    }

    static void buildSYN(MySegment to_send){
         to_send.flag = SYN;
    }

    static void buildFYN(MySegment to_send){
         to_send.flag = FYN;
    }

    static void buildACK(MySegment to_send){
        to_send.flag = ACK;
    }

    static void buildSYNWithFileName(MySegment to_send, String filename){
         to_send.flag = SYN;
         to_send.fileData = filename.getBytes();
    }

    static void buildErrorFileFYN(MySegment to_send){
         to_send.flag = FYNERRORFILE;
    }

    public static void buildSYNACK(MySegment to_send){
        to_send.flag = SYNACK;
    }

}
